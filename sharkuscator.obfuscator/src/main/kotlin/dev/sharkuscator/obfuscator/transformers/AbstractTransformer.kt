package dev.sharkuscator.obfuscator.transformers

import dev.sharkuscator.obfuscator.configuration.JsonConfiguration
import dev.sharkuscator.obfuscator.configuration.transformers.TransformerConfiguration

abstract class AbstractTransformer<T : TransformerConfiguration>(private val name: String, private val clazz: Class<T>) : SharkTransformer<T> {
    protected lateinit var configuration: T

    override fun initialization(configuration: JsonConfiguration): Boolean {
        this.configuration = configuration.fromTransformer(this, clazz)
        return this.configuration.enabled
    }

    override fun getConfiguration(): Class<T> {
        return clazz
    }

    override fun getName(): String {
        return name
    }
}
