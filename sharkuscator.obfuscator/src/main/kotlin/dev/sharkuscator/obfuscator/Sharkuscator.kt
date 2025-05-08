package dev.sharkuscator.obfuscator

import dev.sharkuscator.obfuscator.assembler.ResolvingDumper
import dev.sharkuscator.obfuscator.configuration.GsonConfiguration
import dev.sharkuscator.obfuscator.configuration.exclusions.AnnotationExclusionRule
import dev.sharkuscator.obfuscator.configuration.exclusions.ExclusionRule
import dev.sharkuscator.obfuscator.configuration.exclusions.MixedExclusionRule
import dev.sharkuscator.obfuscator.configuration.exclusions.StringExclusionRule
import dev.sharkuscator.obfuscator.events.ObfuscatorEvents
import dev.sharkuscator.obfuscator.events.TransformerEvents
import dev.sharkuscator.obfuscator.extensions.toSnakeCase
import dev.sharkuscator.obfuscator.transformers.obfuscators.DynamicInvokeTransformer
import dev.sharkuscator.obfuscator.transformers.obfuscators.NativeObfuscateTransformer
import dev.sharkuscator.obfuscator.transformers.obfuscators.SyntheticAccessTransformer
import dev.sharkuscator.obfuscator.transformers.obfuscators.constants.LongConstantEncryptionTransformer
import dev.sharkuscator.obfuscator.transformers.obfuscators.constants.NumberComplexityTransformer
import dev.sharkuscator.obfuscator.transformers.obfuscators.constants.StringEncryptionTransformer
import dev.sharkuscator.obfuscator.transformers.obfuscators.renamers.ClassRenameTransformer
import dev.sharkuscator.obfuscator.transformers.obfuscators.renamers.FieldRenameTransformer
import dev.sharkuscator.obfuscator.transformers.obfuscators.renamers.MethodRenameTransformer
import dev.sharkuscator.obfuscator.transformers.obfuscators.renamers.ReflectRenameTransformer
import dev.sharkuscator.obfuscator.transformers.obfuscators.renamers.ResourceRenameTransformer
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
        ReflectRenameTransformer(),

        LongConstantEncryptionTransformer(),
        StringEncryptionTransformer(),
        NumberComplexityTransformer(),
        SyntheticAccessTransformer(),
        DynamicInvokeTransformer(),
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
        if (!inputJarFile.exists()) {
            ObfuscatorServices.sharkLogger.error("Input jar does not exist!")
            return
        }

        configuration = importConfiguration()
        exclusions = MixedExclusionRule(buildList {
            addAll(configuration.exclusions.map { StringExclusionRule(it.toRegex()) })
            add(AnnotationExclusionRule())
        })

        jarContents = loadJarContents(inputJarFile)
        classSource = ApplicationClassSource(inputJarFile.getName().drop(4), jarContents.classContents)
        classSource.addLibraries(loadLibrarySource(classSource, File(System.getProperty("java.home"), "lib/jce.jar")))
        classSource.addLibraries(loadLibrarySource(classSource, File(System.getProperty("java.home"), "lib/rt.jar")))

        val analysisContext = createAnalysisContext()
        val eventContext = createObfuscationContext(analysisContext)
        transformers.sortBy { it.getPriority() }

        ObfuscatorServices.mainEventBus.registerLambdaFactory("dev.sharkuscator") { lookupInMethod, klass ->
            lookupInMethod.invoke(null, klass, MethodHandles.lookup()) as MethodHandles.Lookup
        }

        for (transformer in transformers.filter { configuration.transformers.has(it.getName().toSnakeCase()) }) {
            if (transformer.initialization(configuration).enabled && transformer.isEnabled()) {
                ObfuscatorServices.mainEventBus.subscribe(transformer)
                dispatchTransformEvents(eventContext)
                transformer.transformed = true
            }
        }

        ObfuscatorServices.sharkLogger.info("Translating SSA IR to standard flavour")
        for ((methodNode, controlFlowGraph) in analysisContext.irCache.entries) {
            controlFlowGraph.verify()

            BoissinotDestructor.leaveSSA(controlFlowGraph)
            LocalsReallocator.realloc(controlFlowGraph)

            ControlFlowGraphDumper(controlFlowGraph, methodNode).dump()
        }

        ObfuscatorServices.sharkLogger.info("Recompiling Class...")
        ResolvingDumper(jarContents, classSource, exclusions).dump(outputJarFile)
        ObfuscatorServices.mainEventBus.post(ObfuscatorEvents.FinalizationEvents(eventContext, inputJarFile, outputJarFile))
    }

    private fun dispatchTransformEvents(obfuscationContext: ObfuscationContext) {
        ObfuscatorServices.mainEventBus.post(ObfuscatorEvents.InitializationEvents(obfuscationContext, inputJarFile, outputJarFile))

        jarContents.resourceContents.namedMap().filter { !exclusions.excluded(it.key) }.forEach {
            ObfuscatorServices.mainEventBus.post(TransformerEvents.ResourceTransformEvent(obfuscationContext, it.value.name, it.value.data))
        }

        jarContents.classContents.namedMap().filter { !exclusions.excluded(it.value) }.forEach { classContent ->
            ObfuscatorServices.mainEventBus.post(TransformerEvents.ClassTransformEvent(obfuscationContext, classContent.value))

            classContent.value.fields.filter { !exclusions.excluded(it) }.forEach {
                ObfuscatorServices.mainEventBus.post(TransformerEvents.FieldTransformEvent(obfuscationContext, it))
            }

            classContent.value.methods.filter { !exclusions.excluded(it) }.forEach {
                ObfuscatorServices.mainEventBus.post(TransformerEvents.MethodTransformEvent(obfuscationContext, it))
            }
        }
    }

    private fun importConfiguration(): GsonConfiguration {
        try {
            ObfuscatorServices.sharkLogger.info("Importing configuration...")
            return ObfuscatorServices.jsonProcessor.fromJson(configJsonPath.readText(), GsonConfiguration::class.java)
        } catch (exception: Exception) {
            exception.printStackTrace()
            return GsonConfiguration()
        }
    }

    private fun loadLibrarySource(classSource: ApplicationClassSource, libraryFile: File): LibraryClassSource {
        return LibraryClassSource(classSource, loadJarContents(libraryFile).classContents)
    }

    private fun loadJarContents(jarFile: File): JarContents<ClassNode> {
        return SingleJarDownloader<ClassNode>(JarInfo(jarFile)).apply { download() }.jarContents
    }

    private fun createAnalysisContext(): AnalysisContext {
        return BasicContextBuilder().apply {
            setDataFlowAnalysis(LiveDataFlowAnalysisImpl(ObfuscatorServices.controlFlowGraphCache))
            setCache(ObfuscatorServices.controlFlowGraphCache)

            setInvocationResolver(DefaultInvocationResolver(classSource))
            setApplicationContext(SimpleApplicationContext(classSource))
            setApplication(classSource)
        }.build()
    }

    private fun createObfuscationContext(analysisContext: AnalysisContext): ObfuscationContext {
        return ObfuscationContext.Builder().apply {
            setSharkuscator(this@Sharkuscator)
            setAnalysisContext(analysisContext)
            setClassSource(classSource)
            setJarContents(jarContents)
        }.build()
    }
}
