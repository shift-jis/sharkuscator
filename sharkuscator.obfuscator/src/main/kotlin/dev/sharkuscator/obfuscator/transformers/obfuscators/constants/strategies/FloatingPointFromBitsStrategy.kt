package dev.sharkuscator.obfuscator.transformers.obfuscators.constants.strategies

import dev.sharkuscator.obfuscator.transformers.strategies.NumericConstantObfuscationStrategy
import dev.sharkuscator.obfuscator.utilities.BytecodeUtils
import org.mapleir.asm.ClassNode
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.AbstractInsnNode
import org.objectweb.asm.tree.InsnList
import org.objectweb.asm.tree.LdcInsnNode
import org.objectweb.asm.tree.MethodInsnNode

class FloatingPointFromBitsStrategy : NumericConstantObfuscationStrategy {
    override fun replaceInstructions(classNode: ClassNode, instructions: InsnList, targetInstruction: AbstractInsnNode, originalValue: Number) {
        val obfuscatedNumber = obfuscateNumber(originalValue)
        when (originalValue) {
            is Int, is Byte -> {
                instructions.insert(targetInstruction, BytecodeUtils.complexIntegerPushInstruction(obfuscatedNumber.first))
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
            is Long, is Int, is Byte, is Short -> Pair(originalValue.toLong(), keyNumber)
            is Double -> Pair(java.lang.Double.doubleToLongBits(originalValue), keyNumber)
            is Float -> Pair(java.lang.Float.floatToIntBits(originalValue), keyNumber)
            else -> throw IllegalStateException("obfuscateNumber cannot handle type ${originalValue::class.simpleName} (value: $originalValue)")
        }
    }
}
