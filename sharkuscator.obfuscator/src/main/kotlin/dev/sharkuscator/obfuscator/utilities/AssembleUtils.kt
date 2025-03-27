package dev.sharkuscator.obfuscator.utilities

import org.objectweb.asm.tree.AbstractInsnNode
import org.objectweb.asm.tree.InsnList

object AssembleUtils {
    fun instructionListOf(vararg instructions: AbstractInsnNode) = InsnList().apply {
        for (instruction in instructions) {
            add(instruction)
        }
    }
}
