package dev.sharkuscator.obfuscator.transformers.obfuscators.constants.strategies

import dev.sharkuscator.obfuscator.ObfuscationContext
import dev.sharkuscator.obfuscator.extensions.invokeStatic
import dev.sharkuscator.obfuscator.extensions.xor
import dev.sharkuscator.obfuscator.transformers.strategies.StringConstantObfuscationStrategy
import dev.sharkuscator.obfuscator.utilities.BytecodeUtils.buildInstructionList
import dev.sharkuscator.obfuscator.utilities.BytecodeUtils.createMethodNode
import org.apache.commons.lang3.RandomStringUtils
import org.mapleir.asm.ClassNode
import org.mapleir.asm.MethodNode
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.*

/**
 * Basic Xor string encryption
 */
class XorStringObfuscationStrategy : StringConstantObfuscationStrategy {
    private val DECODER_METHOD_DESCRIPTOR = "(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;"
    private val DECODER_METHOD_ACCESS = Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC + Opcodes.ACC_BRIDGE + Opcodes.ACC_SYNTHETIC
    private val decodeMethodCache = mutableMapOf<ClassNode, MethodNode>()

    override fun prepareDecoderMethod(context: ObfuscationContext, targetClassNode: ClassNode, decoderMethodName: String): MethodNode {
        if (decodeMethodCache.containsKey(targetClassNode)) {
            return decodeMethodCache.getValue(targetClassNode)
        }

        val builtMethodNode = createMethodNode(DECODER_METHOD_ACCESS, decoderMethodName, DECODER_METHOD_DESCRIPTOR).apply {
            val forLoopBeginLabelNode = LabelNode()
            val forLoopEndLabelNode = LabelNode()

            instructions = buildInstructionList(
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

        return MethodNode(builtMethodNode, targetClassNode).also {
            decodeMethodCache.putIfAbsent(targetClassNode, it)
            targetClassNode.addMethod(it)
        }
    }

    override fun replaceInstructions(preparedDecoder: MethodNode, instructions: InsnList, targetInstruction: AbstractInsnNode, originalString: String) {
        val obfuscatedString = obfuscateString(originalString, RandomStringUtils.randomAlphanumeric(originalString.length))
        val replacementInstructions = buildInstructionList(
            LdcInsnNode(obfuscatedString.first),
            LdcInsnNode(obfuscatedString.second.decodeToString()),
            preparedDecoder.invokeStatic()
        )

        instructions.insert(targetInstruction, replacementInstructions)
        instructions.remove(targetInstruction)
    }

    override fun finalizeClass(context: ObfuscationContext, targetClassNode: ClassNode) {
        // do nothing
    }

    override fun obfuscateString(originalString: String, keyBytes: ByteArray): Pair<String, ByteArray> {
        return originalString xor keyBytes.decodeToString() to keyBytes
    }
}
