package dev.sharkuscator.obfuscator.assembler

import dev.sharkuscator.obfuscator.assembler.operations.AppendOperation
import dev.sharkuscator.obfuscator.assembler.operations.InsertOperation
import dev.sharkuscator.obfuscator.assembler.operations.ModifierOperation
import dev.sharkuscator.obfuscator.assembler.operations.ReplaceOperation
import org.objectweb.asm.tree.AbstractInsnNode
import org.objectweb.asm.tree.InsnList
import org.objectweb.asm.tree.MethodNode

class InstructionModifier {
    private val operations: MutableList<ModifierOperation> = mutableListOf()

    fun removeOperation(original: AbstractInsnNode) {
        replaceOperation(original, InsnList())
    }

    fun replaceOperation(original: AbstractInsnNode, replacement: InsnList) {
        operations.add(ReplaceOperation(original, replacement))
    }

    fun insertOperation(instructions: InsnList) {
        operations.add(InsertOperation(instructions))
    }

    fun appendOperation(original: AbstractInsnNode, instructions: InsnList) {
        operations.add(AppendOperation(original, instructions))
    }

    fun apply(methodNode: MethodNode) {
        val instructions = methodNode.instructions
        operations.forEach { operation -> operation.apply(instructions) }
    }
}
