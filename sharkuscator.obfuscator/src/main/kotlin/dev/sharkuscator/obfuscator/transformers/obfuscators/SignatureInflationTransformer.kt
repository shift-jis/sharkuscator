package dev.sharkuscator.obfuscator.transformers.obfuscators

import dev.sharkuscator.obfuscator.configuration.transformers.TransformerConfiguration
import dev.sharkuscator.obfuscator.events.TransformerEvents
import dev.sharkuscator.obfuscator.extensions.isDeclaredVolatile
import dev.sharkuscator.obfuscator.transformers.BaseTransformer
import meteordevelopment.orbit.EventHandler

object SignatureInflationTransformer : BaseTransformer<TransformerConfiguration>("SignatureInflation", TransformerConfiguration::class.java) {
    private val inflationUnitString = "¯\\_(ツ)_/¯\n"

    @EventHandler
    @Suppress("unused")
    private fun onMethodTransform(event: TransformerEvents.MethodTransformEvent) {
        if (transformed || exclusions.excluded(event.anytypeNode) || event.anytypeNode.isNative) {
            return
        }
        event.anytypeNode.node.signature = "L${buildString { repeat(Short.MAX_VALUE / inflationUnitString.length) { append(inflationUnitString) } }};"
    }

    @EventHandler
    @Suppress("unused")
    private fun onFieldTransform(event: TransformerEvents.FieldTransformEvent) {
        if (transformed || exclusions.excluded(event.anytypeNode) || event.anytypeNode.isDeclaredVolatile()) {
            return
        }
        event.anytypeNode.node.signature = "L${buildString { repeat(Short.MAX_VALUE / inflationUnitString.length) { append(inflationUnitString) } }};"
    }
}
