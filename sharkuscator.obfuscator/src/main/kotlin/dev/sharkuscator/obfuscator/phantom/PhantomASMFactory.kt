package dev.sharkuscator.obfuscator.phantom

import org.clyze.jphantom.ClassMembers
import org.clyze.jphantom.adapters.ClassPhantomExtractor
import org.clyze.jphantom.hier.ClassHierarchy
import org.mapleir.asm.ClassHelper
import org.mapleir.asm.ClassNode
import org.objectweb.asm.ClassReader
import org.topdank.byteengineer.commons.asm.ASMFactory


class PhantomASMFactory(val classHierarchy: ClassHierarchy, val classMembers: ClassMembers) : ASMFactory<ClassNode> {
    override fun create(bytes: ByteArray, name: String): ClassNode {
        val classReader = ClassReader(bytes)
        classReader.accept(ClassPhantomExtractor(classHierarchy, classMembers), 0)

        val classNode = org.objectweb.asm.tree.ClassNode()
        classReader.accept(classNode, 0)

        return ClassHelper.create(classNode)
    }

    override fun write(classNode: ClassNode): ByteArray {
        return ClassHelper.toByteArray(classNode)
    }
}
