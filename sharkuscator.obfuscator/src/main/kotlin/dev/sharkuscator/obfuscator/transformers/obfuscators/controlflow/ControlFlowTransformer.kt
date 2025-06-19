package dev.sharkuscator.obfuscator.transformers.obfuscators.controlflow

import dev.sharkuscator.obfuscator.configuration.transformers.ControlFlowConfiguration
import dev.sharkuscator.obfuscator.events.TransformerEvents
import dev.sharkuscator.obfuscator.extensions.isStaticInitializer
import dev.sharkuscator.obfuscator.transformers.BaseTransformer
import dev.sharkuscator.obfuscator.transformers.TransformerPriority
import dev.sharkuscator.obfuscator.transformers.obfuscators.DynamicInvokeTransformer
import meteordevelopment.orbit.EventHandler

object ControlFlowTransformer : BaseTransformer<ControlFlowConfiguration>("ControlFlow", ControlFlowConfiguration::class.java) {
    private val controlFlowSteps = mutableListOf(
        SwitchObfuscationStep,
        JumpToTableSwitchStep,
        UnconditionalJumpStep,
        JunkInstructionStep,
    )

    @EventHandler
    @Suppress("unused")
    private fun onMethodTransform(event: TransformerEvents.MethodTransformEvent) {
        if (!isEligibleForExecution() || exclusions.excluded(event.anytypeNode) || event.anytypeNode.isNative/* || event.anytypeNode.isStaticInitializer() || event.anytypeNode.isConstructor()*/) {
            return
        }

        val isGeneratedDynamicInvokerMethod = event.anytypeNode.owner.name == DynamicInvokeTransformer.invokerHostClassName && event.anytypeNode.name == DynamicInvokeTransformer.generatedInvokerMethodName
        val applicationChancePercentage = if (event.anytypeNode.isStaticInitializer() || isGeneratedDynamicInvokerMethod) 100 else 80

        for (transformationStep in controlFlowSteps) {
            val instructionsToProcess = event.anytypeNode.node.instructions.toArray().filter { instructionNode ->
                transformationStep.isApplicableFor(instructionNode, if (transformationStep.getObfuscationStrength() == ObfuscationStrength.LIGHT) 40 else applicationChancePercentage)
            }

            instructionsToProcess.forEach { instructionNode ->
                transformationStep.processInstruction(event.anytypeNode.node.instructions, instructionNode)
            }
        }
    }

    override fun getExecutionPriority(): Int {
        return TransformerPriority.SEVENTY_FIVE
    }
}
