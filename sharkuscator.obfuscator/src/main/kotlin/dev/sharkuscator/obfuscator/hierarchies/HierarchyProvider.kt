package dev.sharkuscator.obfuscator.hierarchies

import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.MethodNode

interface HierarchyProvider {
    fun traverseHierarchy(classNode: ClassNode)

    fun getClassParents(classNode: ClassNode): Set<ClassNode>

    fun getClassChildren(classNode: ClassNode): Set<ClassNode>

    fun getRootMethodNode(methodNode: MethodNode): MethodNode?
}
