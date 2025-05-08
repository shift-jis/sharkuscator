package dev.sharkuscator.obfuscator.transformers.shrinkers

import dev.sharkuscator.obfuscator.configuration.transformers.TransformerConfiguration
import dev.sharkuscator.obfuscator.events.TransformerEvents
import dev.sharkuscator.obfuscator.extensions.isStaticInitializer
import dev.sharkuscator.obfuscator.transformers.AbstractTransformer
import meteordevelopment.orbit.EventHandler

class LocalVariableRemoveTransformer : AbstractTransformer<TransformerConfiguration>("LocalVariableRemove", TransformerConfiguration::class.java) {
    @EventHandler
    @Suppress("unused")
    private fun onMethodTransform(event: TransformerEvents.MethodTransformEvent) {
        if (event.eventNode.isNative || event.eventNode.isStaticInitializer()) {
            return
        }
        event.eventNode.node.localVariables = null
    }
}
