package dev.sharkuscator.commons.serializers

import dev.sharkuscator.commons.SkiddedClassWriter
import dev.sharkuscator.commons.providers.ApplicationClassProvider
import org.clyze.jphantom.ClassMembers
import org.clyze.jphantom.adapters.ClassPhantomExtractor
import org.clyze.jphantom.hier.ClassHierarchy
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.tree.ClassNode

class PhantomBytecodeSerializer(val classNodeProvider: ApplicationClassProvider, val classHierarchy: ClassHierarchy, val classMembers: ClassMembers) : BytecodeSerializer<ClassNode> {
    override fun deserialize(byteArray: ByteArray): ClassNode {
        val classReader = ClassReader(byteArray).also { it.accept(ClassPhantomExtractor(classHierarchy, classMembers), 0) }
        return ClassNode().also { classReader.accept(it, 0) }
    }

    override fun serialize(nodeObject: ClassNode): ByteArray {
        val classWriter = SkiddedClassWriter(classNodeProvider, ClassWriter.COMPUTE_FRAMES).also { nodeObject.accept(it) }
        return classWriter.toByteArray()
    }
}
