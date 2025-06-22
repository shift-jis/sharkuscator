package dev.sharkuscator.obfuscator.transformers

import dev.sharkuscator.obfuscator.configuration.GsonConfiguration

interface SharkTransformer<T> {
    fun initialization(configuration: GsonConfiguration): T

    fun transformerStrength(): TransformerStrength

    fun isEligibleForExecution(): Boolean

    fun executionPriority(): Int

    fun transformerName(): String
}
