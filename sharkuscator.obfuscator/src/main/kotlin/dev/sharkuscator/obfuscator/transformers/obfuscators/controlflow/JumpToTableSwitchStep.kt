package dev.sharkuscator.obfuscator.transformers.obfuscators.controlflow

import dev.sharkuscator.obfuscator.ObfuscatorServices
import dev.sharkuscator.obfuscator.utilities.BytecodeUtils.complexIntegerPushInstruction
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.*
import kotlin.random.Random

class JumpToTableSwitchStep : ControlFlowObfuscationStep {
    override fun processInstruction(instructions: InsnList, targetInstruction: AbstractInsnNode) {
        if (targetInstruction !is JumpInsnNode) {
            ObfuscatorServices.sharkLogger.error("JumpToTableSwitchStep received an instruction it cannot process, despite canProcess being true.")
            return
        }

        val prepareTrueCaseForSwitchLabel = LabelNode()
        val falseCaseFinalTargetLabel = LabelNode()
        val trueCaseToSwitchDefaultLabel = LabelNode()
        val actualTableSwitchLabel = LabelNode()

        val tableSwitchKey = Random.nextInt(0xF, 0xFFFF)
        val newInstructionSequence = InsnList()

        newInstructionSequence.add(JumpInsnNode(targetInstruction.opcode, prepareTrueCaseForSwitchLabel))
        newInstructionSequence.add(complexIntegerPushInstruction(tableSwitchKey - 1))
        newInstructionSequence.add(JumpInsnNode(Opcodes.GOTO, actualTableSwitchLabel))
        newInstructionSequence.add(prepareTrueCaseForSwitchLabel)
        newInstructionSequence.add(complexIntegerPushInstruction(tableSwitchKey))
        newInstructionSequence.add(JumpInsnNode(Opcodes.GOTO, actualTableSwitchLabel))
        newInstructionSequence.add(trueCaseToSwitchDefaultLabel)
        newInstructionSequence.add(InsnNode(Opcodes.NOP))
        newInstructionSequence.add(complexIntegerPushInstruction(tableSwitchKey - 2))
        newInstructionSequence.add(actualTableSwitchLabel)
        newInstructionSequence.add(TableSwitchInsnNode(tableSwitchKey - 1, tableSwitchKey, targetInstruction.label, falseCaseFinalTargetLabel, trueCaseToSwitchDefaultLabel))
        newInstructionSequence.add(falseCaseFinalTargetLabel)

        instructions.insert(targetInstruction, newInstructionSequence)
        instructions.remove(targetInstruction)
    }

    override fun isApplicableFor(instruction: AbstractInsnNode, applicationChancePercentage: Int): Boolean {
        val shouldApplyBasedOnChance = Random.nextInt(0, 100) <= applicationChancePercentage
        return instruction is JumpInsnNode && instruction.opcode != Opcodes.GOTO && shouldApplyBasedOnChance
    }
}
