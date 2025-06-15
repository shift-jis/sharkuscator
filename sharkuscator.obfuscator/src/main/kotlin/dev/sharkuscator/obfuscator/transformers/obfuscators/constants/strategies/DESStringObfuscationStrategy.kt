package dev.sharkuscator.obfuscator.transformers.obfuscators.constants.strategies

import dev.sharkuscator.obfuscator.ObfuscationContext
import dev.sharkuscator.obfuscator.extensions.addField
import dev.sharkuscator.obfuscator.extensions.getOrCreateStaticInitializer
import dev.sharkuscator.obfuscator.extensions.invokeStatic
import dev.sharkuscator.obfuscator.extensions.shouldSkipTransform
import dev.sharkuscator.obfuscator.transformers.strategies.StringConstantObfuscationStrategy
import dev.sharkuscator.obfuscator.utilities.BytecodeUtils.buildInstructionList
import dev.sharkuscator.obfuscator.utilities.BytecodeUtils.complexIntegerPushInstruction
import dev.sharkuscator.obfuscator.utilities.BytecodeUtils.createFieldNode
import dev.sharkuscator.obfuscator.utilities.BytecodeUtils.createMethodNode
import dev.sharkuscator.obfuscator.utilities.BytecodeUtils.integerPushInstruction
import dev.sharkuscator.obfuscator.utilities.BytecodeUtils.longPushInstruction
import org.mapleir.asm.ClassNode
import org.mapleir.asm.FieldNode
import org.mapleir.asm.MethodNode
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.*
import java.nio.charset.StandardCharsets
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.DESKeySpec
import javax.crypto.spec.IvParameterSpec
import kotlin.random.Random

class DESStringObfuscationStrategy : StringConstantObfuscationStrategy {
    private class EncryptionKeyChunk(val classNode: ClassNode, val fieldName: String, val chunkOfIndices: List<Int>)

    private val KEY_BYTES_FIELD_DESCRIPTOR = "[B"
    private val KEY_BYTES_FIELD_ACCESS = Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC + Opcodes.ACC_SYNTHETIC + Opcodes.ACC_TRANSIENT

    private val DEOBFUSCATED_STRINGS_FIELD_DESCRIPTOR = "[Ljava/lang/String;"
    private val DEOBFUSCATED_STRINGS_FIELD_ACCESS = Opcodes.ACC_PRIVATE + Opcodes.ACC_STATIC + Opcodes.ACC_SYNTHETIC + Opcodes.ACC_TRANSIENT

    private val DECODER_METHOD_DESCRIPTOR = "(I)Ljava/lang/String;"
    private val DECODER_METHOD_ACCESS = Opcodes.ACC_PRIVATE + Opcodes.ACC_STATIC + Opcodes.ACC_BRIDGE + Opcodes.ACC_SYNTHETIC

    private val classToKeyChunkList = mutableListOf<Pair<ClassNode, EncryptionKeyChunk>>()
    private val classToEncryptedStrings = mutableMapOf<ClassNode, MutableList<String>>()
    private val classToDecoderMethod = mutableMapOf<ClassNode, MethodNode>()
    private val classToKeyDerivationSeed = mutableMapOf<ClassNode, Long>()

    private val initializationVectorSpec = IvParameterSpec(ByteArray(8))
    private val secretKeyFactory = SecretKeyFactory.getInstance("DES")
    private val cipher = Cipher.getInstance("DES/CBC/PKCS5Padding")

    private val stringsFieldName = "tung tung tung sahur"

    override fun prepareDecoderMethod(context: ObfuscationContext, targetClassNode: ClassNode, decoderMethodName: String): MethodNode {
        if (classToDecoderMethod.containsKey(targetClassNode)) {
            return classToDecoderMethod.getValue(targetClassNode)
        }

        classToEncryptedStrings[targetClassNode] = mutableListOf()
        classToKeyDerivationSeed[targetClassNode] = Random.nextLong()

        val builtMethodNode = createMethodNode(DECODER_METHOD_ACCESS, decoderMethodName, DECODER_METHOD_DESCRIPTOR).apply {
            instructions = buildInstructionList(
                FieldInsnNode(Opcodes.GETSTATIC, targetClassNode.name, stringsFieldName, DEOBFUSCATED_STRINGS_FIELD_DESCRIPTOR),
                VarInsnNode(Opcodes.ILOAD, 0),
                InsnNode(Opcodes.AALOAD),
                InsnNode(Opcodes.ARETURN),
            )
        }

        return MethodNode(builtMethodNode, targetClassNode).also {
            classToDecoderMethod.putIfAbsent(targetClassNode, it)
            targetClassNode.addMethod(it)
        }
    }

