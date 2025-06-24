package dev.sharkuscator.obfuscator.transformers.strategies

import dev.sharkuscator.obfuscator.ObfuscationContext
import org.mapleir.asm.ClassNode
import org.objectweb.asm.tree.AbstractInsnNode
import org.objectweb.asm.tree.InsnList

interface NumericConstantObfuscationStrategy {
    fun initialization(obfuscationContext: ObfuscationContext, targetClassNode: ClassNode)

    fun replaceInstructions(targetClassNode: ClassNode, instructions: InsnList, targetInstruction: AbstractInsnNode, originalValue: Number)

    fun obfuscateNumber(originalValue: Number, keyNumber: Number = 0): Pair<Number, Number>
}
