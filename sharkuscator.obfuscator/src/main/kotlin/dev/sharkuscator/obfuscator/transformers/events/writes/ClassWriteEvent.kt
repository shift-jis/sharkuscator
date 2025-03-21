package dev.sharkuscator.obfuscator.transformers.events.writes

import dev.sharkuscator.obfuscator.transformers.events.TransformerEvent
import org.mapleir.app.service.ApplicationClassSource
import org.mapleir.asm.ClassNode
import org.topdank.byteengineer.commons.data.JarContents

class ClassWriteEvent(jarContents: JarContents<ClassNode>, classSource: ApplicationClassSource, classNode: ClassNode, var classData: ByteArray) : TransformerEvent<ClassNode>(jarContents, classSource, classNode)
