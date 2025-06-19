package dev.sharkuscator.obfuscator.transformers

import dev.sharkuscator.obfuscator.configuration.GsonConfiguration

interface SharkTransformer<T> {
    fun initialization(configuration: GsonConfiguration): T

    fun isEligibleForExecution(): Boolean

    fun getExecutionPriority(): Int

    fun getTransformerName(): String
}
