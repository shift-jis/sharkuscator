package dev.sharkuscator.obfuscator.transformers.strategies

import org.mapleir.asm.ClassNode
import org.mapleir.asm.MethodNode
import org.objectweb.asm.tree.AbstractInsnNode
import org.objectweb.asm.tree.InsnList

interface StringConstantObfuscationStrategy {
    fun prepareDecoderMethod(targetClassNode: ClassNode, decoderMethodName: String): MethodNode

    fun replaceInstructions(preparedDecoder: MethodNode, instructions: InsnList, targetInstruction: AbstractInsnNode, originalString: String)

    fun finalizeClass(targetClassNode: ClassNode)

    fun obfuscateString(originalString: String, keyString: String): Pair<String, ByteArray> {
        return obfuscateString(originalString, keyString.encodeToByteArray())
    }

    fun obfuscateString(originalString: String, keyBytes: ByteArray): Pair<String, ByteArray>
}
