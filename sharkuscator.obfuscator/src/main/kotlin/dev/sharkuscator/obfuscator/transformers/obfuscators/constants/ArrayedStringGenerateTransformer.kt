package dev.sharkuscator.obfuscator.transformers.obfuscators.constants

import dev.sharkuscator.obfuscator.configuration.transformers.TransformerConfiguration
import dev.sharkuscator.obfuscator.events.ObfuscatorEvents
import dev.sharkuscator.obfuscator.events.TransformerEvents
import dev.sharkuscator.obfuscator.extensions.isConstructor
import dev.sharkuscator.obfuscator.extensions.resolveStaticInitializer
import dev.sharkuscator.obfuscator.transformers.BaseTransformer
import dev.sharkuscator.obfuscator.transformers.TransformerStrength
import dev.sharkuscator.obfuscator.transformers.obfuscators.constants.generators.ConstantArrayGenerator
import dev.sharkuscator.obfuscator.utilities.AssemblyHelper.containsNonEmptyStrings
import dev.sharkuscator.obfuscator.utilities.AssemblyHelper.findNonEmptyStrings
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
        val targetClassNode = event.anytypeNode.owner
        val targetMethodNode = event.anytypeNode.node
        if (!isEligibleForExecution() || !shouldTransformMethod(event.obfuscationContext, event.anytypeNode)) {
            return
        }

        if (event.anytypeNode.isConstructor() || targetMethodNode.instructions == null || !containsNonEmptyStrings(targetMethodNode.instructions)) {
            return
        }

        constantArrayGenerator.createAndAddArrayField(targetClassNode)
        findNonEmptyStrings(targetMethodNode.instructions).forEach { (instruction, string) ->
            val instructionString = constantArrayGenerator.addValueToRandomArray(targetClassNode, instruction, string) {
                return@addValueToRandomArray this
            }
            targetMethodNode.instructions.insert(instruction, constantArrayGenerator.createGetterInvocation(targetClassNode, instructionString))
            targetMethodNode.instructions.remove(instruction)
        }
    }

    @EventHandler
    @Suppress("unused")
    private fun onPostTransform(event: ObfuscatorEvents.PostTransformEvent) {
        if (!isEligibleForExecution()) {
            return
        }

        event.context.classSource.iterate().filter { !event.context.exclusions.excluded(it) && !exclusions.excluded(it) }.forEach { classNode ->
            val staticInitializer = classNode.resolveStaticInitializer()
            staticInitializer.node.instructions.insert(constantArrayGenerator.createInitializationInstructions(classNode) { arrayFieldMetadataList ->
            })
            constantArrayGenerator.createAndAddArrayGetterMethod(classNode)
        }
    }

    override fun transformerStrength(): TransformerStrength {
        return TransformerStrength.MODERATE
    }
}
