package dev.sharkuscator.obfuscator.transformers.obfuscators.controlflow

import dev.sharkuscator.obfuscator.ObfuscatorServices
import dev.sharkuscator.obfuscator.utilities.BytecodeUtils
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.*
import kotlin.random.Random

class UnconditionalJumpStep : ControlFlowObfuscationStep {
    override fun processInstruction(instructions: InsnList, targetInstruction: AbstractInsnNode) {
        if (targetInstruction !is JumpInsnNode) {
            ObfuscatorServices.sharkLogger.error("UnconditionalJumpStep received an instruction it cannot process, despite canProcess being true.")
            return
        }

        val newInstructionSequence = InsnList()

        newInstructionSequence.add(BytecodeUtils.integerPushInstruction(0))
        newInstructionSequence.add(JumpInsnNode(Opcodes.IFEQ, targetInstruction.label))
        newInstructionSequence.add(LabelNode())
        newInstructionSequence.add(InsnNode(Opcodes.ACONST_NULL))
        newInstructionSequence.add(InsnNode(Opcodes.ATHROW))

        instructions.insert(targetInstruction, newInstructionSequence)
        instructions.remove(targetInstruction)
    }

    override fun isApplicableFor(instruction: AbstractInsnNode, applicationChancePercentage: Int): Boolean {
        val shouldApplyBasedOnChance = Random.nextInt(0, 100) <= applicationChancePercentage
        return instruction is JumpInsnNode && instruction.opcode == Opcodes.GOTO && shouldApplyBasedOnChance
    }

    override fun getObfuscationStrength(): ObfuscationStrength {
        return ObfuscationStrength.LIGHT
    }
}
