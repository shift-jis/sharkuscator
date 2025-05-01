package dev.sharkuscator.obfuscator.transformers.obfuscators.constants.strategy

import org.mapleir.asm.ClassNode
import org.objectweb.asm.tree.InsnList
import org.objectweb.asm.tree.LdcInsnNode

class AggressiveStringEncryption : StringEncryptionStrategy {
    override fun createDecryptClassNode(className: String, methodName: String): ClassNode {
        TODO("Not yet implemented")
    }

    override fun replaceInstructions(instructions: InsnList, original: LdcInsnNode, replacement: String, keyBytes: ByteArray) {
        TODO("Not yet implemented")
    }

    override fun encryptString(value: String, keyBytes: ByteArray): Pair<String, ByteArray> {
        return Pair(value, keyBytes)
    }
}
