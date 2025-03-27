package dev.sharkuscator.obfuscator.transformers.events.transform

import dev.sharkuscator.obfuscator.transformers.events.EventContext
import dev.sharkuscator.obfuscator.transformers.events.TransformerEvent
import org.mapleir.asm.ClassNode

class KlassTransformEvent(context: EventContext, classNode: ClassNode) : TransformerEvent<ClassNode>(context, classNode)
