package dev.sharkuscator.obfuscator.transformers.obfuscators.remaing

import dev.sharkuscator.obfuscator.configuration.transformers.TransformerConfiguration
import dev.sharkuscator.obfuscator.dictionaries.SimilarDictionary
import dev.sharkuscator.obfuscator.transformers.AbstractTransformer
import dev.sharkuscator.obfuscator.transformers.events.assemble.ResourceWriteEvent
import dev.sharkuscator.obfuscator.transformers.events.transform.MethodTransformEvent
import meteordevelopment.orbit.EventHandler
import org.objectweb.asm.tree.LdcInsnNode

class ResourceRenamingTransformer : AbstractTransformer<TransformerConfiguration>("ResourceRenaming", TransformerConfiguration::class.java) {
    private val mappings = mutableMapOf<String, String>()
    private val dictionary = SimilarDictionary(20)

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

        methodNode.instructions.filterIsInstance<LdcInsnNode>().filter { it.cst != null && it.cst is String && (it.cst as String).isNotEmpty() }.forEach { instruction ->
            val resourceName = (instruction.cst as String).substring(1)
            if (event.context.jarContents.resourceContents.any { it.name == resourceName }) {
                if (!mappings.containsKey(resourceName)) {
                    mappings[resourceName] = dictionary.nextString()
                }
                instruction.cst = mappings[resourceName]
            }
        }
    }
}
