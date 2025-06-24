package dev.sharkuscator.obfuscator.transformers.obfuscators.constants

import dev.sharkuscator.obfuscator.configuration.transformers.TransformerConfiguration
import dev.sharkuscator.obfuscator.events.TransformerEvents
import dev.sharkuscator.obfuscator.transformers.BaseTransformer
import dev.sharkuscator.obfuscator.transformers.TransformerPriority
import dev.sharkuscator.obfuscator.transformers.TransformerStrength
import dev.sharkuscator.obfuscator.transformers.obfuscators.constants.strategies.XorNumericObfuscationStrategy
import dev.sharkuscator.obfuscator.utilities.AssemblyHelper.findNumericConstants
import meteordevelopment.orbit.EventHandler

object LongConstantEncryptionTransformer : BaseTransformer<TransformerConfiguration>("LongConstantEncryption", TransformerConfiguration::class.java) {
    private val obfuscationStrategy = XorNumericObfuscationStrategy()

    @EventHandler
    @Suppress("unused")
    private fun onMethodTransform(event: TransformerEvents.MethodTransformEvent) {
        val methodNode = event.anytypeNode.node
        if (!isEligibleForExecution() || !shouldTransformMethod(event.context, event.anytypeNode) || event.anytypeNode.isAbstract || methodNode.instructions == null) {
            return
        }

        findNumericConstants(methodNode.instructions).forEach { (instruction, value) ->
            obfuscationStrategy.replaceInstructions(event.anytypeNode.owner, methodNode.instructions, instruction, value)
        }
    }

    override fun transformerStrength(): TransformerStrength {
        return TransformerStrength.LIGHT
    }

    override fun executionPriority(): Int {
        return TransformerPriority.SIXTY_FIVE
    }
}
