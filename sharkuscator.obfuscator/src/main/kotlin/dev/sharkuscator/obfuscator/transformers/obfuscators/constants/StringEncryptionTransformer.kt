package dev.sharkuscator.obfuscator.transformers.obfuscators.constants

import dev.sharkuscator.obfuscator.configuration.transformers.TransformerConfiguration
import dev.sharkuscator.obfuscator.transformers.AbstractTransformer
import dev.sharkuscator.obfuscator.transformers.TransformerPriority
import dev.sharkuscator.obfuscator.transformers.events.ObfuscatorEvent
import dev.sharkuscator.obfuscator.transformers.events.transforming.MethodTransformEvent
import dev.sharkuscator.obfuscator.transformers.obfuscators.constants.strategies.impl.XorObfuscationStrategy
import dev.sharkuscator.obfuscator.transformers.obfuscators.renamers.ClassRenameTransformer
import dev.sharkuscator.obfuscator.transformers.obfuscators.renamers.MethodRenameTransformer
import dev.sharkuscator.obfuscator.utilities.BytecodeUtils
import meteordevelopment.orbit.EventHandler

class StringEncryptionTransformer : AbstractTransformer<TransformerConfiguration>("StringEncryption", TransformerConfiguration::class.java) {
    val stringEncryption = XorObfuscationStrategy()

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

        BytecodeUtils.findNonEmptyStrings(methodNode.instructions).forEach { (instruction, string) ->
            stringEncryption.replaceInstructions(methodNode.instructions, instruction, string)
        }
    }

    override fun getPriority(): Int {
        return TransformerPriority.LOW
    }
}
