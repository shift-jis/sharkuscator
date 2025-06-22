package dev.sharkuscator.obfuscator.transformers.obfuscators

import dev.sharkuscator.obfuscator.configuration.transformers.TransformerConfiguration
import dev.sharkuscator.obfuscator.events.TransformerEvents
import dev.sharkuscator.obfuscator.extensions.*
import dev.sharkuscator.obfuscator.transformers.BaseTransformer
import dev.sharkuscator.obfuscator.transformers.TransformerStrength
import dev.sharkuscator.obfuscator.transformers.obfuscators.renamers.FieldRenameTransformer
import meteordevelopment.orbit.EventHandler
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type

object SyntheticAccessTransformer : BaseTransformer<TransformerConfiguration>("SyntheticAccess", TransformerConfiguration::class.java) {
    @EventHandler
    @Suppress("unused")
    private fun onClassTransform(event: TransformerEvents.ClassTransformEvent) {
        if (!isEligibleForExecution() || !shouldTransformClass(event.context, event.anytypeNode) || event.anytypeNode.isSpongeMixin()) {
            return
        }

        event.anytypeNode.node.access = event.anytypeNode.node.access or Opcodes.ACC_SYNTHETIC
    }

    @EventHandler
    @Suppress("unused")
    private fun onMethodTransform(event: TransformerEvents.MethodTransformEvent) {
        if (!isEligibleForExecution() || !shouldTransformMethod(event.context, event.anytypeNode)) {
            return
        }

        val targetClassNode = event.anytypeNode.owner
        if (event.anytypeNode.isStaticInitializer() || event.anytypeNode.isConstructor() || targetClassNode.isSpongeMixin() || targetClassNode.isDeclaredAsInterface()) {
            return
        }

        event.anytypeNode.node.access = event.anytypeNode.node.access or Opcodes.ACC_SYNTHETIC
    }

    @EventHandler
    @Suppress("unused")
    private fun onFieldTransform(event: TransformerEvents.FieldTransformEvent) {
        if (!isEligibleForExecution() || !shouldTransformField(event.context, event.anytypeNode)) {
            return
        }

        event.anytypeNode.node.access = event.anytypeNode.node.access or Opcodes.ACC_SYNTHETIC
    }

    override fun transformerStrength(): TransformerStrength {
        return TransformerStrength.LIGHT
    }
}
