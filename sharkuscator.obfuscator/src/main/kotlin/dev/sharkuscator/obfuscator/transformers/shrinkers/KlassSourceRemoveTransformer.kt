package dev.sharkuscator.obfuscator.transformers.shrinkers

import dev.sharkuscator.obfuscator.configuration.transformers.TransformerConfiguration
import dev.sharkuscator.obfuscator.transformers.AbstractTransformer
import dev.sharkuscator.obfuscator.transformers.events.transform.ClassTransformEvent
import meteordevelopment.orbit.EventHandler

class KlassSourceRemoveTransformer : AbstractTransformer<TransformerConfiguration>("ClassSourceRemove", TransformerConfiguration::class.java) {
    @EventHandler
    private fun onClassTransform(transformEvent: ClassTransformEvent) {
        transformEvent.eventNode.node.sourceDebug = null
        transformEvent.eventNode.node.sourceFile = null
    }
}
