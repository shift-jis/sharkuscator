package dev.sharkuscator.obfuscator.transformers.obfuscators.constants

import dev.sharkuscator.obfuscator.configuration.transformers.TransformerConfiguration
import dev.sharkuscator.obfuscator.events.TransformerEvents
import dev.sharkuscator.obfuscator.transformers.BaseTransformer
import dev.sharkuscator.obfuscator.transformers.TransformerPriority
import dev.sharkuscator.obfuscator.transformers.TransformerStrength
import dev.sharkuscator.obfuscator.transformers.obfuscators.constants.strategies.MaskingNumberObfuscationStrategy
import dev.sharkuscator.obfuscator.utilities.AssemblyHelper.findNumericConstants
import meteordevelopment.orbit.EventHandler

@Deprecated("Not implemented yet")
object NumberMaskingTransformer : BaseTransformer<TransformerConfiguration>("NumberMasking", TransformerConfiguration::class.java) {
    private val obfuscationStrategy = MaskingNumberObfuscationStrategy()

    @EventHandler
    @Suppress("unused")
    private fun onMethodTransform(event: TransformerEvents.MethodTransformEvent) {
        val targetMethodNode = event.anytypeNode.node
        if (!isEligibleForExecution() || !shouldTransformMethod(event.obfuscationContext, event.anytypeNode) || event.anytypeNode.isAbstract || targetMethodNode.instructions == null) {
            return
        }

        obfuscationStrategy.initialization(event.obfuscationContext, event.anytypeNode.owner)
        findNumericConstants(targetMethodNode.instructions).forEach { (instruction, value) ->
            obfuscationStrategy.replaceInstructions(event.anytypeNode.owner, targetMethodNode.instructions, instruction, value)
        }
    }

    override fun transformerStrength(): TransformerStrength {
        return TransformerStrength.LIGHT
    }

    override fun executionPriority(): Int {
        return TransformerPriority.SIXTY_FIVE
    }
}
