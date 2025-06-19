package dev.sharkuscator.obfuscator.transformers

import dev.sharkuscator.obfuscator.configuration.GsonConfiguration
import dev.sharkuscator.obfuscator.configuration.exclusions.AnnotationExclusionRule
import dev.sharkuscator.obfuscator.configuration.exclusions.ExclusionRule
import dev.sharkuscator.obfuscator.configuration.exclusions.MixedExclusionRule
import dev.sharkuscator.obfuscator.configuration.exclusions.StringExclusionRule
import dev.sharkuscator.obfuscator.configuration.transformers.TransformerConfiguration

abstract class BaseTransformer<T : TransformerConfiguration>(
    private val transformerName: String,
    private val configurationClass: Class<T>
) : SharkTransformer<T> {
    lateinit var configuration: T
    lateinit var exclusions: ExclusionRule
    var transformed = false

    override fun initialization(configuration: GsonConfiguration): T {
        this.configuration = configuration.fromTransformer(this, configurationClass)
        this.exclusions = MixedExclusionRule(buildList {
            addAll(this@BaseTransformer.configuration.exclusions.map {
                StringExclusionRule(it.replace("**", ".*").replace("/", "\\/").toRegex())
            })
            add(AnnotationExclusionRule())
        })
        return this.configuration
    }

    override fun isEligibleForExecution(): Boolean {
        return configuration.enabled && !transformed
    }

    override fun getExecutionPriority(): Int {
        return TransformerPriority.ZERO
    }

    override fun getTransformerName(): String {
        return transformerName
    }
}
