package dev.sharkuscator.obfuscator.events.transforming

import dev.sharkuscator.obfuscator.ObfuscationContext
import dev.sharkuscator.obfuscator.events.CancellableEvent

class ResourceTransformEvent(val context: ObfuscationContext, var name: String, var resourceData: ByteArray) : CancellableEvent()
