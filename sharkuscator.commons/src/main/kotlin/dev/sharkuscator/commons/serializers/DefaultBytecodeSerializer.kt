package dev.sharkuscator.commons.serializers

import dev.sharkuscator.commons.SkiddedClassWriter
import dev.sharkuscator.commons.providers.ApplicationClassProvider
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.tree.ClassNode

class DefaultBytecodeSerializer(private val classNodeProvider: ApplicationClassProvider) : BytecodeSerializer<ClassNode> {
    override fun deserialize(byteArray: ByteArray): ClassNode {
        val classReader = ClassReader(byteArray)
        return ClassNode().also { classReader.accept(it, 0) }
    }

    override fun serialize(nodeObject: ClassNode): ByteArray {
        val classWriter = SkiddedClassWriter(classNodeProvider, ClassWriter.COMPUTE_FRAMES).also { nodeObject.accept(it) }
        return classWriter.toByteArray()
    }
}
