package dev.sharkuscator.obfuscator.transformers.shrinkers

import dev.sharkuscator.obfuscator.configuration.transformers.TransformerConfiguration
import dev.sharkuscator.obfuscator.transformers.AbstractTransformer
import dev.sharkuscator.obfuscator.transformers.events.transform.KlassTransformEvent
import meteordevelopment.orbit.EventHandler

class SourceStripperTransformer : AbstractTransformer<TransformerConfiguration>("SourceStripper", TransformerConfiguration::class.java) {
    @EventHandler
    private fun onKlassTransform(event: KlassTransformEvent) {
        event.eventNode.node.sourceDebug = null
        event.eventNode.node.sourceFile = null
    }
}
