package dev.sharkuscator.obfuscator.extensions

import org.mapleir.asm.ClassNode
import org.objectweb.asm.Opcodes

fun ClassNode.fullyName(): String = "${node.name}.class"

fun ClassNode.isInterface(): Boolean = (node.access and Opcodes.ACC_INTERFACE) != 0

fun ClassNode.isAnnotation(): Boolean = (node.access and Opcodes.ACC_ANNOTATION) != 0

fun ClassNode.isAbstract(): Boolean = (node.access and Opcodes.ACC_ABSTRACT) != 0
