package dev.sharkuscator.obfuscator.transformers.shrinkers

import dev.sharkuscator.obfuscator.configuration.transformers.TransformerConfiguration
import dev.sharkuscator.obfuscator.events.TransformerEvents
import dev.sharkuscator.obfuscator.transformers.BaseTransformer
import dev.sharkuscator.obfuscator.transformers.TransformerStrength
import meteordevelopment.orbit.EventHandler

object SourceStripperTransformer : BaseTransformer<TransformerConfiguration>("SourceStripper", TransformerConfiguration::class.java) {
    @EventHandler
    @Suppress("unused")
    private fun onClassTransform(event: TransformerEvents.ClassTransformEvent) {
        if (!shouldTransformClass(event.obfuscationContext, event.nodeObject)) {
            return
        }

        event.nodeObject.sourceDebug = null
        event.nodeObject.sourceFile = "¯\\_(ツ)_/¯"
    }

    override fun transformerStrength(): TransformerStrength {
        return TransformerStrength.LIGHT
    }
}
