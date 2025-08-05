package dev.sharkuscator.obfuscator.transformers.obfuscators.controlflow

import dev.sharkuscator.obfuscator.configuration.transformers.TransformerConfiguration
import dev.sharkuscator.obfuscator.events.TransformerEvents
import dev.sharkuscator.obfuscator.extensions.*
import dev.sharkuscator.obfuscator.transformers.BaseTransformer
import dev.sharkuscator.obfuscator.transformers.TransformerStrength
import meteordevelopment.orbit.EventHandler

@Deprecated("Not implemented yet")
object ControlFlowFlatteningTransformer : BaseTransformer<TransformerConfiguration>("ControlFlowFlattening", TransformerConfiguration::class.java) {
    @EventHandler
    @Suppress("unused")
    private fun onMethodTransform(event: TransformerEvents.MethodTransformEvent) {
        val targetMethodNode = event.anytypeNode.node
        if (!isEligibleForExecution() || !shouldTransformMethod(event.obfuscationContext, event.anytypeNode)) {
            return
        }

        if (event.anytypeNode.isStaticInitializer() || event.anytypeNode.isConstructor() || targetMethodNode.instructions.size() < 2) {
            return
        }

        val instructionBlocks = targetMethodNode.instructions.partitionIntoBasicBlocks().requireSizeOrElse(2) { return }
//        targetMethodNode.instructions.clear()

//        for (instructionBlock in instructionBlocks) {
//            targetMethodNode.instructions.add(AssemblyHelper.buildInstructionList {
//                add(LabelNode())
//                add(instructionBlock)
//            })
//        }

        instructionBlocks.last().forEach { instructions ->
            println(event.anytypeNode.getQualifiedName() + " " + instructions.opcode)
        }
    }

    override fun transformerStrength(): TransformerStrength {
        return TransformerStrength.MODERATE
    }
}
