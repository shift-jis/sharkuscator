package dev.sharkuscator.obfuscator.assembler.operations

import org.objectweb.asm.tree.InsnList

interface ModifierOperation {
    fun apply(instructions: InsnList)
}
