package dev.sharkuscator.obfuscator.transformers.obfuscators.constants.strategies

import dev.sharkuscator.obfuscator.ObfuscationContext
import dev.sharkuscator.obfuscator.extensions.addField
import dev.sharkuscator.obfuscator.extensions.getOrCreateStaticInitializer
import dev.sharkuscator.obfuscator.transformers.strategies.NumericConstantObfuscationStrategy
import dev.sharkuscator.obfuscator.utilities.AssemblyHelper.buildInstructionList
import dev.sharkuscator.obfuscator.utilities.AssemblyHelper.createFieldNode
import dev.sharkuscator.obfuscator.utilities.AssemblyHelper.integerPushInstruction
import org.mapleir.asm.ClassNode
import org.mapleir.asm.FieldNode
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

    override fun replaceInstructions(obfuscationContext: ObfuscationContext, classNode: ClassNode, instructions: InsnList, targetInstruction: AbstractInsnNode, originalValue: Number) {
        if (targetInstruction.next.opcode == Opcodes.PUTFIELD || targetInstruction.next.opcode == Opcodes.PUTSTATIC) {
            return
        }

        val obfuscationKeyData = getOrCreateObfuscationKey(obfuscationContext, classNode, Random.nextInt())
        val obfuscatedNumber = obfuscateNumber(originalValue, obfuscationKeyData.keyNumber)

        when (originalValue) {
            is Int, is Byte, is Short -> {
                instructions.insert(targetInstruction, buildInstructionList {
                    add(FieldInsnNode(Opcodes.GETSTATIC, classNode.name, obfuscationKeyData.fieldName, "I"))
                    add(integerPushInstruction(obfuscatedNumber.first))
                    add(InsnNode(Opcodes.IXOR))
                })
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

    private fun getOrCreateObfuscationKey(context: ObfuscationContext, classNode: ClassNode, keyNumber: Number): ObfuscationKeyData {
        if (classToObfuscationKey.containsKey(classNode)) {
            return classToObfuscationKey.getValue(classNode)
        } else {
            val fieldNameGenerator = context.resolveDictionary<FieldNode, ClassNode>(FieldNode::class.java)
            val obfuscationKeyData = ObfuscationKeyData(fieldNameGenerator.generateNextName(classNode), keyNumber)
            createAndInitializeKeyField(classNode, obfuscationKeyData, keyNumber)
            classToObfuscationKey[classNode] = obfuscationKeyData
            return obfuscationKeyData
        }
    }

    private fun createAndInitializeKeyField(classNode: ClassNode, obfuscationKeyData: ObfuscationKeyData, keyNumber: Number) {
        classNode.addField(createFieldNode(Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC + Opcodes.ACC_TRANSIENT, obfuscationKeyData.fieldName, "I"))

        val staticInitializer = classNode.getOrCreateStaticInitializer()
        staticInitializer.node.instructions.insert(buildInstructionList {
            add(integerPushInstruction(keyNumber))
            add(FieldInsnNode(Opcodes.PUTSTATIC, classNode.name, obfuscationKeyData.fieldName, "I"))
        })
    }
}
