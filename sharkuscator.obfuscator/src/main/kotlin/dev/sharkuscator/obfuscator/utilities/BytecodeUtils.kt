package dev.sharkuscator.obfuscator.utilities

import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.*

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

    fun findNonEmptyStrings(instructions: InsnList): List<Pair<LdcInsnNode, String>> {
        return instructions.filterIsInstance<LdcInsnNode>().filter { it.cst is String && (it.cst as String).isNotEmpty() }.map { Pair(it, it.cst as String) }
    }

    fun findNumericConstants(instructions: InsnList): List<Pair<AbstractInsnNode, Number>> {
        return instructions.filter { isNumericConstantInstruction(it) }.map { Pair(it, extractNumericValue(it)) }
    }

    fun buildInstructionList(vararg instructions: AbstractInsnNode): InsnList {
        return InsnList().apply {
            instructions.forEach { add(it) }
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
