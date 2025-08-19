package dev.sharkuscator.commons.extensions

import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.FieldNode
import java.util.*

private val parentClassNodes = WeakHashMap<FieldNode, ClassNode>()

var FieldNode.classNode: ClassNode
    get() {
        return parentClassNodes[this]!!
    }
    set(value) {
        parentClassNodes[this] = value
    }

fun FieldNode.getQualifiedName(): String = "${classNode.name}.${name}"

fun FieldNode.isDeclaredStatic(): Boolean = (access and Opcodes.ACC_STATIC) != 0

fun FieldNode.isDeclaredFinal(): Boolean = (access and Opcodes.ACC_FINAL) != 0

fun FieldNode.isDeclaredSynthetic(): Boolean = (access and Opcodes.ACC_SYNTHETIC) != 0

fun FieldNode.isDeclaredBridge(): Boolean = (access and Opcodes.ACC_BRIDGE) != 0

fun FieldNode.isDeclaredVolatile(): Boolean = (access and Opcodes.ACC_VOLATILE) != 0
