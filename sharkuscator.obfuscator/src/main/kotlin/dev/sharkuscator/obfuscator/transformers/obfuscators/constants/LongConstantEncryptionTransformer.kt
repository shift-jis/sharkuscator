package dev.sharkuscator.obfuscator.transformers.obfuscators.constants

import dev.sharkuscator.obfuscator.configuration.transformers.TransformerConfiguration
import dev.sharkuscator.obfuscator.events.TransformerEvents
import dev.sharkuscator.obfuscator.transformers.BaseTransformer
import dev.sharkuscator.obfuscator.transformers.TransformerPriority
import meteordevelopment.orbit.EventHandler
import org.objectweb.asm.tree.analysis.Analyzer
import org.objectweb.asm.tree.analysis.BasicValue
import org.objectweb.asm.tree.analysis.Frame
import org.objectweb.asm.tree.analysis.SimpleVerifier

// TODO
class LongConstantEncryptionTransformer : BaseTransformer<TransformerConfiguration>("LongConstantEncryption", TransformerConfiguration::class.java) {
    @EventHandler
    @Suppress("unused")
    private fun onMethodTransform(event: TransformerEvents.MethodTransformEvent) {
        val methodNode = event.anytypeNode.node
        if (transformed || event.anytypeNode.isNative || event.anytypeNode.isAbstract || methodNode.instructions == null) {
            return
        }

//        BytecodeUtils.findNumericConstants(methodNode.instructions).forEach { (instruction, value) ->
//            println(value)
//        }
    }

    override fun getExecutionPriority(): Int {
        return TransformerPriority.MEDIUM
    }
}
