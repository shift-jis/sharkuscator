package dev.sharkuscator.obfuscator.transformers.obfuscators.constants

import dev.sharkuscator.obfuscator.configuration.transformers.TransformerConfiguration
import dev.sharkuscator.obfuscator.events.TransformerEvents
import dev.sharkuscator.obfuscator.transformers.BaseTransformer
import dev.sharkuscator.obfuscator.transformers.TransformerPriority
import dev.sharkuscator.obfuscator.transformers.obfuscators.constants.strategies.EncodedNumericConstantStrategy
import dev.sharkuscator.obfuscator.utilities.AssemblyHelper.findNumericConstants
import meteordevelopment.orbit.EventHandler

object NumberComplexityTransformer : BaseTransformer<TransformerConfiguration>("NumberComplexity", TransformerConfiguration::class.java) {
    private val obfuscationStrategy = EncodedNumericConstantStrategy()

    @EventHandler
    @Suppress("unused")
    private fun onMethodTransform(event: TransformerEvents.MethodTransformEvent) {
        val methodNode = event.anytypeNode.node
        if (!isEligibleForExecution() || exclusions.excluded(event.anytypeNode) || event.anytypeNode.isNative || event.anytypeNode.isAbstract || methodNode.instructions == null) {
            return
        }

        findNumericConstants(methodNode.instructions).forEach { (instruction, value) ->
            obfuscationStrategy.replaceInstructions(event.context, event.anytypeNode.owner, methodNode.instructions, instruction, value)
        }
    }

    override fun getExecutionPriority(): Int {
        return TransformerPriority.SEVENTY_FIVE
    }
}
