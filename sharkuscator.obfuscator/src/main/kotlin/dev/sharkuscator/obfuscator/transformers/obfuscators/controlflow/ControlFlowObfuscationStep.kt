package dev.sharkuscator.obfuscator.transformers.obfuscators.controlflow

import org.objectweb.asm.tree.AbstractInsnNode
import org.objectweb.asm.tree.InsnList

interface ControlFlowObfuscationStep {
    fun processInstruction(instructions: InsnList, targetInstruction: AbstractInsnNode)

    fun isApplicableFor(instruction: AbstractInsnNode, applicationChancePercentage: Int): Boolean
}
