package dev.sharkuscator.obfuscator.transformers.obfuscators.remaing

import dev.sharkuscator.obfuscator.configuration.GsonConfiguration
import dev.sharkuscator.obfuscator.configuration.transformers.RenameConfiguration
import dev.sharkuscator.obfuscator.dictionaries.DictionaryFactory
import dev.sharkuscator.obfuscator.dictionaries.MappingDictionary
import dev.sharkuscator.obfuscator.transformers.AbstractTransformer
import dev.sharkuscator.obfuscator.transformers.events.assembling.ResourceWriteEvent
import dev.sharkuscator.obfuscator.transformers.events.transforming.MethodTransformEvent
import meteordevelopment.orbit.EventHandler
import org.objectweb.asm.tree.LdcInsnNode

class ResourceRenameTransformer : AbstractTransformer<RenameConfiguration>("ResourceRename", RenameConfiguration::class.java) {
    private val mappings = mutableMapOf<String, String>()
    private lateinit var dictionary: MappingDictionary

    override fun initialization(configuration: GsonConfiguration): RenameConfiguration {
        dictionary = DictionaryFactory.forName(super.initialization(configuration).dictionary)
        return this.configuration
    }

    @EventHandler
    private fun onResourceWrite(event: ResourceWriteEvent) {
        event.name = mappings[event.name] ?: event.name
    }

    @EventHandler
    private fun onMethodTransform(event: MethodTransformEvent) {
        val methodNode = event.eventNode.node
        if (event.eventNode.isNative || event.eventNode.isAbstract || methodNode.instructions == null) {
            return
        }

        methodNode.instructions.filterIsInstance<LdcInsnNode>().filter { it.cst is String && (it.cst as String).isNotEmpty() }.forEach { instruction ->
            val constantValue = (instruction.cst as String).substring(1)
            if (event.context.jarContents.resourceContents.any { it.name == constantValue }) {
                if (!mappings.containsKey(constantValue)) {
                    mappings[constantValue] = "${configuration.prefix}${dictionary.nextString()}"
                }
                instruction.cst = mappings[constantValue]
            }
        }
    }
}
