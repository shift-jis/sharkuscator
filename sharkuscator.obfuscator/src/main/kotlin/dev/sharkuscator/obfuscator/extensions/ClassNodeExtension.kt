package dev.sharkuscator.obfuscator.extensions

import org.mapleir.asm.ClassNode
import org.objectweb.asm.Opcodes

fun ClassNode.fullyName(): String = "${node.name}.class"

fun ClassNode.isInterface(): Boolean = (node.access and Opcodes.ACC_INTERFACE) != 0
