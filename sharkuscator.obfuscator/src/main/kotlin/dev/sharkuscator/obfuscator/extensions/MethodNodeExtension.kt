package dev.sharkuscator.obfuscator.extensions

import dev.sharkuscator.obfuscator.utilities.BytecodeAssembler
import org.mapleir.asm.MethodNode
import org.objectweb.asm.tree.MethodInsnNode

fun MethodNode.fullyName(): String = "${owner.name}.${node.name}${node.desc}"

fun MethodNode.isMain(): Boolean = name == "main" && desc == "([Ljava/lang/String;)V"

fun MethodNode.isClInit(): Boolean = name == "<clinit>" && desc == "()V"

fun MethodNode.isInit(): Boolean = name == "<init>"

fun MethodNode.invokeStatic(): MethodInsnNode {
    return BytecodeAssembler.createInvokeStatic(owner.name, name, desc)
}
