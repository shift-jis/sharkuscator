package dev.sharkuscator.obfuscator.transformers.events.transforming

import dev.sharkuscator.obfuscator.transformers.events.CancellableEvent
import dev.sharkuscator.obfuscator.transformers.events.EventContext
import org.mapleir.app.service.ApplicationClassSource

class ResourceTransformEvent(val context: EventContext, var name: String, var resourceData: ByteArray) : CancellableEvent()
