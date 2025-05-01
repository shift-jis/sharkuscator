package dev.sharkuscator.obfuscator.transformers.obfuscators.renamers

import dev.sharkuscator.obfuscator.SharedInstances
import dev.sharkuscator.obfuscator.configuration.GsonConfiguration
import dev.sharkuscator.obfuscator.configuration.transformers.RenameConfiguration
import dev.sharkuscator.obfuscator.dictionaries.DictionaryFactory
import dev.sharkuscator.obfuscator.dictionaries.MappingDictionary
import dev.sharkuscator.obfuscator.extensions.isAnnotation
import dev.sharkuscator.obfuscator.extensions.isMainClass
import dev.sharkuscator.obfuscator.transformers.AbstractTransformer
import dev.sharkuscator.obfuscator.transformers.TransformerPriority
import dev.sharkuscator.obfuscator.transformers.events.assembling.ResourceWriteEvent
import dev.sharkuscator.obfuscator.transformers.events.transforming.ClassTransformEvent
import meteordevelopment.orbit.EventHandler
import meteordevelopment.orbit.EventPriority

class ClassRenameTransformer : AbstractTransformer<RenameConfiguration>("ClassRename", RenameConfiguration::class.java) {
    private val defaultDictionary = DictionaryFactory.defaultDictionary()
    lateinit var dictionary: MappingDictionary

    override fun initialization(configuration: GsonConfiguration): RenameConfiguration {
        dictionary = DictionaryFactory.forName(super.initialization(configuration).dictionary)
        return this.configuration
    }

    @EventHandler(priority = EventPriority.HIGH)
    private fun onClassTransform(event: ClassTransformEvent) {
        if (transformed || event.context.classSource.isLibraryClass(event.eventNode.name)) {
            return
        }

        var classMapping = dictionary.nextString()
        if (dictionary.isDangerous() && (event.eventNode.isMainClass() || event.eventNode.isAnnotation())) {
            classMapping = defaultDictionary.nextString()
        }

        SharedInstances.classRemapper.setMapping(event.eventNode.name, "${configuration.prefix}${classMapping}")
    }

    @EventHandler
    private fun onResourceWrite(event: ResourceWriteEvent) {
        val decodedManifest = event.resourceData.decodeToString()
        if (event.name != "META-INF/MANIFEST.MF" && !decodedManifest.contains("Main-Class")) {
            return
        }

        SharedInstances.classRemapper.mappings.filter { !it.key.contains(".") }.forEach { (previous, newest) ->
            val mainClassRegex = "(?<=[: ])${previous.replace("/", ".")}".toRegex()
            event.resourceData = mainClassRegex.replace(event.resourceData.decodeToString(), newest).toByteArray()
        }
    }

    override fun getPriority(): Int {
        return TransformerPriority.BELOW_MEDIUM
    }
}
