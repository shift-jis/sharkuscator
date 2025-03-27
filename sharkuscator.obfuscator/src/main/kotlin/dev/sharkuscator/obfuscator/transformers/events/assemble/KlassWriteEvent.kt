package dev.sharkuscator.obfuscator.transformers.events.assemble

import dev.sharkuscator.obfuscator.transformers.events.CancellableEvent
import org.mapleir.asm.ClassNode

class KlassWriteEvent(val classNode: ClassNode, var classData: ByteArray) : CancellableEvent()
