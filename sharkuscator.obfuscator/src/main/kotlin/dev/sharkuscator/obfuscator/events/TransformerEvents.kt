package dev.sharkuscator.obfuscator.events

import dev.sharkuscator.obfuscator.ObfuscationContext
import org.mapleir.asm.ClassNode
import org.mapleir.asm.FieldNode
import org.mapleir.asm.MethodNode

open class TransformerEvents<T>(val context: ObfuscationContext, val anytypeNode: T) : CancellableEvent() {
    class ClassTransformEvent(context: ObfuscationContext, classNode: ClassNode) : TransformerEvents<ClassNode>(context, classNode)
    class FieldTransformEvent(context: ObfuscationContext, fieldNode: FieldNode) : TransformerEvents<FieldNode>(context, fieldNode)
    class MethodTransformEvent(context: ObfuscationContext, methodNode: MethodNode) : TransformerEvents<MethodNode>(context, methodNode)
    class ResourceTransformEvent(val context: ObfuscationContext, var name: String, var data: ByteArray) : CancellableEvent()
}
