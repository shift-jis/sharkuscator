package dev.sharkuscator.obfuscator.hierarchies

import org.mapleir.asm.ClassNode
import org.mapleir.asm.MethodNode

interface HierarchyProvider {
    fun traverseHierarchy(classNode: ClassNode)

    fun getClassParents(classNode: ClassNode): Set<ClassNode>

    fun getClassChildren(classNode: ClassNode): Set<ClassNode>

    fun getRootMethodNode(methodNode: MethodNode): MethodNode?
}
