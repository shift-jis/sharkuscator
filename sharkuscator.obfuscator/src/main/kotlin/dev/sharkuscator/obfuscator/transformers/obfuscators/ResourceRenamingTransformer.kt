package dev.sharkuscator.obfuscator.transformers.obfuscators

import dev.sharkuscator.obfuscator.SharedInstances
import dev.sharkuscator.obfuscator.configuration.transformers.TransformerConfiguration
import dev.sharkuscator.obfuscator.dictionaries.SimilarDictionary
import dev.sharkuscator.obfuscator.extensions.overwrite
import dev.sharkuscator.obfuscator.transformers.AbstractTransformer
import dev.sharkuscator.obfuscator.transformers.events.transforms.MethodTransformEvent
import dev.sharkuscator.obfuscator.transformers.events.writes.ResourceWriteEvent
import meteordevelopment.orbit.EventHandler
import org.mapleir.ir.code.Expr
import org.mapleir.ir.code.expr.ConstantExpr
import org.objectweb.asm.Type
import org.objectweb.asm.tree.LdcInsnNode

class ResourceRenamingTransformer : AbstractTransformer<TransformerConfiguration>("ResourceRenaming", TransformerConfiguration::class.java) {
    private val mappings = mutableMapOf<String, String>()
    private val dictionary = SimilarDictionary(20)

    @EventHandler
    private fun onResourceWrite(writeEvent: ResourceWriteEvent) {
        writeEvent.name = mappings[writeEvent.name] ?: writeEvent.name
    }

    @EventHandler
    private fun onMethodTransform(transformEvent: MethodTransformEvent) {
        val methodNode = transformEvent.eventNode.node
        if (transformEvent.eventNode.isNative || transformEvent.eventNode.isAbstract || methodNode.instructions == null) {
            return
        }

//        SharedInstances.irFactory.getFor(transformEvent.eventNode).allExprStream().filter { it is ConstantExpr && it.constant is String && (it.constant as String).isNotEmpty() }.forEach { unit ->
//            val resourceName = ((unit as ConstantExpr).constant as String).substring(1)
//            if (transformEvent.jarContents.resourceContents.any { it.name == resourceName }) {
//                SharedInstances.logger.info(resourceName)
//            }
//        }

        methodNode.instructions.filterIsInstance<LdcInsnNode>().filter { it.cst != null && it.cst is String && (it.cst as String).isNotEmpty() }.forEach { instruction ->
            val resourceName = (instruction.cst as String).substring(1)
            if (transformEvent.jarContents.resourceContents.any { it.name == resourceName }) {
                if (mappings.containsKey(resourceName)) {
                    instruction.cst = mappings[resourceName]
                } else {
                    mappings[resourceName] = dictionary.nextString()
                    instruction.cst = mappings[resourceName]
                }
            }
        }
    }
}
