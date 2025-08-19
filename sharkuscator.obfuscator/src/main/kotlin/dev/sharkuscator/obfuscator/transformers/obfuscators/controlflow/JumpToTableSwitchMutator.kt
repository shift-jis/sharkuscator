package dev.sharkuscator.obfuscator.transformers.obfuscators.controlflow

import dev.sharkuscator.commons.AssemblyHelper.buildInstructionList
import dev.sharkuscator.commons.AssemblyHelper.integerPushInstruction
import dev.sharkuscator.obfuscator.ObfuscatorServices
import dev.sharkuscator.obfuscator.transformers.TransformerStrength
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.*
import kotlin.random.Random

object JumpToTableSwitchMutator : ControlFlowMangleMutator {
    override fun processInstruction(instructions: InsnList, targetInstruction: AbstractInsnNode) {
        if (targetInstruction !is JumpInsnNode) {
            ObfuscatorServices.sharkLogger.error("JumpToTableSwitchMutator received an instruction it cannot process, despite canProcess being true.")
            return
        }

        val tableSwitchKey = Random.nextInt(0xF, 0xFFFF)
        val trueBranchTargetLabel = LabelNode()
        val falseBranchTargetLabel = LabelNode()
        val switchTrueCaseTargetLabel = LabelNode()
        val switchEvaluationLabel = LabelNode()

        instructions.insert(targetInstruction, buildInstructionList {
            add(JumpInsnNode(targetInstruction.opcode, trueBranchTargetLabel))
            add(integerPushInstruction(tableSwitchKey - 1))
            add(JumpInsnNode(Opcodes.GOTO, switchEvaluationLabel))
            add(trueBranchTargetLabel)
            add(integerPushInstruction(tableSwitchKey))
            add(JumpInsnNode(Opcodes.GOTO, switchEvaluationLabel))
            add(switchTrueCaseTargetLabel)
            add(InsnNode(Opcodes.NOP))
            add(integerPushInstruction(tableSwitchKey - 2))
            add(switchEvaluationLabel)
            add(TableSwitchInsnNode(tableSwitchKey - 1, tableSwitchKey, targetInstruction.label, falseBranchTargetLabel, switchTrueCaseTargetLabel))
            add(falseBranchTargetLabel)
        })
        instructions.remove(targetInstruction)
    }

    override fun isApplicableFor(instruction: AbstractInsnNode, applicationChancePercentage: Int): Boolean {
        val shouldApplyBasedOnChance = Random.nextInt(0, 100) <= applicationChancePercentage
        return instruction is JumpInsnNode && instruction.opcode != Opcodes.GOTO && shouldApplyBasedOnChance
    }

    override fun transformerStrength(): TransformerStrength {
        return TransformerStrength.MODERATE
    }
}
