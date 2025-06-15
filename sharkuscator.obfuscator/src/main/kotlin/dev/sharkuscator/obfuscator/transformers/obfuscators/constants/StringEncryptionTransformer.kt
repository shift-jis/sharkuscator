package dev.sharkuscator.obfuscator.transformers.obfuscators.constants

import dev.sharkuscator.obfuscator.configuration.transformers.TransformerConfiguration
import dev.sharkuscator.obfuscator.events.ObfuscatorEvents
import dev.sharkuscator.obfuscator.events.TransformerEvents
import dev.sharkuscator.obfuscator.extensions.isConstructor
import dev.sharkuscator.obfuscator.transformers.BaseTransformer
import dev.sharkuscator.obfuscator.transformers.TransformerPriority
import dev.sharkuscator.obfuscator.transformers.obfuscators.constants.strategies.DESStringObfuscationStrategy
import dev.sharkuscator.obfuscator.utilities.BytecodeUtils.containsNonEmptyStrings
import dev.sharkuscator.obfuscator.utilities.BytecodeUtils.findNonEmptyStrings
import meteordevelopment.orbit.EventHandler
import org.mapleir.asm.MethodNode

object StringEncryptionTransformer : BaseTransformer<TransformerConfiguration>("StringEncryption", TransformerConfiguration::class.java) {
    val obfuscationStrategy = DESStringObfuscationStrategy()

    @EventHandler
    @Suppress("unused")
    private fun onMethodTransform(event: TransformerEvents.MethodTransformEvent) {
        val methodNode = event.anytypeNode.node
        if (transformed || event.anytypeNode.isConstructor() || event.anytypeNode.owner.isEnum || methodNode.instructions == null || !containsNonEmptyStrings(methodNode.instructions)) {
            return
        }

        val methodDictionary = event.context.resolveDictionary(MethodNode::class.java)
        val decodeMethodNode = obfuscationStrategy.prepareDecoderMethod(event.context, event.anytypeNode.owner, methodDictionary.generateNextName(event.anytypeNode.owner))

        findNonEmptyStrings(methodNode.instructions).forEach { (instruction, string) ->
            obfuscationStrategy.replaceInstructions(decodeMethodNode, methodNode.instructions, instruction, string)
        }
    }

    @EventHandler
    @Suppress("unused")
    private fun onPostTransform(event: ObfuscatorEvents.PostTransformEvent) {
        if (transformed) {
            return
        }

        event.context.jarContents.classContents.forEach { obfuscationStrategy.finalizeClass(event.context, it) }
        event.context.jarContents.classContents.forEach { obfuscationStrategy.initializeKeyChunkFields(it) }
    }

    override fun getExecutionPriority(): Int {
        return TransformerPriority.FIFTY
    }
}
