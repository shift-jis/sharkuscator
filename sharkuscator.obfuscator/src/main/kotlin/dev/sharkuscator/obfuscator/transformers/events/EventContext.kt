package dev.sharkuscator.obfuscator.transformers.events

import org.mapleir.app.service.ApplicationClassSource
import org.mapleir.asm.ClassNode
import org.mapleir.context.AnalysisContext
import org.topdank.byteengineer.commons.data.JarContents

class EventContext private constructor(val jarContents: JarContents<ClassNode>, val classSource: ApplicationClassSource, val analysisContext: AnalysisContext) {
    class EventContextBuilder {
        private lateinit var jarContents: JarContents<ClassNode>
        private lateinit var classSource: ApplicationClassSource
        private lateinit var analysisContext: AnalysisContext

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
            return EventContext(jarContents, classSource, analysisContext)
        }
    }
}
