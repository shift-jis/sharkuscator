package dev.sharkuscator.obfuscator.transformers.obfuscators.constants

import dev.sharkuscator.obfuscator.configuration.transformers.TransformerConfiguration
import dev.sharkuscator.obfuscator.events.TransformerEvents
import dev.sharkuscator.obfuscator.transformers.BaseTransformer
import dev.sharkuscator.obfuscator.transformers.TransformerPriority
import dev.sharkuscator.obfuscator.transformers.TransformerStrength
import dev.sharkuscator.obfuscator.transformers.obfuscators.constants.strategies.CombineNumberObfuscationStrategy
import meteordevelopment.orbit.EventHandler

object NumberComplexityTransformer : BaseTransformer<TransformerConfiguration>("NumberComplexity", TransformerConfiguration::class.java) {
    private val obfuscationStrategy = CombineNumberObfuscationStrategy()

    @EventHandler
    @Suppress("unused")
    private fun onMethodTransform(event: TransformerEvents.MethodTransformEvent) {
//        val methodNode = event.anytypeNode.node
//        if (!isEligibleForExecution() || !shouldTransformMethod(event.obfuscationContext, event.anytypeNode) || event.anytypeNode.isAbstract || methodNode.instructions == null) {
//            return
//        }
//
//        findNumericConstants(methodNode.instructions).forEach { (instruction, value) ->
//            obfuscationStrategy.replaceInstructions(event.anytypeNode.owner, methodNode.instructions, instruction, value)
//        }
    }

    override fun transformerStrength(): TransformerStrength {
        return TransformerStrength.MODERATE
    }

    override fun executionPriority(): Int {
        return TransformerPriority.SEVENTY_FIVE
    }
}
