package dev.sharkuscator.obfuscator.transformers.obfuscators.constants.strategies

import org.objectweb.asm.tree.AbstractInsnNode
import org.objectweb.asm.tree.InsnList

interface NumericConstantObfuscationStrategy {
    fun replaceInstructions(instructions: InsnList, targetInstruction: AbstractInsnNode, originalValue: Number)

    fun obfuscateNumber(originalValue: Number, keyNumber: Number = 0): Pair<Number, Number>
}
