package dev.sharkuscator.obfuscator.transformers.obfuscators.renamers.miscellaneous

import dev.sharkuscator.obfuscator.configuration.GsonConfiguration
import dev.sharkuscator.obfuscator.configuration.transformers.RenameConfiguration
import dev.sharkuscator.obfuscator.dictionaries.DictionaryFactory
import dev.sharkuscator.obfuscator.dictionaries.MappingDictionary
import dev.sharkuscator.obfuscator.events.TransformerEvents
import dev.sharkuscator.obfuscator.transformers.BaseTransformer
import dev.sharkuscator.obfuscator.transformers.TransformerPriority
import dev.sharkuscator.obfuscator.transformers.shrinkers.LocalVariableRemoveTransformer
import meteordevelopment.orbit.EventHandler
import org.mapleir.asm.MethodNode

object LocalVariableRenameTransformer : BaseTransformer<RenameConfiguration>("LocalVariableRename", RenameConfiguration::class.java) {
    lateinit var dictionary: MappingDictionary<MethodNode>

    override fun initialization(configuration: GsonConfiguration): RenameConfiguration {
        dictionary = DictionaryFactory.createDictionary(super.initialization(configuration).dictionary)
        return this.configuration
    }

    @EventHandler
    @Suppress("unused")
    private fun onMethodTransformer(event: TransformerEvents.MethodTransformEvent) {
        if (transformed || event.anytypeNode.node.localVariables == null || LocalVariableRemoveTransformer.canTransform()) {
            return
        }

        event.anytypeNode.node.localVariables.filter { it.name != "this" }.forEach {
            it.name = dictionary.generateNextName(event.anytypeNode)
        }
    }

    override fun getExecutionPriority(): Int {
        return TransformerPriority.FIFTY
    }
}
