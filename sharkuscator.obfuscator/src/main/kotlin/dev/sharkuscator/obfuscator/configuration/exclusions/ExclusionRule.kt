package dev.sharkuscator.obfuscator.configuration.exclusions

import org.mapleir.asm.ClassNode
import org.mapleir.asm.FieldNode
import org.mapleir.asm.MethodNode

interface ExclusionRule {
    fun excluded(string: String): Boolean

    fun excluded(classNode: ClassNode): Boolean

    fun excluded(fieldNode: FieldNode): Boolean

    fun excluded(methodNode: MethodNode): Boolean
}
