package dev.sharkuscator.obfuscator.extensions

import org.mapleir.asm.FieldNode
import org.objectweb.asm.Opcodes

fun FieldNode.getQualifiedName(): String = "${owner.name}.${node.name}"

fun FieldNode.isDeclaredStatic(): Boolean = (node.access and Opcodes.ACC_STATIC) != 0

fun FieldNode.isDeclaredFinal(): Boolean = (node.access and Opcodes.ACC_FINAL) != 0

fun FieldNode.isDeclaredSynthetic(): Boolean = (node.access and Opcodes.ACC_SYNTHETIC) != 0

fun FieldNode.isDeclaredBridge(): Boolean = (node.access and Opcodes.ACC_BRIDGE) != 0

fun FieldNode.isDeclaredVolatile(): Boolean = (node.access and Opcodes.ACC_VOLATILE) != 0
