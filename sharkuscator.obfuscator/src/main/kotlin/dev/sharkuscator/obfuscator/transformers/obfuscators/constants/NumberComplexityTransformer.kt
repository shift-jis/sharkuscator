package dev.sharkuscator.obfuscator.transformers.obfuscators.constants

import dev.sharkuscator.obfuscator.configuration.transformers.TransformerConfiguration
import dev.sharkuscator.obfuscator.transformers.AbstractTransformer
import dev.sharkuscator.obfuscator.transformers.TransformerPriority
import dev.sharkuscator.obfuscator.transformers.events.transforming.MethodTransformEvent
import dev.sharkuscator.obfuscator.transformers.obfuscators.constants.strategy.NormalNumberComplexity
import dev.sharkuscator.obfuscator.utilities.BytecodeAssembler
import meteordevelopment.orbit.EventHandler

class NumberComplexityTransformer : AbstractTransformer<TransformerConfiguration>("NumberComplexity", TransformerConfiguration::class.java) {
    private val numberComplexity = NormalNumberComplexity()

    @EventHandler
    private fun onMethodTransform(event: MethodTransformEvent) {
        val methodNode = event.eventNode.node
        if (transformed || event.eventNode.isNative || event.eventNode.isAbstract || methodNode.instructions == null) {
            return
        }

        BytecodeAssembler.findNonZeroNumbers(methodNode.instructions).forEach { (instruction, value) ->
//            println(value)
        }
    }

    override fun getPriority(): Int {
        return TransformerPriority.LOW
    }
}
