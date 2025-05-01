package dev.sharkuscator.obfuscator.extensions

import jdk.internal.org.objectweb.asm.Opcodes
import org.mapleir.asm.FieldNode

fun FieldNode.fullyName(): String = "${owner.name}.${node.name}"

fun FieldNode.isFinal(): Boolean = (node.access and Opcodes.ACC_FINAL) != 0

fun FieldNode.isSynthetic(): Boolean = (node.access and org.objectweb.asm.Opcodes.ACC_SYNTHETIC) != 0

fun FieldNode.isBridge(): Boolean = (node.access and org.objectweb.asm.Opcodes.ACC_BRIDGE) != 0
