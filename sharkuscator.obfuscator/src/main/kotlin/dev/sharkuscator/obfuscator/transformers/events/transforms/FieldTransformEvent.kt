package dev.sharkuscator.obfuscator.transformers.events.transforms

import dev.sharkuscator.obfuscator.transformers.events.TransformerEvent
import org.mapleir.app.service.ApplicationClassSource
import org.mapleir.asm.ClassNode
import org.mapleir.asm.FieldNode
import org.topdank.byteengineer.commons.data.JarContents

class FieldTransformEvent(jarContents: JarContents<ClassNode>, classSource: ApplicationClassSource, fieldNode: FieldNode) : TransformerEvent<FieldNode>(jarContents, classSource, fieldNode)
