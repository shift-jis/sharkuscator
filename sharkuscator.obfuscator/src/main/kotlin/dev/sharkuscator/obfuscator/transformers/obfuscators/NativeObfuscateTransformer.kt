package dev.sharkuscator.obfuscator.transformers.obfuscators

import dev.sharkuscator.obfuscator.ObfuscatorServices
import dev.sharkuscator.obfuscator.configuration.transformers.TransformerConfiguration
import dev.sharkuscator.obfuscator.events.ObfuscatorEvents
import dev.sharkuscator.obfuscator.transformers.BaseTransformer
import dev.sharkuscator.obfuscator.transformers.TransformerPriority
import meteordevelopment.orbit.EventHandler
import java.nio.file.Files
import java.nio.file.Paths
import java.util.concurrent.TimeUnit
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.absolutePathString
import kotlin.io.path.deleteRecursively
import kotlin.io.path.writeText

class NativeObfuscateTransformer : BaseTransformer<TransformerConfiguration>("NativeObfuscate", TransformerConfiguration::class.java) {
    private val nativeObfuscatorToolPath = Paths.get("./thirdparty", "native-obfuscator.jar")

    @EventHandler
    @Suppress("unused")
    @OptIn(ExperimentalPathApi::class)
    private fun onFinalization(event: ObfuscatorEvents.FinalizationEvent) {
        if (!Files.exists(nativeObfuscatorToolPath)) {
            ObfuscatorServices.sharkLogger.error("native-obfuscator.jar does not exist!")
            return
        }

        if (event.context.isInputRecognizedAsMinecraftMod) {
            ObfuscatorServices.sharkLogger.error("Minecraft mod not supported by NativeObfuscateTransformer")
            return
        }

        ObfuscatorServices.sharkLogger.info("Running Native obfuscator...")
        val exclusionsFilePath = Files.createTempFile(null, ".txt")
        exclusionsFilePath.writeText(configuration.exclusions.joinToString("\n") { it.replace(".", "/") })
        exclusionsFilePath.toFile().deleteOnExit()

        val outputNativeLibDirPath = Paths.get("./native_library")
        if (Files.exists(outputNativeLibDirPath)) {
            outputNativeLibDirPath.deleteRecursively()
        }

        val nativeObfuscatorProcessBuilder = ProcessBuilder("java", "-jar", nativeObfuscatorToolPath.toString(), event.outputJarFile.name, outputNativeLibDirPath.toString(), "-p", "std_java", "-b", exclusionsFilePath.absolutePathString())
        ObfuscatorServices.sharkLogger.debug("Commandline: ${nativeObfuscatorProcessBuilder.command().joinToString(" ")}")

        val nativeObfuscatorProcess = nativeObfuscatorProcessBuilder.start()
        nativeObfuscatorProcess.inputStream.bufferedReader().use { ObfuscatorServices.sharkLogger.info(it.readText()) }
        nativeObfuscatorProcess.waitFor(1, TimeUnit.MINUTES)
    }

    override fun getExecutionPriority(): Int {
        return TransformerPriority.LOWEST
    }
}
