package dev.sharkuscator.obfuscator.transformers.shrinkers

import dev.sharkuscator.obfuscator.configuration.transformers.TransformerConfiguration
import dev.sharkuscator.obfuscator.transformers.AbstractTransformer
import dev.sharkuscator.obfuscator.transformers.events.transform.ClassTransformEvent
import meteordevelopment.orbit.EventHandler

class InnerKlassRemoveTransform : AbstractTransformer<TransformerConfiguration>("InnerClassRemove", TransformerConfiguration::class.java) {
    @EventHandler
    private fun onClassTransform(event: ClassTransformEvent) {
        if (event.eventNode.node.innerClasses != null) {
            event.eventNode.node.innerClasses = emptyList()
        }
    }
}
