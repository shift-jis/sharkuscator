package dev.sharkuscator.obfuscator.events.transforming

import dev.sharkuscator.obfuscator.ObfuscationContext
import dev.sharkuscator.obfuscator.events.TransformerEvent
import org.mapleir.asm.MethodNode

class MethodTransformEvent(context: ObfuscationContext, methodNode: MethodNode) : TransformerEvent<MethodNode>(context, methodNode)
