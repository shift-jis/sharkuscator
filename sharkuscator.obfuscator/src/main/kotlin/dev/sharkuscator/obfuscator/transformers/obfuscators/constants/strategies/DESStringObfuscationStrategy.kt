package dev.sharkuscator.obfuscator.transformers.obfuscators.constants.strategies

import dev.sharkuscator.obfuscator.ObfuscationContext
import dev.sharkuscator.obfuscator.extensions.addField
import dev.sharkuscator.obfuscator.extensions.isSpongeMixin
import dev.sharkuscator.obfuscator.extensions.resolveStaticInitializer
import dev.sharkuscator.obfuscator.extensions.shouldSkipTransform
import dev.sharkuscator.obfuscator.transformers.obfuscators.constants.generators.ConstantArrayGenerator
import dev.sharkuscator.obfuscator.transformers.strategies.StringConstantObfuscationStrategy
import dev.sharkuscator.obfuscator.utilities.AssemblyHelper.buildInstructionList
import dev.sharkuscator.obfuscator.utilities.AssemblyHelper.createFieldNode
import dev.sharkuscator.obfuscator.utilities.AssemblyHelper.integerPushInstruction
import dev.sharkuscator.obfuscator.utilities.AssemblyHelper.longPushInstruction
import org.mapleir.asm.ClassNode
import org.mapleir.asm.FieldNode
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.*
import java.nio.charset.StandardCharsets
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.DESKeySpec
import javax.crypto.spec.IvParameterSpec
import kotlin.random.Random

class DESStringObfuscationStrategy : StringConstantObfuscationStrategy {
    companion object {
        private const val KEY_BYTES_FIELD_ACCESS = Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC + Opcodes.ACC_TRANSIENT
        private const val KEY_BYTES_FIELD_DESCRIPTOR = "[B"
        private const val INSTRUCTION_CHAR_OFFSET = 0xAB00
    }

    private class EncryptionKeyChunk(val classNode: ClassNode, val fieldName: String, val chunkOfIndices: List<Int>)

    private val keyChunkListByClass = mutableMapOf<ClassNode, MutableList<EncryptionKeyChunk>>()
    private val keyDerivationSeedByClass = mutableMapOf<ClassNode, Long>()

    private val constantArrayGenerator = ConstantArrayGenerator(String::class.java)
    private val initializationVectorSpec = IvParameterSpec(ByteArray(8))
    private val secretKeyFactory = SecretKeyFactory.getInstance("DES")
    private val cipher = Cipher.getInstance("DES/CBC/PKCS5Padding")

    override fun initialization(obfuscationContext: ObfuscationContext, targetClassNode: ClassNode) {
        constantArrayGenerator.createAndAddArrayField(targetClassNode)
        keyDerivationSeedByClass.computeIfAbsent(targetClassNode) { Random.nextLong() }
    }

    override fun replaceInstructions(targetClassNode: ClassNode, instructions: InsnList, targetInstruction: AbstractInsnNode, originalString: String) {
        val instructionString = constantArrayGenerator.addValueToRandomArray(targetClassNode, targetInstruction, originalString) {
            obfuscateString(this, deriveKeyFromSeed(keyDerivationSeedByClass.getValue(targetClassNode))).first
        }
        instructions.insert(targetInstruction, constantArrayGenerator.createGetterInvocation(targetClassNode, instructionString))
        instructions.remove(targetInstruction)
    }

