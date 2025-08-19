package dev.sharkuscator.commons

import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.*
import kotlin.random.Random

object AssemblyHelper {
    private const val MAX_OBFUSCATION_RECURSION_DEPTH = 1

    private val constNumberOpcodesMap = mapOf<Int, Number>(
        Opcodes.ICONST_M1 to -1,
        Opcodes.ICONST_0 to 0,
        Opcodes.ICONST_1 to 1,
        Opcodes.ICONST_2 to 2,
        Opcodes.ICONST_3 to 3,
        Opcodes.ICONST_4 to 4,
        Opcodes.ICONST_5 to 5,

        Opcodes.LCONST_0 to 0L,
        Opcodes.LCONST_1 to 1L,

        Opcodes.FCONST_0 to 0.0F,
        Opcodes.FCONST_1 to 1.0F,
        Opcodes.FCONST_2 to 2.0F,

        Opcodes.DCONST_0 to 0.0,
        Opcodes.DCONST_1 to 1.0,

        Opcodes.BIPUSH to -1,
        Opcodes.SIPUSH to -1,
    )

    fun invertJumpCondition(opcode: Int): Int {
        return when (opcode) {
            Opcodes.IFNE -> Opcodes.IFEQ
            Opcodes.IFEQ -> Opcodes.IFNE
            Opcodes.IFGE -> Opcodes.IFLT
            Opcodes.IFGT -> Opcodes.IFLE
            Opcodes.IFLE -> Opcodes.IFGT
            Opcodes.IFLT -> Opcodes.IFGE
            Opcodes.IFNONNULL -> Opcodes.IFNULL
            Opcodes.IFNULL -> Opcodes.IFNONNULL
            Opcodes.IF_ACMPEQ -> Opcodes.IF_ACMPNE
            Opcodes.IF_ACMPNE -> Opcodes.IF_ACMPEQ
            Opcodes.IF_ICMPEQ -> Opcodes.IF_ICMPNE
            Opcodes.IF_ICMPNE -> Opcodes.IF_ICMPEQ
            Opcodes.IF_ICMPGE -> Opcodes.IF_ICMPLT
            Opcodes.IF_ICMPGT -> Opcodes.IF_ICMPLE
            Opcodes.IF_ICMPLE -> Opcodes.IF_ICMPGT
            Opcodes.IF_ICMPLT -> Opcodes.IF_ICMPGE
            else -> throw IllegalStateException(String.format("Unable to reverse jump opcode: %d", opcode))
        }
    }

    fun integerPushInstruction(value: Number): AbstractInsnNode {
        return when (value) {
            -1 -> InsnNode(Opcodes.ICONST_M1)
            0 -> InsnNode(Opcodes.ICONST_0)
            1 -> InsnNode(Opcodes.ICONST_1)
            2 -> InsnNode(Opcodes.ICONST_2)
            3 -> InsnNode(Opcodes.ICONST_3)
            4 -> InsnNode(Opcodes.ICONST_4)
            5 -> InsnNode(Opcodes.ICONST_5)
            in Short.MIN_VALUE..Short.MAX_VALUE -> IntInsnNode(Opcodes.SIPUSH, value.toInt())
            in Byte.MIN_VALUE..Byte.MAX_VALUE -> IntInsnNode(Opcodes.BIPUSH, value.toInt())
            else -> LdcInsnNode(value)
        }
    }

    fun longPushInstruction(value: Long): AbstractInsnNode {
        return when (value) {
            0L -> InsnNode(Opcodes.LCONST_0)
            1L -> InsnNode(Opcodes.LCONST_1)
            else -> LdcInsnNode(value)
        }
    }

    fun doublePushInstruction(value: Double): AbstractInsnNode {
        return when (value) {
            0.0 -> InsnNode(Opcodes.DCONST_0)
            1.0 -> InsnNode(Opcodes.DCONST_1)
            else -> LdcInsnNode(value)
        }
    }

    fun floatPushInstruction(value: Float): AbstractInsnNode {
        return when (value) {
            0f -> InsnNode(Opcodes.FCONST_0)
            1f -> InsnNode(Opcodes.FCONST_1)
            2f -> InsnNode(Opcodes.FCONST_2)
            else -> LdcInsnNode(value)
        }
    }

    fun xorPushInstruction(value: Number, operand: Long = Random.Default.nextLong()): InsnList {
        return when (value) {
            is Int, is Byte, is Short -> buildInstructionList {
                add(integerPushInstruction(value.toInt() xor operand.toInt()))
                add(integerPushInstruction(operand.toInt()))
                add(InsnNode(Opcodes.IXOR))
            }

            is Long -> buildInstructionList {
                add(longPushInstruction(value xor operand))
                add(longPushInstruction(operand))
                add(InsnNode(Opcodes.LXOR))
            }

            else -> buildInstructionList {
                add(integerPushInstruction(value))
            }
        }
    }

