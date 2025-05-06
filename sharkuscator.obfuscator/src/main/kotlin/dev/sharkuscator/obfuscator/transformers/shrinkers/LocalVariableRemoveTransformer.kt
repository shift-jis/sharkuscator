package dev.sharkuscator.obfuscator.transformers.shrinkers

import dev.sharkuscator.obfuscator.configuration.transformers.TransformerConfiguration
import dev.sharkuscator.obfuscator.events.transforming.MethodTransformEvent
import dev.sharkuscator.obfuscator.extensions.isStaticInitializer
import dev.sharkuscator.obfuscator.transformers.AbstractTransformer
import meteordevelopment.orbit.EventHandler

class LocalVariableRemoveTransformer : AbstractTransformer<TransformerConfiguration>("LocalVariableRemove", TransformerConfiguration::class.java) {
    @EventHandler
    @Suppress("unused")
    private fun onMethodTransform(event: MethodTransformEvent) {
        if (event.eventNode.node.localVariables == null || event.eventNode.isNative || event.eventNode.isStaticInitializer()) {
            return
        }
        event.eventNode.node.localVariables = null
    }
}