    override fun replaceInstructions(preparedDecoder: MethodNode, instructions: InsnList, targetInstruction: AbstractInsnNode, originalString: String) {
        val obfuscatedString = obfuscateString(originalString, deriveKeyFromSeed(classToKeyDerivationSeed.getValue(preparedDecoder.owner)))
        classToEncryptedStrings[preparedDecoder.owner]!!.add(obfuscatedString.first)

        val replacementInstructions = buildInstructionList(
            complexIntegerPushInstruction(classToEncryptedStrings.getValue(preparedDecoder.owner).size - 1),
            preparedDecoder.invokeStatic()
        )

        instructions.insert(targetInstruction, replacementInstructions)
        instructions.remove(targetInstruction)
    }

    override fun finalizeClass(context: ObfuscationContext, targetClassNode: ClassNode) {
        if (!classToEncryptedStrings.containsKey(targetClassNode) || !classToKeyDerivationSeed.containsKey(targetClassNode)) {
            return
        }

        val currentClassEncryptedStrings = classToEncryptedStrings.getValue(targetClassNode)
        if (currentClassEncryptedStrings.isEmpty()) {
            return
        }

        targetClassNode.addField(createFieldNode(DEOBFUSCATED_STRINGS_FIELD_ACCESS, stringsFieldName, DEOBFUSCATED_STRINGS_FIELD_DESCRIPTOR))

        val fieldDictionary = context.resolveDictionary(FieldNode::class.java)
        val decryptionKeyBytes = deriveKeyFromSeed(classToKeyDerivationSeed.getValue(targetClassNode))

        val keyByteIndexChunks = (0..decryptionKeyBytes.size - 1).chunked((1..decryptionKeyBytes.size - 1).random())
        keyByteIndexChunks.forEach { chunkOfIndices ->
            val hostClassNode = context.jarContents.classContents.filter { !it.shouldSkipTransform() && !context.exclusions.excluded(it) }.random() ?: return@forEach
            val generatedKeyFieldName = fieldDictionary.generateNextName(null)
            hostClassNode.addField(createFieldNode(KEY_BYTES_FIELD_ACCESS, generatedKeyFieldName, KEY_BYTES_FIELD_DESCRIPTOR))
            classToKeyChunkList.add(targetClassNode to EncryptionKeyChunk(hostClassNode, generatedKeyFieldName, chunkOfIndices))
        }

        val combinedEncryptedPayload = StringBuilder()
        for ((index, string) in currentClassEncryptedStrings.withIndex()) {
            combinedEncryptedPayload.append(string)
            if (currentClassEncryptedStrings.size > index + 1) {
                combinedEncryptedPayload.append(currentClassEncryptedStrings[index + 1].length.toChar())
            }
        }

        val staticInitializerExitLabel = LabelNode()
        val decryptionLoopStartLabel = LabelNode()
        val initializerInstructions = InsnList()

        // Initialize the deobfuscatedStrings array
        initializerInstructions.add(integerPushInstruction(currentClassEncryptedStrings.size))
        initializerInstructions.add(TypeInsnNode(Opcodes.ANEWARRAY, "java/lang/String"))
        initializerInstructions.add(FieldInsnNode(Opcodes.PUTSTATIC, targetClassNode.name, stringsFieldName, DEOBFUSCATED_STRINGS_FIELD_DESCRIPTOR))

        // DES Key and Cipher Initialization (original logic)
        initializerInstructions.add(LdcInsnNode("DES"))
        initializerInstructions.add(MethodInsnNode(Opcodes.INVOKESTATIC, "javax/crypto/SecretKeyFactory", "getInstance", "(Ljava/lang/String;)Ljavax/crypto/SecretKeyFactory;"))
        initializerInstructions.add(VarInsnNode(Opcodes.ASTORE, 1))

        initializerInstructions.add(LdcInsnNode("DES/CBC/PKCS5Padding"))
        initializerInstructions.add(MethodInsnNode(Opcodes.INVOKESTATIC, "javax/crypto/Cipher", "getInstance", "(Ljava/lang/String;)Ljavax/crypto/Cipher;"))
        initializerInstructions.add(VarInsnNode(Opcodes.ASTORE, 2))

        initializerInstructions.add(integerPushInstruction(decryptionKeyBytes.size))
        initializerInstructions.add(IntInsnNode(Opcodes.NEWARRAY, Opcodes.T_BYTE))
        initializerInstructions.add(VarInsnNode(Opcodes.ASTORE, 3))

        var keySegmentOffset = 0
        for ((_, keyChunk) in classToKeyChunkList.filter { it.first == targetClassNode }) {
            initializerInstructions.add(FieldInsnNode(Opcodes.GETSTATIC, keyChunk.classNode.name, keyChunk.fieldName, KEY_BYTES_FIELD_DESCRIPTOR))
            initializerInstructions.add(integerPushInstruction(0))
            initializerInstructions.add(VarInsnNode(Opcodes.ALOAD, 3))
            initializerInstructions.add(complexIntegerPushInstruction(keySegmentOffset))
            initializerInstructions.add(FieldInsnNode(Opcodes.GETSTATIC, keyChunk.classNode.name, keyChunk.fieldName, KEY_BYTES_FIELD_DESCRIPTOR))
            initializerInstructions.add(InsnNode(Opcodes.ARRAYLENGTH))
            initializerInstructions.add(MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/System", "arraycopy", "(Ljava/lang/Object;ILjava/lang/Object;II)V"))
            keySegmentOffset += keyChunk.chunkOfIndices.size
        }

        // Cipher init
        initializerInstructions.add(VarInsnNode(Opcodes.ALOAD, 2))
        initializerInstructions.add(InsnNode(Opcodes.ICONST_2))
        initializerInstructions.add(VarInsnNode(Opcodes.ALOAD, 1))
        initializerInstructions.add(TypeInsnNode(Opcodes.NEW, "javax/crypto/spec/DESKeySpec"))
        initializerInstructions.add(InsnNode(Opcodes.DUP))
        initializerInstructions.add(VarInsnNode(Opcodes.ALOAD, 3))
        initializerInstructions.add(MethodInsnNode(Opcodes.INVOKESPECIAL, "javax/crypto/spec/DESKeySpec", "<init>", "([B)V"))
        initializerInstructions.add(MethodInsnNode(Opcodes.INVOKEVIRTUAL, "javax/crypto/SecretKeyFactory", "generateSecret", "(Ljava/security/spec/KeySpec;)Ljavax/crypto/SecretKey;"))
        initializerInstructions.add(TypeInsnNode(Opcodes.NEW, "javax/crypto/spec/IvParameterSpec"))
        initializerInstructions.add(InsnNode(Opcodes.DUP))
        initializerInstructions.add(integerPushInstruction(8))
        initializerInstructions.add(IntInsnNode(Opcodes.NEWARRAY, 8))
        initializerInstructions.add(MethodInsnNode(Opcodes.INVOKESPECIAL, "javax/crypto/spec/IvParameterSpec", "<init>", "([B)V"))
        initializerInstructions.add(MethodInsnNode(Opcodes.INVOKEVIRTUAL, "javax/crypto/Cipher", "init", "(ILjava/security/Key;Ljava/security/spec/AlgorithmParameterSpec;)V"))

        // String processing loop setup
        initializerInstructions.add(LdcInsnNode(combinedEncryptedPayload.toString()))
        initializerInstructions.add(VarInsnNode(Opcodes.ASTORE, 4))
        initializerInstructions.add(VarInsnNode(Opcodes.ALOAD, 4))
        initializerInstructions.add(MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/String", "length", "()I"))
        initializerInstructions.add(VarInsnNode(Opcodes.ISTORE, 5))

        initializerInstructions.add(integerPushInstruction(currentClassEncryptedStrings[0].length))
        initializerInstructions.add(VarInsnNode(Opcodes.ISTORE, 6))
        initializerInstructions.add(integerPushInstruction(-1))
        initializerInstructions.add(VarInsnNode(Opcodes.ISTORE, 7))
        initializerInstructions.add(integerPushInstruction(0))
        initializerInstructions.add(VarInsnNode(Opcodes.ISTORE, 8))

        initializerInstructions.add(decryptionLoopStartLabel)

        initializerInstructions.add(IincInsnNode(7, 1))
        initializerInstructions.add(VarInsnNode(Opcodes.ILOAD, 7))
        initializerInstructions.add(VarInsnNode(Opcodes.ISTORE, 9))

        // Decrypt part
        initializerInstructions.add(VarInsnNode(Opcodes.ALOAD, 2))
        initializerInstructions.add(VarInsnNode(Opcodes.ALOAD, 4))
        initializerInstructions.add(VarInsnNode(Opcodes.ILOAD, 9))
        initializerInstructions.add(VarInsnNode(Opcodes.ILOAD, 9))
        initializerInstructions.add(VarInsnNode(Opcodes.ILOAD, 6))
        initializerInstructions.add(InsnNode(Opcodes.IADD))
        initializerInstructions.add(MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/String", "substring", "(II)Ljava/lang/String;"))
        initializerInstructions.add(FieldInsnNode(Opcodes.GETSTATIC, "java/nio/charset/StandardCharsets", "ISO_8859_1", "Ljava/nio/charset/Charset;"))
        initializerInstructions.add(MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/String", "getBytes", "(Ljava/nio/charset/Charset;)[B"))
        initializerInstructions.add(MethodInsnNode(Opcodes.INVOKEVIRTUAL, "javax/crypto/Cipher", "doFinal", "([B)[B"))
        initializerInstructions.add(VarInsnNode(Opcodes.ASTORE, 10))

        // Store decrypted string
        initializerInstructions.add(FieldInsnNode(Opcodes.GETSTATIC, targetClassNode.name, stringsFieldName, DEOBFUSCATED_STRINGS_FIELD_DESCRIPTOR))
        initializerInstructions.add(VarInsnNode(Opcodes.ILOAD, 8))
        initializerInstructions.add(IincInsnNode(8, 1))
        initializerInstructions.add(TypeInsnNode(Opcodes.NEW, "java/lang/String"))
        initializerInstructions.add(InsnNode(Opcodes.DUP))
        initializerInstructions.add(VarInsnNode(Opcodes.ALOAD, 10))
        initializerInstructions.add(FieldInsnNode(Opcodes.GETSTATIC, "java/nio/charset/StandardCharsets", "UTF_8", "Ljava/nio/charset/Charset;"))
        initializerInstructions.add(MethodInsnNode(Opcodes.INVOKESPECIAL, "java/lang/String", "<init>", "([BLjava/nio/charset/Charset;)V"))
        initializerInstructions.add(InsnNode(Opcodes.AASTORE))

        // Advance currentReadPosition (i3 in snippet) by chunkLength (c in snippet)
        initializerInstructions.add(VarInsnNode(Opcodes.ILOAD, 7))
        initializerInstructions.add(VarInsnNode(Opcodes.ILOAD, 6))
        initializerInstructions.add(InsnNode(Opcodes.IADD))
        initializerInstructions.add(InsnNode(Opcodes.DUP))
        initializerInstructions.add(VarInsnNode(Opcodes.ISTORE, 7))
        initializerInstructions.add(VarInsnNode(Opcodes.ILOAD, 5))

        // if (i5 >= length) goto returnLabel;
        initializerInstructions.add(JumpInsnNode(Opcodes.IF_ICMPGE, staticInitializerExitLabel))

        initializerInstructions.add(VarInsnNode(Opcodes.ALOAD, 4))
        initializerInstructions.add(VarInsnNode(Opcodes.ILOAD, 7))
        initializerInstructions.add(MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/String", "charAt", "(I)C"))
        initializerInstructions.add(VarInsnNode(Opcodes.ISTORE, 6))
        initializerInstructions.add(JumpInsnNode(Opcodes.GOTO, decryptionLoopStartLabel))

        initializerInstructions.add(staticInitializerExitLabel)

        val staticInitializer = targetClassNode.getOrCreateStaticInitializer()
        staticInitializer.node.instructions.insert(initializerInstructions)
    }

    override fun obfuscateString(originalString: String, keyBytes: ByteArray): Pair<String, ByteArray> {
        cipher.init(Cipher.ENCRYPT_MODE, secretKeyFactory.generateSecret(DESKeySpec(keyBytes)), initializationVectorSpec)
        return String(cipher.doFinal(originalString.toByteArray(StandardCharsets.UTF_8)), StandardCharsets.ISO_8859_1) to keyBytes
    }

    fun initializeKeyChunkFields(targetClassNode: ClassNode) {
        if (!classToEncryptedStrings.containsKey(targetClassNode) || !classToKeyDerivationSeed.containsKey(targetClassNode)) {
            return
        }

        val keyDerivationSeed = classToKeyDerivationSeed.getValue(targetClassNode)
        var localVarBaseIndex = 0
        var chunkByteOffset = 0

        for ((_, keyChunk) in classToKeyChunkList.filter { it.first == targetClassNode }) {
            val replacementInstructions = InsnList()
            val forLoopStartLabel = LabelNode()
            val forLoopExitLabel = LabelNode()

            // 1. long j = keyDerivationSeed;
            replacementInstructions.add(longPushInstruction(keyDerivationSeed))
            replacementInstructions.add(VarInsnNode(Opcodes.LSTORE, localVarBaseIndex))

            // 2. byte[] bArr = new byte[keyChunk.chunkOfIndices.size()];
            replacementInstructions.add(integerPushInstruction(keyChunk.chunkOfIndices.size))
            replacementInstructions.add(IntInsnNode(Opcodes.NEWARRAY, Opcodes.T_BYTE))
            replacementInstructions.add(VarInsnNode(Opcodes.ASTORE, localVarBaseIndex + 2)) // Corrected slot

            // 3. Loop: for (int i = 0; i < bArr.length; i++)
            // Initialize loop counter i = 0
            replacementInstructions.add(integerPushInstruction(0))
            replacementInstructions.add(VarInsnNode(Opcodes.ISTORE, localVarBaseIndex + 3))

            replacementInstructions.add(forLoopStartLabel)
            // Loop condition: if (i >= bArr.length) goto forLoopExitLabel
            replacementInstructions.add(VarInsnNode(Opcodes.ILOAD, localVarBaseIndex + 3))
            replacementInstructions.add(VarInsnNode(Opcodes.ALOAD, localVarBaseIndex + 2))
            replacementInstructions.add(InsnNode(Opcodes.ARRAYLENGTH))
            replacementInstructions.add(JumpInsnNode(Opcodes.IF_ICMPGE, forLoopExitLabel))

            // Inside loop: bArr[i] = (byte) ((jSeed << (i * 8)) >>> 56);
            // Array reference and index for BASTORE
            replacementInstructions.add(VarInsnNode(Opcodes.ALOAD, localVarBaseIndex + 2))
            replacementInstructions.add(VarInsnNode(Opcodes.ILOAD, localVarBaseIndex + 3))

            // Calculate value:
            replacementInstructions.add(VarInsnNode(Opcodes.LLOAD, localVarBaseIndex))
            replacementInstructions.add(VarInsnNode(Opcodes.ILOAD, localVarBaseIndex + 3))
            replacementInstructions.add(complexIntegerPushInstruction(chunkByteOffset))
            replacementInstructions.add(InsnNode(Opcodes.IADD))
            replacementInstructions.add(integerPushInstruction(8))
            replacementInstructions.add(InsnNode(Opcodes.IMUL))
            replacementInstructions.add(InsnNode(Opcodes.LSHL))
            replacementInstructions.add(integerPushInstruction(56))
            replacementInstructions.add(InsnNode(Opcodes.LUSHR))
            replacementInstructions.add(InsnNode(Opcodes.L2I))
            replacementInstructions.add(InsnNode(Opcodes.I2B))

            replacementInstructions.add(InsnNode(Opcodes.BASTORE))

            // Increment loop counter: i++
            replacementInstructions.add(IincInsnNode(localVarBaseIndex + 3, 1))
            replacementInstructions.add(JumpInsnNode(Opcodes.GOTO, forLoopStartLabel))

            replacementInstructions.add(forLoopExitLabel)
            // Store the populated bArr into the static field
            replacementInstructions.add(VarInsnNode(Opcodes.ALOAD, localVarBaseIndex + 2))
            replacementInstructions.add(FieldInsnNode(Opcodes.PUTSTATIC, keyChunk.classNode.name, keyChunk.fieldName, KEY_BYTES_FIELD_DESCRIPTOR))

            val staticInitializer = keyChunk.classNode.getOrCreateStaticInitializer()
            staticInitializer.node.instructions.insert(replacementInstructions)

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
