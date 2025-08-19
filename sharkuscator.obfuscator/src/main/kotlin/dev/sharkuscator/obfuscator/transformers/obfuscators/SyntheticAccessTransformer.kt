package dev.sharkuscator.obfuscator.transformers.obfuscators

import dev.sharkuscator.commons.extensions.classNode
import dev.sharkuscator.commons.extensions.isConstructor
import dev.sharkuscator.commons.extensions.isDeclaredAsInterface
import dev.sharkuscator.commons.extensions.isStaticInitializer
import dev.sharkuscator.obfuscator.configuration.transformers.TransformerConfiguration
import dev.sharkuscator.obfuscator.events.TransformerEvents
import dev.sharkuscator.obfuscator.transformers.BaseTransformer
import dev.sharkuscator.obfuscator.transformers.TransformerStrength
import meteordevelopment.orbit.EventHandler
import org.objectweb.asm.Opcodes

object SyntheticAccessTransformer : BaseTransformer<TransformerConfiguration>("SyntheticAccess", TransformerConfiguration::class.java) {
    @EventHandler
    @Suppress("unused")
    private fun onMethodTransform(event: TransformerEvents.MethodTransformEvent) {
        if (!isEligibleForExecution() || !shouldTransformMethod(event.obfuscationContext, event.nodeObject)) {
            return
        }

        if (event.nodeObject.isStaticInitializer() || event.nodeObject.isConstructor() || event.nodeObject.classNode.isDeclaredAsInterface()) {
            return
        }

        event.nodeObject.access = event.nodeObject.access or Opcodes.ACC_BRIDGE
        event.nodeObject.access = event.nodeObject.access or Opcodes.ACC_SYNTHETIC
    }

    @EventHandler
    @Suppress("unused")
    private fun onFieldTransform(event: TransformerEvents.FieldTransformEvent) {
        if (!isEligibleForExecution() || !shouldTransformField(event.obfuscationContext, event.nodeObject)) {
            return
        }

        event.nodeObject.access = event.nodeObject.access or Opcodes.ACC_SYNTHETIC
    }

    override fun transformerStrength(): TransformerStrength {
        return TransformerStrength.LIGHT
    }
}
