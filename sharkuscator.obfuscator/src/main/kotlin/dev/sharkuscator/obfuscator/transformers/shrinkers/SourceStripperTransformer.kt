package dev.sharkuscator.obfuscator.transformers.shrinkers

import dev.sharkuscator.obfuscator.configuration.transformers.TransformerConfiguration
import dev.sharkuscator.obfuscator.events.transforming.ClassTransformEvent
import dev.sharkuscator.obfuscator.transformers.AbstractTransformer
import meteordevelopment.orbit.EventHandler

class SourceStripperTransformer : AbstractTransformer<TransformerConfiguration>("SourceStripper", TransformerConfiguration::class.java) {
    @EventHandler
    @Suppress("unused")
    private fun onClassTransform(event: ClassTransformEvent) {
        if (event.eventNode.node.sourceDebug == null || event.eventNode.node.sourceFile == null) {
            return
        }
        event.eventNode.node.sourceDebug = null
        event.eventNode.node.sourceFile = null
    }
}
