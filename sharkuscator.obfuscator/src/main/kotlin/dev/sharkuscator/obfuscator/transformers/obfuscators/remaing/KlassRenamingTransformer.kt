package dev.sharkuscator.obfuscator.transformers.obfuscators.remaing

import dev.sharkuscator.obfuscator.SharedInstances
import dev.sharkuscator.obfuscator.configuration.GsonConfiguration
import dev.sharkuscator.obfuscator.configuration.transformers.RenamingConfiguration
import dev.sharkuscator.obfuscator.dictionaries.AlphabetDictionary
import dev.sharkuscator.obfuscator.dictionaries.DictionaryFactory
import dev.sharkuscator.obfuscator.dictionaries.MappingDictionary
import dev.sharkuscator.obfuscator.extensions.isAnnotation
import dev.sharkuscator.obfuscator.transformers.AbstractTransformer
import dev.sharkuscator.obfuscator.transformers.events.assemble.ResourceWriteEvent
import dev.sharkuscator.obfuscator.transformers.events.transform.KlassTransformEvent
import meteordevelopment.orbit.EventHandler

class KlassRenamingTransformer : AbstractTransformer<RenamingConfiguration>("ClassRenaming", RenamingConfiguration::class.java) {
    private lateinit var dictionary: MappingDictionary

    override fun initialization(configuration: GsonConfiguration): RenamingConfiguration {
        dictionary = DictionaryFactory.forName(super.initialization(configuration).dictionary)
        return this.configuration
    }

    @EventHandler
    private fun onKlassTransform(event: KlassTransformEvent) {
        if (event.eventNode.isAnnotation() || event.context.classSource.isLibraryClass(event.eventNode.name)) {
            return
        }
        SharedInstances.klassRemapper.setMapping(event.eventNode.name, "${configuration.prefix}${dictionary.nextString()}")
    }

    @EventHandler
    private fun onResourceWrite(event: ResourceWriteEvent) {
        val decodedManifest = event.resourceData.decodeToString()
        if (event.name != "META-INF/MANIFEST.MF" && !decodedManifest.contains("Main-Class")) {
            return
        }

        SharedInstances.klassRemapper.mappings.filter { !it.key.contains(".") }.forEach { (previous, newest) ->
            val mainClassRegex = "(?<=[: ])${previous.replace("/", ".")}".toRegex()
            event.resourceData = mainClassRegex.replace(event.resourceData.decodeToString(), newest).toByteArray()
        }
    }
}
