package dev.sharkuscator.obfuscator.transformers.obfuscators.constants.generators

import dev.sharkuscator.obfuscator.ObfuscationContext
import dev.sharkuscator.obfuscator.transformers.obfuscators.renamers.ClassRenameTransformer
import dev.sharkuscator.obfuscator.utilities.AssemblyHelper.createClassNode
import dev.sharkuscator.obfuscator.utilities.AssemblyHelper.createMethodNode
import org.mapleir.asm.ClassHelper
import org.mapleir.asm.ClassNode
import org.mapleir.asm.MethodNode
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.InsnNode

class MaskingNumberGenerator(private var maskingClassesGenerated: Boolean = false) {
    companion object {
        private const val CONFIGURE_METHOD_ACCESS = Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC
    }

    fun generateMaskingClasses(obfuscationContext: ObfuscationContext) {
        if (maskingClassesGenerated) {
            return
        }

        val maskingClassNameGenerator = ObfuscationContext.resolveDictionary<ClassNode, String>(ClassNode::class.java)
        val methodNameGenerator = ObfuscationContext.resolveDictionary<MethodNode, ClassNode>(MethodNode::class.java)
        val targetPackagePath = ClassRenameTransformer.effectiveClassPrefix

        val maskedNumberClassNode = ClassHelper.create(createClassNode(maskingClassNameGenerator.generateNextName(targetPackagePath))).apply {

        }

        val maskingConfiguratorClassNode = ClassHelper.create(createClassNode(maskingClassNameGenerator.generateNextName(targetPackagePath))).apply {
            addMethod(MethodNode(createMethodNode(CONFIGURE_METHOD_ACCESS, methodNameGenerator.generateNextName(this), "()L${maskedNumberClassNode.name};").apply {
                instructions.add(InsnNode(Opcodes.RETURN))
            }, this))
            addMethod(MethodNode(createMethodNode(CONFIGURE_METHOD_ACCESS, methodNameGenerator.generateNextName(this), "(L${maskedNumberClassNode.name};)V").apply {
                instructions.add(InsnNode(Opcodes.RETURN))
            }, this))
        }

        obfuscationContext.jarContents.classContents.add(maskedNumberClassNode)
        obfuscationContext.jarContents.classContents.add(maskingConfiguratorClassNode)
        maskingClassesGenerated = true
    }
}
