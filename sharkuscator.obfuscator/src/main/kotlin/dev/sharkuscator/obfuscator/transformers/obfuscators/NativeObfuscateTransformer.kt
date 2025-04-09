package dev.sharkuscator.obfuscator.transformers.obfuscators

import dev.sharkuscator.obfuscator.SharedInstances
import dev.sharkuscator.obfuscator.configuration.transformers.TransformerConfiguration
import dev.sharkuscator.obfuscator.transformers.AbstractTransformer
import dev.sharkuscator.obfuscator.transformers.events.ObfuscatorEvent
import meteordevelopment.orbit.EventHandler
import meteordevelopment.orbit.EventPriority
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.absolutePathString
import kotlin.io.path.deleteRecursively
import kotlin.io.path.writeText

class NativeObfuscateTransformer : AbstractTransformer<TransformerConfiguration>("NativeObfuscate", TransformerConfiguration::class.java) {
    private val obfuscatorPath = Paths.get("./thirdparty", "native-obfuscator.jar")

    @EventHandler(priority = EventPriority.LOWEST)
    @OptIn(ExperimentalPathApi::class)
    private fun onFinalization(event: ObfuscatorEvent.FinalizationEvent) {
        if (!Files.exists(obfuscatorPath)) {
            SharedInstances.logger.error("native-obfuscator.jar does not exist!")
            return
        }

        SharedInstances.logger.info("Running Native obfuscator...")
        val blackListFilePath = Files.createTempFile(null, ".txt")
        blackListFilePath.writeText(configuration.exclusions.joinToString("\n") { it.replace(".", "/") })
        blackListFilePath.toFile().deleteOnExit()

        val nativeLibraryPath = Paths.get("./native_library")
        if (Files.exists(nativeLibraryPath)) {
            nativeLibraryPath.deleteRecursively()
        }

        val obfuscatorProcess = ProcessBuilder("java", "-jar", obfuscatorPath.toString(), event.outputJarFile.name, nativeLibraryPath.toString(), "-p", "std_java", "-b", blackListFilePath.absolutePathString()).start()
        obfuscatorProcess.inputStream.bufferedReader().use {
            SharedInstances.logger.info(it.readText())
        }
        obfuscatorProcess.waitFor()
    }
}
