package dev.sharkuscator.obfuscator.transformers.obfuscators

import dev.sharkuscator.obfuscator.configuration.transformers.TransformerConfiguration
import dev.sharkuscator.obfuscator.events.TransformerEvents
import dev.sharkuscator.obfuscator.transformers.BaseTransformer
import dev.sharkuscator.obfuscator.transformers.TransformerStrength
import meteordevelopment.orbit.EventHandler
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.JumpInsnNode

object GotoChainOptimizeTransformer : BaseTransformer<TransformerConfiguration>("GotoChainOptimize", TransformerConfiguration::class.java) {
    @EventHandler
    @Suppress("unused")
    private fun onMethodTransform(event: TransformerEvents.MethodTransformEvent) {
        if (!isEligibleForExecution() || !shouldTransformMethod(event.obfuscationContext, event.anytypeNode)) {
            return
        }

        event.anytypeNode.node.instructions.filter { it is JumpInsnNode && it.opcode == Opcodes.GOTO }.forEach { instruction ->
            val currentGotoInstruction = instruction as JumpInsnNode
            val instructionAfterTarget = currentGotoInstruction.label.next
            if (instructionAfterTarget != null && instructionAfterTarget.opcode == Opcodes.GOTO) {
                val chainedGotoInstruction = instructionAfterTarget as JumpInsnNode
                currentGotoInstruction.label = chainedGotoInstruction.label
            }
        }
    }

    override fun transformerStrength(): TransformerStrength {
        return TransformerStrength.LIGHT
    }
}