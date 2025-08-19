package dev.sharkuscator.obfuscator.transformers.obfuscators.constants

import dev.sharkuscator.commons.AssemblyHelper.containsNonEmptyStrings
import dev.sharkuscator.commons.AssemblyHelper.findNonEmptyStrings
import dev.sharkuscator.commons.extensions.classNode
import dev.sharkuscator.commons.extensions.isConstructor
import dev.sharkuscator.obfuscator.configuration.transformers.TransformerConfiguration
import dev.sharkuscator.obfuscator.events.ObfuscatorEvents
import dev.sharkuscator.obfuscator.events.TransformerEvents
import dev.sharkuscator.obfuscator.transformers.BaseTransformer
import dev.sharkuscator.obfuscator.transformers.TransformerPriority
import dev.sharkuscator.obfuscator.transformers.TransformerStrength
import dev.sharkuscator.obfuscator.transformers.obfuscators.constants.strategies.DESStringObfuscationStrategy
import dev.sharkuscator.obfuscator.transformers.strategies.StringConstantObfuscationStrategy
import meteordevelopment.orbit.EventHandler

object StringEncryptionTransformer : BaseTransformer<TransformerConfiguration>("StringEncryption", TransformerConfiguration::class.java) {
    val obfuscationStrategy: StringConstantObfuscationStrategy = DESStringObfuscationStrategy()

    @EventHandler
    @Suppress("unused")
    private fun onMethodTransform(event: TransformerEvents.MethodTransformEvent) {
        if (!isEligibleForExecution() || !shouldTransformMethod(event.obfuscationContext, event.nodeObject)) {
            return
        }

        if (event.nodeObject.isConstructor() || event.nodeObject.instructions == null || !containsNonEmptyStrings(event.nodeObject.instructions)) {
            return
        }

        obfuscationStrategy.initialization(event.obfuscationContext, event.nodeObject.classNode)
        findNonEmptyStrings(event.nodeObject.instructions).forEach { (instruction, string) ->
            obfuscationStrategy.replaceInstructions(event.nodeObject.classNode, event.nodeObject.instructions, instruction, string)
        }
    }

    @EventHandler
    @Suppress("unused")
    private fun onPostTransform(event: ObfuscatorEvents.PostTransformEvent) {
        if (!isEligibleForExecution()) {
            return
        }

        event.context.classNodeProvider.asIterable().filter { shouldTransformClass(event.context, it) }.forEach { classNode ->
            obfuscationStrategy.buildDecryptionRoutine(event.context, classNode) {
                return@buildDecryptionRoutine shouldTransformClass(event.context, it)
            }
        }

        if (obfuscationStrategy is DESStringObfuscationStrategy) {
            event.context.classNodeProvider.asIterable().filter { shouldTransformClass(event.context, it) }.forEach { classNode ->
                obfuscationStrategy.initializeKeyChunkFields(classNode)
            }
        }
    }

    override fun transformerStrength(): TransformerStrength {
        return TransformerStrength.MODERATE
    }

    override fun executionPriority(): Int {
        return TransformerPriority.FIFTY
    }
}
