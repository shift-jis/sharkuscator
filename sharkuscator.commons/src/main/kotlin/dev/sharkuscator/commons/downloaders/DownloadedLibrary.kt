package dev.sharkuscator.commons.downloaders

import org.objectweb.asm.tree.ClassNode

data class DownloadedLibrary(val classNodes: MutableList<ClassNode>, val generatedClassNodes: MutableList<ClassNode>, val resources: MutableList<Pair<String, ByteArray>>)
