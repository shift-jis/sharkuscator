package dev.sharkuscator.obfuscator.assembly

import org.mapleir.asm.ClassNode

class SharkClassNode(originalNode: ClassNode) : NodeAdapter<ClassNode>(originalNode) {
    override fun fullyName(): String {
        return "${originalNode.name}.class"
    }
}
