package dev.sharkuscator.obfuscator.transformers.obfuscators.controlflow

import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.*
import kotlin.random.Random

object JunkInstructionStep : ControlFlowObfuscationStep {
    private const val LDC_BIPUSH_MIN_VALUE = -64
    private const val LDC_BIPUSH_EXCLUSIVE_UPPER_BOUND = 64

    override fun processInstruction(instructions: InsnList, targetInstruction: AbstractInsnNode) {
        insertBipushPopPair(instructions, targetInstruction, Random.nextInt(LDC_BIPUSH_MIN_VALUE, LDC_BIPUSH_EXCLUSIVE_UPPER_BOUND))
    }

    override fun isApplicableFor(instruction: AbstractInsnNode, applicationChancePercentage: Int): Boolean {
        val shouldApplyBasedOnChance = Random.nextInt(0, 100) <= applicationChancePercentage
        return instruction is LdcInsnNode && shouldApplyBasedOnChance
    }

    override fun getObfuscationStrength(): ObfuscationStrength {
        return ObfuscationStrength.LIGHT
    }

    private fun insertBipushPopPair(instructions: InsnList, beforeNode: AbstractInsnNode, value: Int) {
        instructions.insertBefore(beforeNode, IntInsnNode(Opcodes.BIPUSH, value))
        instructions.insertBefore(beforeNode, InsnNode(Opcodes.POP))
    }
}
