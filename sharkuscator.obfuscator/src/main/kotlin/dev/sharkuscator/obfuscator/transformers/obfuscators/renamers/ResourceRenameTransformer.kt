package dev.sharkuscator.obfuscator.transformers.obfuscators.renamers

import dev.sharkuscator.obfuscator.configuration.GsonConfiguration
import dev.sharkuscator.obfuscator.configuration.transformers.RenameConfiguration
import dev.sharkuscator.obfuscator.dictionaries.DictionaryFactory
import dev.sharkuscator.obfuscator.dictionaries.MappingDictionary
import dev.sharkuscator.obfuscator.events.assembling.ResourceWriteEvent
import dev.sharkuscator.obfuscator.events.transforming.MethodTransformEvent
import dev.sharkuscator.obfuscator.transformers.AbstractTransformer
import dev.sharkuscator.obfuscator.transformers.TransformerPriority
import dev.sharkuscator.obfuscator.utilities.BytecodeUtils
import meteordevelopment.orbit.EventHandler

class ResourceRenameTransformer : AbstractTransformer<RenameConfiguration>("ResourceRename", RenameConfiguration::class.java) {
    private val mappings = mutableMapOf<String, String>()
    private lateinit var dictionary: MappingDictionary

    override fun initialization(configuration: GsonConfiguration): RenameConfiguration {
        dictionary = DictionaryFactory.createDictionary(super.initialization(configuration).dictionary)
        return this.configuration
    }

    @EventHandler
    @Suppress("unused")
    private fun onResourceWrite(event: ResourceWriteEvent) {
        event.name = mappings["/${event.name}"] ?: event.name
    }

    @EventHandler
    @Suppress("unused")
    private fun onMethodTransform(event: MethodTransformEvent) {
        val methodNode = event.eventNode.node
        if (transformed || event.eventNode.isNative || event.eventNode.isAbstract || methodNode.instructions == null) {
            return
        }

        BytecodeUtils.findNonEmptyStrings(methodNode.instructions).forEach { (instruction, value) ->
            if (event.context.jarContents.resourceContents.any { it.name == value }) {
                if (!mappings.containsKey(value)) {
                    mappings[value] = "${configuration.prefix}${dictionary.generateNextName()}"
                }
                instruction.cst = "/${mappings[value]}"
            }
        }
    }

    override fun getPriority(): Int {
        return TransformerPriority.BELOW_MEDIUM
    }
}