    override fun buildDecryptionRoutine(obfuscationContext: ObfuscationContext, targetClassNode: ClassNode, classEligibilityPredicate: (classNode: ClassNode) -> Boolean) {
        if (!keyDerivationSeedByClass.containsKey(targetClassNode)) {
            return
        }

        constantArrayGenerator.createAndAddArrayGetterMethod(targetClassNode)

        val keyFieldNameGenerator = ObfuscationContext.resolveDictionary<FieldNode, ClassNode>(FieldNode::class.java)
        val decryptionKeyBytes = deriveKeyFromSeed(keyDerivationSeedByClass.getValue(targetClassNode))

        val keyByteIndexChunks = (0..decryptionKeyBytes.size - 1).chunked((1..decryptionKeyBytes.size - 1).random())
        keyByteIndexChunks.forEach { chunkOfIndices ->
            val selectedHostClassNode = obfuscationContext.classSource.iterate().filter { !it.shouldSkipTransform() && classEligibilityPredicate(it) }.random() ?: return@forEach
            val generatedKeyFieldName = keyFieldNameGenerator.generateNextName(selectedHostClassNode)
            selectedHostClassNode.addField(createFieldNode(KEY_BYTES_FIELD_ACCESS, generatedKeyFieldName, KEY_BYTES_FIELD_DESCRIPTOR).apply {
                if (selectedHostClassNode.isSpongeMixin()) {
                    visibleAnnotations = mutableListOf(AnnotationNode("Lorg/spongepowered/asm/mixin/Unique;"))
                }
            })
            keyChunkListByClass.computeIfAbsent(targetClassNode) { mutableListOf() }.add(EncryptionKeyChunk(selectedHostClassNode, generatedKeyFieldName, chunkOfIndices))
        }

        val staticInitializerExitLabel = LabelNode()
        val decryptionLoopStartLabel = LabelNode()
        val switchDefaultLabel = LabelNode()

        val staticInitializer = targetClassNode.resolveStaticInitializer()
        staticInitializer.node.instructions.insert(buildInstructionList {
            add(constantArrayGenerator.createInitializationInstructions(targetClassNode) { arrayFieldMetadataList ->
                val initialChunkLength = arrayFieldMetadataList.firstNotNullOfOrNull {
                    it.valueChunks.values.flatten().firstNotNullOfOrNull { (_, chunkValue) -> chunkValue.length }
                } ?: 0
                val flattenedChunkList = arrayFieldMetadataList.flatMap { arrayFieldMetadata ->
                    arrayFieldMetadata.valueChunks.values.flatten().map { it.second to arrayFieldMetadata.fieldIndex }
                }

                val combinedEncryptedPayload = StringBuilder()
                flattenedChunkList.forEachIndexed { index, (chunkValue, fieldIndex) ->
                    combinedEncryptedPayload.append(chunkValue)
                    combinedEncryptedPayload.append(fieldIndex.toChar() + INSTRUCTION_CHAR_OFFSET)
                    if (index < flattenedChunkList.size - 1) {
                        val nextChunkValue = flattenedChunkList[index + 1].first
                        combinedEncryptedPayload.append(nextChunkValue.length.toChar())
                    }
                }

                // DES Key and Cipher Initialization (original logic)
                add(LdcInsnNode("DES"))
                add(MethodInsnNode(Opcodes.INVOKESTATIC, "javax/crypto/SecretKeyFactory", "getInstance", "(Ljava/lang/String;)Ljavax/crypto/SecretKeyFactory;"))
                add(VarInsnNode(Opcodes.ASTORE, 1))

                add(LdcInsnNode("DES/CBC/PKCS5Padding"))
                add(MethodInsnNode(Opcodes.INVOKESTATIC, "javax/crypto/Cipher", "getInstance", "(Ljava/lang/String;)Ljavax/crypto/Cipher;"))
                add(VarInsnNode(Opcodes.ASTORE, 2))

                add(integerPushInstruction(decryptionKeyBytes.size))
                add(IntInsnNode(Opcodes.NEWARRAY, Opcodes.T_BYTE))
                add(VarInsnNode(Opcodes.ASTORE, 3))

                var keySegmentOffset = 0
                for (keyChunk in keyChunkListByClass[targetClassNode] ?: return@createInitializationInstructions) {
                    add(FieldInsnNode(Opcodes.GETSTATIC, keyChunk.classNode.name, keyChunk.fieldName, KEY_BYTES_FIELD_DESCRIPTOR))
                    add(integerPushInstruction(0))
                    add(VarInsnNode(Opcodes.ALOAD, 3))
                    add(integerPushInstruction(keySegmentOffset))
                    add(FieldInsnNode(Opcodes.GETSTATIC, keyChunk.classNode.name, keyChunk.fieldName, KEY_BYTES_FIELD_DESCRIPTOR))
                    add(InsnNode(Opcodes.ARRAYLENGTH))
                    add(MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/System", "arraycopy", "(Ljava/lang/Object;ILjava/lang/Object;II)V"))
                    keySegmentOffset += keyChunk.chunkOfIndices.size
                }

                // Cipher init
                add(VarInsnNode(Opcodes.ALOAD, 2))
                add(InsnNode(Opcodes.ICONST_2))
                add(VarInsnNode(Opcodes.ALOAD, 1))
                add(TypeInsnNode(Opcodes.NEW, "javax/crypto/spec/DESKeySpec"))
                add(InsnNode(Opcodes.DUP))
                add(VarInsnNode(Opcodes.ALOAD, 3))
                add(MethodInsnNode(Opcodes.INVOKESPECIAL, "javax/crypto/spec/DESKeySpec", "<init>", "([B)V"))
                add(MethodInsnNode(Opcodes.INVOKEVIRTUAL, "javax/crypto/SecretKeyFactory", "generateSecret", "(Ljava/security/spec/KeySpec;)Ljavax/crypto/SecretKey;"))
                add(TypeInsnNode(Opcodes.NEW, "javax/crypto/spec/IvParameterSpec"))
                add(InsnNode(Opcodes.DUP))
                add(integerPushInstruction(8))
                add(IntInsnNode(Opcodes.NEWARRAY, 8))
                add(MethodInsnNode(Opcodes.INVOKESPECIAL, "javax/crypto/spec/IvParameterSpec", "<init>", "([B)V"))
                add(MethodInsnNode(Opcodes.INVOKEVIRTUAL, "javax/crypto/Cipher", "init", "(ILjava/security/Key;Ljava/security/spec/AlgorithmParameterSpec;)V"))

                // String processing loop setup
                add(LdcInsnNode(combinedEncryptedPayload.toString()))
                add(VarInsnNode(Opcodes.ASTORE, 4))
                add(VarInsnNode(Opcodes.ALOAD, 4))
                add(MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/String", "length", "()I"))
                add(VarInsnNode(Opcodes.ISTORE, 5))

                val fieldCounterBaseIndex = 13
                arrayFieldMetadataList.forEachIndexed { index, _ ->
                    add(integerPushInstruction(0))
                    add(VarInsnNode(Opcodes.ISTORE, fieldCounterBaseIndex + index))
                }

                add(integerPushInstruction(initialChunkLength))
                add(VarInsnNode(Opcodes.ISTORE, 6))
                add(integerPushInstruction(-2))
                add(VarInsnNode(Opcodes.ISTORE, 7))
                add(integerPushInstruction(0))
                add(VarInsnNode(Opcodes.ISTORE, 8))

                add(decryptionLoopStartLabel)

                add(IincInsnNode(7, 2))
                add(VarInsnNode(Opcodes.ILOAD, 7))
                add(VarInsnNode(Opcodes.ISTORE, 9))

                // Decrypt part
                add(VarInsnNode(Opcodes.ALOAD, 2))
                add(VarInsnNode(Opcodes.ALOAD, 4))
                add(VarInsnNode(Opcodes.ILOAD, 9))
                add(VarInsnNode(Opcodes.ILOAD, 9))
                add(VarInsnNode(Opcodes.ILOAD, 6))
                add(InsnNode(Opcodes.IADD))
                add(MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/String", "substring", "(II)Ljava/lang/String;"))
                add(FieldInsnNode(Opcodes.GETSTATIC, "java/nio/charset/StandardCharsets", "ISO_8859_1", "Ljava/nio/charset/Charset;"))
                add(MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/String", "getBytes", "(Ljava/nio/charset/Charset;)[B"))
                add(MethodInsnNode(Opcodes.INVOKEVIRTUAL, "javax/crypto/Cipher", "doFinal", "([B)[B"))
                add(VarInsnNode(Opcodes.ASTORE, 10))

                // Store decrypted string
                add(VarInsnNode(Opcodes.ALOAD, 4))
                add(VarInsnNode(Opcodes.ILOAD, 7))
                add(VarInsnNode(Opcodes.ILOAD, 6))
                add(InsnNode(Opcodes.IADD))
                add(VarInsnNode(Opcodes.ISTORE, 11))

                add(VarInsnNode(Opcodes.ILOAD, 11))
                add(MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/String", "charAt", "(I)C"))
                add(integerPushInstruction(INSTRUCTION_CHAR_OFFSET))
                add(InsnNode(Opcodes.ISUB))
                add(VarInsnNode(Opcodes.ISTORE, 12))

                val switchCaseLabels = arrayFieldMetadataList.map { LabelNode() }.toTypedArray()
                add(VarInsnNode(Opcodes.ILOAD, 12))
                add(TableSwitchInsnNode(0, arrayFieldMetadataList.size - 1, switchDefaultLabel, *switchCaseLabels))

                arrayFieldMetadataList.forEachIndexed { index, arrayFieldMetadata ->
                    add(switchCaseLabels[index])
                    add(FieldInsnNode(Opcodes.GETSTATIC, targetClassNode.name, arrayFieldMetadata.fieldName, arrayFieldMetadata.fieldDescriptor))
                    add(VarInsnNode(Opcodes.ILOAD, fieldCounterBaseIndex + index))
                    add(TypeInsnNode(Opcodes.NEW, "java/lang/String"))
                    add(InsnNode(Opcodes.DUP))
                    add(VarInsnNode(Opcodes.ALOAD, 10))
                    add(FieldInsnNode(Opcodes.GETSTATIC, "java/nio/charset/StandardCharsets", "UTF_8", "Ljava/nio/charset/Charset;"))
                    add(MethodInsnNode(Opcodes.INVOKESPECIAL, "java/lang/String", "<init>", "([BLjava/nio/charset/Charset;)V"))
                    add(InsnNode(Opcodes.AASTORE))
                    add(IincInsnNode(fieldCounterBaseIndex + index, 1))
                    add(JumpInsnNode(Opcodes.GOTO, switchDefaultLabel))
                }

                add(switchDefaultLabel)

                // Advance currentReadPosition (i3 in snippet) by chunkLength (c in snippet)
                add(VarInsnNode(Opcodes.ILOAD, 11))
                add(InsnNode(Opcodes.DUP))
                add(VarInsnNode(Opcodes.ISTORE, 7))
                add(VarInsnNode(Opcodes.ILOAD, 5))
                add(integerPushInstruction(1))
                add(InsnNode(Opcodes.ISUB))

                // if (i5 >= length) goto returnLabel;
                add(JumpInsnNode(Opcodes.IF_ICMPGE, staticInitializerExitLabel))

                add(VarInsnNode(Opcodes.ALOAD, 4))
                add(VarInsnNode(Opcodes.ILOAD, 7))
                add(integerPushInstruction(1))
                add(InsnNode(Opcodes.IADD))
                add(MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/String", "charAt", "(I)C"))
                add(VarInsnNode(Opcodes.ISTORE, 6))
                add(JumpInsnNode(Opcodes.GOTO, decryptionLoopStartLabel))

                add(staticInitializerExitLabel)
            })
        })
    }

    override fun obfuscateString(originalString: String, keyBytes: ByteArray): Pair<String, ByteArray> {
        cipher.init(Cipher.ENCRYPT_MODE, secretKeyFactory.generateSecret(DESKeySpec(keyBytes)), initializationVectorSpec)
        return String(cipher.doFinal(originalString.toByteArray(StandardCharsets.UTF_8)), StandardCharsets.ISO_8859_1) to keyBytes
    }

    fun initializeKeyChunkFields(targetClassNode: ClassNode) {
        if (!keyDerivationSeedByClass.containsKey(targetClassNode)) {
            return
        }

        val keyDerivationSeed = keyDerivationSeedByClass.getValue(targetClassNode)
        var localVarBaseIndex = 0
        var chunkByteOffset = 0

        for (keyChunk in keyChunkListByClass[targetClassNode] ?: return) {
            val loopBeginLabelNode = LabelNode()
            val loopEndLabelNode = LabelNode()

            val staticInitializer = keyChunk.classNode.resolveStaticInitializer()
            staticInitializer.node.instructions.insert(buildInstructionList {
                // 1. long j = keyDerivationSeed;
                add(longPushInstruction(keyDerivationSeed))
                add(VarInsnNode(Opcodes.LSTORE, localVarBaseIndex))

                // 2. byte[] bArr = new byte[keyChunk.chunkOfIndices.size()];
                add(integerPushInstruction(keyChunk.chunkOfIndices.size))
                add(IntInsnNode(Opcodes.NEWARRAY, Opcodes.T_BYTE))
                add(VarInsnNode(Opcodes.ASTORE, localVarBaseIndex + 2)) // Corrected slot

                // 3. Loop: for (int i = 0; i < bArr.length; i++)
                // Initialize loop counter i = 0
                add(integerPushInstruction(0))
                add(VarInsnNode(Opcodes.ISTORE, localVarBaseIndex + 3))

                add(loopBeginLabelNode)
                // Loop condition: if (i >= bArr.length) goto forLoopExitLabel
                add(VarInsnNode(Opcodes.ILOAD, localVarBaseIndex + 3))
                add(VarInsnNode(Opcodes.ALOAD, localVarBaseIndex + 2))
                add(InsnNode(Opcodes.ARRAYLENGTH))
                add(JumpInsnNode(Opcodes.IF_ICMPGE, loopEndLabelNode))

                // Inside loop: bArr[i] = (byte) ((jSeed << (i * 8)) >>> 56);
                // Array reference and index for BASTORE
                add(VarInsnNode(Opcodes.ALOAD, localVarBaseIndex + 2))
                add(VarInsnNode(Opcodes.ILOAD, localVarBaseIndex + 3))

                // Calculate value:
                add(VarInsnNode(Opcodes.LLOAD, localVarBaseIndex))
                add(VarInsnNode(Opcodes.ILOAD, localVarBaseIndex + 3))
                add(integerPushInstruction(chunkByteOffset))
                add(InsnNode(Opcodes.IADD))
                add(integerPushInstruction(8))
                add(InsnNode(Opcodes.IMUL))
                add(InsnNode(Opcodes.LSHL))
                add(integerPushInstruction(56))
                add(InsnNode(Opcodes.LUSHR))
                add(InsnNode(Opcodes.L2I))
                add(InsnNode(Opcodes.I2B))

                add(InsnNode(Opcodes.BASTORE))

                // Increment loop counter: i++
                add(IincInsnNode(localVarBaseIndex + 3, 1))
                add(JumpInsnNode(Opcodes.GOTO, loopBeginLabelNode))

                add(loopEndLabelNode)
                // Store the populated bArr into the static field
                add(VarInsnNode(Opcodes.ALOAD, localVarBaseIndex + 2))
                add(FieldInsnNode(Opcodes.PUTSTATIC, keyChunk.classNode.name, keyChunk.fieldName, KEY_BYTES_FIELD_DESCRIPTOR))
            })

            localVarBaseIndex += 4
            chunkByteOffset += keyChunk.chunkOfIndices.size
        }
    }

    private fun deriveKeyFromSeed(seed: Long): ByteArray {
        val desKeyBytes = ByteArray(8)
        for (i in 0..<desKeyBytes.size) {
            desKeyBytes[i] = ((seed shl (i * 8)) ushr 56).toByte()
        }
        return desKeyBytes
    }
}
