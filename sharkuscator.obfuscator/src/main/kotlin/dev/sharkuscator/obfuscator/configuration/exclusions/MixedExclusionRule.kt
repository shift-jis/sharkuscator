package dev.sharkuscator.obfuscator.configuration.exclusions

import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.FieldNode
import org.objectweb.asm.tree.MethodNode

class MixedExclusionRule(private val rules: List<ExclusionRule>) : ExclusionRule {
    override fun excluded(string: String): Boolean {
        return rules.any { it.excluded(string) }
    }

    override fun excluded(classNode: ClassNode): Boolean {
        return rules.any { it.excluded(classNode) }
    }

    override fun excluded(fieldNode: FieldNode): Boolean {
        return rules.any { it.excluded(fieldNode) }
    }

    override fun excluded(methodNode: MethodNode): Boolean {
        return rules.any { it.excluded(methodNode) }
    }
}
