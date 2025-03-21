package dev.sharkuscator.obfuscator.transformers.shrinkers

import dev.sharkuscator.obfuscator.configuration.transformers.TransformerConfiguration
import dev.sharkuscator.obfuscator.transformers.AbstractTransformer
import dev.sharkuscator.obfuscator.transformers.events.transform.ClassTransformEvent
import meteordevelopment.orbit.EventHandler

class KlassSourceRemoveTransformer : AbstractTransformer<TransformerConfiguration>("ClassSourceRemove", TransformerConfiguration::class.java) {
    @EventHandler
    private fun onClassTransform(event: ClassTransformEvent) {
        event.eventNode.node.sourceDebug = null
        event.eventNode.node.sourceFile = null
    }
}
