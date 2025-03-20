package dev.sharkuscator.obfuscator.assembler

import org.mapleir.app.service.ApplicationClassSource
import org.mapleir.app.service.ClassTree
import org.mapleir.app.service.CompleteResolvingJarDumper
import org.mapleir.asm.ClassNode
import org.objectweb.asm.ClassWriter
import org.topdank.byteengineer.commons.data.JarContents

class KlassResolvingDumper(jarContents: JarContents<ClassNode>, private val classSource: ApplicationClassSource) : CompleteResolvingJarDumper(jarContents, classSource) {
    override fun buildClassWriter(tree: ClassTree, flags: Int): ClassWriter {
        return KlassWriter(classSource, flags)
    }
}
