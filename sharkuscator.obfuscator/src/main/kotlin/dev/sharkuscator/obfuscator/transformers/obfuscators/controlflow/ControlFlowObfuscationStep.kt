package dev.sharkuscator.obfuscator.transformers.obfuscators.controlflow

import org.objectweb.asm.tree.AbstractInsnNode
import org.objectweb.asm.tree.InsnList

abstract class ControlFlowObfuscationStep(protected val applicationChancePercentage: Int = 50) {
    abstract fun processInstruction(instructions: InsnList, targetInstruction: AbstractInsnNode)

    abstract fun isApplicableFor(instruction: AbstractInsnNode): Boolean
}
