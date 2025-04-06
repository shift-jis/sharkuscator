package dev.sharkuscator.obfuscator.assembler.operations

import org.objectweb.asm.tree.AbstractInsnNode
import org.objectweb.asm.tree.InsnList

class AppendOperation(private val instruction: AbstractInsnNode, private val instructions: InsnList) : ModifierOperation {
    override fun apply(instructions: InsnList) {
        instructions.insert(instruction, this.instructions)
    }
}
