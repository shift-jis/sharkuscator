package dev.sharkuscator.obfuscator

import dev.sharkuscator.obfuscator.configuration.GsonConfiguration
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

class ObfuscationContext(
    val jarContents: JarContents<ClassNode>, val classSource: ApplicationClassSource,
    val hierarchyProvider: HierarchyProvider, val analysisContext: AnalysisContext,
    val configuration: GsonConfiguration, val exclusions: ExclusionRule,
    val isInputRecognizedAsMinecraftMod: Boolean
) {
    companion object {
        val defaultDictionary = DictionaryFactory.createDefaultDictionary<Any>()

        @Suppress("UNCHECKED_CAST")
        fun <T, E> resolveDictionary(targetType: Class<T>): MappingDictionary<E> {
            return when (targetType.name) {
                "org.mapleir.asm.MethodNode", "" -> MethodRenameTransformer.methodMappingDictionary
                "org.mapleir.asm.FieldNode" -> FieldRenameTransformer.fieldMappingDictionary
                "org.mapleir.asm.ClassNode" -> ClassRenameTransformer.classMappingDictionary
                else -> defaultDictionary
            } as MappingDictionary<E>
        }
    }
}
