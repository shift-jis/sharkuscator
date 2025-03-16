package dev.sharkuscator.obfuscator.transformers

import dev.sharkuscator.obfuscator.configuration.JsonConfiguration

interface SharkTransformer<T> {
    fun initialization(configuration: JsonConfiguration): Boolean

    fun getConfiguration(): Class<T>

    fun getName(): String
}
