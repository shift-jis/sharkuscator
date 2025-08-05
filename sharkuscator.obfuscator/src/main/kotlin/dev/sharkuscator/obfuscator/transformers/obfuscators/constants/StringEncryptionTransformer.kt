package dev.sharkuscator.obfuscator.transformers.obfuscators.constants

import dev.sharkuscator.obfuscator.configuration.transformers.TransformerConfiguration
import dev.sharkuscator.obfuscator.events.ObfuscatorEvents
import dev.sharkuscator.obfuscator.events.TransformerEvents
import dev.sharkuscator.obfuscator.extensions.isConstructor
import dev.sharkuscator.obfuscator.transformers.BaseTransformer
import dev.sharkuscator.obfuscator.transformers.TransformerPriority
import dev.sharkuscator.obfuscator.transformers.TransformerStrength
import dev.sharkuscator.obfuscator.transformers.obfuscators.constants.strategies.XorStringObfuscationStrategy
import dev.sharkuscator.obfuscator.utilities.AssemblyHelper.containsNonEmptyStrings
import dev.sharkuscator.obfuscator.utilities.AssemblyHelper.findNonEmptyStrings
import meteordevelopment.orbit.EventHandler

object StringEncryptionTransformer : BaseTransformer<TransformerConfiguration>("StringEncryption", TransformerConfiguration::class.java) {
    val obfuscationStrategy = XorStringObfuscationStrategy()

    @EventHandler
    @Suppress("unused")
    private fun onMethodTransform(event: TransformerEvents.MethodTransformEvent) {
        val targetMethodNode = event.anytypeNode.node
        if (!isEligibleForExecution() || !shouldTransformMethod(event.obfuscationContext, event.anytypeNode)) {
            return
        }

        if (event.anytypeNode.isConstructor() || targetMethodNode.instructions == null || !containsNonEmptyStrings(targetMethodNode.instructions)) {
            return
        }

        obfuscationStrategy.initialization(event.obfuscationContext, event.anytypeNode.owner)
        findNonEmptyStrings(targetMethodNode.instructions).forEach { (instruction, string) ->
            obfuscationStrategy.replaceInstructions(event.anytypeNode.owner, targetMethodNode.instructions, instruction, string)
        }
    }

    @EventHandler
    @Suppress("unused")
    private fun onPostTransform(event: ObfuscatorEvents.PostTransformEvent) {
        if (!isEligibleForExecution()) {
            return
        }

        event.context.classSource.iterate().filter { shouldTransformClass(event.context, it) }.forEach { classNode ->
            obfuscationStrategy.buildDecryptionRoutine(event.context, classNode) {
                return@buildDecryptionRoutine shouldTransformClass(event.context, it)
            }
        }
//        event.context.classSource.iterate().filter { shouldTransformClass(event.context, it) }.forEach { classNode ->
//            obfuscationStrategy.initializeKeyChunkFields(classNode)
//        }
    }

    override fun transformerStrength(): TransformerStrength {
        return TransformerStrength.MODERATE
    }

    override fun executionPriority(): Int {
        return TransformerPriority.FIFTY
    }
}
