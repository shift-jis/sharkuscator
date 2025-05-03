package dev.sharkuscator.obfuscator.extensions

import org.mapleir.asm.FieldNode
import org.objectweb.asm.Opcodes

fun FieldNode.fullyName(): String = "${owner.name}.${node.name}"

fun FieldNode.isStatic(): Boolean = (node.access and Opcodes.ACC_STATIC) != 0

fun FieldNode.isFinal(): Boolean = (node.access and Opcodes.ACC_FINAL) != 0

fun FieldNode.isSynthetic(): Boolean = (node.access and Opcodes.ACC_SYNTHETIC) != 0

fun FieldNode.isBridge(): Boolean = (node.access and Opcodes.ACC_BRIDGE) != 0

fun FieldNode.isVolatile(): Boolean = (node.access and Opcodes.ACC_VOLATILE) != 0
