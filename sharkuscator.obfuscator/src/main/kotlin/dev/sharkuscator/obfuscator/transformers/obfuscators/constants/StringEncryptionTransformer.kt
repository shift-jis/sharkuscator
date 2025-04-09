package dev.sharkuscator.obfuscator.transformers.obfuscators.constants

import dev.sharkuscator.obfuscator.configuration.transformers.TransformerConfiguration
import dev.sharkuscator.obfuscator.transformers.AbstractTransformer
import dev.sharkuscator.obfuscator.transformers.events.ObfuscatorEvent
import dev.sharkuscator.obfuscator.transformers.events.transforming.FieldTransformEvent
import dev.sharkuscator.obfuscator.transformers.events.transforming.MethodTransformEvent
import dev.sharkuscator.obfuscator.transformers.obfuscators.constants.strategy.NormalStringEncryption
import dev.sharkuscator.obfuscator.utilities.BytecodeAssembler
import meteordevelopment.orbit.EventHandler
import org.apache.commons.lang3.RandomStringUtils

class StringEncryptionTransformer : AbstractTransformer<TransformerConfiguration>("StringEncryption", TransformerConfiguration::class.java) {
    private val stringEncryption = NormalStringEncryption()
    private val stringPool = StringPoolGenerator()

    @EventHandler
    private fun onInitialization(event: ObfuscatorEvent.InitializationEvent) {
        event.context.jarContents.classContents.add(stringEncryption.createDecryptorClassNode("StringDecryptor"))
    }

    @EventHandler
    private fun onMethodTransform(event: MethodTransformEvent) {
        val methodNode = event.eventNode.node
        if (event.eventNode.isNative || event.eventNode.isAbstract || methodNode.instructions == null) {
            return
        }

        BytecodeAssembler.findNonEmptyStrings(methodNode.instructions).forEach { (instruction, string) ->
            val resultPair = stringEncryption.encryptString(string, RandomStringUtils.randomAlphanumeric(string.length))
            stringEncryption.replaceInstructions(methodNode.instructions, instruction, resultPair.first, resultPair.second)
        }
    }

    @EventHandler
    private fun onFieldTransform(event: FieldTransformEvent) {
    }
}
