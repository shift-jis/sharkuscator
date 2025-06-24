package dev.sharkuscator.obfuscator.transformers.obfuscators.constants.strategies

import dev.sharkuscator.obfuscator.ObfuscationContext
import dev.sharkuscator.obfuscator.transformers.strategies.NumericConstantObfuscationStrategy
import dev.sharkuscator.obfuscator.utilities.AssemblyHelper.obfuscatedNumericPushInstructions
import org.mapleir.asm.ClassNode
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.AbstractInsnNode
import org.objectweb.asm.tree.InsnList
import org.objectweb.asm.tree.LdcInsnNode
import org.objectweb.asm.tree.MethodInsnNode
import kotlin.random.Random

class EncodedNumericConstantStrategy : NumericConstantObfuscationStrategy {
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
