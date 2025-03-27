package dev.sharkuscator.obfuscator.assembler.operations

import org.objectweb.asm.tree.InsnList

data class InsertOperation(private val instructions: InsnList) : ModifierOperation {
    override fun apply(instructions: InsnList) {
        instructions.insert(this.instructions)
    }
}
