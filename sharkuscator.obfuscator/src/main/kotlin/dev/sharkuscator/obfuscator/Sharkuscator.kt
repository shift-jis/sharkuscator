package dev.sharkuscator.obfuscator

import dev.sharkuscator.obfuscator.assembler.KlassResolvingDumper
import dev.sharkuscator.obfuscator.configuration.GsonConfiguration
import dev.sharkuscator.obfuscator.configuration.exclusions.ExclusionRule
import dev.sharkuscator.obfuscator.configuration.exclusions.MixedExclusionRule
import dev.sharkuscator.obfuscator.configuration.exclusions.StringExclusionRule
import dev.sharkuscator.obfuscator.extensions.toSnakeCase
import dev.sharkuscator.obfuscator.transformers.events.transforms.ClassTransformEvent
import dev.sharkuscator.obfuscator.transformers.events.transforms.FieldTransformEvent
import dev.sharkuscator.obfuscator.transformers.events.transforms.MethodTransformEvent
import dev.sharkuscator.obfuscator.transformers.events.transforms.ResourceTransformEvent
import dev.sharkuscator.obfuscator.transformers.obfuscators.ClassEncryptionTransformer
import dev.sharkuscator.obfuscator.transformers.obfuscators.ResourceRenamingTransformer
import org.mapleir.DefaultInvocationResolver
import org.mapleir.app.client.SimpleApplicationContext
import org.mapleir.app.service.ApplicationClassSource
import org.mapleir.app.service.LibraryClassSource
import org.mapleir.asm.ClassNode
import org.mapleir.context.AnalysisContext
import org.mapleir.context.BasicAnalysisContext.BasicContextBuilder
import org.mapleir.context.IRCache
import org.mapleir.deob.dataflow.LiveDataFlowAnalysisImpl
import org.mapleir.ir.cfg.builder.ControlFlowGraphBuilder
import org.topdank.byteengineer.commons.data.JarContents
import org.topdank.byteengineer.commons.data.JarInfo
import org.topdank.byteio.`in`.SingleJarDownloader
import java.io.File
import java.io.OutputStream
import java.io.PrintStream
import java.lang.invoke.MethodHandles
import java.nio.file.Path
import kotlin.io.path.readText


class Sharkuscator(private val configJsonPath: Path, private val inputJarFile: File, private val outputJarFile: File) {
    private val transformers = mutableListOf(
        ResourceRenamingTransformer(),
        ClassEncryptionTransformer(),
    )

    private lateinit var configuration: GsonConfiguration
    private lateinit var exclusions: ExclusionRule

    private lateinit var jarContents: JarContents<ClassNode>
    private lateinit var classSource: ApplicationClassSource
    private lateinit var analysis: AnalysisContext

    fun obfuscate() {
//        SharedInstances.logger.level = Level.DEBUG
        System.setOut(PrintStream(object : OutputStream() {
            override fun write(b: Int) {
            }
        }))

        configuration = importConfiguration()
        exclusions = MixedExclusionRule(configuration.exclusions.map { StringExclusionRule(it.toRegex()) })

        jarContents = downloadJarContents(inputJarFile)
        classSource = ApplicationClassSource(inputJarFile.getName().drop(4), jarContents.classContents)
        classSource.addLibraries(resolveLibrary(classSource, File(System.getProperty("java.home"), "lib/jce.jar")))
        classSource.addLibraries(resolveLibrary(classSource, File(System.getProperty("java.home"), "lib/rt.jar")))

        analysis = BasicContextBuilder().apply {
            setDataFlowAnalysis(LiveDataFlowAnalysisImpl(SharedInstances.irFactory))
            setCache(SharedInstances.irFactory)

            setInvocationResolver(DefaultInvocationResolver(classSource))
            setApplicationContext(SimpleApplicationContext(classSource))
            setApplication(classSource)
        }.build()

        SharedInstances.eventBus.registerLambdaFactory("dev.sharkuscator") { lookupInMethod, klass ->
            lookupInMethod.invoke(null, klass, MethodHandles.lookup()) as MethodHandles.Lookup
        }

        for (transformer in transformers.filter { configuration.transformers.has(it.getName().toSnakeCase()) }) {
            if (transformer.initialization(configuration) && transformer.isEnabled()) {
                SharedInstances.eventBus.subscribe(transformer.apply { initialization(configuration) })
                SharedInstances.logger.info("${transformer.getName()} subscribed")
            }
        }

        jarContents.resourceContents.namedMap().filter { !exclusions.excluded(it.key) }.forEach {
            SharedInstances.eventBus.post(ResourceTransformEvent(classSource, it.value.name, it.value.data))
        }

        jarContents.classContents.namedMap().filter { !exclusions.excluded(it.value) }.forEach { classContent ->
            SharedInstances.eventBus.post(ClassTransformEvent(jarContents, classSource, classContent.value))

            classContent.value.methods.filter { !exclusions.excluded(it) }.forEach {
                SharedInstances.eventBus.post(MethodTransformEvent(jarContents, classSource, it))
            }

            classContent.value.fields.filter { !exclusions.excluded(it) }.forEach {
                SharedInstances.eventBus.post(FieldTransformEvent(jarContents, classSource, it))
            }
        }

        SharedInstances.logger.info("Recompiling Class...")
        KlassResolvingDumper(jarContents, classSource).dump(outputJarFile)
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
}
