package dev.sharkuscator.obfuscator.transformers.events.transforming

import dev.sharkuscator.obfuscator.transformers.events.EventContext
import dev.sharkuscator.obfuscator.transformers.events.TransformerEvent
import org.mapleir.asm.MethodNode

class MethodTransformEvent(context: EventContext, methodNode: MethodNode) : TransformerEvent<MethodNode>(context, methodNode)
