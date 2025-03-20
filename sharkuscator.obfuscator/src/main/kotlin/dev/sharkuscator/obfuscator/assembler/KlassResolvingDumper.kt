package dev.sharkuscator.obfuscator.assembler

import dev.sharkuscator.obfuscator.SharedInstances
import dev.sharkuscator.obfuscator.transformers.events.ClassWriteEvent
import org.mapleir.app.service.ApplicationClassSource
import org.mapleir.app.service.ClassTree
import org.mapleir.app.service.CompleteResolvingJarDumper
import org.mapleir.asm.ClassNode
import org.objectweb.asm.ClassWriter
import org.topdank.byteengineer.commons.data.JarContents
import java.util.jar.JarEntry
import java.util.jar.JarOutputStream

class KlassResolvingDumper(jarContents: JarContents<ClassNode>, private val classSource: ApplicationClassSource) : CompleteResolvingJarDumper(jarContents, classSource) {
//    override fun dump(outputJarFile: File) {
//        if (outputJarFile.exists() && !outputJarFile.delete()) {
//            Sharkuscator.logger.error("Could not delete an already existing destination file")
//            return
//        }
//
//        JarOutputStream(FileOutputStream(outputJarFile)).use {
//            it.setLevel(Deflater.DEFAULT_COMPRESSION)
//        }
//    }

    override fun dumpClass(outputStream: JarOutputStream, name: String, classNode: ClassNode): Int {
        val classEntry = JarEntry("${classNode.name}.class")
        outputStream.putNextEntry(classEntry)

        try {
            val classWriter = buildClassWriter(classSource.classTree, ClassWriter.COMPUTE_FRAMES)
            classNode.node.accept(classWriter)

            val classWriteEvent = ClassWriteEvent(classSource, classWriter.toByteArray())
            SharedInstances.eventBus.post(classWriteEvent)
            if (classWriteEvent.isCancelled) {
                return 1
            }

            outputStream.write(classWriteEvent.classData)
        } catch (exception: Exception) {
            val classWriter = buildClassWriter(classSource.classTree, ClassWriter.COMPUTE_MAXS)
            classNode.node.accept(classWriter)

            val classWriteEvent = ClassWriteEvent(classSource, classWriter.toByteArray())
            SharedInstances.eventBus.post(classWriteEvent)
            if (classWriteEvent.isCancelled) {
                return 1
            }

            outputStream.write(classWriteEvent.classData)
        }

        return 1
    }

    override fun buildClassWriter(tree: ClassTree, flags: Int): ClassWriter {
        return KlassWriter(classSource, flags)
    }
}
