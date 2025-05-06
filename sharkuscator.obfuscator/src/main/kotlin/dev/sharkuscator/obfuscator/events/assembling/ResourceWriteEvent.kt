package dev.sharkuscator.obfuscator.events.assembling

import dev.sharkuscator.obfuscator.events.CancellableEvent
import org.mapleir.app.service.ApplicationClassSource

class ResourceWriteEvent(val classSource: ApplicationClassSource, var name: String, var resourceData: ByteArray) : CancellableEvent()
