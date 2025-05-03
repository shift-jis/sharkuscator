package dev.sharkuscator.obfuscator.transformers.obfuscators.constants.strategies.impl

import dev.sharkuscator.obfuscator.transformers.obfuscators.constants.strategies.NumericConstantObfuscationStrategy
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.AbstractInsnNode
import org.objectweb.asm.tree.InsnList
import org.objectweb.asm.tree.InsnNode
import org.objectweb.asm.tree.LdcInsnNode
import org.objectweb.asm.tree.MethodInsnNode
import org.objectweb.asm.tree.TypeInsnNode

class LoadBitsAndConvertStrategy : NumericConstantObfuscationStrategy {
    override fun replaceInstructions(instructions: InsnList, targetInstruction: AbstractInsnNode, originalValue: Number) {
        val obfuscatedNumber = obfuscateNumber(originalValue)
        when (originalValue) {
            is Double -> {
                instructions.insert(targetInstruction, MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/Double", "longBitsToDouble", "(J)D"))
                instructions.insert(targetInstruction, LdcInsnNode(obfuscatedNumber.first))
                instructions.remove(targetInstruction)
            }
            is Float -> {
                instructions.insert(targetInstruction, MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/Float", "intBitsToFloat", "(J)D"))
                instructions.insert(targetInstruction, LdcInsnNode(obfuscatedNumber.first))
                instructions.remove(targetInstruction)
            }
//            is Int -> {
//                instructions.insert(targetInstruction, MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/Long", "intValue", "()I"))
//                instructions.insert(targetInstruction, MethodInsnNode(Opcodes.INVOKESPECIAL, "java/lang/Long", "<init>", "(J)V"))
//                instructions.insert(targetInstruction, LdcInsnNode(obfuscatedNumber.first))
//                instructions.insert(targetInstruction, InsnNode(Opcodes.DUP))
//                instructions.insert(targetInstruction, TypeInsnNode(Opcodes.NEW, "java/lang/Long"))
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
