package dev.sharkuscator.obfuscator.transformers.obfuscators.constants.generators

import dev.sharkuscator.annotations.LightObfuscation
import dev.sharkuscator.obfuscator.ObfuscationContext
import dev.sharkuscator.obfuscator.ObfuscatorServices
import dev.sharkuscator.obfuscator.extensions.addField
import dev.sharkuscator.obfuscator.utilities.AssemblyHelper.buildInstructionList
import dev.sharkuscator.obfuscator.utilities.AssemblyHelper.createFieldNode
import dev.sharkuscator.obfuscator.utilities.AssemblyHelper.createMethodNode
import dev.sharkuscator.obfuscator.utilities.AssemblyHelper.integerPushInstruction
import org.mapleir.asm.ClassNode
import org.mapleir.asm.FieldNode
import org.mapleir.asm.MethodNode
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import org.objectweb.asm.tree.*
import kotlin.random.Random

class ConstantArrayGenerator<T>(private val arrayElementType: Class<T>, private val maxFieldsPerClass: Int = 2) {
    companion object {
        private const val CONSTANT_ARRAY_FIELD_ACCESS = Opcodes.ACC_PRIVATE + Opcodes.ACC_STATIC + Opcodes.ACC_SYNTHETIC + Opcodes.ACC_TRANSIENT
        private const val CONSTANT_GETTER_METHOD_ACCESS = Opcodes.ACC_PRIVATE + Opcodes.ACC_STATIC + Opcodes.ACC_BRIDGE + Opcodes.ACC_SYNTHETIC
        private const val INSTRUCTION_CHAR_OFFSET = 0xAB00
    }

    class ArrayFieldMetadata<T>(sourceFieldNode: FieldNode, val fieldIndex: Int, val valueChunks: MutableMap<T, MutableList<Pair<AbstractInsnNode, T>>>) {
        val fieldDescriptor: String = sourceFieldNode.node.desc
        val fieldName: String = sourceFieldNode.name

        val totalChunkCount: Int
            get() = valueChunks.values.sumOf { it.size }

        fun computeIfAbsent(key: T): MutableList<Pair<AbstractInsnNode, T>> {
            return valueChunks.computeIfAbsent(key) { mutableListOf() }
        }
    }

    private val generatedArrayFieldsByClass = mutableMapOf<ClassNode, MutableList<ArrayFieldMetadata<T>>>()
    private val arrayGetterMethodNameByClass = mutableMapOf<ClassNode, String>()

    private val arrayJvmTypeDescriptor = when {
        arrayElementType.isAssignableFrom(String::class.java) -> "Ljava/lang/String;"
        arrayElementType.isAssignableFrom(Int::class.java) -> "I"
        arrayElementType.isAssignableFrom(Long::class.java) -> "J"
        arrayElementType.isAssignableFrom(Byte::class.java) -> "B"
        arrayElementType.isAssignableFrom(Short::class.java) -> "S"
        else -> throw IllegalStateException("Unsupported array element type")
    }
    private val getterMethodDescriptor = "(Ljava/lang/String;)$arrayJvmTypeDescriptor"

    fun createAndAddArrayField(obfuscationContext: ObfuscationContext, targetClassNode: ClassNode, requestedFieldCount: Int = maxFieldsPerClass) {
        if (generatedArrayFieldsByClass.containsKey(targetClassNode) && generatedArrayFieldsByClass.getValue(targetClassNode).size >= maxFieldsPerClass) {
            return
        }

        val arrayGetterMethodName = obfuscationContext.resolveDictionary<MethodNode, ClassNode>(MethodNode::class.java).generateNextName(targetClassNode)
        arrayGetterMethodNameByClass.computeIfAbsent(targetClassNode) { arrayGetterMethodName }

        val generatedFieldsForClass = generatedArrayFieldsByClass.computeIfAbsent(targetClassNode) { mutableListOf() }
        (1..requestedFieldCount.coerceIn(1, maxFieldsPerClass)).forEach { fieldIndex ->
            val arrayFieldName = obfuscationContext.resolveDictionary<FieldNode, ClassNode>(FieldNode::class.java).generateNextName(targetClassNode)
            FieldNode(createFieldNode(CONSTANT_ARRAY_FIELD_ACCESS, arrayFieldName, "[$arrayJvmTypeDescriptor"), targetClassNode).also {
                generatedFieldsForClass.add(ArrayFieldMetadata(it, fieldIndex - 1, mutableMapOf()))
                targetClassNode.addField(it.node)
            }
        }
    }

