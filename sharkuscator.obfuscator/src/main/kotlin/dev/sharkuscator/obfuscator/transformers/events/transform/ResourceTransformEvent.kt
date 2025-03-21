package dev.sharkuscator.obfuscator.transformers.events.transform

import dev.sharkuscator.obfuscator.transformers.events.CancellableEvent
import org.mapleir.app.service.ApplicationClassSource

class ResourceTransformEvent(val classSource: ApplicationClassSource, var name: String, var resourceData: ByteArray) : CancellableEvent()
