package dev.sharkuscator.obfuscator.transformers.obfuscators.remaing

import dev.sharkuscator.obfuscator.SharedInstances
import dev.sharkuscator.obfuscator.configuration.GsonConfiguration
import dev.sharkuscator.obfuscator.configuration.transformers.RenameConfiguration
import dev.sharkuscator.obfuscator.dictionaries.DictionaryFactory
import dev.sharkuscator.obfuscator.dictionaries.MappingDictionary
import dev.sharkuscator.obfuscator.extensions.*
import dev.sharkuscator.obfuscator.transformers.AbstractTransformer
import dev.sharkuscator.obfuscator.transformers.events.transforming.MethodTransformEvent
import meteordevelopment.orbit.EventHandler

class MethodRenameTransformer : AbstractTransformer<RenameConfiguration>("MethodRename", RenameConfiguration::class.java) {
    private val badInterfaces = listOf("com.sun.jna.*".toRegex())
    private lateinit var dictionary: MappingDictionary

    override fun initialization(configuration: GsonConfiguration): RenameConfiguration {
        dictionary = DictionaryFactory.forName(super.initialization(configuration).dictionary)
        return this.configuration
    }

    @EventHandler
    private fun onMethodTransform(event: MethodTransformEvent) {
        if (event.eventNode.isNative || event.eventNode.isClInit() || event.eventNode.isInit() || event.eventNode.isMain()) {
            return
        }

        val classNode = event.eventNode.owner
        if (classNode.isAnnotation() || classNode.node.superName != "java/lang/Object" || SharedInstances.classRemapper.contains(event.eventNode.fullyName())) {
            return
        }

        // Protect native library calls
        if (classNode.node.interfaces.any { className -> badInterfaces.any { it.matches(className) } }) {
            return
        }

        val methodMapping = "${configuration.prefix}${dictionary.nextString()}"
        SharedInstances.classRemapper.setMapping(event.eventNode.fullyName(), methodMapping)

        val invocationResolver = event.context.analysisContext.invocationResolver
        for (methodNode in invocationResolver.getHierarchyMethodChain(classNode, event.eventNode.name, event.eventNode.desc, true)) {
            SharedInstances.classRemapper.setMapping(methodNode.fullyName(), methodMapping)
        }
    }
}
