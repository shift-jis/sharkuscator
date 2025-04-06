package dev.sharkuscator.obfuscator.transformers.events.assembling

import dev.sharkuscator.obfuscator.transformers.events.CancellableEvent
import org.mapleir.app.service.ApplicationClassSource

class ResourceWriteEvent(val classSource: ApplicationClassSource, var name: String, var resourceData: ByteArray) : CancellableEvent()
