package dev.sharkuscator.obfuscator.extensions

import org.mapleir.asm.MethodNode

fun MethodNode.fullyName(): String = "${owner.name}.${node.name}${node.desc}"

fun MethodNode.isMain(): Boolean = name == "main" && desc == "([Ljava/lang/String;)V"

fun MethodNode.isClInit(): Boolean = name == "<clinit>" && desc == "()V"

fun MethodNode.isInit(): Boolean = name == "<init>"
