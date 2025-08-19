package dev.sharkuscator.obfuscator.transformers.obfuscators.renamers.miscellaneous

import dev.sharkuscator.obfuscator.configuration.GsonConfiguration
import dev.sharkuscator.obfuscator.configuration.transformers.RenameConfiguration
import dev.sharkuscator.obfuscator.dictionaries.DictionaryFactory
import dev.sharkuscator.obfuscator.dictionaries.MappingDictionary
import dev.sharkuscator.obfuscator.events.TransformerEvents
import dev.sharkuscator.obfuscator.transformers.BaseTransformer
import dev.sharkuscator.obfuscator.transformers.TransformerPriority
import dev.sharkuscator.obfuscator.transformers.TransformerStrength
import meteordevelopment.orbit.EventHandler
import org.objectweb.asm.tree.MethodNode

object ParameterRenameTransformer : BaseTransformer<RenameConfiguration>("ParameterRename", RenameConfiguration::class.java) {
    lateinit var parameterMappingDictionary: MappingDictionary<MethodNode>

    override fun initialization(configuration: GsonConfiguration): RenameConfiguration {
        parameterMappingDictionary = DictionaryFactory.createDictionary(super.initialization(configuration).dictionary)
        return this.configuration
    }

    @EventHandler
    @Suppress("unused")
    private fun onMethodTransformer(event: TransformerEvents.MethodTransformEvent) {
        if (!isEligibleForExecution() || !shouldTransformMethod(event.obfuscationContext, event.nodeObject)) {
            return
        }

        event.nodeObject.parameters?.forEach { it.name = parameterMappingDictionary.generateNextName(event.nodeObject) }
    }

    override fun transformerStrength(): TransformerStrength {
        return TransformerStrength.LIGHT
    }

    override fun executionPriority(): Int {
        return TransformerPriority.FIFTY
    }
}
