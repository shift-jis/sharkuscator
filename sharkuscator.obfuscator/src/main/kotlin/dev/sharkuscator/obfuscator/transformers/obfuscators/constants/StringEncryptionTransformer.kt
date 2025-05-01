package dev.sharkuscator.obfuscator.transformers.obfuscators.constants

import dev.sharkuscator.obfuscator.configuration.transformers.TransformerConfiguration
import dev.sharkuscator.obfuscator.transformers.AbstractTransformer
import dev.sharkuscator.obfuscator.transformers.TransformerPriority
import dev.sharkuscator.obfuscator.transformers.events.ObfuscatorEvent
import dev.sharkuscator.obfuscator.transformers.events.transforming.MethodTransformEvent
import dev.sharkuscator.obfuscator.transformers.obfuscators.constants.strategy.NormalStringEncryption
import dev.sharkuscator.obfuscator.transformers.obfuscators.renamers.ClassRenameTransformer
import dev.sharkuscator.obfuscator.transformers.obfuscators.renamers.MethodRenameTransformer
import dev.sharkuscator.obfuscator.utilities.BytecodeAssembler
import meteordevelopment.orbit.EventHandler
import org.apache.commons.lang3.RandomStringUtils

class StringEncryptionTransformer : AbstractTransformer<TransformerConfiguration>("StringEncryption", TransformerConfiguration::class.java) {
    val stringEncryption = NormalStringEncryption()

    @EventHandler
    private fun onInitialization(event: ObfuscatorEvent.InitializationEvent) {
        val classRenameTransformer = event.context.findTransformer(ClassRenameTransformer::class.java)!!
        val methodRenameTransformer = event.context.findTransformer(MethodRenameTransformer::class.java)!!

        val decryptClassNode = stringEncryption.createDecryptClassNode(
            classRenameTransformer.dictionary.nextString(),
            methodRenameTransformer.dictionary.nextString()
        )
        event.context.jarContents.classContents.add(decryptClassNode)
    }

    @EventHandler
    private fun onMethodTransform(event: MethodTransformEvent) {
        val methodNode = event.eventNode.node
        if (transformed || event.eventNode.isNative || event.eventNode.isAbstract || methodNode.instructions == null) {
            return
        }

        BytecodeAssembler.findNonEmptyStrings(methodNode.instructions).forEach { (instruction, string) ->
            val resultPair = stringEncryption.encryptString(string, RandomStringUtils.randomAlphanumeric(string.length))
            stringEncryption.replaceInstructions(methodNode.instructions, instruction, resultPair.first, resultPair.second)
        }
    }

    override fun getPriority(): Int {
        return TransformerPriority.LOW
    }
}
