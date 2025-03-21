package dev.sharkuscator.obfuscator.transformers.events

import org.mapleir.app.service.ApplicationClassSource
import org.mapleir.asm.ClassNode
import org.topdank.byteengineer.commons.data.JarContents

open class TransformerEvent<T>(val jarContents: JarContents<ClassNode>, val classSource: ApplicationClassSource, val eventNode: T) : CancellableEvent()
