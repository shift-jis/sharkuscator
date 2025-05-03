package dev.sharkuscator.obfuscator.transformers.obfuscators.constants.strategies

import org.mapleir.asm.ClassNode
import org.objectweb.asm.tree.AbstractInsnNode
import org.objectweb.asm.tree.InsnList

interface StringConstantObfuscationStrategy {
    fun createDecryptClassNode(className: String, methodName: String): ClassNode

    fun replaceInstructions(instructions: InsnList, targetInstruction: AbstractInsnNode, originalString: String)

    fun obfuscateString(originalString: String, keyString: String): Pair<String, ByteArray> {
        return obfuscateString(originalString, keyString.encodeToByteArray())
    }

    fun obfuscateString(originalString: String, keyBytes: ByteArray): Pair<String, ByteArray>
}
