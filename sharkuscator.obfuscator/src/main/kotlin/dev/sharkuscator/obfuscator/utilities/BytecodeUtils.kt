package dev.sharkuscator.obfuscator.utilities

import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.*
import kotlin.random.Random

private data class IntegerDeobfuscationStep(val key: Int, val reverseOpcode: Int)

private enum class IntegerObfuscationOpType {
    XOR,
    ADD,
    SUB,
}

object BytecodeUtils {
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

    fun xorPushInstruction(value: Number, operand: Long = Random.nextLong()): InsnList {
        return when (value) {
            is Int, is Byte, is Short -> buildInstructionList(
                integerPushInstruction(value.toInt() xor operand.toInt()),
                integerPushInstruction(operand.toInt()),
                InsnNode(Opcodes.IXOR)
            )

            is Long -> buildInstructionList(
                longPushInstruction(value xor operand),
                longPushInstruction(operand),
                InsnNode(Opcodes.LXOR)
            )

            else -> buildInstructionList(integerPushInstruction(value))
        }
    }

    fun shiftLeftPushInstruction(value: Number, shiftAmount: Long = Random.nextLong()): InsnList {
        return when (value) {
            is Int -> {
                val actualShiftAmount = shiftAmount % 32
                val transformedValue = value ushr actualShiftAmount.toInt()
                buildInstructionList(
                    integerPushInstruction(transformedValue),
                    integerPushInstruction(actualShiftAmount),
                    InsnNode(Opcodes.ISHL)
                )
            }

            is Long -> {
                val actualShiftAmount = shiftAmount % 64
                val transformedValue = value ushr actualShiftAmount.toInt()
                buildInstructionList(
                    longPushInstruction(transformedValue),
                    integerPushInstruction(actualShiftAmount),
                    InsnNode(Opcodes.LSHL)
                )
            }

            else -> throw IllegalArgumentException("Unsupported type for shift left operation: ${value::class.java.name}. Expected Int or Long.")
        }
    }

    fun shiftRightPushInstruction(value: Number, shiftAmount: Long = Random.nextLong()): InsnList {
        return when (value) {
            is Int -> {
                val actualShiftAmount = shiftAmount % 32
                val transformedValue = value shl actualShiftAmount.toInt()
                buildInstructionList(
                    integerPushInstruction(transformedValue),
                    integerPushInstruction(actualShiftAmount),
                    InsnNode(Opcodes.ISHR)
                )
            }

            is Long -> {
                val actualShiftAmount = shiftAmount % 64
                val transformedValue = value shl actualShiftAmount.toInt()
                buildInstructionList(
                    longPushInstruction(transformedValue),
                    integerPushInstruction(actualShiftAmount),
                    InsnNode(Opcodes.LSHR)
                )
            }

            else -> throw IllegalArgumentException("Unsupported type for shift right (restore) operation: ${value::class.java.name}. Expected Int or Long.")
        }
    }

    fun complexIntegerPushInstruction(value: Number): InsnList {
        val restorationSteps = mutableListOf<IntegerDeobfuscationStep>()
        var currentValue = value.toInt()
        val instructions = InsnList()

        (0 until Random.nextInt(3, 5)).forEach { layer ->
            val layerKey = Random.nextInt()
            when (IntegerObfuscationOpType.entries.toTypedArray().random()) {
                IntegerObfuscationOpType.XOR -> {
                    currentValue = currentValue xor layerKey
                    restorationSteps.add(0, IntegerDeobfuscationStep(layerKey, Opcodes.IXOR))
                }

                IntegerObfuscationOpType.ADD -> {
                    currentValue += layerKey
                    restorationSteps.add(0, IntegerDeobfuscationStep(layerKey, Opcodes.ISUB))
                }

                IntegerObfuscationOpType.SUB -> {
                    currentValue -= layerKey
                    restorationSteps.add(0, IntegerDeobfuscationStep(layerKey, Opcodes.IADD))
                }
            }
        }

        instructions.add(integerPushInstruction(currentValue))
        for (restorationStep in restorationSteps) {
            instructions.add(integerPushInstruction(restorationStep.key))
            instructions.add(InsnNode(restorationStep.reverseOpcode))
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
        return instructions.filterIsInstance<LdcInsnNode>().filter { it.cst is String && (it.cst as String).isNotEmpty() }.map { Pair(it, it.cst as String) }
    }

    fun findNumericConstants(instructions: InsnList): List<Pair<AbstractInsnNode, Number>> {
        return instructions.filter { isNumericConstantInstruction(it) }.map { Pair(it, extractNumericValue(it)) }
    }

    fun buildInstructionList(vararg elements: Any): InsnList {
        return InsnList().apply {
            elements.forEach { element ->
                when (element) {
                    is AbstractInsnNode -> add(element)
                    is InsnList -> add(element)
                    else -> throw IllegalArgumentException(
                        "Unsupported type in buildInstructionList: ${element::class.java.name}. " +
                                "Only AbstractInsnNode and InsnList are supported."
                    )
                }
            }
        }
    }

    fun createClassNode(name: String): ClassNode {
        return ClassNode().apply {
            this.superName = "java/lang/Object"
            this.version = Opcodes.V1_8

            this.access = Opcodes.ACC_PUBLIC
            this.name = name
        }
    }

    fun createFieldNode(access: Int, name: String, descriptor: String, value: Any? = null): FieldNode {
        return FieldNode(access, name, descriptor, null, value)
    }

    fun createMethodNode(access: Int, name: String, descriptor: String): MethodNode {
        return MethodNode(access, name, descriptor, null, null)
    }

    fun createInvokeStatic(owner: String, name: String, descriptor: String): MethodInsnNode {
        return MethodInsnNode(Opcodes.INVOKESTATIC, owner, name, descriptor)
    }
}
