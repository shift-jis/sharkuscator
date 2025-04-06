package dev.sharkuscator.obfuscator.transformers.events.assembling

import dev.sharkuscator.obfuscator.transformers.events.CancellableEvent
import org.mapleir.asm.ClassNode

class ClassWriteEvent(val classNode: ClassNode, var classData: ByteArray) : CancellableEvent()
