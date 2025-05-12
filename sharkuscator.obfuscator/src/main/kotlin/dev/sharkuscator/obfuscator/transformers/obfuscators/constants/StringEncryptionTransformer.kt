package dev.sharkuscator.obfuscator.transformers.obfuscators.constants

import dev.sharkuscator.obfuscator.configuration.transformers.TransformerConfiguration
import dev.sharkuscator.obfuscator.events.AssemblerEvents
import dev.sharkuscator.obfuscator.events.TransformerEvents
import dev.sharkuscator.obfuscator.transformers.BaseTransformer
import dev.sharkuscator.obfuscator.transformers.TransformerPriority
import dev.sharkuscator.obfuscator.transformers.obfuscators.constants.strategies.DESStringObfuscationStrategy
import dev.sharkuscator.obfuscator.utilities.BytecodeUtils
import meteordevelopment.orbit.EventHandler
import org.mapleir.asm.MethodNode

class StringEncryptionTransformer : BaseTransformer<TransformerConfiguration>("StringEncryption", TransformerConfiguration::class.java) {
    val obfuscationStrategy = DESStringObfuscationStrategy()

    @EventHandler
    @Suppress("unused")
    private fun onMethodTransform(event: TransformerEvents.MethodTransformEvent) {
        val methodNode = event.eventNode.node
        if (transformed || methodNode.instructions == null || !BytecodeUtils.containsNonEmptyStrings(methodNode.instructions)) {
            return
        }

        val methodDictionary = event.context.resolveDictionary(MethodNode::class.java)
        val decodeMethodNode = obfuscationStrategy.prepareDecoderMethod(event.context, event.eventNode.owner, methodDictionary.generateNextName(event.eventNode.owner))

        BytecodeUtils.findNonEmptyStrings(methodNode.instructions).forEach { (instruction, string) ->
            obfuscationStrategy.replaceInstructions(decodeMethodNode, methodNode.instructions, instruction, string)
        }
    }

    @EventHandler
    @Suppress("unused")
    private fun onClassDump(event: AssemblerEvents.ClassDumpEvent) {
        obfuscationStrategy.finalizeClass(event.context, event.classNode)
    }

    override fun getExecutionPriority(): Int {
        return TransformerPriority.LOW
    }
}
