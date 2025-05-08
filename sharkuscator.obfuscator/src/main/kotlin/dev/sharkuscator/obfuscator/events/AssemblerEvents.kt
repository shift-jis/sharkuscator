package dev.sharkuscator.obfuscator.events

import org.mapleir.app.service.ApplicationClassSource
import org.mapleir.asm.ClassNode

open class AssemblerEvents : CancellableEvent() {
    class ResourceWriteEvent(val classSource: ApplicationClassSource, var name: String, var resourceData: ByteArray) : AssemblerEvents()
    class ClassWriteEvent(val classNode: ClassNode, var classData: ByteArray) : AssemblerEvents()
    class ClassDumpEvent(val classNode: ClassNode) : AssemblerEvents()
}
