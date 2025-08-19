package dev.sharkuscator.obfuscator.configuration.exclusions

import dev.sharkuscator.commons.extensions.getQualifiedName
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.FieldNode
import org.objectweb.asm.tree.MethodNode

class StringExclusionRule(private val rule: Regex) : ExclusionRule {
    override fun excluded(string: String): Boolean {
        return rule.matches(string)
    }

    override fun excluded(classNode: ClassNode): Boolean {
        return rule.matches(classNode.getQualifiedName())
    }

    override fun excluded(fieldNode: FieldNode): Boolean {
        return rule.matches(fieldNode.getQualifiedName())
    }

    override fun excluded(methodNode: MethodNode): Boolean {
        return rule.matches(methodNode.getQualifiedName())
    }
}
