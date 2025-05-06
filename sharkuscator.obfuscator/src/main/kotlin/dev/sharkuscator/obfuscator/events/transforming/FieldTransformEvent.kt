package dev.sharkuscator.obfuscator.events.transforming

import dev.sharkuscator.obfuscator.ObfuscationContext
import dev.sharkuscator.obfuscator.events.TransformerEvent
import org.mapleir.asm.FieldNode

class FieldTransformEvent(context: ObfuscationContext, fieldNode: FieldNode) : TransformerEvent<FieldNode>(context, fieldNode)
