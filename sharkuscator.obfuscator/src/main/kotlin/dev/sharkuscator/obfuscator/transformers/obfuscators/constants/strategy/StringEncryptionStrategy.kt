package dev.sharkuscator.obfuscator.transformers.obfuscators.constants.strategy

import org.mapleir.asm.ClassNode
import org.objectweb.asm.tree.InsnList
import org.objectweb.asm.tree.LdcInsnNode

interface StringEncryptionStrategy {
    fun createDecryptClassNode(className: String, methodName: String): ClassNode

    fun replaceInstructions(instructions: InsnList, original: LdcInsnNode, replacement: String, keyBytes: ByteArray)

    fun encryptString(value: String, keyString: String): Pair<String, ByteArray> {
        return encryptString(value, keyString.encodeToByteArray())
    }

    fun encryptString(value: String, keyBytes: ByteArray): Pair<String, ByteArray>
}
