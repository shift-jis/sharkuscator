package dev.sharkuscator.obfuscator.transformers.obfuscators.constants

import dev.sharkuscator.commons.AssemblyHelper.containsNonEmptyStrings
import dev.sharkuscator.commons.AssemblyHelper.findNonEmptyStrings
import dev.sharkuscator.commons.extensions.classNode
import dev.sharkuscator.commons.extensions.isConstructor
import dev.sharkuscator.commons.extensions.resolveStaticInitializer
import dev.sharkuscator.obfuscator.configuration.transformers.TransformerConfiguration
import dev.sharkuscator.obfuscator.events.ObfuscatorEvents
import dev.sharkuscator.obfuscator.events.TransformerEvents
import dev.sharkuscator.obfuscator.transformers.BaseTransformer
import dev.sharkuscator.obfuscator.transformers.TransformerStrength
import dev.sharkuscator.obfuscator.transformers.obfuscators.constants.generators.ConstantArrayGenerator
import meteordevelopment.orbit.EventHandler

@Deprecated(
    message = "This transformer is for debugging purposes only and will be removed in future versions. " +
            "For production string obfuscation, use DESStringObfuscationStrategy instead."
)
object ArrayedStringGenerateTransformer : BaseTransformer<TransformerConfiguration>("ArrayedStringGenerate", TransformerConfiguration::class.java) {
    private val constantArrayGenerator = ConstantArrayGenerator(String::class.java)

    @EventHandler
    @Suppress("unused")
    private fun onMethodTransform(event: TransformerEvents.MethodTransformEvent) {
        if (!isEligibleForExecution() || !shouldTransformMethod(event.obfuscationContext, event.nodeObject)) {
            return
        }

        if (event.nodeObject.isConstructor() || event.nodeObject.instructions == null || !containsNonEmptyStrings(event.nodeObject.instructions)) {
            return
        }

        constantArrayGenerator.createAndAddArrayField(event.nodeObject.classNode)
        findNonEmptyStrings(event.nodeObject.instructions).forEach { (instruction, string) ->
            val instructionString = constantArrayGenerator.addValueToRandomArray(event.nodeObject.classNode, instruction, string) {
                return@addValueToRandomArray this
            }
            event.nodeObject.instructions.insert(instruction, constantArrayGenerator.createGetterInvocation(event.nodeObject.classNode, instructionString))
            event.nodeObject.instructions.remove(instruction)
        }
    }

    @EventHandler
    @Suppress("unused")
    private fun onPostTransform(event: ObfuscatorEvents.PostTransformEvent) {
        if (!isEligibleForExecution()) {
            return
        }

        event.context.classNodeProvider.asIterable().filter { !event.context.exclusions.excluded(it) && !exclusions.excluded(it) }.forEach { classNode ->
            val staticInitializer = classNode.resolveStaticInitializer()
            staticInitializer.instructions.insert(constantArrayGenerator.createInitializationInstructions(classNode) { arrayFieldMetadataList ->
            })
            constantArrayGenerator.createAndAddArrayGetterMethod(classNode)
        }
    }

    override fun transformerStrength(): TransformerStrength {
        return TransformerStrength.MODERATE
    }
}
