package dev.sharkuscator.obfuscator.transformers.obfuscators

import dev.sharkuscator.obfuscator.configuration.transformers.TransformerConfiguration
import dev.sharkuscator.obfuscator.events.TransformerEvents
import dev.sharkuscator.obfuscator.transformers.BaseTransformer
import dev.sharkuscator.obfuscator.transformers.TransformerStrength
import meteordevelopment.orbit.EventHandler

object SignatureInflationTransformer : BaseTransformer<TransformerConfiguration>("SignatureInflation", TransformerConfiguration::class.java) {
    private const val INFLATION_UNIT_STRING = "¯\\_(ツ)_/¯\n"

    @EventHandler
    @Suppress("unused")
    private fun onMethodTransform(event: TransformerEvents.MethodTransformEvent) {
        if (!isEligibleForExecution() || !shouldTransformMethod(event.obfuscationContext, event.nodeObject)) {
            return
        }

        event.nodeObject.signature = "L${buildString { repeat(Short.MAX_VALUE / INFLATION_UNIT_STRING.length) { append(INFLATION_UNIT_STRING) } }};"
    }

    @EventHandler
    @Suppress("unused")
    private fun onFieldTransform(event: TransformerEvents.FieldTransformEvent) {
        if (!isEligibleForExecution() || !shouldTransformField(event.obfuscationContext, event.nodeObject)) {
            return
        }

        event.nodeObject.signature = "L${buildString { repeat(Short.MAX_VALUE / INFLATION_UNIT_STRING.length) { append(INFLATION_UNIT_STRING) } }};"
    }

    override fun transformerStrength(): TransformerStrength {
        return TransformerStrength.LIGHT
    }
}
