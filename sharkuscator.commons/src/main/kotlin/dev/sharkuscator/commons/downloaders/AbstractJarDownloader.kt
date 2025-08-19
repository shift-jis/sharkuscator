package dev.sharkuscator.commons.downloaders

import dev.sharkuscator.commons.serializers.BytecodeSerializer
import org.objectweb.asm.tree.ClassNode
import java.util.jar.JarFile

abstract class AbstractJarDownloader<T>(protected val bytecodeSerializer: BytecodeSerializer<T>, protected val jarFile: JarFile) {
    protected val downloadedClassNodes = mutableListOf<ClassNode>()
    protected val downloadedResources = mutableListOf<Pair<String, ByteArray>>()

    abstract fun download(): DownloadedLibrary
}