    fun obfuscatedNumericPushInstructions(value: Number, recursionDepth: Int = 0): InsnList {
        val instructions = InsnList()

        val isIntegerFamily = value is Int || value is Byte || value is Short
        val isLongFamily = value is Long

        if ((isIntegerFamily || isLongFamily) && Random.Default.nextBoolean()) {
            val allowFurtherRecursion = recursionDepth < MAX_OBFUSCATION_RECURSION_DEPTH

            if (Random.Default.nextBoolean()) {
                if (isIntegerFamily) {
                    val intValue = value.toInt()
                    val (operandA, operandB) = OperandFactory.generateOperandsForAnd(intValue)

                    if (allowFurtherRecursion && Random.Default.nextBoolean()) {
                        instructions.add(obfuscatedNumericPushInstructions(operandA.toInt(), recursionDepth + 1))
                    } else {
                        instructions.add(integerPushInstruction(operandA.toInt()))
                    }

                    if (allowFurtherRecursion && Random.Default.nextBoolean()) {
                        instructions.add(obfuscatedNumericPushInstructions(operandB.toInt(), recursionDepth + 1))
                    } else {
                        instructions.add(integerPushInstruction(operandB.toInt()))
                    }

                    instructions.add(InsnNode(Opcodes.IAND))
                } else {
                    val longValue = value.toLong()
                    val (operandA, operandB) = OperandFactory.generateOperandsForAnd(longValue)

                    if (allowFurtherRecursion && Random.Default.nextBoolean()) {
                        instructions.add(obfuscatedNumericPushInstructions(operandA.toLong(), recursionDepth + 1))
                    } else {
                        instructions.add(longPushInstruction(operandA.toLong()))
                    }

                    if (allowFurtherRecursion && Random.Default.nextBoolean()) {
                        instructions.add(obfuscatedNumericPushInstructions(operandB.toLong(), recursionDepth + 1))
                    } else {
                        instructions.add(longPushInstruction(operandB.toLong()))
                    }

                    instructions.add(InsnNode(Opcodes.LAND))
                }
            } else {
                if (isIntegerFamily) {
                    val intValue = value.toInt()
                    val randomXorOperand = Random.Default.nextInt()
                    instructions.add(integerPushInstruction(intValue xor randomXorOperand))
                    instructions.add(integerPushInstruction(randomXorOperand))
                    instructions.add(InsnNode(Opcodes.IXOR))
                } else {
                    val longValue = value.toLong()
                    val randomXorOperand = Random.Default.nextLong()
                    instructions.add(longPushInstruction(longValue xor randomXorOperand))
                    instructions.add(longPushInstruction(randomXorOperand))
                    instructions.add(InsnNode(Opcodes.LXOR))
                }
            }
        } else {
            instructions.add(
                when (value) {
                    is Int, is Byte, is Short -> integerPushInstruction(value)
                    is Long -> longPushInstruction(value)
                    else -> LdcInsnNode(value)
                }
            )
        }
        return instructions
    }

    fun isNumericConstantInstruction(instruction: AbstractInsnNode): Boolean {
        return constNumberOpcodesMap.contains(instruction.opcode) || (instruction is LdcInsnNode && instruction.cst is Number)
    }

    fun extractNumericValue(instruction: AbstractInsnNode): Number {
        return when {
            instruction is LdcInsnNode && instruction.cst is Number -> instruction.cst as Number
            instruction is IntInsnNode && (instruction.opcode == Opcodes.BIPUSH || instruction.opcode == Opcodes.SIPUSH) -> instruction.operand
            constNumberOpcodesMap.contains(instruction.opcode) -> constNumberOpcodesMap[instruction.opcode]!!
            else -> throw IllegalStateException("Instruction $instruction (opcode ${instruction.opcode}) is not a recognized numeric constant instruction.")
        }
    }

    fun containsNonEmptyStrings(instructions: InsnList): Boolean {
        return findNonEmptyStrings(instructions).isNotEmpty()
    }

    fun findNonEmptyStrings(instructions: InsnList): List<Pair<LdcInsnNode, String>> {
        return instructions.filterIsInstance<LdcInsnNode>().filter { it.cst is String && (it.cst as String).isNotEmpty() }.map { it to it.cst as String }
    }

    fun findNumericConstants(instructions: InsnList): List<Pair<AbstractInsnNode, Number>> {
        return instructions.filter { isNumericConstantInstruction(it) }.map { it to extractNumericValue(it) }
    }

    fun buildInstructionList(builder: InsnList.() -> Unit): InsnList {
        return InsnList().apply(builder)
    }

    fun createClassNode(nodeName: String, superName: String = "java/lang/Object"): ClassNode {
        return ClassNode().apply {
            this.superName = superName
            this.version = Opcodes.V1_8
            this.access = Opcodes.ACC_PUBLIC
            this.name = nodeName
        }
    }

    fun createFieldNode(access: Int, nodeName: String, descriptor: String, value: Any? = null): FieldNode {
        return FieldNode(access, nodeName, descriptor, null, value)
    }

    fun createMethodNode(access: Int, nodeName: String, descriptor: String): MethodNode {
        return MethodNode(access, nodeName, descriptor, null, null)
    }
}
