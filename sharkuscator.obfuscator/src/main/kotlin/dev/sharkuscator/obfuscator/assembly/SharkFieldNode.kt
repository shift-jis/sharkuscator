package dev.sharkuscator.obfuscator.assembly

import org.mapleir.asm.FieldNode

class SharkFieldNode(private val parentNode: SharkClassNode, originalNode: FieldNode) : NodeAdapter<FieldNode>(originalNode) {
    override fun fullyName(): String {
        return "${parentNode.originalNode.name}.${originalNode.name}"
    }
}
