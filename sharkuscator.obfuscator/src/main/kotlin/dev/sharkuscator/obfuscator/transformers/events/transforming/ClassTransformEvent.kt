package dev.sharkuscator.obfuscator.transformers.events.transforming

import dev.sharkuscator.obfuscator.transformers.events.EventContext
import dev.sharkuscator.obfuscator.transformers.events.TransformerEvent
import org.mapleir.asm.ClassNode

class ClassTransformEvent(context: EventContext, classNode: ClassNode) : TransformerEvent<ClassNode>(context, classNode)
