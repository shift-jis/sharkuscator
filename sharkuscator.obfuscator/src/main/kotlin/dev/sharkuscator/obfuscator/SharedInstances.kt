package dev.sharkuscator.obfuscator

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import meteordevelopment.orbit.EventBus
import org.apache.log4j.LogManager
import org.apache.log4j.Logger
import org.mapleir.context.IRCache
import org.mapleir.ir.cfg.builder.ControlFlowGraphBuilder
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption

object SharedInstances {
    val logger: Logger = LogManager.getLogger("(Sharkuscator)")
    val gson: Gson = GsonBuilder().create()

    val irFactory = IRCache(ControlFlowGraphBuilder::build)
    val eventBus = EventBus()

    fun useNativeLibrary(nativeLibrary: String): Boolean {
        var absolutePath = extractResource(nativeLibrary, File.createTempFile(System.nanoTime().toString(), ".dll")) ?: return false
        absolutePath = absolutePath.replace("\\", "/")

        val libraryDirectory = absolutePath.split("/").dropLast(1).joinToString("/")
        System.setProperty("java.library.path", "${System.getProperty("java.library.path")};${libraryDirectory};")

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
        return true
    }

    fun extractResource(name: String, destination: File): String? {
        if (!destination.exists() && !destination.createNewFile()) {
            return null
        }

        val inputStream = Sharkuscator::class.java.getResourceAsStream(name) ?: return null
        Files.copy(inputStream, destination.toPath(), StandardCopyOption.REPLACE_EXISTING)
        inputStream.close()
        return destination.absolutePath
    }
}
