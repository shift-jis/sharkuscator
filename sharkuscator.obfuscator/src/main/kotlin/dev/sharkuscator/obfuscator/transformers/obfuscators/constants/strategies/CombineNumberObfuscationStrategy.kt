package dev.sharkuscator.obfuscator.transformers.obfuscators.constants.strategies

import dev.sharkuscator.obfuscator.ObfuscationContext
import dev.sharkuscator.obfuscator.transformers.strategies.NumericConstantObfuscationStrategy
import dev.sharkuscator.obfuscator.utilities.AssemblyHelper.obfuscatedNumericPushInstructions
import org.mapleir.asm.ClassNode
import org.objectweb.asm.tree.AbstractInsnNode
import org.objectweb.asm.tree.InsnList
import kotlin.random.Random

class CombineNumberObfuscationStrategy : NumericConstantObfuscationStrategy {
    override fun initialization(obfuscationContext: ObfuscationContext, targetClassNode: ClassNode) {
    }

    override fun replaceInstructions(targetClassNode: ClassNode, instructions: InsnList, targetInstruction: AbstractInsnNode, originalValue: Number) {
        val obfuscatedNumber = obfuscateNumber(originalValue, Random.nextInt())
        when (originalValue) {
            is Int, is Byte, is Short -> {
                instructions.insert(targetInstruction, obfuscatedNumericPushInstructions(obfuscatedNumber.first.toInt()))
                instructions.remove(targetInstruction)
            }

            is Long -> {
                instructions.insert(targetInstruction, obfuscatedNumericPushInstructions(obfuscatedNumber.first.toLong()))
                instructions.remove(targetInstruction)
            }
        }
    }

    override fun obfuscateNumber(originalValue: Number, keyNumber: Number): Pair<Number, Number> {
        return when (originalValue) {
            is Long, is Int, is Byte, is Short -> originalValue.toLong() to keyNumber
            else -> originalValue to keyNumber
        }
    }
}
