package dev.sharkuscator.obfuscator.transformers.obfuscators.remaing

import com.esotericsoftware.asm.Type
import dev.sharkuscator.obfuscator.SharedInstances
import dev.sharkuscator.obfuscator.configuration.transformers.RenamingConfiguration
import dev.sharkuscator.obfuscator.dictionaries.AlphabetDictionary
import dev.sharkuscator.obfuscator.extensions.fullyName
import dev.sharkuscator.obfuscator.transformers.AbstractTransformer
import dev.sharkuscator.obfuscator.transformers.events.transform.FieldTransformEvent
import meteordevelopment.orbit.EventHandler

class FieldRenamingTransformer : AbstractTransformer<RenamingConfiguration>("FieldRenaming", RenamingConfiguration::class.java) {
    private val dictionary = AlphabetDictionary()

    @EventHandler
    private fun onFieldTransform(transformEvent: FieldTransformEvent) {
        SharedInstances.remapper.setMapping(
            Type.getObjectType(transformEvent.eventNode.fullyName()).internalName,
            "${configuration.prefix}${dictionary.nextString()}"
        )
    }
}
