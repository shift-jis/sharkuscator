package dev.sharkuscator.obfuscator.transformers.events.transform

import dev.sharkuscator.obfuscator.transformers.events.TransformerEvent
import org.mapleir.app.service.ApplicationClassSource
import org.mapleir.asm.ClassNode
import org.topdank.byteengineer.commons.data.JarContents

class ClassTransformEvent(jarContents: JarContents<ClassNode>, classSource: ApplicationClassSource, classNode: ClassNode) : TransformerEvent<ClassNode>(jarContents, classSource, classNode)
