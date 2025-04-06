package dev.sharkuscator.obfuscator

import dev.sharkuscator.obfuscator.assembler.ClassResolvingDumper
import dev.sharkuscator.obfuscator.configuration.GsonConfiguration
import dev.sharkuscator.obfuscator.configuration.exclusions.AnnotationExclusionRule
import dev.sharkuscator.obfuscator.configuration.exclusions.ExclusionRule
import dev.sharkuscator.obfuscator.configuration.exclusions.MixedExclusionRule
import dev.sharkuscator.obfuscator.configuration.exclusions.StringExclusionRule
import dev.sharkuscator.obfuscator.extensions.toSnakeCase
import dev.sharkuscator.obfuscator.transformers.events.EventContext
import dev.sharkuscator.obfuscator.transformers.events.ObfuscatorEvent
import dev.sharkuscator.obfuscator.transformers.events.transforming.ClassTransformEvent
import dev.sharkuscator.obfuscator.transformers.events.transforming.FieldTransformEvent
import dev.sharkuscator.obfuscator.transformers.events.transforming.MethodTransformEvent
import dev.sharkuscator.obfuscator.transformers.events.transforming.ResourceTransformEvent
import dev.sharkuscator.obfuscator.transformers.obfuscators.NativeObfuscateTransformer
import dev.sharkuscator.obfuscator.transformers.obfuscators.SyntheticAccessTransformer
import dev.sharkuscator.obfuscator.transformers.obfuscators.remaing.ClassRenameTransformer
import dev.sharkuscator.obfuscator.transformers.obfuscators.remaing.FieldRenameTransformer
import dev.sharkuscator.obfuscator.transformers.obfuscators.remaing.MethodRenameTransformer
import dev.sharkuscator.obfuscator.transformers.obfuscators.remaing.ResourceRenameTransformer
import dev.sharkuscator.obfuscator.transformers.obfuscators.strings.StringEncryptionTransformer
import dev.sharkuscator.obfuscator.transformers.shrinkers.LocalVariableRemoveTransformer
import dev.sharkuscator.obfuscator.transformers.shrinkers.SourceStripperTransformer
import org.mapleir.DefaultInvocationResolver
import org.mapleir.app.client.SimpleApplicationContext
import org.mapleir.app.service.ApplicationClassSource
import org.mapleir.app.service.LibraryClassSource
import org.mapleir.asm.ClassNode
import org.mapleir.context.AnalysisContext
import org.mapleir.context.BasicAnalysisContext.BasicContextBuilder
import org.mapleir.deob.dataflow.LiveDataFlowAnalysisImpl
import org.mapleir.ir.algorithms.BoissinotDestructor
import org.mapleir.ir.algorithms.LocalsReallocator
import org.mapleir.ir.codegen.ControlFlowGraphDumper
import org.topdank.byteengineer.commons.data.JarContents
import org.topdank.byteengineer.commons.data.JarInfo
import org.topdank.byteio.`in`.SingleJarDownloader
import java.io.File
import java.lang.invoke.MethodHandles
import java.nio.file.Path
import kotlin.io.path.readText


class Sharkuscator(private val configJsonPath: Path, private val inputJarFile: File, private val outputJarFile: File) {
    val transformers = mutableListOf(
        // obfuscates
        ClassRenameTransformer(),
        FieldRenameTransformer(),
        MethodRenameTransformer(),
        ResourceRenameTransformer(),

        StringEncryptionTransformer(),
        SyntheticAccessTransformer(),
        NativeObfuscateTransformer(),

        // shrinks
        LocalVariableRemoveTransformer(),
        SourceStripperTransformer(),
    )

    private lateinit var configuration: GsonConfiguration
    private lateinit var exclusions: ExclusionRule

    private lateinit var jarContents: JarContents<ClassNode>
    private lateinit var classSource: ApplicationClassSource

