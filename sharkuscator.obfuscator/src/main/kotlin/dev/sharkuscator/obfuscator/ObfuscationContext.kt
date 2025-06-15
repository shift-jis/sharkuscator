package dev.sharkuscator.obfuscator

import dev.sharkuscator.obfuscator.configuration.exclusions.ExclusionRule
import dev.sharkuscator.obfuscator.dictionaries.DictionaryFactory
import dev.sharkuscator.obfuscator.dictionaries.MappingDictionary
import dev.sharkuscator.obfuscator.hierarchies.HierarchyProvider
import dev.sharkuscator.obfuscator.transformers.obfuscators.renamers.ClassRenameTransformer
import dev.sharkuscator.obfuscator.transformers.obfuscators.renamers.FieldRenameTransformer
import dev.sharkuscator.obfuscator.transformers.obfuscators.renamers.MethodRenameTransformer
import org.mapleir.app.service.ApplicationClassSource
import org.mapleir.asm.ClassNode
import org.mapleir.context.AnalysisContext
import org.topdank.byteengineer.commons.data.JarContents

class ObfuscationContext private constructor(
    private val sharkuscator: Sharkuscator,
    val jarContents: JarContents<ClassNode>,
    val classSource: ApplicationClassSource,
    val hierarchyProvider: HierarchyProvider,
    val analysisContext: AnalysisContext,
    val exclusions: ExclusionRule,
) {
    val defaultDictionary = DictionaryFactory.createDefaultDictionary<Any>()
    val isInputRecognizedAsMinecraftMod: Boolean get() = sharkuscator.isInputRecognizedAsMinecraftMod

    @Suppress("UNCHECKED_CAST")
    fun <T> resolveDictionary(targetType: Class<T>): MappingDictionary<Any> {
        return when (targetType.name) {
            "org.mapleir.asm.MethodNode" -> MethodRenameTransformer.dictionary
            "org.mapleir.asm.FieldNode" -> FieldRenameTransformer.dictionary
            "org.mapleir.asm.ClassNode" -> ClassRenameTransformer.dictionary
            else -> defaultDictionary
        } as MappingDictionary<Any>
    }

    class Builder {
        private lateinit var sharkuscator: Sharkuscator
        private lateinit var jarContents: JarContents<ClassNode>
        private lateinit var classSource: ApplicationClassSource
        private lateinit var hierarchyProvider: HierarchyProvider
        private lateinit var analysisContext: AnalysisContext
        private lateinit var exclusions: ExclusionRule

        fun setSharkuscator(sharkuscator: Sharkuscator) {
            this.sharkuscator = sharkuscator
        }

        fun setJarContents(jarContents: JarContents<ClassNode>) {
            this.jarContents = jarContents
        }

        fun setClassSource(classSource: ApplicationClassSource) {
            this.classSource = classSource
        }

        fun setHierarchyProvider(hierarchyProvider: HierarchyProvider) {
            this.hierarchyProvider = hierarchyProvider
        }

        fun setAnalysisContext(analysisContext: AnalysisContext) {
            this.analysisContext = analysisContext
        }

        fun setExclusions(exclusions: ExclusionRule) {
            this.exclusions = exclusions
        }

        fun build(): ObfuscationContext {
            return ObfuscationContext(sharkuscator, jarContents, classSource, hierarchyProvider, analysisContext, exclusions)
        }
    }
}
