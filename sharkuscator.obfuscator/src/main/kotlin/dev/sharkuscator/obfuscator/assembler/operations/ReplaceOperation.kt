package dev.sharkuscator.obfuscator.assembler.operations

import org.objectweb.asm.tree.AbstractInsnNode
import org.objectweb.asm.tree.InsnList

data class ReplaceOperation(private val instruction: AbstractInsnNode, private val replacement: InsnList) : ModifierOperation {
    override fun apply(instructions: InsnList) {
        instructions.insert(instruction, replacement)
        instructions.remove(instruction)
    }
}
