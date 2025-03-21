package dev.sharkuscator.obfuscator.transformers.events.assemble

import dev.sharkuscator.obfuscator.transformers.events.CancellableEvent
import dev.sharkuscator.obfuscator.transformers.events.EventContext
import dev.sharkuscator.obfuscator.transformers.events.TransformerEvent
import org.mapleir.app.service.ApplicationClassSource
import org.mapleir.asm.ClassNode
import org.topdank.byteengineer.commons.data.JarContents

class ClassWriteEvent(val classNode: ClassNode, var classData: ByteArray) : CancellableEvent()
