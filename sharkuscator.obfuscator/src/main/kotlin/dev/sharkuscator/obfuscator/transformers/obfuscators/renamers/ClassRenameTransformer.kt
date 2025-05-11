package dev.sharkuscator.obfuscator.transformers.obfuscators.renamers

import dev.sharkuscator.obfuscator.ObfuscatorServices
import dev.sharkuscator.obfuscator.configuration.GsonConfiguration
import dev.sharkuscator.obfuscator.configuration.transformers.RenameConfiguration
import dev.sharkuscator.obfuscator.dictionaries.DictionaryFactory
import dev.sharkuscator.obfuscator.dictionaries.MappingDictionary
import dev.sharkuscator.obfuscator.events.AssemblerEvents
import dev.sharkuscator.obfuscator.events.TransformerEvents
import dev.sharkuscator.obfuscator.extensions.containsMainMethod
import dev.sharkuscator.obfuscator.extensions.isDeclaredAsAnnotation
import dev.sharkuscator.obfuscator.transformers.BaseTransformer
import dev.sharkuscator.obfuscator.transformers.TransformerPriority
import meteordevelopment.orbit.EventHandler
import org.mapleir.asm.ClassNode

class ClassRenameTransformer : BaseTransformer<RenameConfiguration>("ClassRename", RenameConfiguration::class.java) {
    private val defaultDictionary = DictionaryFactory.createDefaultDictionary<ClassNode>()
    lateinit var dictionary: MappingDictionary<ClassNode>

    override fun initialization(configuration: GsonConfiguration): RenameConfiguration {
        dictionary = DictionaryFactory.createDictionary(super.initialization(configuration).dictionary)
        return this.configuration
    }

    @EventHandler
    @Suppress("unused")
    private fun onClassTransform(event: TransformerEvents.ClassTransformEvent) {
        if (transformed || event.context.classSource.isLibraryClass(event.eventNode.name)) {
            return
        }

        var classMapping = dictionary.generateNextName(null)
        if (dictionary.generatesUnsafeNames() && (event.eventNode.containsMainMethod() || event.eventNode.isDeclaredAsAnnotation())) {
            classMapping = defaultDictionary.generateNextName(null)
        }

        ObfuscatorServices.symbolRemapper.setMapping(event.eventNode.name, "${configuration.prefix}$classMapping")
    }

    @EventHandler
    @Suppress("unused")
    private fun onResourceWrite(event: AssemblerEvents.ResourceWriteEvent) {
        val decodedManifest = event.resourceData.decodeToString()
        if (event.name != "META-INF/MANIFEST.MF" && !decodedManifest.contains("Main-Class")) {
            return
        }

        ObfuscatorServices.symbolRemapper.symbolMappings.filter { !it.key.contains(".") }.forEach { (previous, newest) ->
            val mainClassRegex = "(?<=[: ])${previous.replace("/", ".")}".toRegex()
            event.resourceData = mainClassRegex.replace(event.resourceData.decodeToString(), newest).toByteArray()
        }
    }

    override fun getExecutionPriority(): Int {
        return TransformerPriority.BELOW_MEDIUM
    }
}
