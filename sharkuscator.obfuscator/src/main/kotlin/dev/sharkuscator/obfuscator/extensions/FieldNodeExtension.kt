package dev.sharkuscator.obfuscator.extensions

import org.mapleir.asm.FieldNode

fun FieldNode.fullyName(): String = "${owner.name}.${node.name}"
