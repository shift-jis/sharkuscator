package dev.sharkuscator.obfuscator.transformers.obfuscators.renamers

import dev.sharkuscator.obfuscator.configuration.GsonConfiguration
import dev.sharkuscator.obfuscator.configuration.transformers.RenameConfiguration
import dev.sharkuscator.obfuscator.dictionaries.DictionaryFactory
import dev.sharkuscator.obfuscator.dictionaries.MappingDictionary
import dev.sharkuscator.obfuscator.events.AssemblerEvents
import dev.sharkuscator.obfuscator.events.TransformerEvents
import dev.sharkuscator.obfuscator.transformers.BaseTransformer
import dev.sharkuscator.obfuscator.transformers.TransformerPriority
import dev.sharkuscator.obfuscator.utilities.BytecodeUtils
import meteordevelopment.orbit.EventHandler

class ResourceRenameTransformer : BaseTransformer<RenameConfiguration>("ResourceRename", RenameConfiguration::class.java) {
    private val resourceNameMappings = mutableMapOf<String, String>()
    private lateinit var dictionary: MappingDictionary<String>

    override fun initialization(configuration: GsonConfiguration): RenameConfiguration {
        dictionary = DictionaryFactory.createDictionary(super.initialization(configuration).dictionary)
        return this.configuration
    }

    @EventHandler
    @Suppress("unused")
    private fun onResourceWrite(event: AssemblerEvents.ResourceWriteEvent) {
        event.name = resourceNameMappings[event.name] ?: resourceNameMappings["/${event.name}"] ?: event.name
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
                if (!resourceNameMappings.containsKey(string)) {
                    resourceNameMappings[string] = "${configuration.prefix}${dictionary.generateNextName(null)}"
                }

                val leadingSlashIfPresent = if (string.startsWith("/")) "/" else ""
                instruction.cst = "$leadingSlashIfPresent${resourceNameMappings[string]}"
            }
        }
    }

    override fun getExecutionPriority(): Int {
        return TransformerPriority.BELOW_MEDIUM
    }
}
