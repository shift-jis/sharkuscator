package dev.sharkuscator.obfuscator.utilities

import dev.sharkuscator.obfuscator.Sharkuscator
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption

object ResourceLoader {
    fun loadNativeLibraryFromResources(nativeLibraryName: String): Boolean {
        var absolutePath = extractResource(nativeLibraryName, Files.createTempFile(null, ".dll")) ?: return false
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

    fun extractResource(name: String, destination: Path = Files.createTempFile(null, null), deleteOnExit: Boolean = true): String? {
        val destinationFile = destination.toFile()
        if (!Files.exists(destination) && !destinationFile.createNewFile()) {
            return null
        }

        if (deleteOnExit) {
            destinationFile.deleteOnExit()
        }

        val resourceAsStream = Sharkuscator::class.java.getResourceAsStream(name) ?: return null
        Files.copy(resourceAsStream, destination, StandardCopyOption.REPLACE_EXISTING)
        resourceAsStream.close()
        return destinationFile.absolutePath
    }
}
