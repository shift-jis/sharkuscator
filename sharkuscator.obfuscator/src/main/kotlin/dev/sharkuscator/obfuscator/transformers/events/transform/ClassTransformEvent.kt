package dev.sharkuscator.obfuscator.transformers.events.transform

import dev.sharkuscator.obfuscator.transformers.events.EventContext
import dev.sharkuscator.obfuscator.transformers.events.TransformerEvent
import org.mapleir.app.service.ApplicationClassSource
import org.mapleir.asm.ClassNode
import org.topdank.byteengineer.commons.data.JarContents

class ClassTransformEvent(context: EventContext, classNode: ClassNode) : TransformerEvent<ClassNode>(context, classNode)
