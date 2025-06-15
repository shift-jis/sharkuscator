package dev.sharkuscator.obfuscator.transformers.shrinkers

import dev.sharkuscator.obfuscator.configuration.transformers.TransformerConfiguration
import dev.sharkuscator.obfuscator.events.TransformerEvents
import dev.sharkuscator.obfuscator.transformers.BaseTransformer
import meteordevelopment.orbit.EventHandler

object SourceStripperTransformer : BaseTransformer<TransformerConfiguration>("SourceStripper", TransformerConfiguration::class.java) {
    @EventHandler
    @Suppress("unused")
    private fun onClassTransform(event: TransformerEvents.ClassTransformEvent) {
        event.anytypeNode.node.sourceDebug = null
        event.anytypeNode.node.sourceFile = "¯\\_(ツ)_/¯"
    }
}
