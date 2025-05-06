package dev.sharkuscator.obfuscator.extensions

import org.mapleir.asm.ClassNode
import org.mapleir.asm.FieldNode
import org.objectweb.asm.Opcodes

fun ClassNode.getQualifiedName(): String = "${node.name}.class"

fun ClassNode.isDeclaredAsInterface(): Boolean = (node.access and Opcodes.ACC_INTERFACE) != 0

fun ClassNode.isDeclaredAsAnnotation(): Boolean = (node.access and Opcodes.ACC_ANNOTATION) != 0

fun ClassNode.isDeclaredAsAbstract(): Boolean = (node.access and Opcodes.ACC_ABSTRACT) != 0

fun ClassNode.containsMainMethod(): Boolean = methods.any { it.hasMainSignature() }

fun ClassNode.addField(fieldNode: FieldNode) {
    fields.add(fieldNode)
    node.fields.add(fieldNode.node)
}
