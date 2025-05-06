package dev.sharkuscator.obfuscator.events

import dev.sharkuscator.obfuscator.ObfuscationContext

open class TransformerEvent<T>(val context: ObfuscationContext, val eventNode: T) : CancellableEvent()
