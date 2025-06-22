package dev.sharkuscator.obfuscator.transformers.obfuscators.renamers

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
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
import dev.sharkuscator.obfuscator.transformers.TransformerStrength
import meteordevelopment.orbit.EventHandler

object ClassRenameTransformer : BaseTransformer<RenameConfiguration>("ClassRename", RenameConfiguration::class.java) {
    lateinit var classMappingDictionary: MappingDictionary<String>
    lateinit var generatedMixinPackageSegment: String
    lateinit var effectiveClassPrefix: String

    override fun initialization(configuration: GsonConfiguration): RenameConfiguration {
        val initializedConfiguration = super.initialization(configuration)

        classMappingDictionary = DictionaryFactory.createDictionary(initializedConfiguration.dictionary)
        generatedMixinPackageSegment = classMappingDictionary.generateNextName(null)

        val lengthOfLastSegmentPlusOne = initializedConfiguration.namePrefix.length - initializedConfiguration.namePrefix.lastIndexOf("/")
        val trimmedNamePrefix = initializedConfiguration.namePrefix.dropLast(lengthOfLastSegmentPlusOne - 1)
        effectiveClassPrefix = formatPrefixTemplate(trimmedNamePrefix.repeat(initializedConfiguration.prefixRepetitions))
        return this.configuration
    }

    @EventHandler
    @Suppress("unused")
    private fun onClassTransform(event: TransformerEvents.ClassTransformEvent) {
        if (!isEligibleForExecution() || !shouldTransformClass(event.context, event.anytypeNode)) {
            return
        }

        var targetPackagePath = effectiveClassPrefix
        if (event.anytypeNode.isSpongeMixin()) {
            targetPackagePath = when {
                targetPackagePath.isEmpty() -> "$generatedMixinPackageSegment/"
                targetPackagePath.endsWith("/") -> "$targetPackagePath$generatedMixinPackageSegment/"
                else -> "$targetPackagePath/$generatedMixinPackageSegment"
            }
        }

        var classMapping = classMappingDictionary.generateNextName(targetPackagePath)
        if (classMappingDictionary.generatesUnsafeNames() && (event.anytypeNode.containsMainMethod() || event.anytypeNode.isDeclaredAsAnnotation())) {
            classMapping = event.context.defaultDictionary.generateNextName(targetPackagePath)
        }

        ObfuscatorServices.symbolRemapper.setMapping(event.anytypeNode.name, "$targetPackagePath${configuration.namePrefix.substringAfterLast("/")}$classMapping")
    }

    @EventHandler
    @Suppress("unused")
    private fun onResourceWrite(event: AssemblerEvents.ResourceWriteEvent) {
        val resourceContentString = event.resourceData.decodeToString()
        when {
            event.name == "META-INF/MANIFEST.MF" && resourceContentString.contains("Main-Class") -> updateMainClassInManifest(event)
            event.name.startsWith("mixins") && event.name.endsWith(".json") -> updateClassNamesInMixinConfiguration(event, resourceContentString)
        }
    }

    override fun transformerStrength(): TransformerStrength {
        return TransformerStrength.MODERATE
    }

    override fun executionPriority(): Int {
        return TransformerPriority.TWENTY_FIVE
    }

    private fun updateMainClassInManifest(event: AssemblerEvents.ResourceWriteEvent) {
        ObfuscatorServices.symbolRemapper.symbolMappings.filter { !it.key.contains(".") }.forEach { (previous, newest) ->
            val mainClassPattern = "(?<=[: ])${previous.replace("/", ".")}".toRegex()
            event.resourceData = mainClassPattern.replace(event.resourceData.decodeToString(), newest).toByteArray()
        }
    }

    private fun updateClassNamesInMixinConfiguration(event: AssemblerEvents.ResourceWriteEvent, resourceContentString: String) {
        val mixinConfigJson = ObfuscatorServices.prettyGson.fromJson(resourceContentString, JsonObject::class.java)

        if (mixinConfigJson.has("package") && mixinConfigJson.get("package").isJsonPrimitive) {
            if (mixinConfigJson.has("client")) {
                updateClassNamesInMixinArray(mixinConfigJson.get("package").asString, mixinConfigJson.get("client").asJsonArray)
            }

            if (mixinConfigJson.has("mixins")) {
                updateClassNamesInMixinArray(mixinConfigJson.get("package").asString, mixinConfigJson.get("mixins").asJsonArray)
            }

            var configPrefixAsPackagePath = effectiveClassPrefix.replace("/", ".")
            if (configPrefixAsPackagePath.endsWith(".")) {
                configPrefixAsPackagePath = configPrefixAsPackagePath.dropLast(1)
            }

            var newMixinPackageDeclaration = generatedMixinPackageSegment
            if (configPrefixAsPackagePath.isNotEmpty()) {
                newMixinPackageDeclaration = "$configPrefixAsPackagePath.$generatedMixinPackageSegment"
            }

            mixinConfigJson.remove("package")
            mixinConfigJson.add("package", JsonPrimitive(newMixinPackageDeclaration))
        }

        if (mixinConfigJson.has("mappings") && mixinConfigJson.get("mappings").isJsonObject) {
            val classMappingsObject = mixinConfigJson.get("mappings").asJsonObject
            for (originalClassPathKey in classMappingsObject.keySet().toList()) {
                val newClassPathKey = ObfuscatorServices.symbolRemapper.symbolMappings[originalClassPathKey]
                if (newClassPathKey == null) {
                    continue
                }

                val classMappingValue = classMappingsObject.remove(originalClassPathKey)
                classMappingsObject.add(newClassPathKey, classMappingValue)
            }
        }

        if (mixinConfigJson.has("data") && mixinConfigJson.get("data").isJsonObject) {
            val referenceDataObject = mixinConfigJson.get("data").asJsonObject
            val intermediaryObject = referenceDataObject.get("named:intermediary").asJsonObject
            for (originalClassPathKey in intermediaryObject.keySet().toList()) {
                val newClassPathKey = ObfuscatorServices.symbolRemapper.symbolMappings[originalClassPathKey]
                if (newClassPathKey == null) {
                    continue
                }

                val classMappingValue = intermediaryObject.remove(originalClassPathKey)
                intermediaryObject.add(newClassPathKey, classMappingValue)
            }
        }

        event.resourceData = ObfuscatorServices.prettyGson.toJson(mixinConfigJson).toByteArray()
    }

    private fun updateClassNamesInMixinArray(mixinPackageName: String, mixinClassEntriesArray: JsonArray) {
        for ((entryIndex, classJsonElement) in mixinClassEntriesArray.withIndex()) {
            val originalClassPathKey = "${mixinPackageName}.${classJsonElement.asString}".replace(".", "/")
            val remappedClassPath = ObfuscatorServices.symbolRemapper.symbolMappings[originalClassPathKey] ?: originalClassPathKey
            mixinClassEntriesArray.set(entryIndex, JsonPrimitive(remappedClassPath.split("/").last()))
        }
    }

    private fun formatPrefixTemplate(template: String): String {
        val dictionaryPlaceholderRegex = "%dictionary%".toRegex()
        return dictionaryPlaceholderRegex.replace(template) {
            classMappingDictionary.generateNextName(null)
        }
    }
}
