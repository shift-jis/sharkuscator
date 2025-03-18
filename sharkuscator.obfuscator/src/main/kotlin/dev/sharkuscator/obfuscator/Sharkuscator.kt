package dev.sharkuscator.obfuscator

import com.google.gson.FormattingStyle
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dev.sharkuscator.obfuscator.assembly.SharkClassNode
import dev.sharkuscator.obfuscator.classsource.ZipImportResult
import dev.sharkuscator.obfuscator.configuration.JsonConfiguration
import dev.sharkuscator.obfuscator.encryption.ClassEncrypter
import dev.sharkuscator.obfuscator.hierarchies.DefaultHierarchy
import dev.sharkuscator.obfuscator.hierarchies.HierarchyCache
import org.apache.log4j.LogManager
import org.apache.log4j.Logger
import org.mapleir.asm.ClassNode
import org.mapleir.asm.FieldNode
import org.mapleir.asm.MethodNode
import org.objectweb.asm.ClassReader
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.jar.JarFile
import kotlin.io.path.readText

object Sharkuscator {
    private val logger: Logger = LogManager.getLogger("(Sharkuscator)")

    private val loadedLibraries: MutableList<String> = mutableListOf()
    private val hierarchy: HierarchyCache = DefaultHierarchy()

    private val prettyStyle = FormattingStyle.PRETTY.withIndent("    ")
    val gson: Gson = GsonBuilder().setFormattingStyle(prettyStyle).create()

    private lateinit var configuration: JsonConfiguration
    private lateinit var sharkSession: SharkSession
    private lateinit var importResult: ZipImportResult

    fun obfuscate(sharkSession: SharkSession) {
        this.sharkSession = sharkSession
        this.configuration = importConfiguration()
        this.importResult = importZipEntries()
    }

    private fun importConfiguration(): JsonConfiguration {
        try {
            logger.info("Importing configuration...")
            return gson.fromJson(sharkSession.configurationFile.readText(), JsonConfiguration::class.java)
        } catch (exception: Exception) {
            exception.printStackTrace()
            return JsonConfiguration();
        }
    }

    private fun importZipEntries(): ZipImportResult {
        val classNodes = mutableMapOf<String, SharkClassNode>()
        val resources = mutableMapOf<String, ByteArray>()
        val packages = mutableMapOf<String, ByteArray>()
        logger.info("Importing jar entries...")

        try {
            val inputJarFile = JarFile(sharkSession.inputJarPath.toFile())
            for (sortedEntry in inputJarFile.entries().toList().sortedBy { !it.name.split("/").last().contains(".") }) {
                val streamData = inputJarFile.getInputStream(sortedEntry).readBytes()
                if (sortedEntry.name.endsWith(".class")) {
                    classNodes[sortedEntry.name] = SharkClassNode(ClassNode().apply {
                        ClassReader(streamData).accept(node, ClassReader.EXPAND_FRAMES)
                        node.methods.forEach { methods.add(MethodNode(it, this)) }
                        node.fields.forEach { fields.add(FieldNode(it, this)) }
                    })
                } else if (!sortedEntry.name.endsWith("/")) {
                    resources[sortedEntry.name] = streamData
                } else {
                    packages[sortedEntry.name] = streamData
                }
            }
        } catch (exception: Exception) {
            exception.printStackTrace()
        }

        return ZipImportResult(classNodes, resources, packages)
    }

    private fun useNativeLibrary(nativeLibrary: String): Boolean {
        if (loadedLibraries.contains(nativeLibrary)) {
            return true
        }

        var absolutePath = extractResource(nativeLibrary, ".dll") ?: return false
        absolutePath = absolutePath.replace("\\", "/")

        val libraryDirectory = absolutePath.split("/").dropLast(1).joinToString("/")
        System.setProperty("java.library.path", "${System.getProperty("java.library.path")};${libraryDirectory};")
        logger.info(System.getProperty("java.library.path"))

        try {
            val declaredField = ClassLoader::class.java.getDeclaredField("sys_paths")
            declaredField.setAccessible(true)
            declaredField.set(null, null)
        } catch (exception: Exception) {
            exception.printStackTrace()
            return false;
        }

        System.loadLibrary(absolutePath.split("/").last().split(".dll").first())
        loadedLibraries.add(nativeLibrary)
        return true
    }

    private fun extractResource(name: String, suffix: String): String? {
        val inputStream = Sharkuscator.javaClass.getResourceAsStream(name) ?: return null
        val extractedFile = File.createTempFile(System.nanoTime().toString(), suffix)
        Files.copy(inputStream, extractedFile.toPath(), StandardCopyOption.REPLACE_EXISTING)
        inputStream.close()
        return extractedFile.absolutePath
    }
}
