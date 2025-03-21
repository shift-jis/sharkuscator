package dev.sharkuscator.obfuscator.transformers.shrinkers

import dev.sharkuscator.obfuscator.configuration.transformers.TransformerConfiguration
import dev.sharkuscator.obfuscator.extensions.isClInit
import dev.sharkuscator.obfuscator.transformers.AbstractTransformer
import dev.sharkuscator.obfuscator.transformers.events.transform.MethodTransformEvent
import meteordevelopment.orbit.EventHandler

class LocalVariableRemoveTransformer : AbstractTransformer<TransformerConfiguration>("LocalVariableRemove", TransformerConfiguration::class.java) {
    @EventHandler
    private fun onMethodTransform(transformEvent: MethodTransformEvent) {
        if (transformEvent.eventNode.isNative || transformEvent.eventNode.isClInit()) {
            return
        }

        transformEvent.eventNode.node.localVariables = null
    }
}
