package dev.sharkuscator.obfuscator.hierarchies

import dev.sharkuscator.obfuscator.assembly.SharkClassNode

interface HierarchyCache {
    fun visitHierarchy(classNode: SharkClassNode)
}
