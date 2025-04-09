package dev.sharkuscator.obfuscator.transformers.obfuscators.renamers

import dev.sharkuscator.obfuscator.configuration.GsonConfiguration
import dev.sharkuscator.obfuscator.configuration.transformers.RenameConfiguration
import dev.sharkuscator.obfuscator.dictionaries.DictionaryFactory
import dev.sharkuscator.obfuscator.dictionaries.MappingDictionary
import dev.sharkuscator.obfuscator.transformers.AbstractTransformer
import dev.sharkuscator.obfuscator.transformers.events.assembling.ResourceWriteEvent
import dev.sharkuscator.obfuscator.transformers.events.transforming.MethodTransformEvent
import dev.sharkuscator.obfuscator.utilities.BytecodeAssembler
import meteordevelopment.orbit.EventHandler

class ResourceRenameTransformer : AbstractTransformer<RenameConfiguration>("ResourceRename", RenameConfiguration::class.java) {
    private val mappings = mutableMapOf<String, String>()
    private lateinit var dictionary: MappingDictionary

    override fun initialization(configuration: GsonConfiguration): RenameConfiguration {
        dictionary = DictionaryFactory.forName(super.initialization(configuration).dictionary)
        return this.configuration
    }

    @EventHandler
    private fun onResourceWrite(event: ResourceWriteEvent) {
        event.name = mappings["/${event.name}"] ?: event.name
    }

    @EventHandler
    private fun onMethodTransform(event: MethodTransformEvent) {
        val methodNode = event.eventNode.node
        if (event.eventNode.isNative || event.eventNode.isAbstract || methodNode.instructions == null) {
            return
        }

        BytecodeAssembler.findNonEmptyStrings(methodNode.instructions).forEach { (instruction, value) ->
            if (event.context.jarContents.resourceContents.any { it.name == value }) {
                if (!mappings.containsKey(value)) {
                    mappings[value] = "${configuration.prefix}${dictionary.nextString()}"
                }
                instruction.cst = "/${mappings[value]}"
            }
        }
    }
}
