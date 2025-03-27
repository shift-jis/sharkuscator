package dev.sharkuscator.obfuscator.assembler

import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.AbstractInsnNode
import org.objectweb.asm.tree.InsnList
import org.objectweb.asm.tree.InsnNode

fun instructionBuilder(application: InstructionBuilder.() -> Unit): InsnList {
    return InstructionBuilder().apply(application).instructions
}

class InstructionBuilder {
    val instructions = InsnList()

    private fun add(instruction: AbstractInsnNode) {
        instructions.add(instruction)
    }

    private fun add(instructions: InsnList) {
        this.instructions.add(instructions)
    }

    operator fun AbstractInsnNode.unaryPlus() = add(this)
    operator fun InsnList.unaryPlus() = add(this)

    private fun instruction(opcode: Int) = +InsnNode(opcode)

    fun intNeg() = instruction(Opcodes.INEG)
    fun intSub() = instruction(Opcodes.ISUB)
    fun intAdd() = instruction(Opcodes.IADD)
    fun intMul() = instruction(Opcodes.IMUL)
    fun intDiv() = instruction(Opcodes.IDIV)
    fun intAnd() = instruction(Opcodes.IAND)
    fun intXor() = instruction(Opcodes.IXOR)
    fun intRem() = instruction(Opcodes.IREM)
    fun intOr() = instruction(Opcodes.IOR)

    fun longNeg() = instruction(Opcodes.LNEG)
    fun longSub() = instruction(Opcodes.LSUB)
    fun longAdd() = instruction(Opcodes.LADD)
    fun longMul() = instruction(Opcodes.LMUL)
    fun longAnd() = instruction(Opcodes.LAND)
    fun longXor() = instruction(Opcodes.LXOR)
    fun longOr() = instruction(Opcodes.LOR)
}
