package dev.sharkuscator.obfuscator.transformers.obfuscators.controlflow

import dev.sharkuscator.obfuscator.configuration.transformers.TransformerConfiguration
import dev.sharkuscator.obfuscator.events.TransformerEvents
import dev.sharkuscator.obfuscator.extensions.getQualifiedName
import dev.sharkuscator.obfuscator.extensions.isConstructor
import dev.sharkuscator.obfuscator.extensions.isStaticInitializer
import dev.sharkuscator.obfuscator.extensions.partitionIntoBasicBlocks
import dev.sharkuscator.obfuscator.extensions.requireSizeOrElse
import dev.sharkuscator.obfuscator.transformers.BaseTransformer
import dev.sharkuscator.obfuscator.transformers.TransformerStrength
import dev.sharkuscator.obfuscator.utilities.AssemblyHelper
import meteordevelopment.orbit.EventHandler
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.LabelNode

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
