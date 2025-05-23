package dev.sharkuscator.obfuscator.transformers

import dev.sharkuscator.obfuscator.configuration.GsonConfiguration
import dev.sharkuscator.obfuscator.configuration.transformers.TransformerConfiguration

abstract class BaseTransformer<T : TransformerConfiguration>(
    private val transformerName: String,
    private val configurationType: Class<T>
) : SharkTransformer<T> {
    lateinit var configuration: T
    var transformed = false

    override fun initialization(configuration: GsonConfiguration): T {
        this.configuration = configuration.fromTransformer(this, configurationType)
        return this.configuration
    }

    override fun getConfiguration(): Class<T> {
        return configurationType
    }

    override fun canTransform(): Boolean {
        return configuration.enabled && !transformed
    }

    override fun getExecutionPriority(): Int {
        return TransformerPriority.ZERO
    }

    override fun getTransformerName(): String {
        return transformerName
    }
}
