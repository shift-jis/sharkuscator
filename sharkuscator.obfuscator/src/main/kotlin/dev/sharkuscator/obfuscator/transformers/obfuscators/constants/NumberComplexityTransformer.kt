package dev.sharkuscator.obfuscator.transformers.obfuscators.constants

import dev.sharkuscator.obfuscator.configuration.transformers.TransformerConfiguration
import dev.sharkuscator.obfuscator.events.transforming.MethodTransformEvent
import dev.sharkuscator.obfuscator.transformers.AbstractTransformer
import dev.sharkuscator.obfuscator.transformers.TransformerPriority
import dev.sharkuscator.obfuscator.transformers.obfuscators.constants.strategies.FloatingPointFromBitsStrategy
import dev.sharkuscator.obfuscator.utilities.BytecodeUtils
import meteordevelopment.orbit.EventHandler

class NumberComplexityTransformer : AbstractTransformer<TransformerConfiguration>("NumberComplexity", TransformerConfiguration::class.java) {
    private val obfuscationStrategy = FloatingPointFromBitsStrategy()

    @EventHandler
    @Suppress("unused")
    private fun onMethodTransform(event: MethodTransformEvent) {
        val methodNode = event.eventNode.node
        if (transformed || event.eventNode.isNative || event.eventNode.isAbstract || methodNode.instructions == null) {
            return
        }

        BytecodeUtils.findNumericConstants(methodNode.instructions).forEach { (instruction, value) ->
            obfuscationStrategy.replaceInstructions(event.eventNode.owner, methodNode.instructions, instruction, value)
        }
    }

    override fun getPriority(): Int {
        return TransformerPriority.LOW
    }
}
