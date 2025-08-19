package dev.sharkuscator.obfuscator.transformers.obfuscators.controlflow

import dev.sharkuscator.commons.extensions.classNode
import dev.sharkuscator.commons.extensions.isStaticInitializer
import dev.sharkuscator.obfuscator.configuration.transformers.ControlFlowMangleConfiguration
import dev.sharkuscator.obfuscator.events.TransformerEvents
import dev.sharkuscator.obfuscator.transformers.BaseTransformer
import dev.sharkuscator.obfuscator.transformers.TransformerPriority
import dev.sharkuscator.obfuscator.transformers.TransformerStrength
import dev.sharkuscator.obfuscator.transformers.obfuscators.DynamicInvokeTransformer
import meteordevelopment.orbit.EventHandler

object ControlFlowMangleTransformer : BaseTransformer<ControlFlowMangleConfiguration>("ControlFlowMangle", ControlFlowMangleConfiguration::class.java) {
    private val mangleMutators = mutableListOf(
        GotoChainOptimizeMutator,
        ConditionalJumpInversionMutator,
        JumpToTableSwitchMutator,
        SwitchKeyEncryptionMutator,
    )

    @EventHandler
    @Suppress("unused")
    private fun onMethodTransform(event: TransformerEvents.MethodTransformEvent) {
        if (!isEligibleForExecution() || !shouldTransformMethod(event.obfuscationContext, event.nodeObject)) {
            return
        }

        val isGeneratedDynamicInvokerMethod = event.nodeObject.classNode.name == DynamicInvokeTransformer.invokerHostClassName && event.nodeObject.name == DynamicInvokeTransformer.generatedInvokerMethodName
        val applicationChancePercentage = if (event.nodeObject.isStaticInitializer() || isGeneratedDynamicInvokerMethod) 100 else 60

        for (mangleMutator in mangleMutators) {
            val instructionsToProcess = event.nodeObject.instructions.toArray().filter { instructionNode ->
                mangleMutator.isApplicableFor(instructionNode, applicationChancePercentage)
            }

            instructionsToProcess.forEach { instructionNode ->
                mangleMutator.processInstruction(event.nodeObject.instructions, instructionNode)
            }
        }
    }

    override fun transformerStrength(): TransformerStrength {
        return TransformerStrength.MODERATE
    }

    override fun executionPriority(): Int {
        return TransformerPriority.SEVENTY_FIVE
    }
}
