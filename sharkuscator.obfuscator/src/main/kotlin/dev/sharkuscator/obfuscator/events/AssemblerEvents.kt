package dev.sharkuscator.obfuscator.events

import dev.sharkuscator.obfuscator.ObfuscationContext
import org.objectweb.asm.tree.ClassNode

open class AssemblerEvents(val context: ObfuscationContext) : CancellableEvent() {
    class ResourceWriteEvent(context: ObfuscationContext, var name: String, var resourceData: ByteArray) : AssemblerEvents(context)
    class ClassWriteEvent(context: ObfuscationContext, val classNode: ClassNode, var classData: ByteArray) : AssemblerEvents(context)
}
