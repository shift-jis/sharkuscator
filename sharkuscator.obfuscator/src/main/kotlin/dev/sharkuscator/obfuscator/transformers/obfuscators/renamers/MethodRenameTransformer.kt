package dev.sharkuscator.obfuscator.transformers.obfuscators.renamers

import dev.sharkuscator.obfuscator.ObfuscatorServices
import dev.sharkuscator.obfuscator.configuration.GsonConfiguration
import dev.sharkuscator.obfuscator.configuration.transformers.RenameConfiguration
import dev.sharkuscator.obfuscator.dictionaries.DictionaryFactory
import dev.sharkuscator.obfuscator.dictionaries.MappingDictionary
import dev.sharkuscator.obfuscator.events.TransformerEvents
import dev.sharkuscator.obfuscator.extensions.getQualifiedName
import dev.sharkuscator.obfuscator.extensions.isDeclaredAsAnnotation
import dev.sharkuscator.obfuscator.extensions.isSpongeMixin
import dev.sharkuscator.obfuscator.extensions.shouldSkipTransform
import dev.sharkuscator.obfuscator.transformers.BaseTransformer
import dev.sharkuscator.obfuscator.transformers.TransformerPriority
import meteordevelopment.orbit.EventHandler
import org.mapleir.asm.ClassNode

class MethodRenameTransformer : BaseTransformer<RenameConfiguration>("MethodRename", RenameConfiguration::class.java) {
    private val badInterfaces = listOf("com.sun.jna.*".toRegex())
    lateinit var dictionary: MappingDictionary<ClassNode>

    override fun initialization(configuration: GsonConfiguration): RenameConfiguration {
        dictionary = DictionaryFactory.createDictionary(super.initialization(configuration).dictionary)
        return this.configuration
    }

    @EventHandler
    @Suppress("unused")
    private fun onMethodTransform(event: TransformerEvents.MethodTransformEvent) {
        if (transformed || event.anytypeNode.isNative || event.anytypeNode.shouldSkipTransform()) {
            return
        }

        val classNode = event.anytypeNode.owner
        if (event.context.isInputRecognizedAsMinecraftMod && (classNode.isSpongeMixin() || event.anytypeNode.name.startsWith("func_"))) {
            return
        }

        if (classNode.isDeclaredAsAnnotation() || classNode.node.superName != "java/lang/Object" || ObfuscatorServices.symbolRemapper.symbolMappings.containsKey(event.anytypeNode.getQualifiedName())) {
            return
        }

        if (classNode.node.interfaces.any { className -> badInterfaces.any { it.matches(className) } }) {
            return
        }

        val methodMapping = "${configuration.prefix}${dictionary.generateNextName(event.anytypeNode.owner)}"
        ObfuscatorServices.symbolRemapper.setMapping(event.anytypeNode.getQualifiedName(), methodMapping)

        val invocationResolver = event.context.analysisContext.invocationResolver
        for (methodNode in invocationResolver.getHierarchyMethodChain(classNode, event.anytypeNode.name, event.anytypeNode.desc, true)) {
            ObfuscatorServices.symbolRemapper.setMapping(methodNode.getQualifiedName(), methodMapping)
            dictionary.generateNextName(methodNode.owner)
        }
    }

    override fun getExecutionPriority(): Int {
        return TransformerPriority.BELOW_MEDIUM
    }
}
