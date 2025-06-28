package dev.sharkuscator.obfuscator.transformers.obfuscators.controlflow

import dev.sharkuscator.obfuscator.configuration.transformers.ControlFlowMangleConfiguration
import dev.sharkuscator.obfuscator.events.TransformerEvents
import dev.sharkuscator.obfuscator.extensions.isStaticInitializer
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
        val targetMethodNode = event.anytypeNode.node
        if (!isEligibleForExecution() || !shouldTransformMethod(event.obfuscationContext, event.anytypeNode)) {
            return
        }

        val isGeneratedDynamicInvokerMethod = event.anytypeNode.owner.name == DynamicInvokeTransformer.invokerHostClassName && event.anytypeNode.name == DynamicInvokeTransformer.generatedInvokerMethodName
        val applicationChancePercentage = if (event.anytypeNode.isStaticInitializer() || isGeneratedDynamicInvokerMethod) 100 else 60

        for (mangleMutator in mangleMutators) {
            val instructionsToProcess = targetMethodNode.instructions.toArray().filter { instructionNode ->
                mangleMutator.isApplicableFor(instructionNode, applicationChancePercentage)
            }

            instructionsToProcess.forEach { instructionNode ->
                mangleMutator.processInstruction(targetMethodNode.instructions, instructionNode)
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
