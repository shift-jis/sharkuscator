package dev.sharkuscator.obfuscator.assembler

import dev.sharkuscator.obfuscator.SharedInstances
import dev.sharkuscator.obfuscator.configuration.exclusions.ExclusionRule
import dev.sharkuscator.obfuscator.transformers.events.assemble.ClassWriteEvent
import dev.sharkuscator.obfuscator.transformers.events.assemble.ResourceWriteEvent
import org.mapleir.app.service.ApplicationClassSource
import org.mapleir.app.service.ClassTree
import org.mapleir.app.service.CompleteResolvingJarDumper
import org.mapleir.asm.ClassNode
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.commons.ClassRemapper
import org.topdank.byteengineer.commons.data.JarContents
import java.io.File
import java.io.FileOutputStream
import java.util.jar.JarEntry
import java.util.jar.JarOutputStream

class KlassResolvingDumper(
    private val jarContents: JarContents<ClassNode>,
    private val classSource: ApplicationClassSource,
    private val exclusions: ExclusionRule,
) : CompleteResolvingJarDumper(jarContents, classSource) {
    private val discoveredPackages = mutableListOf<String>()

    override fun dump(outputJarFile: File) {
        if (outputJarFile.exists() && !outputJarFile.delete()) {
            SharedInstances.logger.error("Could not delete an already existing destination file")
            return
        }

        JarOutputStream(FileOutputStream(outputJarFile)).use { outputStream ->
            jarContents.resourceContents.filter { !it.name.endsWith("/") }.forEach {
                discoveredPackages.add(it.name.split("/").dropLast(1).joinToString("/"))
            }

            for (classNode in jarContents.classContents) {
                dumpClass(outputStream, classNode.name, classNode)
            }

            for (jarResource in jarContents.resourceContents) {
                dumpResource(outputStream, jarResource.name, jarResource.data)
            }
        }
    }

    override fun dumpClass(outputStream: JarOutputStream, name: String, classNode: ClassNode): Int {
        val originalNode = org.objectweb.asm.tree.ClassNode().apply {
            classNode.node.accept(ClassRemapper(this, SharedInstances.remapper))
        }

        val classEntry = JarEntry("${originalNode.name}.class")
        outputStream.putNextEntry(classEntry)

        try {
            val classWriter = buildClassWriter(classSource.classTree, ClassWriter.COMPUTE_FRAMES)
            originalNode.accept(classWriter)

            val classWriteEvent = ClassWriteEvent(classNode, classWriter.toByteArray())
            if (!exclusions.excluded(classNode)) {
                SharedInstances.eventBus.post(classWriteEvent)
                if (classWriteEvent.isCancelled) {
                    return 0
                }
            }

            if (originalNode.name.contains("/")) {
                discoveredPackages.add(originalNode.name.split("/").dropLast(1).joinToString("/"))
            }

            outputStream.write(classWriteEvent.classData)
        } catch (exception: Exception) {
            val classWriter = buildClassWriter(classSource.classTree, ClassWriter.COMPUTE_MAXS)
            originalNode.accept(classWriter)

            val classWriteEvent = ClassWriteEvent(classNode, classWriter.toByteArray())
            if (!exclusions.excluded(classNode)) {
                SharedInstances.eventBus.post(classWriteEvent)
                if (classWriteEvent.isCancelled) {
                    return 0
                }
            }

            if (originalNode.name.contains("/")) {
                discoveredPackages.add(originalNode.name.split("/").dropLast(1).joinToString("/"))
            }

            outputStream.write(classWriteEvent.classData)
        }

        return 1
    }

    override fun dumpResource(outputStream: JarOutputStream, name: String, bytes: ByteArray): Int {
        if (name.endsWith("/") && !discoveredPackages.contains(name)) {
            return 0
        }

        val resourceWriteEvent = ResourceWriteEvent(classSource, name, bytes)
        if (!exclusions.excluded(name)) {
            SharedInstances.eventBus.post(resourceWriteEvent)
            if (resourceWriteEvent.isCancelled) {
                return 0
            }
        }

        val resourceEntry = JarEntry(resourceWriteEvent.name)
        outputStream.putNextEntry(resourceEntry)
        outputStream.write(resourceWriteEvent.resourceData)
        return 1
    }

    override fun buildClassWriter(tree: ClassTree, flags: Int): ClassWriter {
        return KlassWriter(classSource, flags)
    }
}
