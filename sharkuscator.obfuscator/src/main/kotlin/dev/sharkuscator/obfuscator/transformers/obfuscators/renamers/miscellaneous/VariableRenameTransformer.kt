package dev.sharkuscator.obfuscator.transformers.obfuscators.renamers.miscellaneous

import dev.sharkuscator.obfuscator.configuration.GsonConfiguration
import dev.sharkuscator.obfuscator.configuration.transformers.RenameConfiguration
import dev.sharkuscator.obfuscator.dictionaries.DictionaryFactory
import dev.sharkuscator.obfuscator.dictionaries.MappingDictionary
import dev.sharkuscator.obfuscator.events.TransformerEvents
import dev.sharkuscator.obfuscator.transformers.BaseTransformer
import dev.sharkuscator.obfuscator.transformers.TransformerPriority
import dev.sharkuscator.obfuscator.transformers.TransformerStrength
import dev.sharkuscator.obfuscator.transformers.shrinkers.LocalVariableRemoveTransformer
import meteordevelopment.orbit.EventHandler
import org.mapleir.asm.MethodNode

object VariableRenameTransformer : BaseTransformer<RenameConfiguration>("VariableRename", RenameConfiguration::class.java) {
    lateinit var variableMappingDictionary: MappingDictionary<MethodNode>

    override fun initialization(configuration: GsonConfiguration): RenameConfiguration {
        variableMappingDictionary = DictionaryFactory.createDictionary(super.initialization(configuration).dictionary)
        return this.configuration
    }

    @EventHandler
    @Suppress("unused")
    private fun onMethodTransformer(event: TransformerEvents.MethodTransformEvent) {
        if (!isEligibleForExecution() || !shouldTransformMethod(event.context, event.anytypeNode) || LocalVariableRemoveTransformer.configuration.enabled) {
            return
        }

        event.anytypeNode.node.localVariables?.filter { it.name != "this" }?.forEach {
            it.name = variableMappingDictionary.generateNextName(event.anytypeNode)
        }
    }

    override fun transformerStrength(): TransformerStrength {
        return TransformerStrength.LIGHT
    }

    override fun executionPriority(): Int {
        return TransformerPriority.FIFTY
    }
}
