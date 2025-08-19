package dev.sharkuscator.obfuscator

import dev.sharkuscator.commons.downloaders.DownloadedLibrary
import dev.sharkuscator.commons.providers.ApplicationClassProvider
import dev.sharkuscator.obfuscator.configuration.GsonConfiguration
import dev.sharkuscator.obfuscator.configuration.exclusions.ExclusionRule
import dev.sharkuscator.obfuscator.dictionaries.DictionaryFactory
import dev.sharkuscator.obfuscator.dictionaries.MappingDictionary
import dev.sharkuscator.obfuscator.hierarchies.HierarchyProvider
import dev.sharkuscator.obfuscator.transformers.obfuscators.renamers.ClassRenameTransformer
import dev.sharkuscator.obfuscator.transformers.obfuscators.renamers.FieldRenameTransformer
import dev.sharkuscator.obfuscator.transformers.obfuscators.renamers.MethodRenameTransformer

class ObfuscationContext(
    val downloadedLibrary: DownloadedLibrary, val classNodeProvider: ApplicationClassProvider, val hierarchyProvider: HierarchyProvider,
    val configuration: GsonConfiguration, val exclusions: ExclusionRule, val isRecognizedAsMinecraftMod: Boolean
) {
    companion object {
        val defaultDictionary = DictionaryFactory.createDefaultDictionary<Any>()

        @Suppress("UNCHECKED_CAST")
        fun <T, E> resolveDictionary(targetType: Class<T>): MappingDictionary<E> {
            return when (targetType.name) {
                "org.objectweb.asm.tree.MethodNode" -> MethodRenameTransformer.methodMappingDictionary
                "org.objectweb.asm.tree.FieldNode" -> FieldRenameTransformer.fieldMappingDictionary
                "org.objectweb.asm.tree.ClassNode" -> ClassRenameTransformer.classMappingDictionary
                else -> defaultDictionary
            } as MappingDictionary<E>
        }
    }
}
