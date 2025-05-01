package dev.sharkuscator.obfuscator.transformers.obfuscators

import dev.sharkuscator.obfuscator.configuration.transformers.TransformerConfiguration
import dev.sharkuscator.obfuscator.extensions.isClInit
import dev.sharkuscator.obfuscator.extensions.isInit
import dev.sharkuscator.obfuscator.extensions.isInterface
import dev.sharkuscator.obfuscator.extensions.isSynthetic
import dev.sharkuscator.obfuscator.transformers.AbstractTransformer
import dev.sharkuscator.obfuscator.transformers.events.transforming.FieldTransformEvent
import dev.sharkuscator.obfuscator.transformers.events.transforming.ClassTransformEvent
import dev.sharkuscator.obfuscator.transformers.events.transforming.MethodTransformEvent
import meteordevelopment.orbit.EventHandler
import org.objectweb.asm.Opcodes

class SyntheticAccessTransformer : AbstractTransformer<TransformerConfiguration>("SyntheticAccess", TransformerConfiguration::class.java) {
    @EventHandler
    private fun onClassTransform(event: ClassTransformEvent) {
        event.eventNode.node.access = event.eventNode.node.access or Opcodes.ACC_SYNTHETIC
    }

    @EventHandler
    private fun onMethodTransform(event: MethodTransformEvent) {
        if (event.eventNode.isSynthetic() || event.eventNode.isClInit() || event.eventNode.isInit() || event.eventNode.owner.isInterface()) {
            return
        }
        event.eventNode.node.access = event.eventNode.node.access or Opcodes.ACC_SYNTHETIC
    }

    @EventHandler
    private fun onFieldTransform(event: FieldTransformEvent) {
        event.eventNode.node.access = event.eventNode.node.access or Opcodes.ACC_SYNTHETIC
    }
}
