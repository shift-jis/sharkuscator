package dev.sharkuscator.commons.extensions

import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.MethodInsnNode
import org.objectweb.asm.tree.MethodNode
import java.util.*

private val parentClassNodes = WeakHashMap<MethodNode, ClassNode>()

var MethodNode.classNode: ClassNode
    get() {
        return parentClassNodes[this]!!
    }
    set(value) {
        parentClassNodes[this] = value
    }

fun MethodNode.getQualifiedName(): String = "${classNode.name}.${name}${desc}"

fun MethodNode.hasMainSignature(): Boolean = name == "main" && desc == "([Ljava/lang/String;)V"

fun MethodNode.isStaticInitializer(): Boolean = name == "<clinit>" && desc == "()V"

fun MethodNode.isConstructor(): Boolean = name == "<init>"

fun MethodNode.isDeclaredSynthetic(): Boolean = (access and Opcodes.ACC_SYNTHETIC) != 0

fun MethodNode.isDeclaredBridge(): Boolean = (access and Opcodes.ACC_BRIDGE) != 0

fun MethodNode.isDeclaredAbstract(): Boolean = (access and Opcodes.ACC_ABSTRACT) != 0

fun MethodNode.isDeclaredNative(): Boolean = (access and Opcodes.ACC_NATIVE) != 0

fun MethodNode.isMixinAccessor(): Boolean = hasVisibleAnnotation("Lorg/spongepowered/asm/mixin/gen/Accessor;")

fun MethodNode.hasVisibleAnnotationContaining(descriptor: String): Boolean {
    return visibleAnnotations?.any { it.desc.contains(descriptor) } ?: false
}

fun MethodNode.hasVisibleAnnotation(descriptor: String): Boolean {
    return visibleAnnotations?.any { it.desc == descriptor } ?: false
}

fun MethodNode.hasInVisibleAnnotationContaining(descriptor: String): Boolean {
    return invisibleAnnotations?.any { it.desc.contains(descriptor) } ?: false
}

fun MethodNode.hasInVisibleAnnotation(descriptor: String): Boolean {
    return invisibleAnnotations?.any { it.desc == descriptor } ?: false
}

fun MethodNode.shouldSkipTransform(): Boolean {
    return isStaticInitializer() || isConstructor() || hasMainSignature()
}

fun MethodNode.invokeStatic(): MethodInsnNode {
    return MethodInsnNode(Opcodes.INVOKESTATIC, classNode.name, name, desc)
}
