package dev.sharkuscator.obfuscator.transformers.obfuscators.constants

import dev.sharkuscator.obfuscator.configuration.transformers.TransformerConfiguration
import dev.sharkuscator.obfuscator.events.transforming.MethodTransformEvent
import dev.sharkuscator.obfuscator.transformers.AbstractTransformer
import dev.sharkuscator.obfuscator.transformers.TransformerPriority
import dev.sharkuscator.obfuscator.transformers.obfuscators.constants.strategies.DESStringObfuscationStrategy
import dev.sharkuscator.obfuscator.transformers.obfuscators.renamers.MethodRenameTransformer
import dev.sharkuscator.obfuscator.utilities.BytecodeUtils
import meteordevelopment.orbit.EventHandler

class StringEncryptionTransformer : AbstractTransformer<TransformerConfiguration>("StringEncryption", TransformerConfiguration::class.java) {
    val obfuscationStrategy = DESStringObfuscationStrategy()

    @EventHandler
    @Suppress("unused")
    private fun onMethodTransform(event: MethodTransformEvent) {
        val methodNode = event.eventNode.node
        if (transformed || event.eventNode.isNative || event.eventNode.isAbstract || methodNode.instructions == null) {
            return
        }

        val renameTransformer = event.context.findTransformer(MethodRenameTransformer::class.java)!!.dictionary
        val decodeMethodNode = obfuscationStrategy.prepareDecoderMethod(event.eventNode.owner, renameTransformer.generateNextName())

        BytecodeUtils.findNonEmptyStrings(methodNode.instructions).forEach { (instruction, string) ->
            obfuscationStrategy.replaceInstructions(decodeMethodNode, methodNode.instructions, instruction, string)
        }
    }

    override fun getPriority(): Int {
        return TransformerPriority.LOW
    }
}
