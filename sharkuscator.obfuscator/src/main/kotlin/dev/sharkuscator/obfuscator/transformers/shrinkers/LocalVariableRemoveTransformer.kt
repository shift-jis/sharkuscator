package dev.sharkuscator.obfuscator.transformers.shrinkers

import dev.sharkuscator.obfuscator.configuration.transformers.TransformerConfiguration
import dev.sharkuscator.obfuscator.events.TransformerEvents
import dev.sharkuscator.obfuscator.transformers.BaseTransformer
import meteordevelopment.orbit.EventHandler

class LocalVariableRemoveTransformer : BaseTransformer<TransformerConfiguration>("LocalVariableRemove", TransformerConfiguration::class.java) {
    @EventHandler
    @Suppress("unused")
    private fun onMethodTransform(event: TransformerEvents.MethodTransformEvent) {
        event.eventNode.node.localVariables = null
    }
}
