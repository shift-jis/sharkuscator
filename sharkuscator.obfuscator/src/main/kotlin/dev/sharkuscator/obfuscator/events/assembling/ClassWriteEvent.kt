package dev.sharkuscator.obfuscator.events.assembling

import dev.sharkuscator.obfuscator.events.CancellableEvent
import org.mapleir.asm.ClassNode

class ClassWriteEvent(val classNode: ClassNode, var classData: ByteArray) : CancellableEvent()
