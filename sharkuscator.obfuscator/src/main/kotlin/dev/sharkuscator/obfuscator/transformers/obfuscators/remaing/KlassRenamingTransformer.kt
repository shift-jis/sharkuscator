package dev.sharkuscator.obfuscator.transformers.obfuscators.remaing

import com.esotericsoftware.asm.Type
import dev.sharkuscator.obfuscator.SharedInstances
import dev.sharkuscator.obfuscator.configuration.transformers.RenamingConfiguration
import dev.sharkuscator.obfuscator.dictionaries.AlphabetDictionary
import dev.sharkuscator.obfuscator.extensions.isAnnotation
import dev.sharkuscator.obfuscator.transformers.AbstractTransformer
import dev.sharkuscator.obfuscator.transformers.events.assemble.ResourceWriteEvent
import dev.sharkuscator.obfuscator.transformers.events.transform.ClassTransformEvent
import meteordevelopment.orbit.EventHandler

class KlassRenamingTransformer : AbstractTransformer<RenamingConfiguration>("ClassRenaming", RenamingConfiguration::class.java) {
    private val dictionary = AlphabetDictionary()

    @EventHandler
    private fun onClassTransform(event: ClassTransformEvent) {
        if (event.eventNode.isAnnotation() || event.eventNode.isSynthetic) {
            return
        }

        SharedInstances.remapper.setMapping(event.eventNode.name, "${configuration.prefix}/${dictionary.nextString()}")
    }

    @EventHandler
    private fun onResourceWrite(event: ResourceWriteEvent) {
        val decodedManifest = event.resourceData.decodeToString()
        if (event.name != "META-INF/MANIFEST.MF" && !decodedManifest.contains("Main-Class")) {
            return
        }

        SharedInstances.remapper.mappings.filter { !it.key.contains(".") }.forEach { (previous, newest) ->
            val mainClassRegex = "(?<=[: ])${previous.replace("/", ".")}".toRegex()
            event.resourceData = mainClassRegex.replace(event.resourceData.decodeToString(), newest).toByteArray()
        }
    }
}
