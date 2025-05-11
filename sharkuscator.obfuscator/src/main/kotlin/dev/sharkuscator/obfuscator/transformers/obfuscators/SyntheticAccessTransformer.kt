package dev.sharkuscator.obfuscator.transformers.obfuscators

import dev.sharkuscator.obfuscator.configuration.transformers.TransformerConfiguration
import dev.sharkuscator.obfuscator.events.TransformerEvents
import dev.sharkuscator.obfuscator.extensions.*
import dev.sharkuscator.obfuscator.transformers.BaseTransformer
import meteordevelopment.orbit.EventHandler
import org.objectweb.asm.Opcodes

class SyntheticAccessTransformer : BaseTransformer<TransformerConfiguration>("SyntheticAccess", TransformerConfiguration::class.java) {
    @EventHandler
    @Suppress("unused")
    private fun onClassTransform(event: TransformerEvents.ClassTransformEvent) {
        event.eventNode.node.access = event.eventNode.node.access or Opcodes.ACC_SYNTHETIC
    }

    @EventHandler
    @Suppress("unused")
    private fun onMethodTransform(event: TransformerEvents.MethodTransformEvent) {
        if (event.eventNode.isStaticInitializer() || event.eventNode.isConstructor() || event.eventNode.owner.isDeclaredAsInterface()) {
            return
        }
        event.eventNode.node.access = event.eventNode.node.access or Opcodes.ACC_SYNTHETIC
        event.eventNode.node.access = event.eventNode.node.access or Opcodes.ACC_BRIDGE
    }

    @EventHandler
    @Suppress("unused")
    private fun onFieldTransform(event: TransformerEvents.FieldTransformEvent) {
        if (event.eventNode.isDeclaredVolatile() || event.eventNode.isDeclaredSynthetic() || event.eventNode.isDeclaredBridge()) {
            return
        }
        event.eventNode.node.access = event.eventNode.node.access or Opcodes.ACC_SYNTHETIC
    }
}
