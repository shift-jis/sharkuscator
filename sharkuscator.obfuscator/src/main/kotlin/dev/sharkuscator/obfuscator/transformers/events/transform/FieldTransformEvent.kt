package dev.sharkuscator.obfuscator.transformers.events.transform

import dev.sharkuscator.obfuscator.transformers.events.EventContext
import dev.sharkuscator.obfuscator.transformers.events.TransformerEvent
import org.mapleir.app.service.ApplicationClassSource
import org.mapleir.asm.ClassNode
import org.mapleir.asm.FieldNode
import org.topdank.byteengineer.commons.data.JarContents

class FieldTransformEvent(context: EventContext, fieldNode: FieldNode) : TransformerEvent<FieldNode>(context, fieldNode)
