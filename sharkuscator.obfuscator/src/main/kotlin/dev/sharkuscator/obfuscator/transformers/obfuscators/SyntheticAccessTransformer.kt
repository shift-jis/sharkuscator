package dev.sharkuscator.obfuscator.transformers.obfuscators

import dev.sharkuscator.obfuscator.configuration.transformers.TransformerConfiguration
import dev.sharkuscator.obfuscator.events.transforming.ClassTransformEvent
import dev.sharkuscator.obfuscator.events.transforming.FieldTransformEvent
import dev.sharkuscator.obfuscator.events.transforming.MethodTransformEvent
import dev.sharkuscator.obfuscator.extensions.*
import dev.sharkuscator.obfuscator.transformers.AbstractTransformer
import meteordevelopment.orbit.EventHandler
import org.objectweb.asm.Opcodes

class SyntheticAccessTransformer : AbstractTransformer<TransformerConfiguration>("SyntheticAccess", TransformerConfiguration::class.java) {
    @EventHandler
    @Suppress("unused")
    private fun onClassTransform(event: ClassTransformEvent) {
        event.eventNode.node.access = event.eventNode.node.access or Opcodes.ACC_SYNTHETIC
    }

    @EventHandler
    @Suppress("unused")
    private fun onMethodTransform(event: MethodTransformEvent) {
        if (event.eventNode.isStaticInitializer() || event.eventNode.isConstructor() || event.eventNode.owner.isDeclaredAsInterface()) {
            return
        }
        event.eventNode.node.access = event.eventNode.node.access or Opcodes.ACC_SYNTHETIC
        event.eventNode.node.access = event.eventNode.node.access or Opcodes.ACC_BRIDGE
    }

    @EventHandler
    @Suppress("unused")
    private fun onFieldTransform(event: FieldTransformEvent) {
        if (event.eventNode.isDeclaredVolatile() || event.eventNode.isDeclaredSynthetic() || event.eventNode.isDeclaredBridge()) {
            return
        }
        event.eventNode.node.access = event.eventNode.node.access or Opcodes.ACC_SYNTHETIC
    }
}
