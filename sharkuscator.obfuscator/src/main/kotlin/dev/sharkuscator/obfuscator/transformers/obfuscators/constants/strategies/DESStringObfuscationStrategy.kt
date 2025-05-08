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

    private val encryptedStringsByClass = mutableMapOf<ClassNode, MutableList<String>>()
    private val preparedDecoderMethods = mutableMapOf<ClassNode, MethodNode>()
    private val encryptionKeyStore = mutableMapOf<ClassNode, ByteArray>()

    private val initializationVectorSpec = IvParameterSpec(ByteArray(8))
    private val secretKeyFactory = SecretKeyFactory.getInstance("DES")
    private val cipher = Cipher.getInstance("DES/CBC/PKCS5Padding")

    override fun prepareDecoderMethod(targetClassNode: ClassNode, decoderMethodName: String): MethodNode {
        if (preparedDecoderMethods.containsKey(targetClassNode)) {
            return preparedDecoderMethods.getValue(targetClassNode)
        }

        encryptedStringsByClass[targetClassNode] = mutableListOf()
        encryptionKeyStore[targetClassNode] = Random.nextBytes(8)

        val builtMethodNode = BytecodeUtils.createMethodNode(DECODER_METHOD_ACCESS, decoderMethodName, DECODER_METHOD_DESCRIPTOR).apply {
            instructions = BytecodeUtils.buildInstructionList(
                FieldInsnNode(Opcodes.GETSTATIC, targetClassNode.name, "funny", DEOBFUSCATED_STRINGS_FIELD_DESCRIPTOR),
                VarInsnNode(Opcodes.ILOAD, 0),
                InsnNode(Opcodes.AALOAD),
                InsnNode(Opcodes.ARETURN),
            )
        }

        return MethodNode(builtMethodNode, targetClassNode).also {
            preparedDecoderMethods.putIfAbsent(targetClassNode, it)
            targetClassNode.addMethod(it)
        }
    }

    override fun replaceInstructions(preparedDecoder: MethodNode, instructions: InsnList, targetInstruction: AbstractInsnNode, originalString: String) {
        val obfuscatedString = obfuscateString(originalString, encryptionKeyStore.getValue(preparedDecoder.owner))
        encryptedStringsByClass[preparedDecoder.owner]!!.add(obfuscatedString.first)

        val replacementInstructions = BytecodeUtils.buildInstructionList(
            LdcInsnNode(encryptedStringsByClass.getValue(preparedDecoder.owner).size - 1),
            preparedDecoder.invokeStatic()
        )

        instructions.insert(targetInstruction, replacementInstructions)
        instructions.remove(targetInstruction)
    }

    override fun finalizeClass(targetClassNode: ClassNode) {
        if (!encryptedStringsByClass.containsKey(targetClassNode) || !encryptionKeyStore.containsKey(targetClassNode)) {
            return
        }

        val currentClassEncryptedStrings = encryptedStringsByClass.getValue(targetClassNode)
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

        val generatedStringsFieldName = "funny"
        targetClassNode.addField(BytecodeUtils.createFieldNode(DEOBFUSCATED_STRINGS_FIELD_ACCESS, generatedStringsFieldName, DEOBFUSCATED_STRINGS_FIELD_DESCRIPTOR))

        val decryptionKeyBytes = encryptionKeyStore.getValue(targetClassNode)
        val staticInitializerExitLabel = LabelNode()
        val decryptionLoopStartLabel = LabelNode()

        val replacementInstructions = BytecodeUtils.buildInstructionList(
            // Initialize the deobfuscatedStrings array
            BytecodeUtils.integerPushInstruction(currentClassEncryptedStrings.size),
            TypeInsnNode(Opcodes.ANEWARRAY, "java/lang/String"),
            FieldInsnNode(Opcodes.PUTSTATIC, targetClassNode.name, generatedStringsFieldName, DEOBFUSCATED_STRINGS_FIELD_DESCRIPTOR),

            // DES Key and Cipher Initialization (original logic)
            LdcInsnNode("DES"),
            MethodInsnNode(Opcodes.INVOKESTATIC, "javax/crypto/SecretKeyFactory", "getInstance", "(Ljava/lang/String;)Ljavax/crypto/SecretKeyFactory;"),
            VarInsnNode(Opcodes.ASTORE, 1),

            LdcInsnNode("DES/CBC/PKCS5Padding"),
            MethodInsnNode(Opcodes.INVOKESTATIC, "javax/crypto/Cipher", "getInstance", "(Ljava/lang/String;)Ljavax/crypto/Cipher;"),
            VarInsnNode(Opcodes.ASTORE, 2),

            // This is a hardcoded key. This should match the key used for encryption.
            // The original code uses a byte array [0,1,2,3,4,5,6,7] as the key material for DESKeySpec.
            // This needs to be consistent with how `obfuscateString` generates/uses keys.
            // For DES, key needs to be 8 bytes.
            BytecodeUtils.integerPushInstruction(8),
            IntInsnNode(Opcodes.NEWARRAY, Opcodes.T_BYTE),
            InsnNode(Opcodes.DUP),
            BytecodeUtils.integerPushInstruction(0),
            BytecodeUtils.integerPushInstruction(decryptionKeyBytes[0]),
            InsnNode(Opcodes.BASTORE),
            InsnNode(Opcodes.DUP),
            BytecodeUtils.integerPushInstruction(1),
            BytecodeUtils.integerPushInstruction(decryptionKeyBytes[1]),
            InsnNode(Opcodes.BASTORE),
            InsnNode(Opcodes.DUP),
            BytecodeUtils.integerPushInstruction(2),
            BytecodeUtils.integerPushInstruction(decryptionKeyBytes[2]),
            InsnNode(Opcodes.BASTORE),
            InsnNode(Opcodes.DUP),
            BytecodeUtils.integerPushInstruction(3),
            BytecodeUtils.integerPushInstruction(decryptionKeyBytes[3]),
            InsnNode(Opcodes.BASTORE),
            InsnNode(Opcodes.DUP),
            BytecodeUtils.integerPushInstruction(4),
            BytecodeUtils.integerPushInstruction(decryptionKeyBytes[4]),
            InsnNode(Opcodes.BASTORE),
            InsnNode(Opcodes.DUP),
            BytecodeUtils.integerPushInstruction(5),
            BytecodeUtils.integerPushInstruction(decryptionKeyBytes[5]),
            InsnNode(Opcodes.BASTORE),
            InsnNode(Opcodes.DUP),
            BytecodeUtils.integerPushInstruction(6),
            BytecodeUtils.integerPushInstruction(decryptionKeyBytes[6]),
            InsnNode(Opcodes.BASTORE),
            InsnNode(Opcodes.DUP),
            BytecodeUtils.integerPushInstruction(7),
            BytecodeUtils.integerPushInstruction(decryptionKeyBytes[7]),
            InsnNode(Opcodes.BASTORE),
            VarInsnNode(Opcodes.ASTORE, 3),

            // Cipher init
            VarInsnNode(Opcodes.ALOAD, 2),
            InsnNode(Opcodes.ICONST_2),
            VarInsnNode(Opcodes.ALOAD, 1),
            TypeInsnNode(Opcodes.NEW, "javax/crypto/spec/DESKeySpec"),
            InsnNode(Opcodes.DUP),
            VarInsnNode(Opcodes.ALOAD, 3),
            MethodInsnNode(Opcodes.INVOKESPECIAL, "javax/crypto/spec/DESKeySpec", "<init>", "([B)V"),
            MethodInsnNode(Opcodes.INVOKEVIRTUAL, "javax/crypto/SecretKeyFactory", "generateSecret", "(Ljava/security/spec/KeySpec;)Ljavax/crypto/SecretKey;"),
            TypeInsnNode(Opcodes.NEW, "javax/crypto/spec/IvParameterSpec"),
            InsnNode(Opcodes.DUP),
            BytecodeUtils.integerPushInstruction(8),
            IntInsnNode(Opcodes.NEWARRAY, 8),
            MethodInsnNode(Opcodes.INVOKESPECIAL, "javax/crypto/spec/IvParameterSpec", "<init>", "([B)V"),
            MethodInsnNode(Opcodes.INVOKEVIRTUAL, "javax/crypto/Cipher", "init", "(ILjava/security/Key;Ljava/security/spec/AlgorithmParameterSpec;)V"),

            // String processing loop setup
            LdcInsnNode(combinedEncryptedPayload.toString()),
            VarInsnNode(Opcodes.ASTORE, 4),
            VarInsnNode(Opcodes.ALOAD, 4),
            MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/String", "length", "()I"),
            VarInsnNode(Opcodes.ISTORE, 5),

            BytecodeUtils.integerPushInstruction(currentClassEncryptedStrings[0].length),
            VarInsnNode(Opcodes.ISTORE, 6),
            BytecodeUtils.integerPushInstruction(-1),
            VarInsnNode(Opcodes.ISTORE, 7),
            BytecodeUtils.integerPushInstruction(0),
            VarInsnNode(Opcodes.ISTORE, 8),

            decryptionLoopStartLabel,

            IincInsnNode(7, 1),
            VarInsnNode(Opcodes.ILOAD, 7),
            VarInsnNode(Opcodes.ISTORE, 9),

            // Decrypt part
            VarInsnNode(Opcodes.ALOAD, 2),
            VarInsnNode(Opcodes.ALOAD, 4),
            VarInsnNode(Opcodes.ILOAD, 9),
            VarInsnNode(Opcodes.ILOAD, 9),
            VarInsnNode(Opcodes.ILOAD, 6),
            InsnNode(Opcodes.IADD),
            MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/String", "substring", "(II)Ljava/lang/String;"),
            FieldInsnNode(Opcodes.GETSTATIC, "java/nio/charset/StandardCharsets", "ISO_8859_1", "Ljava/nio/charset/Charset;"),
            MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/String", "getBytes", "(Ljava/nio/charset/Charset;)[B"),
            MethodInsnNode(Opcodes.INVOKEVIRTUAL, "javax/crypto/Cipher", "doFinal", "([B)[B"),
            VarInsnNode(Opcodes.ASTORE, 10),

            // Store decrypted string
            FieldInsnNode(Opcodes.GETSTATIC, targetClassNode.name, generatedStringsFieldName, DEOBFUSCATED_STRINGS_FIELD_DESCRIPTOR),
            VarInsnNode(Opcodes.ILOAD, 8),
            IincInsnNode(8, 1),
            TypeInsnNode(Opcodes.NEW, "java/lang/String"),
            InsnNode(Opcodes.DUP),
            VarInsnNode(Opcodes.ALOAD, 10),
            FieldInsnNode(Opcodes.GETSTATIC, "java/nio/charset/StandardCharsets", "UTF_8", "Ljava/nio/charset/Charset;"),
            MethodInsnNode(Opcodes.INVOKESPECIAL, "java/lang/String", "<init>", "([BLjava/nio/charset/Charset;)V"),
            InsnNode(Opcodes.AASTORE),

            // Advance currentReadPosition (i3 in snippet) by chunkLength (c in snippet)
            VarInsnNode(Opcodes.ILOAD, 7),
            VarInsnNode(Opcodes.ILOAD, 6),
            InsnNode(Opcodes.IADD),
            InsnNode(Opcodes.DUP),
            VarInsnNode(Opcodes.ISTORE, 7),
            VarInsnNode(Opcodes.ILOAD, 5),

            // if (i5 >= length) goto returnLabel;
            JumpInsnNode(Opcodes.IF_ICMPGE, staticInitializerExitLabel),

            VarInsnNode(Opcodes.ALOAD, 4),
            VarInsnNode(Opcodes.ILOAD, 7),
            MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/String", "charAt", "(I)C"),
            VarInsnNode(Opcodes.ISTORE, 6),
            JumpInsnNode(Opcodes.GOTO, decryptionLoopStartLabel),

            staticInitializerExitLabel,
        )

        val staticInitializer = targetClassNode.getOrCreateStaticInitializer()
        staticInitializer.node.instructions.insert(replacementInstructions)
    }

    override fun obfuscateString(originalString: String, keyBytes: ByteArray): Pair<String, ByteArray> {
        cipher.init(Cipher.ENCRYPT_MODE, secretKeyFactory.generateSecret(DESKeySpec(keyBytes)), initializationVectorSpec)
        return Pair(String(cipher.doFinal(originalString.toByteArray(StandardCharsets.UTF_8)), StandardCharsets.ISO_8859_1), keyBytes)
    }
}
