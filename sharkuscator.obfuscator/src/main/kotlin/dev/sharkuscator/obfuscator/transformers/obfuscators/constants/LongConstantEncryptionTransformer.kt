package dev.sharkuscator.obfuscator.transformers.obfuscators.constants

import dev.sharkuscator.obfuscator.configuration.transformers.TransformerConfiguration
import dev.sharkuscator.obfuscator.events.TransformerEvents
import dev.sharkuscator.obfuscator.transformers.BaseTransformer
import dev.sharkuscator.obfuscator.transformers.TransformerPriority
import dev.sharkuscator.obfuscator.transformers.obfuscators.constants.strategies.XorNumericObfuscationStrategy
import dev.sharkuscator.obfuscator.utilities.BytecodeUtils.findNumericConstants
import meteordevelopment.orbit.EventHandler

object LongConstantEncryptionTransformer : BaseTransformer<TransformerConfiguration>("LongConstantEncryption", TransformerConfiguration::class.java) {
    private val obfuscationStrategy = XorNumericObfuscationStrategy()

    @EventHandler
    @Suppress("unused")
    private fun onMethodTransform(event: TransformerEvents.MethodTransformEvent) {
        val methodNode = event.anytypeNode.node
        if (transformed || exclusions.excluded(event.anytypeNode) || event.anytypeNode.isNative || event.anytypeNode.isAbstract || methodNode.instructions == null) {
            return
        }

        findNumericConstants(methodNode.instructions).forEach { (instruction, value) ->
            obfuscationStrategy.replaceInstructions(event.anytypeNode.owner, methodNode.instructions, instruction, value)
        }
    }

    override fun getExecutionPriority(): Int {
        return TransformerPriority.SIXTY_FIVE
    }
}
