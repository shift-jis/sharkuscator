package dev.sharkuscator.commons.providers

import dev.sharkuscator.commons.extensions.classNode
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.MethodNode

class DefaultHierarchyProvider(private val classNodeProvider: ApplicationClassProvider) : HierarchyProvider {
    val classChildren: MutableMap<ClassNode, MutableSet<ClassNode>> = mutableMapOf()
    val classParents: MutableMap<ClassNode, MutableSet<ClassNode>> = mutableMapOf()
    private val visitHierarchies: MutableSet<String> = hashSetOf()

    override fun traverseHierarchy(classNode: ClassNode) {
        if (visitHierarchies.add(classNode.name)) {
            if (classNode.superName != null) {
                val resolvedSuperNode = classNodeProvider.getClassNode(classNode.superName!!) ?: return
                classParents.computeIfAbsent(classNode) { mutableSetOf() }.add(resolvedSuperNode)
                classChildren.computeIfAbsent(resolvedSuperNode) { mutableSetOf() }.add(classNode)
                traverseHierarchy(resolvedSuperNode)
            }

            classNode.interfaces.forEach { interfaceName ->
                val resolvedInterfaceNode = classNodeProvider.getClassNode(interfaceName) ?: return
                classParents.computeIfAbsent(classNode) { mutableSetOf() }.add(resolvedInterfaceNode)
                classChildren.computeIfAbsent(resolvedInterfaceNode) { mutableSetOf() }.add(classNode)
                traverseHierarchy(resolvedInterfaceNode)
            }
        }
    }

    override fun getClassParents(classNode: ClassNode): Set<ClassNode> {
        return classParents[classNode] ?: emptySet()
    }

    override fun getClassChildren(classNode: ClassNode): Set<ClassNode> {
        return classChildren[classNode] ?: emptySet()
    }

    override fun getRootMethodNode(methodNode: MethodNode): MethodNode? {
        return getRootMethodNode(methodNode.classNode, methodNode)
    }

    private fun getRootMethodNode(classNode: ClassNode, methodNode: MethodNode): MethodNode? {
        if (methodNode.name == "<init>" || methodNode.name == "<clinit>") {
            return null
        }

        for (parentClassNode in getClassParents(classNode)) {
            for (parentMethodNode in parentClassNode.methods) {
                if (methodNode.name == parentMethodNode.name && methodNode.desc == parentMethodNode.desc) {
                    return parentMethodNode
                }

                val originalMethodNode = getRootMethodNode(parentClassNode, methodNode)
                if (originalMethodNode != null) {
                    return originalMethodNode
                }
            }
        }

        return null
    }
}
