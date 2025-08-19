package dev.sharkuscator.commons.providers

import dev.sharkuscator.commons.downloaders.DownloadedLibrary
import org.objectweb.asm.tree.ClassNode

class ApplicationClassProvider : AbstractClassProvider() {
    private val libraryProviders = mutableListOf<LibraryClassProvider>()

    fun processApplication(downloadedLibrary: DownloadedLibrary) {
        includeLibraries(LibraryClassProvider(downloadedLibrary.generatedClassNodes))
        includeClassNodes(downloadedLibrary.classNodes)
    }

    fun includeLibraries(vararg libraries: LibraryClassProvider) {
        libraryProviders.addAll(libraries)
    }

    fun iterateWithLibraries(): Iterable<ClassNode> {
        return super.asIterable() + libraryProviders.flatMap { it.asIterable() }
    }

    fun isLibraryClass(className: String): Boolean {
        return libraryProviders.any { it.getClassNode(className) != null }
    }

    override fun getClassNode(className: String): ClassNode? {
        return super.getClassNode(className) ?: libraryProviders.firstNotNullOfOrNull { it.getClassNode(className) }
    }
}
