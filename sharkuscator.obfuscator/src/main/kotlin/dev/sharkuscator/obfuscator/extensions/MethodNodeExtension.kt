package dev.sharkuscator.obfuscator.extensions

import dev.sharkuscator.obfuscator.utilities.BytecodeUtils.createInvokeStatic
import org.mapleir.asm.MethodNode
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.MethodInsnNode

fun MethodNode.getQualifiedName(): String = "${owner.name}.${node.name}${node.desc}"

fun MethodNode.hasMainSignature(): Boolean = name == "main" && desc == "([Ljava/lang/String;)V"

fun MethodNode.isStaticInitializer(): Boolean = name == "<clinit>" && desc == "()V"

fun MethodNode.isConstructor(): Boolean = name == "<init>"

fun MethodNode.isDeclaredSynthetic(): Boolean = (node.access and Opcodes.ACC_SYNTHETIC) != 0

fun MethodNode.isDeclaredBridge(): Boolean = (node.access and Opcodes.ACC_BRIDGE) != 0

fun MethodNode.isMixinAccessor(): Boolean = hasVisibleAnnotation("Lorg/spongepowered/asm/mixin/gen/Accessor;")

fun MethodNode.hasVisibleAnnotationContaining(descriptor: String): Boolean {
    return node.visibleAnnotations?.any { it.desc.contains(descriptor) } ?: false
}

fun MethodNode.hasVisibleAnnotation(descriptor: String): Boolean {
    return node.visibleAnnotations?.any { it.desc == descriptor } ?: false
}

fun MethodNode.hasInVisibleAnnotationContaining(descriptor: String): Boolean {
    return node.invisibleAnnotations?.any { it.desc.contains(descriptor) } ?: false
}

fun MethodNode.hasInVisibleAnnotation(descriptor: String): Boolean {
    return node.invisibleAnnotations?.any { it.desc == descriptor } ?: false
}

fun MethodNode.shouldSkipTransform(): Boolean {
    return isStaticInitializer() || isConstructor() || hasMainSignature()
}

fun MethodNode.invokeStatic(): MethodInsnNode {
    return createInvokeStatic(owner.name, name, desc)
}
