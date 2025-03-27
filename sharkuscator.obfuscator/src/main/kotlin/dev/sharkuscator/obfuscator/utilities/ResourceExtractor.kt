package dev.sharkuscator.obfuscator.utilities

import dev.sharkuscator.obfuscator.Sharkuscator
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption

object ResourceExtractor {
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

        val resourceAsStream = Sharkuscator::class.java.getResourceAsStream(name) ?: return null
        Files.copy(resourceAsStream, destination.toPath(), StandardCopyOption.REPLACE_EXISTING)
        resourceAsStream.close()
        return destination.absolutePath
    }
}
