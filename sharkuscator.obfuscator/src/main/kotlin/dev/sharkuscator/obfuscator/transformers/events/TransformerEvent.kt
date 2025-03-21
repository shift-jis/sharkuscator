package dev.sharkuscator.obfuscator.transformers.events

open class TransformerEvent<T>(val context: EventContext, val eventNode: T) : CancellableEvent()
