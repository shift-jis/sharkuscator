package dev.sharkuscator.obfuscator.transformers.obfuscators.controlflow

import dev.sharkuscator.commons.AssemblyHelper.buildInstructionList
import dev.sharkuscator.commons.AssemblyHelper.invertJumpCondition
import dev.sharkuscator.obfuscator.ObfuscatorServices
import dev.sharkuscator.obfuscator.transformers.TransformerStrength
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
}
