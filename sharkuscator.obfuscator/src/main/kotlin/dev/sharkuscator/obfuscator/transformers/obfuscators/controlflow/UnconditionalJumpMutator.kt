package dev.sharkuscator.obfuscator.transformers.obfuscators.controlflow

import dev.sharkuscator.commons.AssemblyHelper
import dev.sharkuscator.commons.AssemblyHelper.buildInstructionList
import dev.sharkuscator.obfuscator.ObfuscatorServices
import dev.sharkuscator.obfuscator.transformers.TransformerStrength
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.*
import kotlin.random.Random

object UnconditionalJumpMutator : ControlFlowMangleMutator {
    override fun processInstruction(instructions: InsnList, targetInstruction: AbstractInsnNode) {
        if (targetInstruction !is JumpInsnNode) {
            ObfuscatorServices.sharkLogger.error("UnconditionalJumpMutator received an instruction it cannot process, despite canProcess being true.")
            return
        }

        instructions.insert(targetInstruction, buildInstructionList {
            add(AssemblyHelper.integerPushInstruction(0))
            add(JumpInsnNode(Opcodes.IFEQ, targetInstruction.label))
            add(LabelNode())
            add(InsnNode(Opcodes.ACONST_NULL))
            add(InsnNode(Opcodes.ATHROW))
        })
        instructions.remove(targetInstruction)
    }

    override fun isApplicableFor(instruction: AbstractInsnNode, applicationChancePercentage: Int): Boolean {
        val shouldApplyBasedOnChance = Random.nextInt(0, 100) <= applicationChancePercentage
        return instruction is JumpInsnNode && instruction.opcode == Opcodes.GOTO && shouldApplyBasedOnChance
    }

    override fun transformerStrength(): TransformerStrength {
        return TransformerStrength.LIGHT
    }
}
