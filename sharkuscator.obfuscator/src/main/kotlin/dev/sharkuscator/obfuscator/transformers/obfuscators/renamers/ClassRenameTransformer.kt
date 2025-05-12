package dev.sharkuscator.obfuscator.transformers.obfuscators.renamers

import com.google.gson.JsonObject
import dev.sharkuscator.obfuscator.ObfuscatorServices
import dev.sharkuscator.obfuscator.configuration.GsonConfiguration
import dev.sharkuscator.obfuscator.configuration.transformers.RenameConfiguration
import dev.sharkuscator.obfuscator.dictionaries.DictionaryFactory
import dev.sharkuscator.obfuscator.dictionaries.MappingDictionary
import dev.sharkuscator.obfuscator.events.AssemblerEvents
import dev.sharkuscator.obfuscator.events.TransformerEvents
import dev.sharkuscator.obfuscator.extensions.containsMainMethod
import dev.sharkuscator.obfuscator.extensions.isDeclaredAsAnnotation
import dev.sharkuscator.obfuscator.extensions.isSpongeMixin
import dev.sharkuscator.obfuscator.transformers.BaseTransformer
import dev.sharkuscator.obfuscator.transformers.TransformerPriority
import meteordevelopment.orbit.EventHandler
import org.mapleir.asm.ClassNode

class ClassRenameTransformer : BaseTransformer<RenameConfiguration>("ClassRename", RenameConfiguration::class.java) {
    lateinit var dictionary: MappingDictionary<ClassNode>

    override fun initialization(configuration: GsonConfiguration): RenameConfiguration {
        dictionary = DictionaryFactory.createDictionary(super.initialization(configuration).dictionary)
        return this.configuration
    }

    @EventHandler
    @Suppress("unused")
    private fun onClassTransform(event: TransformerEvents.ClassTransformEvent) {
        if (transformed || event.eventNode.isSpongeMixin() || event.context.classSource.isLibraryClass(event.eventNode.name)) {
            return
        }

        var classMapping = dictionary.generateNextName(null)
        if (dictionary.generatesUnsafeNames() && (event.eventNode.containsMainMethod() || event.eventNode.isDeclaredAsAnnotation())) {
            classMapping = event.context.defaultDictionary.generateNextName(null)
        }

        ObfuscatorServices.symbolRemapper.setMapping(event.eventNode.name, "${configuration.prefix}$classMapping")
    }

    @EventHandler
    @Suppress("unused")
    private fun onResourceWrite(event: AssemblerEvents.ResourceWriteEvent) {
        val resourceContentString = event.resourceData.decodeToString()
        if (event.name == "META-INF/MANIFEST.MF" && resourceContentString.contains("Main-Class")) {
            ObfuscatorServices.symbolRemapper.symbolMappings.filter { !it.key.contains(".") }.forEach { (previous, newest) ->
                val mainClassPattern = "(?<=[: ])${previous.replace("/", ".")}".toRegex()
                event.resourceData = mainClassPattern.replace(event.resourceData.decodeToString(), newest).toByteArray()
            }
        } else if (event.name.startsWith("mixins") && event.name.endsWith(".json")) {
            ObfuscatorServices.jsonProcessor.fromJson(resourceContentString, JsonObject::class.java)
//            if (mixinConfigObject.has("package")) {
//            }
        }
    }

    override fun getExecutionPriority(): Int {
        return TransformerPriority.BELOW_MEDIUM
    }
}
