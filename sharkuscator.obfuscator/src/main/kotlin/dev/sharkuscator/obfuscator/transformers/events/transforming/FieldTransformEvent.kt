package dev.sharkuscator.obfuscator.transformers.events.transforming

import dev.sharkuscator.obfuscator.transformers.events.EventContext
import dev.sharkuscator.obfuscator.transformers.events.TransformerEvent
import org.mapleir.asm.FieldNode

class FieldTransformEvent(context: EventContext, fieldNode: FieldNode) : TransformerEvent<FieldNode>(context, fieldNode)
