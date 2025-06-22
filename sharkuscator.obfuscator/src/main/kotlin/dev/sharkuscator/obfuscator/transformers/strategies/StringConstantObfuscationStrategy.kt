package dev.sharkuscator.obfuscator.transformers.strategies

import dev.sharkuscator.obfuscator.ObfuscationContext
import org.mapleir.asm.ClassNode
import org.objectweb.asm.tree.AbstractInsnNode
import org.objectweb.asm.tree.InsnList

interface StringConstantObfuscationStrategy {
    fun initialization(obfuscationContext: ObfuscationContext, targetClassNode: ClassNode)

    fun replaceInstructions(targetClassNode: ClassNode, instructions: InsnList, targetInstruction: AbstractInsnNode, originalString: String)

    fun buildDecryptionRoutine(obfuscationContext: ObfuscationContext, targetClassNode: ClassNode)

    fun obfuscateString(originalString: String, keyBytes: ByteArray): Pair<String, ByteArray>
}