    fun createAndAddArrayGetterMethod(targetClassNode: ClassNode) {
        val arrayGetterMethodName = arrayGetterMethodNameByClass[targetClassNode] ?: return
        val arrayFieldMetadataList = generatedArrayFieldsByClass[targetClassNode] ?: return
        MethodNode(createMethodNode(CONSTANT_GETTER_METHOD_ACCESS, arrayGetterMethodName, getterMethodDescriptor), targetClassNode).also {
            val loopBeginLabelNode = LabelNode()
            val loopEndLabelNode = LabelNode()

            val blockStartLabel = LabelNode()
            val blockEndLabel = LabelNode()
            val catchHandlerLabel = LabelNode()
            val continueLoopLabel = LabelNode()

            val switchDefaultLabel = LabelNode()

            it.node.tryCatchBlocks.add(TryCatchBlockNode(blockStartLabel, blockEndLabel, catchHandlerLabel, "java/lang/IndexOutOfBoundsException"))
            it.node.visibleAnnotations = listOf(AnnotationNode(Type.getDescriptor(LightObfuscation::class.java)))
            it.node.instructions = buildInstructionList {
                add(TypeInsnNode(Opcodes.NEW, "java/lang/StringBuilder"))
                add(InsnNode(Opcodes.DUP))
                add(MethodInsnNode(Opcodes.INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "()V"))
                add(VarInsnNode(Opcodes.ASTORE, 1))

                add(integerPushInstruction(0))
                add(VarInsnNode(Opcodes.ISTORE, 2))

                add(loopBeginLabelNode)
                add(VarInsnNode(Opcodes.ILOAD, 2))
                add(VarInsnNode(Opcodes.ALOAD, 0))
                add(MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/String", "length", "()I"))
                add(JumpInsnNode(Opcodes.IF_ICMPGE, loopEndLabelNode))

                add(blockStartLabel)
                add(VarInsnNode(Opcodes.ALOAD, 0))
                add(VarInsnNode(Opcodes.ILOAD, 2))
                add(MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/String", "charAt", "(I)C"))
                add(integerPushInstruction(INSTRUCTION_CHAR_OFFSET))
                add(InsnNode(Opcodes.ISUB))
                add(VarInsnNode(Opcodes.ISTORE, 3))

                add(VarInsnNode(Opcodes.ALOAD, 0))
                add(VarInsnNode(Opcodes.ILOAD, 2))
                add(integerPushInstruction(1))
                add(InsnNode(Opcodes.IADD))
                add(MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/String", "charAt", "(I)C"))
                add(integerPushInstruction(INSTRUCTION_CHAR_OFFSET))
                add(InsnNode(Opcodes.ISUB))
                add(VarInsnNode(Opcodes.ISTORE, 4))

                val switchCaseLabels = arrayFieldMetadataList.map { LabelNode() }.toTypedArray()
                add(VarInsnNode(Opcodes.ILOAD, 3))
                add(TableSwitchInsnNode(0, arrayFieldMetadataList.size - 1, switchDefaultLabel, *switchCaseLabels))

                arrayFieldMetadataList.forEachIndexed { index, arrayFieldMetadata ->
                    add(switchCaseLabels[index])
                    add(VarInsnNode(Opcodes.ALOAD, 1))
                    add(FieldInsnNode(Opcodes.GETSTATIC, targetClassNode.name, arrayFieldMetadata.fieldName, arrayFieldMetadata.fieldDescriptor))
                    add(VarInsnNode(Opcodes.ILOAD, 4))
                    add(InsnNode(Opcodes.AALOAD))
                    add(MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;"))
                    add(InsnNode(Opcodes.POP))
                    add(JumpInsnNode(Opcodes.GOTO, switchDefaultLabel))
                }

                add(switchDefaultLabel)
                add(blockEndLabel)
                add(JumpInsnNode(Opcodes.GOTO, continueLoopLabel))

                add(catchHandlerLabel)
                add(VarInsnNode(Opcodes.ASTORE, 5))

                add(continueLoopLabel)
                add(IincInsnNode(2, 2))
                add(JumpInsnNode(Opcodes.GOTO, loopBeginLabelNode))

                add(loopEndLabelNode)
                add(VarInsnNode(Opcodes.ALOAD, 1))
                add(MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;"))
                add(InsnNode(Opcodes.ARETURN))
            }
            targetClassNode.addMethod(it)
        }
    }

    fun addValueToRandomArray(targetClassNode: ClassNode, instruction: AbstractInsnNode, element: T, chunkCount: Int = 1, transformer: T.() -> T): String {
        val instructionString = StringBuilder()
        when (element) {
            is String -> {
                if (chunkCount <= 1 || element.length < chunkCount) {
                    val randomFieldMetadata = generatedArrayFieldsByClass[targetClassNode]?.random() ?: return ""
                    val arrayElementIndex = randomFieldMetadata.totalChunkCount
                    randomFieldMetadata.computeIfAbsent(element).add(instruction to element.transformer())
                    instructionString.append((randomFieldMetadata.fieldIndex + INSTRUCTION_CHAR_OFFSET).toChar())
                    instructionString.append((arrayElementIndex + INSTRUCTION_CHAR_OFFSET).toChar())
                    return instructionString.toString()
                }

                var remainder = element.length % chunkCount
                var currentPosition = 0

                repeat(chunkCount) {
                    val currentChunkSize = (element.length / chunkCount) + if (remainder > 0) 1 else 0
                    val randomFieldMetadata = generatedArrayFieldsByClass[targetClassNode]?.random() ?: return ""
                    val arrayElementIndex = randomFieldMetadata.totalChunkCount
                    val chunkValue = element.substring(currentPosition, currentPosition + currentChunkSize) as T
                    randomFieldMetadata.computeIfAbsent(element).add(instruction to chunkValue.transformer())
                    instructionString.append((randomFieldMetadata.fieldIndex + INSTRUCTION_CHAR_OFFSET).toChar())
                    instructionString.append((arrayElementIndex + INSTRUCTION_CHAR_OFFSET).toChar())
                    currentPosition += currentChunkSize
                    if (remainder > 0) {
                        remainder--
                    }
                }
            }

            is Int, is Byte, is Short -> {
                if (chunkCount <= 1 || (element.toInt() < chunkCount && chunkCount > element.toInt())) {
                    val randomFieldMetadata = generatedArrayFieldsByClass[targetClassNode]?.random() ?: return ""
                    val arrayElementIndex = randomFieldMetadata.totalChunkCount
                    randomFieldMetadata.computeIfAbsent(element).add(instruction to element.transformer())
                    instructionString.append((randomFieldMetadata.fieldIndex + INSTRUCTION_CHAR_OFFSET).toChar())
                    instructionString.append((arrayElementIndex + INSTRUCTION_CHAR_OFFSET).toChar())
                    return instructionString.toString()
                }

                var remainder = element.toInt() % chunkCount
                var currentPosition = 0

                repeat(chunkCount) {
                    val currentChunkSize = (element.toInt() / chunkCount) + if (remainder > 0) 1 else 0
                    ObfuscatorServices.sharkLogger.info("${element.toInt()} $currentChunkSize")
                    currentPosition += currentChunkSize
                    if (remainder > 0) {
                        remainder--
                    }
                }
            }

            else -> {
                val randomFieldMetadata = generatedArrayFieldsByClass[targetClassNode]?.random() ?: return ""
                randomFieldMetadata.computeIfAbsent(element).add(instruction to element)
            }
        }

        return instructionString.toString()
    }

    fun createGetterInvocation(targetClassNode: ClassNode, instructionString: String): InsnList {
        val arrayGetterMethodName = arrayGetterMethodNameByClass[targetClassNode] ?: return InsnList()
        return buildInstructionList {
            add(LdcInsnNode(instructionString))
            add(MethodInsnNode(Opcodes.INVOKESTATIC, targetClassNode.name, arrayGetterMethodName, getterMethodDescriptor))
        }
    }

    fun createInitializationInstructions(targetClassNode: ClassNode, builder: InsnList.(arrayFieldMetadataList: MutableList<ArrayFieldMetadata<T>>) -> Unit): InsnList {
        return buildInstructionList {
            val arrayFieldMetadataList = generatedArrayFieldsByClass[targetClassNode] ?: return@buildInstructionList
            arrayFieldMetadataList.forEach { arrayFieldMetadata ->
                add(integerPushInstruction(Random.nextInt(1000, 9999)))
                if (arrayJvmTypeDescriptor.startsWith("L")) {
                    add(TypeInsnNode(Opcodes.ANEWARRAY, arrayJvmTypeDescriptor.substring(1, arrayJvmTypeDescriptor.length - 1)))
                } else {
                    val typeOperand = when {
                        arrayElementType.isAssignableFrom(Int::class.java) -> Opcodes.T_INT
                        arrayElementType.isAssignableFrom(Long::class.java) -> Opcodes.T_LONG
                        arrayElementType.isAssignableFrom(Byte::class.java)-> Opcodes.T_BYTE
                        arrayElementType.isAssignableFrom(Short::class.java) -> Opcodes.T_SHORT
                        else -> throw IllegalStateException("Unsupported array element type")
                    }
                    add(IntInsnNode(Opcodes.NEWARRAY, typeOperand))
                }
                add(FieldInsnNode(Opcodes.PUTSTATIC, targetClassNode.name, arrayFieldMetadata.fieldName, arrayFieldMetadata.fieldDescriptor))
            }
            this.builder(arrayFieldMetadataList)
        }
    }
}
