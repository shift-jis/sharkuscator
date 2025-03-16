package dev.sharkuscator.obfuscator.assembly

import org.mapleir.asm.MethodNode

class SharkMethodNode(private val parentNode: SharkClassNode, originalNode: MethodNode) : NodeAdapter<MethodNode>(originalNode) {
    override fun fullyName(): String {
        return "${parentNode.originalNode.name}.${originalNode.name}${originalNode.desc}"
    }
}
