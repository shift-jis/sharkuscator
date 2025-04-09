package dev.sharkuscator.obfuscator.transformers.obfuscators.constants.strategy

import dev.sharkuscator.obfuscator.extensions.invokeStatic
import dev.sharkuscator.obfuscator.extensions.xor
import dev.sharkuscator.obfuscator.utilities.BytecodeAssembler
import org.mapleir.asm.ClassHelper
import org.mapleir.asm.ClassNode
import org.mapleir.asm.MethodNode
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.*

/**
 * Basic Xor string encryption
 */
class NormalStringEncryption : StringEncryptionStrategy {
    private val decryptMethodDescriptor = "(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;"
    private val decryptMethodAccess = Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC

    lateinit var decryptorMethodNode: MethodNode
    lateinit var decryptorClassNode: ClassNode

    override fun createDecryptorClassNode(name: String): ClassNode {
        val decryptMethodNode = BytecodeAssembler.createMethodNode(decryptMethodAccess, "decrypt", decryptMethodDescriptor).apply {
            val forLoopBeginLabelNode = LabelNode()
            val forLoopEndLabelNode = LabelNode()

            instructions = BytecodeAssembler.buildInstructionList(
                // --- Initialization ---
                VarInsnNode(Opcodes.ALOAD, 0),
                MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/String", "length", "()I"),
                VarInsnNode(Opcodes.ISTORE, 2),
                TypeInsnNode(Opcodes.NEW, "java/lang/StringBuilder"),
                InsnNode(Opcodes.DUP),
                VarInsnNode(Opcodes.ILOAD, 2),
                MethodInsnNode(Opcodes.INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "(I)V"),
                VarInsnNode(Opcodes.ASTORE, 4),

                InsnNode(Opcodes.ICONST_0),
                VarInsnNode(Opcodes.ISTORE, 5),

                // --- Loop Start ---
                // loopStartLabel:
                forLoopBeginLabelNode,
                VarInsnNode(Opcodes.ILOAD, 5),
                VarInsnNode(Opcodes.ILOAD, 2),
                // if (i >= len1) goto loopEndLabel;
                JumpInsnNode(Opcodes.IF_ICMPGE, forLoopEndLabelNode),

                // --- Inside Loop ---
                // char char1 = str1.charAt(i);
                VarInsnNode(Opcodes.ALOAD, 0),
                VarInsnNode(Opcodes.ILOAD, 5),
                MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/String", "charAt", "(I)C"),
                VarInsnNode(Opcodes.ISTORE, 6),

                // char char2 = str2.charAt(i);
                VarInsnNode(Opcodes.ALOAD, 1),
                VarInsnNode(Opcodes.ILOAD, 5),
                MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/String", "charAt", "(I)C"),
                VarInsnNode(Opcodes.ISTORE, 7),

                // int xorResultCode = char1 ^ char2;
                VarInsnNode(Opcodes.ILOAD, 6),
                VarInsnNode(Opcodes.ILOAD, 7),
                InsnNode(Opcodes.IXOR),
                VarInsnNode(Opcodes.ISTORE, 8),

                // char resultChar = (char) xorResultCode;
                VarInsnNode(Opcodes.ILOAD, 8),
                InsnNode(Opcodes.I2C),
                VarInsnNode(Opcodes.ISTORE, 9),

                // result.append(resultChar);
                VarInsnNode(Opcodes.ALOAD, 4),
                VarInsnNode(Opcodes.ILOAD, 9),
                MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(C)Ljava/lang/StringBuilder;"),
                InsnNode(Opcodes.POP),

                // --- Loop Increment ---
                IincInsnNode(5, 1),
                JumpInsnNode(Opcodes.GOTO, forLoopBeginLabelNode),
                forLoopEndLabelNode,

                // --- Loop End ---
                VarInsnNode(Opcodes.ALOAD, 4),
                MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;"),
                InsnNode(Opcodes.ARETURN),
            )
        }

        return ClassHelper.create(BytecodeAssembler.createClassNode(name).apply { methods.add(decryptMethodNode) }).also {
            decryptorMethodNode = MethodNode(decryptMethodNode, it)
            decryptorClassNode = it
        }
    }

    override fun replaceInstructions(instructions: InsnList, original: LdcInsnNode, replacement: String, keyBytes: ByteArray) {
        instructions.insert(original, decryptorMethodNode.invokeStatic())
        instructions.insert(original, LdcInsnNode(keyBytes.decodeToString()))
        instructions.insert(original, LdcInsnNode(replacement))
        instructions.remove(original)
    }

    override fun encryptString(value: String, keyBytes: ByteArray): Pair<String, ByteArray> {
        return Pair(value xor keyBytes.decodeToString(), keyBytes)
    }
}