    fun obfuscate() {
//        SharedInstances.logger.level = Level.DEBUG
//        System.setErr(PrintStream(NullOutputStream()))

        configuration = importConfiguration()
        exclusions = MixedExclusionRule(buildList {
            addAll(configuration.exclusions.map { StringExclusionRule(it.toRegex()) })
            add(AnnotationExclusionRule())
        })

        jarContents = downloadJarContents(inputJarFile)
        classSource = ApplicationClassSource(inputJarFile.getName().drop(4), jarContents.classContents)
        classSource.addLibraries(resolveLibrary(classSource, File(System.getProperty("java.home"), "lib/jce.jar")))
        classSource.addLibraries(resolveLibrary(classSource, File(System.getProperty("java.home"), "lib/rt.jar")))

        val analysisContext = buildAnalysisContext()
        val eventContext = buildEventContext(analysisContext)

        SharedInstances.eventBus.registerLambdaFactory("dev.sharkuscator") { lookupInMethod, klass ->
            lookupInMethod.invoke(null, klass, MethodHandles.lookup()) as MethodHandles.Lookup
        }

        for (transformer in transformers.filter { configuration.transformers.has(it.getName().toSnakeCase()) }) {
            if (transformer.initialization(configuration).enabled && transformer.isEnabled()) {
                SharedInstances.eventBus.subscribe(transformer.apply { initialization(configuration) })
                SharedInstances.logger.debug("${transformer.getName()} subscribed")
            }
        }

        SharedInstances.eventBus.post(ObfuscatorEvent.InitializationEvent(eventContext, inputJarFile, outputJarFile))

        jarContents.resourceContents.namedMap().filter { !exclusions.excluded(it.key) }.forEach {
            SharedInstances.eventBus.post(ResourceTransformEvent(classSource, it.value.name, it.value.data))
        }

        jarContents.classContents.namedMap().filter { !exclusions.excluded(it.value) }.forEach { classContent ->
            SharedInstances.eventBus.post(ClassTransformEvent(eventContext, classContent.value))

            classContent.value.methods.filter { !exclusions.excluded(it) }.forEach {
                SharedInstances.eventBus.post(MethodTransformEvent(eventContext, it))
            }

            classContent.value.fields.filter { !exclusions.excluded(it) }.forEach {
                SharedInstances.eventBus.post(FieldTransformEvent(eventContext, it))
            }
        }

        SharedInstances.logger.info("Translating SSA IR to standard flavour")
        for ((methodNode, controlFlowGraph) in analysisContext.irCache.entries) {
            controlFlowGraph.verify()

            BoissinotDestructor.leaveSSA(controlFlowGraph)
            LocalsReallocator.realloc(controlFlowGraph)

            ControlFlowGraphDumper(controlFlowGraph, methodNode).dump()
        }

        SharedInstances.logger.info("Recompiling Class...")
        ClassResolvingDumper(jarContents, classSource, exclusions).dump(outputJarFile)
        SharedInstances.eventBus.post(ObfuscatorEvent.FinalizationEvent(eventContext, inputJarFile, outputJarFile))
    }

    private fun importConfiguration(): GsonConfiguration {
        try {
            SharedInstances.logger.info("Importing configuration...")
            return SharedInstances.gson.fromJson(configJsonPath.readText(), GsonConfiguration::class.java)
        } catch (exception: Exception) {
            exception.printStackTrace()
            return GsonConfiguration()
        }
    }

    private fun resolveLibrary(classSource: ApplicationClassSource, libraryFile: File): LibraryClassSource {
        return LibraryClassSource(classSource, downloadJarContents(libraryFile).classContents)
    }

    private fun downloadJarContents(jarFile: File): JarContents<ClassNode> {
        return SingleJarDownloader<ClassNode>(JarInfo(jarFile)).apply { download() }.jarContents
    }

    private fun buildAnalysisContext(): AnalysisContext {
        return BasicContextBuilder().apply {
            setDataFlowAnalysis(LiveDataFlowAnalysisImpl(SharedInstances.irFactory))
            setCache(SharedInstances.irFactory)

            setInvocationResolver(DefaultInvocationResolver(classSource))
            setApplicationContext(SimpleApplicationContext(classSource))
            setApplication(classSource)
        }.build()
    }

    private fun buildEventContext(analysisContext: AnalysisContext): EventContext {
        return EventContext.EventContextBuilder().apply {
            setSharkuscator(this@Sharkuscator)
            setAnalysisContext(analysisContext)
            setClassSource(classSource)
            setJarContents(jarContents)
        }.build()
    }
}
