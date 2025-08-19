package dev.sharkuscator.obfuscator.configuration.exclusions

import dev.sharkuscator.annotations.DoNotObfuscate
import org.objectweb.asm.Type
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.FieldNode
import org.objectweb.asm.tree.MethodNode

class AnnotationExclusionRule : ExclusionRule {
    private val excludeDescriptor = Type.getDescriptor(DoNotObfuscate::class.java)

    override fun excluded(string: String): Boolean {
        return false
    }

    override fun excluded(classNode: ClassNode): Boolean {
        val annotations = classNode.invisibleAnnotations ?: return false
        return annotations.any { it.desc.equals(excludeDescriptor) }
    }

    override fun excluded(fieldNode: FieldNode): Boolean {
        val annotations = fieldNode.invisibleAnnotations ?: return false
        return annotations.any { it.desc.equals(excludeDescriptor) }
    }

    override fun excluded(methodNode: MethodNode): Boolean {
        val annotations = methodNode.invisibleAnnotations ?: return false
        return annotations.any { it.desc.equals(excludeDescriptor) }
    }
}
