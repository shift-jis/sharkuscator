package dev.sharkuscator.obfuscator.transformers.obfuscators.remaing

import dev.sharkuscator.obfuscator.SharedInstances
import dev.sharkuscator.obfuscator.Sharkuscator
import dev.sharkuscator.obfuscator.configuration.transformers.RenamingConfiguration
import dev.sharkuscator.obfuscator.dictionaries.AlphabetDictionary
import dev.sharkuscator.obfuscator.extensions.*
import dev.sharkuscator.obfuscator.transformers.AbstractTransformer
import dev.sharkuscator.obfuscator.transformers.events.transform.MethodTransformEvent
import meteordevelopment.orbit.EventHandler

class MethodRenamingTransformer : AbstractTransformer<RenamingConfiguration>("MethodRenaming", RenamingConfiguration::class.java) {
    private val badInterfaces = listOf("com.sun.jna.*".toRegex())
    private val dictionary = AlphabetDictionary()

    @EventHandler
    private fun onMethodTransform(event: MethodTransformEvent) {
        if (event.eventNode.isNative || event.eventNode.isClInit() || event.eventNode.isInit() || event.eventNode.isMain()) {
            return
        }

        val classNode = event.eventNode.owner
        if (classNode.node.superName != "java/lang/Object" || SharedInstances.remapper.contains(event.eventNode.fullyName())) {
            return
        }

        // Protect native library calls
        if (classNode.node.interfaces.any { className -> badInterfaces.any { it.matches(className) } }) {
            return
        }

        val methodMapping = "${configuration.prefix}${dictionary.nextString()}"
        SharedInstances.remapper.setMapping(event.eventNode.fullyName(), methodMapping)
        SharedInstances.logger.debug("======= BEGIN ${event.eventNode.fullyName()} =======")

        val invocationResolver = event.context.analysisContext.invocationResolver
        for (methodNode in invocationResolver.getHierarchyMethodChain(classNode, event.eventNode.name, event.eventNode.desc, true)) {
            SharedInstances.remapper.setMapping(methodNode.fullyName(), methodMapping)
            SharedInstances.logger.debug("${methodNode.fullyName()} -> $methodMapping")
        }

        SharedInstances.logger.debug("===================== END =====================")
    }
}
