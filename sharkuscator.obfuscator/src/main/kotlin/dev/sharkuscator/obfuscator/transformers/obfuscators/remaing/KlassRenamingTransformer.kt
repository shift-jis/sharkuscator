package dev.sharkuscator.obfuscator.transformers.obfuscators.remaing

import com.esotericsoftware.asm.Type
import dev.sharkuscator.obfuscator.SharedInstances
import dev.sharkuscator.obfuscator.configuration.transformers.RenamingConfiguration
import dev.sharkuscator.obfuscator.dictionaries.AlphabetDictionary
import dev.sharkuscator.obfuscator.extensions.fullyName
import dev.sharkuscator.obfuscator.extensions.isAnnotation
import dev.sharkuscator.obfuscator.transformers.AbstractTransformer
import dev.sharkuscator.obfuscator.transformers.events.assemble.ResourceWriteEvent
import dev.sharkuscator.obfuscator.transformers.events.transform.ClassTransformEvent
import meteordevelopment.orbit.EventHandler

class KlassRenamingTransformer : AbstractTransformer<RenamingConfiguration>("ClassRenaming", RenamingConfiguration::class.java) {
    private val dictionary = AlphabetDictionary()

    @EventHandler
    private fun onClassTransform(transformEvent: ClassTransformEvent) {
        if (transformEvent.eventNode.isAnnotation()) {
            return
        }

        SharedInstances.remapper.setMapping(
            Type.getObjectType(transformEvent.eventNode.name).internalName,
            "${configuration.prefix}/${dictionary.nextString()}",
        )
    }

    @EventHandler
    private fun onResourceWrite(writeEvent: ResourceWriteEvent) {
        val decodedManifest = writeEvent.resourceData.decodeToString()
        if (writeEvent.name != "META-INF/MANIFEST.MF" && !decodedManifest.contains("Main-Class")) {
            return
        }

        SharedInstances.remapper.mappings.forEach { (previous, newest) ->
            val mainClassRegex = "(?<=[: ])${previous.replace("/", ".")}".toRegex()
            writeEvent.resourceData = mainClassRegex.replace(writeEvent.resourceData.decodeToString(), newest).toByteArray()
        }
    }
}
