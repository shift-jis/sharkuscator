package dev.sharkuscator.obfuscator.transformers.shrinkers

import dev.sharkuscator.obfuscator.configuration.transformers.TransformerConfiguration
import dev.sharkuscator.obfuscator.events.TransformerEvents
import dev.sharkuscator.obfuscator.transformers.BaseTransformer
import dev.sharkuscator.obfuscator.transformers.TransformerStrength
import meteordevelopment.orbit.EventHandler
import org.objectweb.asm.Opcodes

object NopOperationRemoveTransformer : BaseTransformer<TransformerConfiguration>("NopOperationRemove", TransformerConfiguration::class.java) {
    @EventHandler
    @Suppress("unused")
    private fun onMethodTransform(event: TransformerEvents.MethodTransformEvent) {
        val targetMethodNode = event.anytypeNode.node
        if (!isEligibleForExecution() || !shouldTransformMethod(event.obfuscationContext, event.anytypeNode)) {
            return
        }

        targetMethodNode.instructions.removeAll { it.opcode == Opcodes.NOP }
    }

    override fun transformerStrength(): TransformerStrength {
        return TransformerStrength.LIGHT
    }
}