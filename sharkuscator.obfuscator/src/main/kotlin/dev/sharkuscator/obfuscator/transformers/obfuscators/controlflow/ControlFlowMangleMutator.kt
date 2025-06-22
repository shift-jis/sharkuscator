package dev.sharkuscator.obfuscator.transformers.obfuscators.controlflow

import dev.sharkuscator.obfuscator.transformers.TransformerStrength
import org.objectweb.asm.tree.AbstractInsnNode
import org.objectweb.asm.tree.InsnList

interface ControlFlowMangleMutator {
    fun processInstruction(instructions: InsnList, targetInstruction: AbstractInsnNode)

    fun isApplicableFor(instruction: AbstractInsnNode, applicationChancePercentage: Int): Boolean

    fun transformerStrength(): TransformerStrength
}
