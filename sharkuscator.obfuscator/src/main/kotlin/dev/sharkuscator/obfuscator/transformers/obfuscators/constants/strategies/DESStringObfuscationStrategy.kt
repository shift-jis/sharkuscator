package dev.sharkuscator.obfuscator.transformers.obfuscators.constants.strategies

import dev.sharkuscator.obfuscator.extensions.addField
import dev.sharkuscator.obfuscator.extensions.getOrCreateStaticInitializer
import dev.sharkuscator.obfuscator.extensions.invokeStatic
import dev.sharkuscator.obfuscator.transformers.strategies.StringConstantObfuscationStrategy
import dev.sharkuscator.obfuscator.utilities.BytecodeUtils
import org.mapleir.asm.ClassNode
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
    private val DEOBFUSCATED_STRINGS_FIELD_DESCRIPTOR = "[Ljava/lang/String;"
    private val DEOBFUSCATED_STRINGS_FIELD_ACCESS = Opcodes.ACC_PRIVATE + Opcodes.ACC_STATIC + Opcodes.ACC_SYNTHETIC

    private val DECODER_METHOD_DESCRIPTOR = "(I)Ljava/lang/String;"
    private val DECODER_METHOD_ACCESS = Opcodes.ACC_PRIVATE + Opcodes.ACC_STATIC + Opcodes.ACC_BRIDGE + Opcodes.ACC_SYNTHETIC

    private val classToEncryptedStrings = mutableMapOf<ClassNode, MutableList<String>>()
    private val classToDecoderMethod = mutableMapOf<ClassNode, MethodNode>()
    private val classToEncryptionKey = mutableMapOf<ClassNode, ByteArray>()

    private val initializationVectorSpec = IvParameterSpec(ByteArray(8))
    private val secretKeyFactory = SecretKeyFactory.getInstance("DES")
    private val cipher = Cipher.getInstance("DES/CBC/PKCS5Padding")

    private val stringsFieldName = " "

    override fun prepareDecoderMethod(targetClassNode: ClassNode, decoderMethodName: String): MethodNode {
        if (classToDecoderMethod.containsKey(targetClassNode)) {
            return classToDecoderMethod.getValue(targetClassNode)
        }

        classToEncryptedStrings[targetClassNode] = mutableListOf()
        classToEncryptionKey[targetClassNode] = Random.nextBytes(8)

        val builtMethodNode = BytecodeUtils.createMethodNode(DECODER_METHOD_ACCESS, decoderMethodName, DECODER_METHOD_DESCRIPTOR).apply {
            instructions = BytecodeUtils.buildInstructionList(
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
        val obfuscatedString = obfuscateString(originalString, classToEncryptionKey.getValue(preparedDecoder.owner))
        classToEncryptedStrings[preparedDecoder.owner]!!.add(obfuscatedString.first)

        val replacementInstructions = BytecodeUtils.buildInstructionList(
            BytecodeUtils.complexIntegerPushInstruction(classToEncryptedStrings.getValue(preparedDecoder.owner).size - 1),
            preparedDecoder.invokeStatic()
        )

        instructions.insert(targetInstruction, replacementInstructions)
        instructions.remove(targetInstruction)
    }

    override fun finalizeClass(targetClassNode: ClassNode) {
        if (!classToEncryptedStrings.containsKey(targetClassNode) || !classToEncryptionKey.containsKey(targetClassNode)) {
            return
        }

        val currentClassEncryptedStrings = classToEncryptedStrings.getValue(targetClassNode)
        if (currentClassEncryptedStrings.isEmpty()) {
            return
        }

        val combinedEncryptedPayload = StringBuilder()
        for ((index, string) in currentClassEncryptedStrings.withIndex()) {
            combinedEncryptedPayload.append(string)
            if (currentClassEncryptedStrings.size > index + 1) {
                combinedEncryptedPayload.append(currentClassEncryptedStrings[index + 1].length.toChar())
            }
        }

        targetClassNode.addField(BytecodeUtils.createFieldNode(DEOBFUSCATED_STRINGS_FIELD_ACCESS, stringsFieldName, DEOBFUSCATED_STRINGS_FIELD_DESCRIPTOR))

        val staticInitializerExitLabel = LabelNode()
        val decryptionLoopStartLabel = LabelNode()
        val replacementInstructions = InsnList()

        // Initialize the deobfuscatedStrings array
        replacementInstructions.add(BytecodeUtils.integerPushInstruction(currentClassEncryptedStrings.size))
        replacementInstructions.add(TypeInsnNode(Opcodes.ANEWARRAY, "java/lang/String"))
        replacementInstructions.add(FieldInsnNode(Opcodes.PUTSTATIC, targetClassNode.name, stringsFieldName, DEOBFUSCATED_STRINGS_FIELD_DESCRIPTOR))

        // DES Key and Cipher Initialization (original logic)
        replacementInstructions.add(LdcInsnNode("DES"))
        replacementInstructions.add(MethodInsnNode(Opcodes.INVOKESTATIC, "javax/crypto/SecretKeyFactory", "getInstance", "(Ljava/lang/String;)Ljavax/crypto/SecretKeyFactory;"))
        replacementInstructions.add(VarInsnNode(Opcodes.ASTORE, 1))

        replacementInstructions.add(LdcInsnNode("DES/CBC/PKCS5Padding"))
        replacementInstructions.add(MethodInsnNode(Opcodes.INVOKESTATIC, "javax/crypto/Cipher", "getInstance", "(Ljava/lang/String;)Ljavax/crypto/Cipher;"))
        replacementInstructions.add(VarInsnNode(Opcodes.ASTORE, 2))

        // This is a hardcoded key. This should match the key used for encryption.
        // This needs to be consistent with how `obfuscateString` generates/uses keys.
        val decryptionKeyBytes = classToEncryptionKey.getValue(targetClassNode)
        replacementInstructions.add(BytecodeUtils.integerPushInstruction(decryptionKeyBytes.size))
        replacementInstructions.add(IntInsnNode(Opcodes.NEWARRAY, Opcodes.T_BYTE))

        for (index in (0..decryptionKeyBytes.size - 1).shuffled()) {
            replacementInstructions.add(InsnNode(Opcodes.DUP))
            replacementInstructions.add(BytecodeUtils.complexIntegerPushInstruction(index))
            replacementInstructions.add(BytecodeUtils.complexIntegerPushInstruction(decryptionKeyBytes[index]))
            replacementInstructions.add(InsnNode(Opcodes.BASTORE))
        }

        replacementInstructions.add(VarInsnNode(Opcodes.ASTORE, 3))

        // Cipher init
        replacementInstructions.add(VarInsnNode(Opcodes.ALOAD, 2))
        replacementInstructions.add(InsnNode(Opcodes.ICONST_2))
        replacementInstructions.add(VarInsnNode(Opcodes.ALOAD, 1))
        replacementInstructions.add(TypeInsnNode(Opcodes.NEW, "javax/crypto/spec/DESKeySpec"))
        replacementInstructions.add(InsnNode(Opcodes.DUP))
        replacementInstructions.add(VarInsnNode(Opcodes.ALOAD, 3))
        replacementInstructions.add(MethodInsnNode(Opcodes.INVOKESPECIAL, "javax/crypto/spec/DESKeySpec", "<init>", "([B)V"))
        replacementInstructions.add(MethodInsnNode(Opcodes.INVOKEVIRTUAL, "javax/crypto/SecretKeyFactory", "generateSecret", "(Ljava/security/spec/KeySpec;)Ljavax/crypto/SecretKey;"))
        replacementInstructions.add(TypeInsnNode(Opcodes.NEW, "javax/crypto/spec/IvParameterSpec"))
        replacementInstructions.add(InsnNode(Opcodes.DUP))
        replacementInstructions.add(BytecodeUtils.integerPushInstruction(8))
        replacementInstructions.add(IntInsnNode(Opcodes.NEWARRAY, 8))
        replacementInstructions.add(MethodInsnNode(Opcodes.INVOKESPECIAL, "javax/crypto/spec/IvParameterSpec", "<init>", "([B)V"))
        replacementInstructions.add(MethodInsnNode(Opcodes.INVOKEVIRTUAL, "javax/crypto/Cipher", "init", "(ILjava/security/Key;Ljava/security/spec/AlgorithmParameterSpec;)V"))

        // String processing loop setup
        replacementInstructions.add(LdcInsnNode(combinedEncryptedPayload.toString()))
        replacementInstructions.add(VarInsnNode(Opcodes.ASTORE, 4))
        replacementInstructions.add(VarInsnNode(Opcodes.ALOAD, 4))
        replacementInstructions.add(MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/String", "length", "()I"))
        replacementInstructions.add(VarInsnNode(Opcodes.ISTORE, 5))

        replacementInstructions.add(BytecodeUtils.integerPushInstruction(currentClassEncryptedStrings[0].length))
        replacementInstructions.add(VarInsnNode(Opcodes.ISTORE, 6))
        replacementInstructions.add(BytecodeUtils.integerPushInstruction(-1))
        replacementInstructions.add(VarInsnNode(Opcodes.ISTORE, 7))
        replacementInstructions.add(BytecodeUtils.integerPushInstruction(0))
        replacementInstructions.add(VarInsnNode(Opcodes.ISTORE, 8))

        replacementInstructions.add(decryptionLoopStartLabel)

        replacementInstructions.add(IincInsnNode(7, 1))
        replacementInstructions.add(VarInsnNode(Opcodes.ILOAD, 7))
        replacementInstructions.add(VarInsnNode(Opcodes.ISTORE, 9))

        // Decrypt part
        replacementInstructions.add(VarInsnNode(Opcodes.ALOAD, 2))
        replacementInstructions.add(VarInsnNode(Opcodes.ALOAD, 4))
        replacementInstructions.add(VarInsnNode(Opcodes.ILOAD, 9))
        replacementInstructions.add(VarInsnNode(Opcodes.ILOAD, 9))
        replacementInstructions.add(VarInsnNode(Opcodes.ILOAD, 6))
        replacementInstructions.add(InsnNode(Opcodes.IADD))
        replacementInstructions.add(MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/String", "substring", "(II)Ljava/lang/String;"))
        replacementInstructions.add(FieldInsnNode(Opcodes.GETSTATIC, "java/nio/charset/StandardCharsets", "ISO_8859_1", "Ljava/nio/charset/Charset;"))
        replacementInstructions.add(MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/String", "getBytes", "(Ljava/nio/charset/Charset;)[B"))
        replacementInstructions.add(MethodInsnNode(Opcodes.INVOKEVIRTUAL, "javax/crypto/Cipher", "doFinal", "([B)[B"))
        replacementInstructions.add(VarInsnNode(Opcodes.ASTORE, 10))

        // Store decrypted string
        replacementInstructions.add(FieldInsnNode(Opcodes.GETSTATIC, targetClassNode.name, stringsFieldName, DEOBFUSCATED_STRINGS_FIELD_DESCRIPTOR))
        replacementInstructions.add(VarInsnNode(Opcodes.ILOAD, 8))
        replacementInstructions.add(IincInsnNode(8, 1))
        replacementInstructions.add(TypeInsnNode(Opcodes.NEW, "java/lang/String"))
        replacementInstructions.add(InsnNode(Opcodes.DUP))
        replacementInstructions.add(VarInsnNode(Opcodes.ALOAD, 10))
        replacementInstructions.add(FieldInsnNode(Opcodes.GETSTATIC, "java/nio/charset/StandardCharsets", "UTF_8", "Ljava/nio/charset/Charset;"))
        replacementInstructions.add(MethodInsnNode(Opcodes.INVOKESPECIAL, "java/lang/String", "<init>", "([BLjava/nio/charset/Charset;)V"))
        replacementInstructions.add(InsnNode(Opcodes.AASTORE))

        // Advance currentReadPosition (i3 in snippet) by chunkLength (c in snippet)
        replacementInstructions.add(VarInsnNode(Opcodes.ILOAD, 7))
        replacementInstructions.add(VarInsnNode(Opcodes.ILOAD, 6))
        replacementInstructions.add(InsnNode(Opcodes.IADD))
        replacementInstructions.add(InsnNode(Opcodes.DUP))
        replacementInstructions.add(VarInsnNode(Opcodes.ISTORE, 7))
        replacementInstructions.add(VarInsnNode(Opcodes.ILOAD, 5))

        // if (i5 >= length) goto returnLabel;
        replacementInstructions.add(JumpInsnNode(Opcodes.IF_ICMPGE, staticInitializerExitLabel))

        replacementInstructions.add(VarInsnNode(Opcodes.ALOAD, 4))
        replacementInstructions.add(VarInsnNode(Opcodes.ILOAD, 7))
        replacementInstructions.add(MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/String", "charAt", "(I)C"))
        replacementInstructions.add(VarInsnNode(Opcodes.ISTORE, 6))
        replacementInstructions.add(JumpInsnNode(Opcodes.GOTO, decryptionLoopStartLabel))

        replacementInstructions.add(staticInitializerExitLabel)

        val staticInitializer = targetClassNode.getOrCreateStaticInitializer()
        staticInitializer.node.instructions.insert(replacementInstructions)
    }

    override fun obfuscateString(originalString: String, keyBytes: ByteArray): Pair<String, ByteArray> {
        cipher.init(Cipher.ENCRYPT_MODE, secretKeyFactory.generateSecret(DESKeySpec(keyBytes)), initializationVectorSpec)
        return Pair(String(cipher.doFinal(originalString.toByteArray(StandardCharsets.UTF_8)), StandardCharsets.ISO_8859_1), keyBytes)
    }
}
