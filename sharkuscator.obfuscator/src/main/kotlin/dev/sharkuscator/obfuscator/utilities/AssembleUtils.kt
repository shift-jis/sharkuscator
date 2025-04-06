package dev.sharkuscator.obfuscator.utilities

import org.objectweb.asm.tree.AbstractInsnNode
import org.objectweb.asm.tree.InsnList
import org.objectweb.asm.tree.MethodNode

object AssembleUtils {
    fun instructionListOf(vararg instructions: AbstractInsnNode) = InsnList().apply {
        for (instruction in instructions) {
            add(instruction)
        }
    }

    fun createMethodNode(access: Int, name: String, descriptor: String): MethodNode {
        return MethodNode(access, name, descriptor, null, null)
    }
}
