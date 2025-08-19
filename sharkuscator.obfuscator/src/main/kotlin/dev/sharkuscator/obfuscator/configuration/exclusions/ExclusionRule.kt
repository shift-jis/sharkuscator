package dev.sharkuscator.obfuscator.configuration.exclusions

import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.FieldNode
import org.objectweb.asm.tree.MethodNode

interface ExclusionRule {
    fun excluded(string: String): Boolean

    fun excluded(classNode: ClassNode): Boolean

    fun excluded(fieldNode: FieldNode): Boolean

    fun excluded(methodNode: MethodNode): Boolean
}
