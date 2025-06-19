package dev.sharkuscator.obfuscator.transformers.obfuscators.constants.strategies

import dev.sharkuscator.obfuscator.dictionaries.DictionaryFactory
import dev.sharkuscator.obfuscator.extensions.addField
import dev.sharkuscator.obfuscator.extensions.getOrCreateStaticInitializer
import dev.sharkuscator.obfuscator.transformers.strategies.NumericConstantObfuscationStrategy
import dev.sharkuscator.obfuscator.utilities.BytecodeUtils.buildInstructionList
import dev.sharkuscator.obfuscator.utilities.BytecodeUtils.createFieldNode
import dev.sharkuscator.obfuscator.utilities.BytecodeUtils.integerPushInstruction
import org.mapleir.asm.ClassNode
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.AbstractInsnNode
import org.objectweb.asm.tree.FieldInsnNode
import org.objectweb.asm.tree.InsnList
import org.objectweb.asm.tree.InsnNode
import kotlin.random.Random

// TODO
class XorNumericObfuscationStrategy : NumericConstantObfuscationStrategy {
    private data class ObfuscationKeyData(val fieldName: String, val keyNumber: Number)

    private val classToObfuscationKey = mutableMapOf<ClassNode, ObfuscationKeyData>()
    private val dictionary = DictionaryFactory.createDictionary<ClassNode>("alphabetical")

    override fun replaceInstructions(classNode: ClassNode, instructions: InsnList, targetInstruction: AbstractInsnNode, originalValue: Number) {
        if (targetInstruction.next.opcode == Opcodes.PUTFIELD || targetInstruction.next.opcode == Opcodes.PUTSTATIC) {
            return
        }

        val obfuscationKeyData = getOrCreateObfuscationKey(classNode, Random.nextInt())
        val obfuscatedNumber = obfuscateNumber(originalValue, obfuscationKeyData.keyNumber)

        when (originalValue) {
            is Int, is Byte, is Short -> {
                instructions.insert(
                    targetInstruction, buildInstructionList(
                        FieldInsnNode(Opcodes.GETSTATIC, classNode.name, obfuscationKeyData.fieldName, "I"),
                        integerPushInstruction(obfuscatedNumber.first),
                        InsnNode(Opcodes.IXOR),
                    )
                )
                instructions.remove(targetInstruction)
            }
        }
    }

    override fun obfuscateNumber(originalValue: Number, keyNumber: Number): Pair<Number, Number> {
        return when (originalValue) {
            is Int, is Byte, is Short -> originalValue.toInt() xor keyNumber.toInt() to keyNumber
            else -> originalValue to keyNumber
        }
    }

    private fun getOrCreateObfuscationKey(classNode: ClassNode, keyNumber: Number): ObfuscationKeyData {
        if (classToObfuscationKey.containsKey(classNode)) {
            return classToObfuscationKey.getValue(classNode)
        } else {
            val obfuscationKeyData = ObfuscationKeyData(dictionary.generateNextName(classNode), keyNumber)
            createAndInitializeKeyField(classNode, obfuscationKeyData, keyNumber)
            classToObfuscationKey[classNode] = obfuscationKeyData
            return obfuscationKeyData
        }
    }

    private fun createAndInitializeKeyField(classNode: ClassNode, obfuscationKeyData: ObfuscationKeyData, keyNumber: Number) {
        classNode.addField(createFieldNode(Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC + Opcodes.ACC_TRANSIENT, obfuscationKeyData.fieldName, "I"))

        val staticInitializer = classNode.getOrCreateStaticInitializer()
        staticInitializer.node.instructions.insert(
            buildInstructionList(
                integerPushInstruction(keyNumber),
                FieldInsnNode(Opcodes.PUTSTATIC, classNode.name, obfuscationKeyData.fieldName, "I"),
            )
        )
    }
}
