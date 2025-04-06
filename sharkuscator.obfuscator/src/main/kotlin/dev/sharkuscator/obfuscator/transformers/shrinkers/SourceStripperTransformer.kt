package dev.sharkuscator.obfuscator.transformers.shrinkers

import dev.sharkuscator.obfuscator.configuration.transformers.TransformerConfiguration
import dev.sharkuscator.obfuscator.transformers.AbstractTransformer
import dev.sharkuscator.obfuscator.transformers.events.transforming.ClassTransformEvent
import meteordevelopment.orbit.EventHandler

class SourceStripperTransformer : AbstractTransformer<TransformerConfiguration>("SourceStripper", TransformerConfiguration::class.java) {
    @EventHandler
    private fun onClassTransform(event: ClassTransformEvent) {
        event.eventNode.node.sourceDebug = null
        event.eventNode.node.sourceFile = null
    }
}
