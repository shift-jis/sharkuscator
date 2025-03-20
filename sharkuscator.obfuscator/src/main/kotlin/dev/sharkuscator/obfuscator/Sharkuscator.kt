package dev.sharkuscator.obfuscator

import dev.sharkuscator.obfuscator.assembler.KlassResolvingDumper
import dev.sharkuscator.obfuscator.configuration.GsonConfiguration
import dev.sharkuscator.obfuscator.configuration.transformers.TransformerConfiguration
import dev.sharkuscator.obfuscator.extensions.toSnakeCase
import dev.sharkuscator.obfuscator.transformers.AbstractTransformer
import dev.sharkuscator.obfuscator.transformers.obfuscate.ClassEncryptionTransformer
import org.mapleir.app.service.ApplicationClassSource
import org.mapleir.app.service.LibraryClassSource
import org.mapleir.asm.ClassNode
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
    private val transformers = mutableListOf<AbstractTransformer<out TransformerConfiguration>>(
        ClassEncryptionTransformer(),
    )

    private lateinit var configuration: GsonConfiguration
    private lateinit var jarContents: JarContents<ClassNode>
    private lateinit var classSource: ApplicationClassSource

    fun obfuscate() {
        System.setOut(PrintStream(object : OutputStream() {
            override fun write(b: Int) {
            }
        }))

        configuration = importConfiguration()
        jarContents = downloadJarContents(inputJarFile)

        // TODO Support JMods
        classSource = ApplicationClassSource(inputJarFile.getName().drop(4), jarContents.classContents)
        classSource.addLibraries(resolveLibrary(classSource, File(System.getProperty("java.home"), "lib/jce.jar")))
        classSource.addLibraries(resolveLibrary(classSource, File(System.getProperty("java.home"), "lib/rt.jar")))

        SharedInstances.eventBus.registerLambdaFactory("dev.sharkuscator") { lookupInMethod, klass ->
            lookupInMethod.invoke(null, klass, MethodHandles.lookup()) as MethodHandles.Lookup
        }

        for (transformer in transformers.filter { configuration.transformers.has(it.getName().toSnakeCase()) }) {
            SharedInstances.eventBus.subscribe(transformer.apply { initialization(configuration) })
            SharedInstances.logger.info("${transformer.getName()} subscribed")
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
