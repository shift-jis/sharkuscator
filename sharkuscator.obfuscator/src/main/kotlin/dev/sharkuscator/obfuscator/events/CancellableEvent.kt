package dev.sharkuscator.obfuscator.events

import meteordevelopment.orbit.ICancellable

open class CancellableEvent : ICancellable {
    private var cancelled = false

    override fun setCancelled(cancelled: Boolean) {
        this.cancelled = cancelled
    }

    override fun isCancelled(): Boolean {
        return cancelled
    }
}
