package dev.sharkuscator.obfuscator

import com.google.gson.FormattingStyle
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dev.sharkuscator.obfuscator.assembly.SharkClassNode
import dev.sharkuscator.obfuscator.classsource.ZipImportResult
import dev.sharkuscator.obfuscator.configuration.JsonConfiguration
import dev.sharkuscator.obfuscator.hierarchies.DefaultHierarchy
import dev.sharkuscator.obfuscator.hierarchies.HierarchyCache
import me.tongfei.progressbar.ConsoleProgressBarConsumer
import me.tongfei.progressbar.ProgressBar
import me.tongfei.progressbar.ProgressBarStyle
import org.apache.log4j.LogManager
import org.apache.log4j.Logger
import org.mapleir.asm.ClassNode
import org.mapleir.asm.FieldNode
import org.mapleir.asm.MethodNode
import org.objectweb.asm.ClassReader
import java.util.jar.JarFile
import kotlin.io.path.readText

object Sharkuscator {
    private val logger: Logger = LogManager.getLogger("(Sharkuscator)")
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
            createAsciiBar("", inputJarFile.entries().toList().size.toLong()).use { progressBar ->
                for (sortedEntry in inputJarFile.entries().toList().sortedBy { !it.name.split("/").last().contains(".") }) {
                    val entryBytes = inputJarFile.getInputStream(sortedEntry).readBytes()
                    if (sortedEntry.name.endsWith(".class")) {
                        classNodes[sortedEntry.name] = SharkClassNode(ClassNode().apply {
                            ClassReader(entryBytes).accept(node, ClassReader.EXPAND_FRAMES)
                            node.methods.forEach { methods.add(MethodNode(it, this)) }
                            node.fields.forEach { fields.add(FieldNode(it, this)) }
                        })
                    } else if (!sortedEntry.name.endsWith("/")) {
                        resources[sortedEntry.name] = entryBytes
                    } else {
                        packages[sortedEntry.name] = entryBytes
                    }
                    progressBar.step()
                }
            }
        } catch (exception: Exception) {
            exception.printStackTrace()
        }

        return ZipImportResult(classNodes, resources, packages)
    }

    private fun createAsciiBar(taskName: String, initialMax: Long): ProgressBar {
        return ProgressBar.builder()
            .setConsumer(ConsoleProgressBarConsumer(System.out))
            .setStyle(ProgressBarStyle.ASCII)
            .setUpdateIntervalMillis(10)
            .setInitialMax(initialMax)
            .setTaskName(taskName)
            .build()
    }
}
