package dev.sharkuscator.obfuscator.transformers.obfuscators.constants

import dev.sharkuscator.obfuscator.configuration.transformers.TransformerConfiguration
import dev.sharkuscator.obfuscator.events.TransformerEvents
import dev.sharkuscator.obfuscator.transformers.BaseTransformer
import dev.sharkuscator.obfuscator.transformers.TransformerPriority
import meteordevelopment.orbit.EventHandler

// TODO
class LongConstantEncryptionTransformer : BaseTransformer<TransformerConfiguration>("LongConstantEncryption", TransformerConfiguration::class.java) {
    @EventHandler
    @Suppress("unused")
    private fun onMethodTransform(event: TransformerEvents.MethodTransformEvent) {
        val methodNode = event.eventNode.node
        if (transformed || event.eventNode.isNative || event.eventNode.isAbstract || methodNode.instructions == null) {
            return
        }

//        BytecodeUtils.findNumericConstants(methodNode.instructions).forEach { (instruction, value) ->
//            println(value)
//        }
    }

    override fun getExecutionPriority(): Int {
        return TransformerPriority.MEDIUM
    }
}
