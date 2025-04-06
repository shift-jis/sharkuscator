package dev.sharkuscator.obfuscator.transformers.events

import dev.sharkuscator.obfuscator.Sharkuscator
import dev.sharkuscator.obfuscator.configuration.transformers.TransformerConfiguration
import dev.sharkuscator.obfuscator.transformers.AbstractTransformer
import org.mapleir.app.service.ApplicationClassSource
import org.mapleir.asm.ClassNode
import org.mapleir.context.AnalysisContext
import org.topdank.byteengineer.commons.data.JarContents

class EventContext private constructor(private val sharkuscator: Sharkuscator, val jarContents: JarContents<ClassNode>, val classSource: ApplicationClassSource, val analysisContext: AnalysisContext) {
    fun <T : AbstractTransformer<out TransformerConfiguration>> findTransformer(clazz: Class<T>): T? {
        @Suppress("UNCHECKED_CAST")
        return sharkuscator.transformers.find { it::class.java == clazz } as? T
    }

    class EventContextBuilder {
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

        fun build(): EventContext {
            return EventContext(sharkuscator, jarContents, classSource, analysisContext)
        }
    }
}
