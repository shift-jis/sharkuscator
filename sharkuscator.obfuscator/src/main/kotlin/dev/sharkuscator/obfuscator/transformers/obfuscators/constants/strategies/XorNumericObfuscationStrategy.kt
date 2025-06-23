package dev.sharkuscator.obfuscator.transformers.obfuscators.constants.strategies

import dev.sharkuscator.obfuscator.ObfuscationContext
import dev.sharkuscator.obfuscator.extensions.addField
import dev.sharkuscator.obfuscator.extensions.getOrCreateStaticInitializer
import dev.sharkuscator.obfuscator.extensions.isSpongeMixin
import dev.sharkuscator.obfuscator.transformers.strategies.NumericConstantObfuscationStrategy
import dev.sharkuscator.obfuscator.utilities.AssemblyHelper.buildInstructionList
import dev.sharkuscator.obfuscator.utilities.AssemblyHelper.createFieldNode
import dev.sharkuscator.obfuscator.utilities.AssemblyHelper.integerPushInstruction
import org.mapleir.asm.ClassNode
import org.mapleir.asm.FieldNode
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.*
import kotlin.random.Random

// TODO
class XorNumericObfuscationStrategy : NumericConstantObfuscationStrategy {
    private data class ObfuscationKeyData(val fieldName: String, val keyNumber: Number)

    private val classToObfuscationKey = mutableMapOf<ClassNode, ObfuscationKeyData>()

    override fun replaceInstructions(obfuscationContext: ObfuscationContext, targetClassNode: ClassNode, instructions: InsnList, targetInstruction: AbstractInsnNode, originalValue: Number) {
        if (targetInstruction.next.opcode == Opcodes.PUTFIELD || targetInstruction.next.opcode == Opcodes.PUTSTATIC) {
            return
        }

        val obfuscationKeyData = getOrCreateObfuscationKey(obfuscationContext, targetClassNode, Random.nextInt())
        val obfuscatedNumber = obfuscateNumber(originalValue, obfuscationKeyData.keyNumber)

        when (originalValue) {
            is Int, is Byte, is Short -> {
                instructions.insert(targetInstruction, buildInstructionList {
                    add(FieldInsnNode(Opcodes.GETSTATIC, targetClassNode.name, obfuscationKeyData.fieldName, "I"))
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

    private fun getOrCreateObfuscationKey(obfuscationContext: ObfuscationContext, targetClassNode: ClassNode, keyNumber: Number): ObfuscationKeyData {
        if (classToObfuscationKey.containsKey(targetClassNode)) {
            return classToObfuscationKey.getValue(targetClassNode)
        } else {
            val fieldNameGenerator = obfuscationContext.resolveDictionary<FieldNode, ClassNode>(FieldNode::class.java)
            val obfuscationKeyData = ObfuscationKeyData(fieldNameGenerator.generateNextName(targetClassNode), keyNumber)
            createAndInitializeKeyField(targetClassNode, obfuscationKeyData, keyNumber)
            classToObfuscationKey[targetClassNode] = obfuscationKeyData
            return obfuscationKeyData
        }
    }

    private fun createAndInitializeKeyField(targetClassNode: ClassNode, obfuscationKeyData: ObfuscationKeyData, keyNumber: Number) {
        targetClassNode.addField(createFieldNode(Opcodes.ACC_PRIVATE + Opcodes.ACC_STATIC + Opcodes.ACC_TRANSIENT, obfuscationKeyData.fieldName, "I").apply {
            if (targetClassNode.isSpongeMixin()) {
                visibleAnnotations = listOf(AnnotationNode("Lorg/spongepowered/asm/mixin/Unique;"))
            }
        })

        val staticInitializer = targetClassNode.getOrCreateStaticInitializer()
        staticInitializer.node.instructions.insert(buildInstructionList {
            add(integerPushInstruction(keyNumber))
            add(FieldInsnNode(Opcodes.PUTSTATIC, targetClassNode.name, obfuscationKeyData.fieldName, "I"))
        })
    }
}
