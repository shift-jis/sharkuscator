package dev.sharkuscator.obfuscator.configuration.exclusions

import dev.sharkuscator.annotations.Exclude
import org.mapleir.asm.ClassNode
import org.mapleir.asm.FieldNode
import org.mapleir.asm.MethodNode
import org.objectweb.asm.Type

class AnnotationExclusionRule : ExclusionRule {
    private val excludeDescriptor = Type.getDescriptor(Exclude::class.java)

    override fun excluded(string: String): Boolean {
        return false
    }

    override fun excluded(classNode: ClassNode): Boolean {
        val annotations = classNode.node.invisibleAnnotations ?: return false
        return annotations.any { it.desc.equals(excludeDescriptor) }
    }

    override fun excluded(fieldNode: FieldNode): Boolean {
        val annotations = fieldNode.node.invisibleAnnotations ?: return false
        return annotations.any { it.desc.equals(excludeDescriptor) }
    }

    override fun excluded(methodNode: MethodNode): Boolean {
        val annotations = methodNode.node.invisibleAnnotations ?: return false
        return annotations.any { it.desc.equals(excludeDescriptor) }
    }
}
