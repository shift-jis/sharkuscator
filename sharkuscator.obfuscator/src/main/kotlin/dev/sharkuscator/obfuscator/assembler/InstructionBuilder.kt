package dev.sharkuscator.obfuscator.assembler

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

    fun instruction(opcode: Int) = add(InsnNode(opcode))
}
