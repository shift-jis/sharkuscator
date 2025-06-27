package dev.sharkuscator.obfuscator.transformers.obfuscators.constants.strategies

import dev.sharkuscator.obfuscator.ObfuscationContext
import dev.sharkuscator.obfuscator.extensions.addField
import dev.sharkuscator.obfuscator.extensions.isSpongeMixin
import dev.sharkuscator.obfuscator.extensions.resolveStaticInitializer
import dev.sharkuscator.obfuscator.transformers.obfuscators.constants.generators.MaskingNumberGenerator
import dev.sharkuscator.obfuscator.transformers.strategies.NumericConstantObfuscationStrategy
import dev.sharkuscator.obfuscator.utilities.AssemblyHelper.buildInstructionList
import dev.sharkuscator.obfuscator.utilities.AssemblyHelper.createFieldNode
import dev.sharkuscator.obfuscator.utilities.AssemblyHelper.integerPushInstruction
import dev.sharkuscator.obfuscator.utilities.AssemblyHelper.longPushInstruction
import org.mapleir.asm.ClassNode
import org.mapleir.asm.FieldNode
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.*
import kotlin.random.Random

class MaskingNumberObfuscationStrategy : NumericConstantObfuscationStrategy {
    private data class XorKeyMetadata(val keyFieldName: String, val keyOperand: Number, var isInitialized: Boolean)

    private val keyMetadataByClass = mutableMapOf<ClassNode, MutableMap<Class<out Number>, XorKeyMetadata>>()
    private val maskingNumberGenerator = MaskingNumberGenerator()

    override fun initialization(obfuscationContext: ObfuscationContext, targetClassNode: ClassNode) {
        maskingNumberGenerator.generateMaskingClasses(obfuscationContext)
    }

    override fun replaceInstructions(targetClassNode: ClassNode, instructions: InsnList, targetInstruction: AbstractInsnNode, originalValue: Number) {
        if (targetInstruction.next.opcode == Opcodes.PUTFIELD || targetInstruction.next.opcode == Opcodes.PUTSTATIC) {
            return
        }

        val keyFieldNameGenerator = ObfuscationContext.resolveDictionary<FieldNode, ClassNode>(FieldNode::class.java)
        val xorKeyMetadata = keyMetadataByClass.computeIfAbsent(targetClassNode) { mutableMapOf() }.computeIfAbsent(Long::class.java) {
            XorKeyMetadata(keyFieldNameGenerator.generateNextName(targetClassNode), Random.nextLong(), false)
        }
        val obfuscatedNumber = obfuscateNumber(originalValue, xorKeyMetadata.keyOperand)

        when (originalValue) {
            is Int, is Byte, is Short -> {
                createAndInitializeKeyField(targetClassNode, xorKeyMetadata, "J")
                instructions.insert(targetInstruction, buildInstructionList {
                    add(FieldInsnNode(Opcodes.GETSTATIC, targetClassNode.name, xorKeyMetadata.keyFieldName, "J"))
                    add(InsnNode(Opcodes.L2I))
                    add(integerPushInstruction(obfuscatedNumber.first.toInt()))
                    add(InsnNode(Opcodes.IXOR))
                })
                instructions.remove(targetInstruction)
            }

            is Long -> {
                createAndInitializeKeyField(targetClassNode, xorKeyMetadata, "J")
                instructions.insert(targetInstruction, buildInstructionList {
                    add(FieldInsnNode(Opcodes.GETSTATIC, targetClassNode.name, xorKeyMetadata.keyFieldName, "J"))
                    add(longPushInstruction(obfuscatedNumber.first.toLong()))
                    add(InsnNode(Opcodes.LXOR))
                })
                instructions.remove(targetInstruction)
            }
        }
    }

    override fun obfuscateNumber(originalValue: Number, keyNumber: Number): Pair<Number, Number> {
        return when (originalValue) {
            is Int, is Byte, is Short -> (originalValue.toInt() xor keyNumber.toInt()) to keyNumber
            is Long -> (originalValue xor keyNumber.toLong()) to keyNumber
            else -> originalValue to keyNumber // No change for unsupported types
        }
    }

    private fun createAndInitializeKeyField(targetClassNode: ClassNode, xorKeyMetadata: XorKeyMetadata, jvmTypeDescriptor: String) {
        if (!keyMetadataByClass.containsKey(targetClassNode) || xorKeyMetadata.isInitialized) {
            return
        }

        targetClassNode.addField(createFieldNode(Opcodes.ACC_PRIVATE + Opcodes.ACC_STATIC + Opcodes.ACC_TRANSIENT, xorKeyMetadata.keyFieldName, jvmTypeDescriptor).apply {
            if (targetClassNode.isSpongeMixin()) {
                visibleAnnotations = listOf(AnnotationNode("Lorg/spongepowered/asm/mixin/Unique;"))
            }
        })

        val keyNumberPushInstruction = when (xorKeyMetadata.keyOperand) {
            is Int, is Byte, is Short -> integerPushInstruction(xorKeyMetadata.keyOperand)
            is Long -> longPushInstruction(xorKeyMetadata.keyOperand)
            else -> throw IllegalStateException("Unsupported key type for field initialization")
        }

        val staticInitializer = targetClassNode.resolveStaticInitializer()
        staticInitializer.node.instructions.insert(buildInstructionList {
            add(keyNumberPushInstruction)
            add(FieldInsnNode(Opcodes.PUTSTATIC, targetClassNode.name, xorKeyMetadata.keyFieldName, jvmTypeDescriptor))
        })
        xorKeyMetadata.isInitialized = true
    }
}
