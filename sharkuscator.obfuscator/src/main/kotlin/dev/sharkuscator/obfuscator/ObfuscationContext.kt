package dev.sharkuscator.obfuscator

import dev.sharkuscator.obfuscator.configuration.transformers.TransformerConfiguration
import dev.sharkuscator.obfuscator.dictionaries.DictionaryFactory
import dev.sharkuscator.obfuscator.dictionaries.MappingDictionary
import dev.sharkuscator.obfuscator.transformers.BaseTransformer
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
    val analysisContext: AnalysisContext
) {
    val defaultDictionary = DictionaryFactory.createDefaultDictionary<Any>()
    val isInputRecognizedAsMinecraftMod: Boolean get() = sharkuscator.isInputRecognizedAsMinecraftMod

    @Suppress("UNCHECKED_CAST")
    fun <T> resolveDictionary(targetType: Class<T>): MappingDictionary<Any> {
        return when (targetType.name) {
            "org.mapleir.asm.MethodNode" -> findTransformer(MethodRenameTransformer::class.java)?.dictionary ?: defaultDictionary
            "org.mapleir.asm.FieldNode" -> findTransformer(FieldRenameTransformer::class.java)?.dictionary ?: defaultDictionary
            "org.mapleir.asm.ClassNode" -> findTransformer(ClassRenameTransformer::class.java)?.dictionary ?: defaultDictionary
            else -> defaultDictionary
        } as MappingDictionary<Any>
    }

    fun <T : BaseTransformer<out TransformerConfiguration>> findTransformer(targetType: Class<T>): T? {
        @Suppress("UNCHECKED_CAST")
        return sharkuscator.registeredTransformers.find { it::class.java == targetType } as? T
    }

    class Builder {
        private lateinit var sharkuscator: Sharkuscator
        private lateinit var jarContents: JarContents<ClassNode>
        private lateinit var classSource: ApplicationClassSource
        private lateinit var analysisContext: AnalysisContext

        fun setSharkuscator(sharkuscator: Sharkuscator) {
            this.sharkuscator = sharkuscator
        }

        fun setJarContents(jarContents: JarContents<ClassNode>) {
            this.jarContents = jarContents
        }

        fun setClassSource(classSource: ApplicationClassSource) {
            this.classSource = classSource
        }

        fun setAnalysisContext(analysisContext: AnalysisContext) {
            this.analysisContext = analysisContext
        }

        fun build(): ObfuscationContext {
            return ObfuscationContext(sharkuscator, jarContents, classSource, analysisContext)
        }
    }
}