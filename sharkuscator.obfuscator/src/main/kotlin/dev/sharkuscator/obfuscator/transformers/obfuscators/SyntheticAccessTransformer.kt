package dev.sharkuscator.obfuscator.transformers.obfuscators

import dev.sharkuscator.obfuscator.configuration.transformers.TransformerConfiguration
import dev.sharkuscator.obfuscator.events.TransformerEvents
import dev.sharkuscator.obfuscator.extensions.*
import dev.sharkuscator.obfuscator.transformers.BaseTransformer
import meteordevelopment.orbit.EventHandler
import org.objectweb.asm.Opcodes

object SyntheticAccessTransformer : BaseTransformer<TransformerConfiguration>("SyntheticAccess", TransformerConfiguration::class.java) {
    @EventHandler
    @Suppress("unused")
    private fun onClassTransform(event: TransformerEvents.ClassTransformEvent) {
        event.anytypeNode.node.access = event.anytypeNode.node.access or Opcodes.ACC_SYNTHETIC
    }

    @EventHandler
    @Suppress("unused")
    private fun onMethodTransform(event: TransformerEvents.MethodTransformEvent) {
        if (transformed || event.anytypeNode.isStaticInitializer() || event.anytypeNode.isConstructor() || event.anytypeNode.owner.isDeclaredAsInterface()) {
            return
        }
        event.anytypeNode.node.access = event.anytypeNode.node.access or Opcodes.ACC_SYNTHETIC
        event.anytypeNode.node.access = event.anytypeNode.node.access or Opcodes.ACC_BRIDGE
    }

    @EventHandler
    @Suppress("unused")
    private fun onFieldTransform(event: TransformerEvents.FieldTransformEvent) {
        if (event.anytypeNode.isDeclaredVolatile() || event.anytypeNode.isDeclaredSynthetic() || event.anytypeNode.isDeclaredBridge()) {
            return
        }
        event.anytypeNode.node.access = event.anytypeNode.node.access or Opcodes.ACC_SYNTHETIC
    }
}
