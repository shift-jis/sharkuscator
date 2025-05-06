package dev.sharkuscator.obfuscator.events.transforming

import dev.sharkuscator.obfuscator.ObfuscationContext
import dev.sharkuscator.obfuscator.events.TransformerEvent
import org.mapleir.asm.ClassNode

class ClassTransformEvent(context: ObfuscationContext, classNode: ClassNode) : TransformerEvent<ClassNode>(context, classNode)
