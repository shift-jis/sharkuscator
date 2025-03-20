package dev.sharkuscator.obfuscator.transformers.events

import meteordevelopment.orbit.ICancellable
import org.mapleir.app.service.ApplicationClassSource

open class TransformerEvent(classSource: ApplicationClassSource) : ICancellable {
    private var cancelled = false

    override fun setCancelled(cancelled: Boolean) {
        this.cancelled = cancelled
    }

    override fun isCancelled(): Boolean {
        return cancelled
    }
}
