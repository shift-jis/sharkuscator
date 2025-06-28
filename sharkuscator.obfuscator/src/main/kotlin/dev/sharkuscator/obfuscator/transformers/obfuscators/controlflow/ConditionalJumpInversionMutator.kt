package dev.sharkuscator.obfuscator.transformers.obfuscators.controlflow

import dev.sharkuscator.obfuscator.ObfuscatorServices
import dev.sharkuscator.obfuscator.transformers.TransformerStrength
import dev.sharkuscator.obfuscator.utilities.AssemblyHelper.buildInstructionList
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.AbstractInsnNode
import org.objectweb.asm.tree.InsnList
import org.objectweb.asm.tree.JumpInsnNode
import org.objectweb.asm.tree.LabelNode


object ConditionalJumpInversionMutator : ControlFlowMangleMutator {
    override fun processInstruction(instructions: InsnList, targetInstruction: AbstractInsnNode) {
        if (targetInstruction !is JumpInsnNode) {
            ObfuscatorServices.sharkLogger.error("ConditionalJumpInversionMutator received an instruction it cannot process, despite canProcess being true.")
            return
        }

        val originalLabel = targetInstruction.label
        val fallThroughLabel = LabelNode()
        targetInstruction.setOpcode(invertJumpCondition(targetInstruction.opcode))
        targetInstruction.label = fallThroughLabel

        instructions.insert(targetInstruction, buildInstructionList {
            add(JumpInsnNode(Opcodes.GOTO, originalLabel))
            add(fallThroughLabel)
        })
    }

    override fun isApplicableFor(instruction: AbstractInsnNode, applicationChancePercentage: Int): Boolean {
        return instruction.opcode >= Opcodes.IFEQ && instruction.opcode <= Opcodes.IF_ACMPNE
    }

    override fun transformerStrength(): TransformerStrength {
        return TransformerStrength.MODERATE
    }

    private fun invertJumpCondition(opcode: Int): Int {
        return when (opcode) {
            Opcodes.IFNE -> Opcodes.IFEQ
            Opcodes.IFEQ -> Opcodes.IFNE
            Opcodes.IFGE -> Opcodes.IFLT
            Opcodes.IFGT -> Opcodes.IFLE
            Opcodes.IFLE -> Opcodes.IFGT
            Opcodes.IFLT -> Opcodes.IFGE
            Opcodes.IFNONNULL -> Opcodes.IFNULL
            Opcodes.IFNULL -> Opcodes.IFNONNULL
            Opcodes.IF_ACMPEQ -> Opcodes.IF_ACMPNE
            Opcodes.IF_ACMPNE -> Opcodes.IF_ACMPEQ
            Opcodes.IF_ICMPEQ -> Opcodes.IF_ICMPNE
            Opcodes.IF_ICMPNE -> Opcodes.IF_ICMPEQ
            Opcodes.IF_ICMPGE -> Opcodes.IF_ICMPLT
            Opcodes.IF_ICMPGT -> Opcodes.IF_ICMPLE
            Opcodes.IF_ICMPLE -> Opcodes.IF_ICMPGT
            Opcodes.IF_ICMPLT -> Opcodes.IF_ICMPGE
            else -> throw IllegalStateException(String.format("Unable to reverse jump opcode: %d", opcode))
        }
    }
}
