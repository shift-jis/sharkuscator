package dev.sharkuscator.obfuscator.transformers.obfuscators.renamers

import dev.sharkuscator.obfuscator.configuration.GsonConfiguration
import dev.sharkuscator.obfuscator.configuration.transformers.RenameConfiguration
import dev.sharkuscator.obfuscator.dictionaries.DictionaryFactory
import dev.sharkuscator.obfuscator.dictionaries.MappingDictionary
import dev.sharkuscator.obfuscator.events.AssemblerEvents
import dev.sharkuscator.obfuscator.events.TransformerEvents
import dev.sharkuscator.obfuscator.transformers.BaseTransformer
import dev.sharkuscator.obfuscator.transformers.TransformerPriority
import dev.sharkuscator.obfuscator.transformers.TransformerStrength
import dev.sharkuscator.obfuscator.utilities.AssemblyHelper.findNonEmptyStrings
import meteordevelopment.orbit.EventHandler

object ResourceRenameTransformer : BaseTransformer<RenameConfiguration>("ResourceRename", RenameConfiguration::class.java) {
    private val resourceNameMappings = mutableMapOf<String, String>()
    private lateinit var resourceMappingDictionary: MappingDictionary<String>

    override fun initialization(configuration: GsonConfiguration): RenameConfiguration {
        resourceMappingDictionary = DictionaryFactory.createDictionary(super.initialization(configuration).dictionary)
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
        val methodNode = event.anytypeNode.node
        if (!isEligibleForExecution() || exclusions.excluded(event.anytypeNode) || event.anytypeNode.isNative || event.anytypeNode.isAbstract || methodNode.instructions == null) {
            return
        }

        findNonEmptyStrings(methodNode.instructions).forEach { (instruction, string) ->
            if (event.context.jarContents.resourceContents.any { it.name == string }) {
                if (!resourceNameMappings.containsKey(string)) {
                    resourceNameMappings[string] = "${configuration.namePrefix}${resourceMappingDictionary.generateNextName(null)}"
                }

                val leadingSlashIfPresent = if (string.startsWith("/")) "/" else ""
                instruction.cst = "$leadingSlashIfPresent${resourceNameMappings[string]}"
            }
        }
    }

    override fun transformerStrength(): TransformerStrength {
        return TransformerStrength.MODERATE
    }

    override fun executionPriority(): Int {
        return TransformerPriority.TWENTY_FIVE
    }
}
