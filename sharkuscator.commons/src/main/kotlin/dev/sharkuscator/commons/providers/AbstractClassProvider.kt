package dev.sharkuscator.commons.providers

import org.objectweb.asm.tree.ClassNode

abstract class AbstractClassProvider {
    protected val classNodeMap: MutableMap<String, ClassNode> = mutableMapOf()

    fun includeClassNodes(classNodes: List<ClassNode>) {
        classNodes.forEach { this.classNodeMap[it.name] = it }
    }

    fun asIterable(): Iterable<ClassNode> {
        return classNodeMap.values.asIterable()
    }

    open fun getClassNode(className: String): ClassNode? {
        return classNodeMap[className]
    }
}
