package dev.sharkuscator.obfuscator.transformers.obfuscators.constants.strategies

import dev.sharkuscator.obfuscator.ObfuscationContext
import dev.sharkuscator.obfuscator.transformers.strategies.NumericConstantObfuscationStrategy
import dev.sharkuscator.obfuscator.utilities.AssemblyHelper.integerPushInstruction
import dev.sharkuscator.obfuscator.utilities.AssemblyHelper.longPushInstruction
import dev.sharkuscator.obfuscator.utilities.Mathematics
import org.mapleir.asm.ClassNode
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.*
import kotlin.random.Random

class EncodedNumericConstantStrategy : NumericConstantObfuscationStrategy {
    override fun replaceInstructions(context: ObfuscationContext, classNode: ClassNode, instructions: InsnList, targetInstruction: AbstractInsnNode, originalValue: Number) {
        val obfuscatedNumber = obfuscateNumber(originalValue, Random.nextInt())
        when (originalValue) {
            is Int, is Byte, is Short -> {
                val (operandA, operandB) = Mathematics.generateOperandsForAnd(obfuscatedNumber.first.toInt())
                instructions.insert(targetInstruction, InsnNode(Opcodes.IAND))
                instructions.insert(targetInstruction, integerPushInstruction(operandB.toInt()))
                instructions.insert(targetInstruction, integerPushInstruction(operandA.toInt()))
                instructions.remove(targetInstruction)
            }

            is Long -> {
                val (operandA, operandB) = Mathematics.generateOperandsForAnd(obfuscatedNumber.first.toLong())
                instructions.insert(targetInstruction, InsnNode(Opcodes.LAND))
                instructions.insert(targetInstruction, longPushInstruction(operandB.toLong()))
                instructions.insert(targetInstruction, longPushInstruction(operandA.toLong()))
                instructions.remove(targetInstruction)
            }

            is Double -> {
                instructions.insert(targetInstruction, MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/Double", "longBitsToDouble", "(J)D"))
                instructions.insert(targetInstruction, LdcInsnNode(obfuscatedNumber.first))
                instructions.remove(targetInstruction)
            }

//            is Float -> {
//                instructions.insert(targetInstruction, MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/Float", "intBitsToFloat", "(J)D"))
//                instructions.insert(targetInstruction, LdcInsnNode(obfuscatedNumber.first))
//                instructions.remove(targetInstruction)
//            }
        }
    }

    override fun obfuscateNumber(originalValue: Number, keyNumber: Number): Pair<Number, Number> {
        return when (originalValue) {
            is Long, is Int, is Byte, is Short -> originalValue.toLong() to keyNumber
            is Double -> java.lang.Double.doubleToLongBits(originalValue) to keyNumber
            is Float -> java.lang.Float.floatToIntBits(originalValue) to keyNumber
            else -> throw IllegalStateException("obfuscateNumber cannot handle type ${originalValue::class.simpleName} (value: $originalValue)")
        }
    }
}
