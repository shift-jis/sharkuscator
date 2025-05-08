package dev.sharkuscator.obfuscator.transformers.obfuscators.renamers

import dev.sharkuscator.obfuscator.configuration.GsonConfiguration
import dev.sharkuscator.obfuscator.configuration.transformers.RenameConfiguration
import dev.sharkuscator.obfuscator.dictionaries.DictionaryFactory
import dev.sharkuscator.obfuscator.dictionaries.MappingDictionary
import dev.sharkuscator.obfuscator.events.AssemblerEvents
import dev.sharkuscator.obfuscator.events.TransformerEvents
import dev.sharkuscator.obfuscator.transformers.AbstractTransformer
import dev.sharkuscator.obfuscator.transformers.TransformerPriority
import dev.sharkuscator.obfuscator.utilities.BytecodeUtils
import meteordevelopment.orbit.EventHandler

class ResourceRenameTransformer : AbstractTransformer<RenameConfiguration>("ResourceRename", RenameConfiguration::class.java) {
    private val mappings = mutableMapOf<String, String>()
    private lateinit var dictionary: MappingDictionary<String>

    override fun initialization(configuration: GsonConfiguration): RenameConfiguration {
        dictionary = DictionaryFactory.createDictionary(super.initialization(configuration).dictionary)
        return this.configuration
    }

    @EventHandler
    @Suppress("unused")
    private fun onResourceWrite(event: AssemblerEvents.ResourceWriteEvent) {
        event.name = mappings["/${event.name}"] ?: event.name
    }

    @EventHandler
    @Suppress("unused")
    private fun onMethodTransform(event: TransformerEvents.MethodTransformEvent) {
        val methodNode = event.eventNode.node
        if (transformed || event.eventNode.isNative || event.eventNode.isAbstract || methodNode.instructions == null) {
            return
        }

        BytecodeUtils.findNonEmptyStrings(methodNode.instructions).forEach { (instruction, string) ->
            if (event.context.jarContents.resourceContents.any { it.name == string }) {
                if (!mappings.containsKey(string)) {
                    mappings[string] = "${configuration.prefix}${dictionary.generateNextName(null)}"
                }
                instruction.cst = "/${mappings[string]}"
            }
        }
    }

    override fun getPriority(): Int {
        return TransformerPriority.BELOW_MEDIUM
    }
}
