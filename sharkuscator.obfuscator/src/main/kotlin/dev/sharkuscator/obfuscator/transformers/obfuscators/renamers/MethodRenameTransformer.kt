package dev.sharkuscator.obfuscator.transformers.obfuscators.renamers

import dev.sharkuscator.obfuscator.ObfuscatorServices
import dev.sharkuscator.obfuscator.configuration.GsonConfiguration
import dev.sharkuscator.obfuscator.configuration.transformers.RenameConfiguration
import dev.sharkuscator.obfuscator.dictionaries.DictionaryFactory
import dev.sharkuscator.obfuscator.dictionaries.MappingDictionary
import dev.sharkuscator.obfuscator.events.TransformerEvents
import dev.sharkuscator.obfuscator.extensions.*
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
        if (transformed || event.eventNode.isNative || event.eventNode.isStaticInitializer() || event.eventNode.isConstructor() || event.eventNode.hasMainSignature()) {
            return
        }

        val classNode = event.eventNode.owner
        if (classNode.isDeclaredAsAnnotation() || classNode.node.superName != "java/lang/Object" || ObfuscatorServices.symbolRemapper.symbolMappings.containsKey(event.eventNode.getQualifiedName())) {
            return
        }

        // Protect native library calls
        if (classNode.node.interfaces.any { className -> badInterfaces.any { it.matches(className) } }) {
            return
        }

        val methodMapping = "${configuration.prefix}${dictionary.generateNextName(event.eventNode.owner)}"
        ObfuscatorServices.symbolRemapper.setMapping(event.eventNode.getQualifiedName(), methodMapping)

        val invocationResolver = event.context.analysisContext.invocationResolver
        for (methodNode in invocationResolver.getHierarchyMethodChain(classNode, event.eventNode.name, event.eventNode.desc, true)) {
            ObfuscatorServices.symbolRemapper.setMapping(methodNode.getQualifiedName(), methodMapping)
            dictionary.generateNextName(methodNode.owner)
        }
    }

    override fun getExecutionPriority(): Int {
        return TransformerPriority.BELOW_MEDIUM
    }
}
