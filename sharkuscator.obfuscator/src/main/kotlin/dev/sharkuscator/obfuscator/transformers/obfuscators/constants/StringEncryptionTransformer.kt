package dev.sharkuscator.obfuscator.transformers.obfuscators.constants

import dev.sharkuscator.obfuscator.configuration.transformers.TransformerConfiguration
import dev.sharkuscator.obfuscator.events.AssemblerEvents
import dev.sharkuscator.obfuscator.events.TransformerEvents
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
    private fun onMethodTransform(event: TransformerEvents.MethodTransformEvent) {
        val methodNode = event.eventNode.node
        if (transformed || methodNode.instructions == null || !BytecodeUtils.containsNonEmptyStrings(methodNode.instructions)) {
            return
        }

        val renameTransformer = event.context.findTransformer(MethodRenameTransformer::class.java)!!.dictionary
        val decodeMethodNode = obfuscationStrategy.prepareDecoderMethod(event.eventNode.owner, renameTransformer.generateNextName(event.eventNode.owner))

        BytecodeUtils.findNonEmptyStrings(methodNode.instructions).forEach { (instruction, string) ->
            obfuscationStrategy.replaceInstructions(decodeMethodNode, methodNode.instructions, instruction, string)
        }
    }

    @EventHandler
    @Suppress("unused")
    private fun onClassDump(event: AssemblerEvents.ClassDumpEvent) {
        obfuscationStrategy.finalizeClass(event.classNode)
    }

    override fun getPriority(): Int {
        return TransformerPriority.LOW
    }
}
