package dev.sharkuscator.obfuscator.transformers.obfuscators.remaing

import dev.sharkuscator.obfuscator.SharedInstances
import dev.sharkuscator.obfuscator.configuration.GsonConfiguration
import dev.sharkuscator.obfuscator.configuration.transformers.RenameConfiguration
import dev.sharkuscator.obfuscator.dictionaries.DictionaryFactory
import dev.sharkuscator.obfuscator.dictionaries.MappingDictionary
import dev.sharkuscator.obfuscator.extensions.fullyName
import dev.sharkuscator.obfuscator.transformers.AbstractTransformer
import dev.sharkuscator.obfuscator.transformers.events.transforming.FieldTransformEvent
import meteordevelopment.orbit.EventHandler

class FieldRenameTransformer : AbstractTransformer<RenameConfiguration>("FieldRename", RenameConfiguration::class.java) {
    private val badInterfaces = listOf("com.sun.jna.*".toRegex())
    private lateinit var dictionary: MappingDictionary

    override fun initialization(configuration: GsonConfiguration): RenameConfiguration {
        dictionary = DictionaryFactory.forName(super.initialization(configuration).dictionary)
        return this.configuration
    }

    @EventHandler
    private fun onFieldTransform(event: FieldTransformEvent) {
        if (badInterfaces.any { it.matches(event.eventNode.owner.node.superName) }) {
            return
        }
        SharedInstances.classRemapper.setMapping(event.eventNode.fullyName(), "${configuration.prefix}${dictionary.nextString()}")
    }
}
