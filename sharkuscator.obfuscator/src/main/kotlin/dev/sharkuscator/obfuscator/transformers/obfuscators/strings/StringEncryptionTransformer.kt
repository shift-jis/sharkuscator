package dev.sharkuscator.obfuscator.transformers.obfuscators.strings

import dev.sharkuscator.obfuscator.configuration.transformers.TransformerConfiguration
import dev.sharkuscator.obfuscator.transformers.AbstractTransformer
import dev.sharkuscator.obfuscator.transformers.events.ObfuscatorEvent
import dev.sharkuscator.obfuscator.transformers.events.transforming.ClassTransformEvent
import dev.sharkuscator.obfuscator.transformers.events.transforming.FieldTransformEvent
import dev.sharkuscator.obfuscator.transformers.events.transforming.MethodTransformEvent
import meteordevelopment.orbit.EventHandler
import org.objectweb.asm.tree.LdcInsnNode

class StringEncryptionTransformer : AbstractTransformer<TransformerConfiguration>("StringEncryption", TransformerConfiguration::class.java) {
    @EventHandler
    private fun onInitialization(event: ObfuscatorEvent.InitializationEvent) {
    }

    @EventHandler
    private fun onClassTransform(event: ClassTransformEvent) {
    }

    @EventHandler
    private fun onMethodTransform(event: MethodTransformEvent) {
        val methodNode = event.eventNode.node
        if (event.eventNode.isNative || event.eventNode.isAbstract || methodNode.instructions == null) {
            return
        }

//        methodNode.instructions.filterIsInstance<LdcInsnNode>().filter { it.cst is String && (it.cst as String).isNotEmpty() }.forEach { instruction ->
//            val constantValue = (instruction.cst as String).substring(1)
//            println(constantValue)
//        }
    }

    @EventHandler
    private fun onFieldTransform(event: FieldTransformEvent) {
    }
}
