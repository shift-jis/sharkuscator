package dev.sharkuscator.obfuscator.transformers.obfuscators.renamers

import dev.sharkuscator.obfuscator.ObfuscatorServices
import dev.sharkuscator.obfuscator.configuration.GsonConfiguration
import dev.sharkuscator.obfuscator.configuration.transformers.RenameConfiguration
import dev.sharkuscator.obfuscator.dictionaries.DictionaryFactory
import dev.sharkuscator.obfuscator.dictionaries.MappingDictionary
import dev.sharkuscator.obfuscator.events.transforming.FieldTransformEvent
import dev.sharkuscator.obfuscator.extensions.getQualifiedName
import dev.sharkuscator.obfuscator.transformers.AbstractTransformer
import dev.sharkuscator.obfuscator.transformers.TransformerPriority
import meteordevelopment.orbit.EventHandler

class FieldRenameTransformer : AbstractTransformer<RenameConfiguration>("FieldRename", RenameConfiguration::class.java) {
    private val badInterfaces = listOf("com.sun.jna.*".toRegex())
    private lateinit var dictionary: MappingDictionary

    override fun initialization(configuration: GsonConfiguration): RenameConfiguration {
        dictionary = DictionaryFactory.createDictionary(super.initialization(configuration).dictionary)
        return this.configuration
    }

    @EventHandler
    @Suppress("unused")
    private fun onFieldTransform(event: FieldTransformEvent) {
        if (transformed || badInterfaces.any { it.matches(event.eventNode.owner.node.superName) }) {
            return
        }
        ObfuscatorServices.symbolRemapper.setMapping(event.eventNode.getQualifiedName(), "${configuration.prefix}${dictionary.generateNextName()}")
    }

    override fun getPriority(): Int {
        return TransformerPriority.BELOW_MEDIUM
    }
}
