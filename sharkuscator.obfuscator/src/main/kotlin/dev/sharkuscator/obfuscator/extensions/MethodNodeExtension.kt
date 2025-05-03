package dev.sharkuscator.obfuscator.extensions

import dev.sharkuscator.obfuscator.utilities.BytecodeUtils
import org.mapleir.asm.MethodNode
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.MethodInsnNode

fun MethodNode.fullyName(): String = "${owner.name}.${node.name}${node.desc}"

fun MethodNode.isMain(): Boolean = name == "main" && desc == "([Ljava/lang/String;)V"

fun MethodNode.isClInit(): Boolean = name == "<clinit>" && desc == "()V"

fun MethodNode.isInit(): Boolean = name == "<init>"

fun MethodNode.isSynthetic(): Boolean = (node.access and Opcodes.ACC_SYNTHETIC) != 0

fun MethodNode.isBridge(): Boolean = (node.access and Opcodes.ACC_BRIDGE) != 0

fun MethodNode.invokeStatic(): MethodInsnNode {
    return BytecodeUtils.createInvokeStatic(owner.name, name, desc)
}
