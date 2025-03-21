package dev.sharkuscator.obfuscator.transformers.obfuscators.remaing

import dev.sharkuscator.obfuscator.SharedInstances
import dev.sharkuscator.obfuscator.configuration.transformers.RenamingConfiguration
import dev.sharkuscator.obfuscator.dictionaries.AlphabetDictionary
import dev.sharkuscator.obfuscator.extensions.fullyName
import dev.sharkuscator.obfuscator.transformers.AbstractTransformer
import dev.sharkuscator.obfuscator.transformers.events.transform.FieldTransformEvent
import meteordevelopment.orbit.EventHandler

class FieldRenamingTransformer : AbstractTransformer<RenamingConfiguration>("FieldRenaming", RenamingConfiguration::class.java) {
    private val badInterfaces = listOf("com.sun.jna.*".toRegex())
    private val dictionary = AlphabetDictionary()

    @EventHandler
    private fun onFieldTransform(event: FieldTransformEvent) {
        if (badInterfaces.any { it.matches(event.eventNode.owner.node.superName) }) {
            return
        }
        SharedInstances.remapper.setMapping(event.eventNode.fullyName(), "${configuration.prefix}${dictionary.nextString()}")
    }
}
