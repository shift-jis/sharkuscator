package dev.sharkuscator.obfuscator.transformers.shrinkers

import dev.sharkuscator.obfuscator.configuration.transformers.TransformerConfiguration
import dev.sharkuscator.obfuscator.events.TransformerEvents
import dev.sharkuscator.obfuscator.transformers.BaseTransformer
import dev.sharkuscator.obfuscator.transformers.TransformerStrength
import meteordevelopment.orbit.EventHandler

object KotlinMetadataRemoveTransformer : BaseTransformer<TransformerConfiguration>("KotlinMetadataRemove", TransformerConfiguration::class.java) {
    @EventHandler
    private fun onClassTransform(event: TransformerEvents.ClassTransformEvent) {
        if (event.anytypeNode.node.invisibleAnnotations != null) {
            event.anytypeNode.node.invisibleAnnotations.removeIf { it.desc == "Lkotlin/jvm/internal/SourceDebugExtension;" }
        }

        if (event.anytypeNode.node.visibleAnnotations != null) {
            event.anytypeNode.node.visibleAnnotations.removeIf { it.desc == "Lkotlin/Metadata;" }
        }
    }

    override fun transformerStrength(): TransformerStrength {
        return TransformerStrength.LIGHT
    }
}