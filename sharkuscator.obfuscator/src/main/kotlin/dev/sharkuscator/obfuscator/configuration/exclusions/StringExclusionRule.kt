package dev.sharkuscator.obfuscator.configuration.exclusions

import dev.sharkuscator.obfuscator.extensions.fullyName
import org.mapleir.asm.ClassNode
import org.mapleir.asm.FieldNode
import org.mapleir.asm.MethodNode

class StringExclusionRule(private val rule: Regex) : ExclusionRule {
    override fun excluded(string: String): Boolean {
        return rule.matches(string)
    }

    override fun excluded(classNode: ClassNode): Boolean {
        return rule.matches(classNode.fullyName())
    }

    override fun excluded(fieldNode: FieldNode): Boolean {
        return rule.matches(fieldNode.fullyName())
    }

    override fun excluded(methodNode: MethodNode): Boolean {
        return rule.matches(methodNode.fullyName())
    }
}