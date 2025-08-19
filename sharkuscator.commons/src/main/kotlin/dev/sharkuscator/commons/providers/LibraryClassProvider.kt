package dev.sharkuscator.commons.providers

import org.objectweb.asm.tree.ClassNode

class LibraryClassProvider : AbstractClassProvider {
    constructor(initialClasses: List<ClassNode>) {
        includeClassNodes(initialClasses)
    }
}
