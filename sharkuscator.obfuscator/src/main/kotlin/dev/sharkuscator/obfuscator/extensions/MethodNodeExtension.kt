package dev.sharkuscator.obfuscator.extensions

import dev.sharkuscator.obfuscator.utilities.BytecodeUtils
import org.mapleir.asm.MethodNode
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.MethodInsnNode

fun MethodNode.getQualifiedName(): String = "${owner.name}.${node.name}${node.desc}"

fun MethodNode.hasMainSignature(): Boolean = name == "main" && desc == "([Ljava/lang/String;)V"

fun MethodNode.isStaticInitializer(): Boolean = name == "<clinit>" && desc == "()V"

fun MethodNode.isConstructor(): Boolean = name == "<init>"

fun MethodNode.isDeclaredSynthetic(): Boolean = (node.access and Opcodes.ACC_SYNTHETIC) != 0

fun MethodNode.isDeclaredBridge(): Boolean = (node.access and Opcodes.ACC_BRIDGE) != 0

fun MethodNode.invokeStatic(): MethodInsnNode {
    return BytecodeUtils.createInvokeStatic(owner.name, name, desc)
}
