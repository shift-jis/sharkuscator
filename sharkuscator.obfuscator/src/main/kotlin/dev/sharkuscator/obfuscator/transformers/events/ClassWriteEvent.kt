package dev.sharkuscator.obfuscator.transformers.events

import org.mapleir.app.service.ApplicationClassSource

class ClassWriteEvent(classSource: ApplicationClassSource, var classData: ByteArray) : TransformerEvent(classSource)
