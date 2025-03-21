package dev.sharkuscator.obfuscator.assembler

import dev.sharkuscator.obfuscator.SharedInstances
import dev.sharkuscator.obfuscator.transformers.events.writes.ResourceWriteEvent
import dev.sharkuscator.obfuscator.transformers.events.writes.ClassWriteEvent
import org.mapleir.app.service.ApplicationClassSource
import org.mapleir.app.service.ClassTree
import org.mapleir.app.service.CompleteResolvingJarDumper
import org.mapleir.asm.ClassNode
import org.objectweb.asm.ClassWriter
import org.topdank.byteengineer.commons.data.JarContents
import java.util.jar.JarEntry
import java.util.jar.JarOutputStream

class KlassResolvingDumper(private val jarContents: JarContents<ClassNode>, private val classSource: ApplicationClassSource) : CompleteResolvingJarDumper(jarContents, classSource) {
    private val singleSuccessful = 1

    override fun dumpClass(outputStream: JarOutputStream, name: String, classNode: ClassNode): Int {
        val classEntry = JarEntry("${classNode.name}.class")
        outputStream.putNextEntry(classEntry)

        try {
            val classWriter = buildClassWriter(classSource.classTree, ClassWriter.COMPUTE_FRAMES)
            classNode.node.accept(classWriter)

            val classWriteEvent = ClassWriteEvent(jarContents, classSource, classNode, classWriter.toByteArray())
            SharedInstances.eventBus.post(classWriteEvent)
            if (classWriteEvent.isCancelled) {
                return singleSuccessful
            }

            outputStream.write(classWriteEvent.classData)
        } catch (exception: Exception) {
            val classWriter = buildClassWriter(classSource.classTree, ClassWriter.COMPUTE_MAXS)
            classNode.node.accept(classWriter)

            val classWriteEvent = ClassWriteEvent(jarContents, classSource, classNode, classWriter.toByteArray())
            SharedInstances.eventBus.post(classWriteEvent)
            if (classWriteEvent.isCancelled) {
                return singleSuccessful
            }

            outputStream.write(classWriteEvent.classData)
        }

        return singleSuccessful
    }

    override fun dumpResource(outputStream: JarOutputStream, name: String, bytes: ByteArray): Int {
        val resourceWriteEvent = ResourceWriteEvent(classSource, name, bytes)
        SharedInstances.eventBus.post(resourceWriteEvent)
        if (resourceWriteEvent.isCancelled) {
            return singleSuccessful
        }

        val resourceEntry = JarEntry(resourceWriteEvent.name)
        outputStream.putNextEntry(resourceEntry)
        outputStream.write(resourceWriteEvent.bytes)
        return singleSuccessful
    }

    override fun buildClassWriter(tree: ClassTree, flags: Int): ClassWriter {
        return KlassWriter(classSource, flags)
    }
}
