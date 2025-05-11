package dev.sharkuscator.obfuscator.transformers.shrinkers

import dev.sharkuscator.obfuscator.configuration.transformers.TransformerConfiguration
import dev.sharkuscator.obfuscator.events.TransformerEvents
import dev.sharkuscator.obfuscator.transformers.BaseTransformer
import meteordevelopment.orbit.EventHandler

class SourceStripperTransformer : BaseTransformer<TransformerConfiguration>("SourceStripper", TransformerConfiguration::class.java) {
    @EventHandler
    @Suppress("unused")
    private fun onClassTransform(event: TransformerEvents.ClassTransformEvent) {
        if (event.eventNode.node.sourceDebug == null || event.eventNode.node.sourceFile == null) {
            return
        }
        event.eventNode.node.sourceDebug = null
        event.eventNode.node.sourceFile = "¯\\_(ツ)_/¯"
    }
}
