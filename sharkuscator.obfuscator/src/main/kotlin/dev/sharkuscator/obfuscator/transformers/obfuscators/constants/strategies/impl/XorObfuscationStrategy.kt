package dev.sharkuscator.obfuscator.transformers.obfuscators.constants.strategies.impl

import dev.sharkuscator.obfuscator.extensions.invokeStatic
import dev.sharkuscator.obfuscator.extensions.xor
import dev.sharkuscator.obfuscator.transformers.obfuscators.constants.strategies.StringConstantObfuscationStrategy
import dev.sharkuscator.obfuscator.utilities.BytecodeUtils
import org.apache.commons.lang3.RandomStringUtils
import org.mapleir.asm.ClassHelper
import org.mapleir.asm.ClassNode
import org.mapleir.asm.MethodNode
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.AbstractInsnNode
import org.objectweb.asm.tree.IincInsnNode
import org.objectweb.asm.tree.InsnList
import org.objectweb.asm.tree.InsnNode
import org.objectweb.asm.tree.JumpInsnNode
import org.objectweb.asm.tree.LabelNode
import org.objectweb.asm.tree.LdcInsnNode
import org.objectweb.asm.tree.MethodInsnNode
import org.objectweb.asm.tree.TypeInsnNode
import org.objectweb.asm.tree.VarInsnNode

/**
 * Basic Xor string encryption
 */
class XorObfuscationStrategy : StringConstantObfuscationStrategy {
    private val decryptMethodDescriptor = "(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;"
    private val decryptMethodAccess = Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC + Opcodes.ACC_BRIDGE + Opcodes.ACC_SYNTHETIC

    private lateinit var decryptorMethodNode: MethodNode
    private lateinit var decryptorClassNode: ClassNode

    override fun createDecryptClassNode(className: String, methodName: String): ClassNode {
        val decryptMethodNode = BytecodeUtils.createMethodNode(decryptMethodAccess, methodName, decryptMethodDescriptor).apply {
            val forLoopBeginLabelNode = LabelNode()
            val forLoopEndLabelNode = LabelNode()

            instructions = BytecodeUtils.buildInstructionList(
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

        return ClassHelper.create(BytecodeUtils.createClassNode(className).apply { methods.add(decryptMethodNode) }).also {
            decryptorMethodNode = MethodNode(decryptMethodNode, it)
            decryptorClassNode = it
        }
    }

    override fun replaceInstructions(instructions: InsnList, targetInstruction: AbstractInsnNode, originalString: String) {
        val resultPair = obfuscateString(originalString, RandomStringUtils.randomAlphanumeric(originalString.length))
        instructions.insert(targetInstruction, decryptorMethodNode.invokeStatic())
        instructions.insert(targetInstruction, LdcInsnNode(resultPair.second.decodeToString()))
        instructions.insert(targetInstruction, LdcInsnNode(resultPair.first))
        instructions.remove(targetInstruction)
    }

    override fun obfuscateString(originalString: String, keyBytes: ByteArray): Pair<String, ByteArray> {
        return Pair(originalString xor keyBytes.decodeToString(), keyBytes)
    }
}