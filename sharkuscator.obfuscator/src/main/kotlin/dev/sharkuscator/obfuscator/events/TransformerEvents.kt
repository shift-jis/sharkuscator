package dev.sharkuscator.obfuscator.events

import dev.sharkuscator.obfuscator.ObfuscationContext
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.FieldNode
import org.objectweb.asm.tree.MethodNode

open class TransformerEvents<T>(val obfuscationContext: ObfuscationContext, val nodeObject: T) : CancellableEvent() {
    class ClassTransformEvent(obfuscationContext: ObfuscationContext, classNode: ClassNode) : TransformerEvents<ClassNode>(obfuscationContext, classNode)
    class FieldTransformEvent(obfuscationContext: ObfuscationContext, fieldNode: FieldNode) : TransformerEvents<FieldNode>(obfuscationContext, fieldNode)
    class MethodTransformEvent(obfuscationContext: ObfuscationContext, methodNode: MethodNode) : TransformerEvents<MethodNode>(obfuscationContext, methodNode)
    class ResourceTransformEvent(val obfuscationContext: ObfuscationContext, var name: String, var data: ByteArray) : CancellableEvent()
}
