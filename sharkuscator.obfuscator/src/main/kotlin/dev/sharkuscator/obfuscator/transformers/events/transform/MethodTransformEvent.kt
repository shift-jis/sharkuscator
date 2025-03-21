package dev.sharkuscator.obfuscator.transformers.events.transform

import dev.sharkuscator.obfuscator.transformers.events.TransformerEvent
import org.mapleir.app.service.ApplicationClassSource
import org.mapleir.asm.ClassNode
import org.mapleir.asm.MethodNode
import org.topdank.byteengineer.commons.data.JarContents

class MethodTransformEvent(jarContents: JarContents<ClassNode>, classSource: ApplicationClassSource, methodNode: MethodNode) : TransformerEvent<MethodNode>(jarContents, classSource, methodNode)
