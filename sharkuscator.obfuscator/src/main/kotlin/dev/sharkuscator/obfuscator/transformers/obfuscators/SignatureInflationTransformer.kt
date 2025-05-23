package dev.sharkuscator.obfuscator.transformers.obfuscators

import dev.sharkuscator.obfuscator.configuration.transformers.TransformerConfiguration
import dev.sharkuscator.obfuscator.events.TransformerEvents
import dev.sharkuscator.obfuscator.transformers.BaseTransformer
import meteordevelopment.orbit.EventHandler

class SignatureInflationTransformer : BaseTransformer<TransformerConfiguration>("SignatureInflation", TransformerConfiguration::class.java) {
    private val inflationUnitString = "What are you looking at?\n"

    @EventHandler
    @Suppress("unused")
    private fun onMethodTransform(event: TransformerEvents.MethodTransformEvent) {
        if (transformed || event.anytypeNode.isNative/* || event.anytypeNode.isStaticInitializer() || event.anytypeNode.isConstructor()*/) {
            return
        }
        event.anytypeNode.node.signature = "L${buildString { repeat(Short.MAX_VALUE / inflationUnitString.length) { append(inflationUnitString) } }};"
    }
}
