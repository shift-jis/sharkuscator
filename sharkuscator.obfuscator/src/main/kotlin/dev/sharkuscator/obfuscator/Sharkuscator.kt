package dev.sharkuscator.obfuscator

import com.google.gson.FormattingStyle
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dev.sharkuscator.obfuscator.configuration.JsonConfiguration
import lombok.extern.slf4j.Slf4j
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.jar.JarFile
import kotlin.io.path.readText

@Slf4j
object Sharkuscator {
    private val LOGGER: Logger = LoggerFactory.getLogger("(Sharkuscator)")
    private val SHARK_FORMAT = FormattingStyle.PRETTY.withIndent("    ")
    val GSON: Gson = GsonBuilder().setFormattingStyle(SHARK_FORMAT).create()

    private lateinit var configuration: JsonConfiguration
    private lateinit var sharkSession: SharkSession

    fun obfuscate(sharkSession: SharkSession) {
        this.sharkSession = sharkSession

        importConfiguration()
        importJarEntries()
    }

    private fun importConfiguration() {
        try {
            LOGGER.info("Importing configuration...")
            val jsonText = sharkSession.configurationFile.readText()
            configuration = GSON.fromJson(jsonText, JsonConfiguration::class.java)
        } catch (exception: Exception) {
            exception.printStackTrace()
        }
    }

    private fun importJarEntries() {
        val resources = mutableMapOf<String, ByteArray>()
        val classes = mutableMapOf<String, ByteArray>()
        LOGGER.info("Importing jar entries...")

        try {
            val inputJarFile = JarFile(sharkSession.inputJarPath.toFile())
            ProgressBarFactory.createAsciiBar("", inputJarFile.entries().toList().size.toLong()).use { progressBar ->
                for (sortedEntry in inputJarFile.entries().toList().sortedBy { !it.name.split("/").last().contains(".") }) {
                    val entryBytes = inputJarFile.getInputStream(sortedEntry).readBytes()
                    if (!sortedEntry.name.endsWith(".class")) {
                        resources[sortedEntry.name] = entryBytes
                    } else {
                        classes[sortedEntry.name] = entryBytes
                    }
                    progressBar.step()
                }
            }
        } catch (exception: Exception) {
            exception.printStackTrace()
        }
    }
}
