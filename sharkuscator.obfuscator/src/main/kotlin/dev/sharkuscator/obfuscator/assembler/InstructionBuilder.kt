package dev.sharkuscator.obfuscator.assembler

import jdk.internal.org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.AbstractInsnNode
import org.objectweb.asm.tree.InsnList
import org.objectweb.asm.tree.InsnNode
import org.objectweb.asm.tree.LdcInsnNode
import org.objectweb.asm.tree.MethodInsnNode

fun instructionBuilder(application: InstructionBuilder.() -> Unit): InsnList {
    return InstructionBuilder().apply(application).instructions
}

class InstructionBuilder {
    val instructions = InsnList()

    private fun add(instruction: AbstractInsnNode) {
        instructions.add(instruction)
    }

    private fun addAll(instructions: InsnList) {
        this.instructions.add(instructions)
    }

    fun instruction(opcode: Int) = add(InsnNode(opcode))

    fun loadConstant(value: String) = add(LdcInsnNode(value))

    fun invokeStatic(owner: String, name: String, descriptor: String) {
        add(MethodInsnNode(Opcodes.INVOKESTATIC, owner, name, descriptor))
    }
}
