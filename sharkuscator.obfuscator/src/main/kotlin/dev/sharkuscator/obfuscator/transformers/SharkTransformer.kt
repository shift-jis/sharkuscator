package dev.sharkuscator.obfuscator.transformers

import dev.sharkuscator.obfuscator.configuration.GsonConfiguration

interface SharkTransformer<T> {
    fun initialization(configuration: GsonConfiguration): Boolean

    fun getConfiguration(): Class<T>

    fun isEnabled(): Boolean

    fun getName(): String
}
