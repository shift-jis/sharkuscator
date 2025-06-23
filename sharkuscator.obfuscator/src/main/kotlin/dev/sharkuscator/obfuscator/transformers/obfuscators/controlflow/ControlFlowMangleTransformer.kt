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
//        SwitchMangleMutator,
        JumpToTableSwitchMutator,
        UnconditionalJumpMutator,
    )

    @EventHandler
    @Suppress("unused")
    private fun onMethodTransform(event: TransformerEvents.MethodTransformEvent) {
        if (!isEligibleForExecution() || !shouldTransformMethod(event.context, event.anytypeNode)) {
            return
        }

        val isGeneratedDynamicInvokerMethod = event.anytypeNode.owner.name == DynamicInvokeTransformer.invokerHostClassName && event.anytypeNode.name == DynamicInvokeTransformer.generatedInvokerMethodName
        val applicationChancePercentage = if (event.anytypeNode.isStaticInitializer() || isGeneratedDynamicInvokerMethod) 100 else 60

        for (mangleMutator in mangleMutators) {
            val instructionsToProcess = event.anytypeNode.node.instructions.toArray().filter { instructionNode ->
                mangleMutator.isApplicableFor(instructionNode, if (mangleMutator.transformerStrength() == TransformerStrength.LIGHT) 40 else applicationChancePercentage)
            }

            instructionsToProcess.forEach { instructionNode ->
                mangleMutator.processInstruction(event.anytypeNode.node.instructions, instructionNode)
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
