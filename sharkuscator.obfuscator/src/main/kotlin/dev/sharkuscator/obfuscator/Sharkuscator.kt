package dev.sharkuscator.obfuscator

import com.google.common.collect.ImmutableList
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dev.sharkuscator.obfuscator.assembler.KlassResolvingDumper
import dev.sharkuscator.obfuscator.assembler.KlassWriter
import dev.sharkuscator.obfuscator.configuration.GsonConfiguration
import dev.sharkuscator.obfuscator.configuration.transformers.TransformerConfiguration
import dev.sharkuscator.obfuscator.extensions.toSnakeCase
import dev.sharkuscator.obfuscator.transformers.SharkTransformer
import dev.sharkuscator.obfuscator.transformers.obfuscate.ClassEncryptionTransformer
import dev.sharkuscator.obfuscator.transformers.obfuscate.ExampleTransformer
import org.apache.log4j.LogManager
import org.apache.log4j.Logger
import org.mapleir.app.service.ApplicationClassSource
import org.mapleir.app.service.CompleteResolvingJarDumper
import org.mapleir.app.service.LibraryClassSource
import org.mapleir.asm.ClassNode
import org.objectweb.asm.ClassWriter
import org.topdank.byteengineer.commons.data.JarContents
import org.topdank.byteengineer.commons.data.JarInfo
import org.topdank.byteio.`in`.SingleJarDownloader
import java.io.File
import java.io.FileOutputStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.util.jar.JarEntry
import java.util.jar.JarOutputStream
import java.util.zip.ZipEntry
import kotlin.io.path.readText


class Sharkuscator(
    private val configJsonPath: Path,
    private val inputJarFile: File,
    private val outputJarFile: File,
) {
    companion object {
        val logger: Logger = LogManager.getLogger("(Sharkuscator)")
        val gson: Gson = GsonBuilder().create()
    }

    private val loadedNatives = mutableListOf<String>()
    private val transformers = mutableListOf(
        ExampleTransformer(),
        ClassEncryptionTransformer(),
    )

    private lateinit var configuration: GsonConfiguration
    private lateinit var jarContents: JarContents<ClassNode>
    private lateinit var classSource: ApplicationClassSource

    fun obfuscate() {
        configuration = importConfiguration()
        jarContents = downloadJarContents(inputJarFile)

        // TODO Support JMods
        classSource = ApplicationClassSource(inputJarFile.getName().drop(4), jarContents.classContents)
        classSource.addLibraries(resolveLibraries(classSource, File(System.getProperty("java.home"), "lib/jce.jar")))
        classSource.addLibraries(resolveLibraries(classSource, File(System.getProperty("java.home"), "lib/rt.jar")))

        KlassResolvingDumper(jarContents, classSource).dump(outputJarFile)
    }

    private fun importConfiguration(): GsonConfiguration {
        try {
            logger.info("Importing configuration...")
            return gson.fromJson(configJsonPath.readText(), GsonConfiguration::class.java)
        } catch (exception: Exception) {
            exception.printStackTrace()
            return GsonConfiguration()
        }
    }

    private fun initializeTransformers(): ImmutableList<SharkTransformer<TransformerConfiguration>> {
        val builder = ImmutableList.builder<SharkTransformer<TransformerConfiguration>>()
        for (transformer in transformers.filter { configuration.transformers.has(it.getName().toSnakeCase()) }) {
            builder.add(transformer.apply { initialization(configuration) })
        }
        return builder.build()
    }

    private fun resolveLibraries(classSource: ApplicationClassSource, libraryFile: File): LibraryClassSource {
        return LibraryClassSource(classSource, downloadJarContents(libraryFile).classContents)
    }

    private fun downloadJarContents(jarFile: File): JarContents<ClassNode> {
        return SingleJarDownloader<ClassNode>(JarInfo(jarFile)).apply { download() }.jarContents
    }

    private fun useNativeLibrary(nativeLibrary: String): Boolean {
        if (loadedNatives.contains(nativeLibrary)) {
            return true
        }

        var absolutePath = extractResource(nativeLibrary, ".dll") ?: return false
        absolutePath = absolutePath.replace("\\", "/")

        val libraryDirectory = absolutePath.split("/").dropLast(1).joinToString("/")
        System.setProperty("java.library.path", "${System.getProperty("java.library.path")};${libraryDirectory};")
        logger.info(System.getProperty("java.library.path"))

        try {
            ClassLoader::class.java.getDeclaredField("sys_paths").apply {
                isAccessible = true
                set(null, null)
            }
        } catch (exception: Exception) {
            exception.printStackTrace()
            return false
        }

        System.loadLibrary(absolutePath.split("/").last().split(".dll").first())
        loadedNatives.add(nativeLibrary)
        return true
    }

    private fun extractResource(name: String, suffix: String): String? {
        val inputStream = Sharkuscator::class.java.getResourceAsStream(name) ?: return null
        val extractedFile = File.createTempFile(System.nanoTime().toString(), suffix)
        Files.copy(inputStream, extractedFile.toPath(), StandardCopyOption.REPLACE_EXISTING)
        inputStream.close()
        return extractedFile.absolutePath
    }
}
