package dev.sharkuscator.obfuscator.transformers.obfuscators.constants

import dev.sharkuscator.obfuscator.configuration.transformers.TransformerConfiguration
import dev.sharkuscator.obfuscator.events.TransformerEvents
import dev.sharkuscator.obfuscator.transformers.BaseTransformer
import dev.sharkuscator.obfuscator.transformers.TransformerPriority
import dev.sharkuscator.obfuscator.transformers.obfuscators.constants.strategies.FloatingPointFromBitsStrategy
import dev.sharkuscator.obfuscator.utilities.BytecodeUtils.findNumericConstants
import meteordevelopment.orbit.EventHandler

class NumberComplexityTransformer : BaseTransformer<TransformerConfiguration>("NumberComplexity", TransformerConfiguration::class.java) {
    private val obfuscationStrategy = FloatingPointFromBitsStrategy()

    @EventHandler
    @Suppress("unused")
    private fun onMethodTransform(event: TransformerEvents.MethodTransformEvent) {
        val methodNode = event.anytypeNode.node
        if (transformed || event.anytypeNode.isNative || event.anytypeNode.isAbstract || methodNode.instructions == null) {
            return
        }

        findNumericConstants(methodNode.instructions).forEach { (instruction, value) ->
            obfuscationStrategy.replaceInstructions(event.anytypeNode.owner, methodNode.instructions, instruction, value)
        }
    }

    override fun getExecutionPriority(): Int {
        return TransformerPriority.SEVENTY_FIVE
    }
}
