package dev.sharkuscator.obfuscator.transformers.obfuscators.constants.strategies

import dev.sharkuscator.obfuscator.ObfuscationContext
import dev.sharkuscator.obfuscator.extensions.invokeStatic
import dev.sharkuscator.obfuscator.extensions.xor
import dev.sharkuscator.obfuscator.transformers.strategies.StringConstantObfuscationStrategy
import dev.sharkuscator.obfuscator.utilities.AssemblyHelper.buildInstructionList
import dev.sharkuscator.obfuscator.utilities.AssemblyHelper.createMethodNode
import org.apache.commons.lang3.RandomStringUtils
import org.mapleir.asm.ClassNode
import org.mapleir.asm.MethodNode
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.AbstractInsnNode
import org.objectweb.asm.tree.IincInsnNode
import org.objectweb.asm.tree.InsnList
import org.objectweb.asm.tree.InsnNode
import org.objectweb.asm.tree.JumpInsnNode
import org.objectweb.asm.tree.LabelNode
import org.objectweb.asm.tree.LdcInsnNode
import org.objectweb.asm.tree.MethodInsnNode
import org.objectweb.asm.tree.TypeInsnNode
import org.objectweb.asm.tree.VarInsnNode

class XorStringObfuscationStrategy : StringConstantObfuscationStrategy {
    companion object {
        private const val DECODER_METHOD_ACCESS = Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC + Opcodes.ACC_BRIDGE + Opcodes.ACC_SYNTHETIC
    }

    private val decoderMethodByClass = mutableMapOf<ClassNode, MethodNode>()

    override fun initialization(obfuscationContext: ObfuscationContext, targetClassNode: ClassNode) {
        decoderMethodByClass.computeIfAbsent(targetClassNode) {
            val decodeMethodNameGenerator = ObfuscationContext.resolveDictionary<MethodNode, ClassNode>(MethodNode::class.java)
            val decodeMethodNode = createMethodNode(DECODER_METHOD_ACCESS, decodeMethodNameGenerator.generateNextName(targetClassNode), "(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;").apply {
                val loopBeginLabelNode = LabelNode()
                val loopEndLabelNode = LabelNode()

                instructions = buildInstructionList {
                    // --- Initialization ---
                    add(VarInsnNode(Opcodes.ALOAD, 0))
                    add(MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/String", "length", "()I"))
                    add(VarInsnNode(Opcodes.ISTORE, 2))
                    add(TypeInsnNode(Opcodes.NEW, "java/lang/StringBuilder"))
                    add(InsnNode(Opcodes.DUP))
                    add(VarInsnNode(Opcodes.ILOAD, 2))
                    add(MethodInsnNode(Opcodes.INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "(I)V"))
                    add(VarInsnNode(Opcodes.ASTORE, 4))

                    add(InsnNode(Opcodes.ICONST_0))
                    add(VarInsnNode(Opcodes.ISTORE, 5))

                    // --- Loop Start ---
                    // loopStartLabel:
                    add(loopBeginLabelNode)
                    add(VarInsnNode(Opcodes.ILOAD, 5))
                    add(VarInsnNode(Opcodes.ILOAD, 2))
                    // if (i >= len1) goto loopEndLabel;
                    add(JumpInsnNode(Opcodes.IF_ICMPGE, loopEndLabelNode))

                    // --- Inside Loop ---
                    // char char1 = str1.charAt(i);
                    add(VarInsnNode(Opcodes.ALOAD, 0))
                    add(VarInsnNode(Opcodes.ILOAD, 5))
                    add(MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/String", "charAt", "(I)C"))
                    add(VarInsnNode(Opcodes.ISTORE, 6))

                    // char char2 = str2.charAt(i);
                    add(VarInsnNode(Opcodes.ALOAD, 1))
                    add(VarInsnNode(Opcodes.ILOAD, 5))
                    add(MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/String", "charAt", "(I)C"))
                    add(VarInsnNode(Opcodes.ISTORE, 7))

                    // int xorResultCode = char1 ^ char2;
                    add(VarInsnNode(Opcodes.ILOAD, 6))
                    add(VarInsnNode(Opcodes.ILOAD, 7))
                    add(InsnNode(Opcodes.IXOR))
                    add(VarInsnNode(Opcodes.ISTORE, 8))

                    // char resultChar = (char) xorResultCode;
                    add(VarInsnNode(Opcodes.ILOAD, 8))
                    add(InsnNode(Opcodes.I2C))
                    add(VarInsnNode(Opcodes.ISTORE, 9))

                    // result.append(resultChar);
                    add(VarInsnNode(Opcodes.ALOAD, 4))
                    add(VarInsnNode(Opcodes.ILOAD, 9))
                    add(MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(C)Ljava/lang/StringBuilder;"))
                    add(InsnNode(Opcodes.POP))

                    // --- Loop Increment ---
                    add(IincInsnNode(5, 1))
                    add(JumpInsnNode(Opcodes.GOTO, loopBeginLabelNode))
                    add(loopEndLabelNode)

                    // --- Loop End ---
                    add(VarInsnNode(Opcodes.ALOAD, 4))
                    add(MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;"))
                    add(InsnNode(Opcodes.ARETURN))
                }
            }
            MethodNode(decodeMethodNode, targetClassNode).also { targetClassNode.addMethod(it) }
        }
    }

    override fun replaceInstructions(targetClassNode: ClassNode, instructions: InsnList, targetInstruction: AbstractInsnNode, originalString: String) {
        val obfuscatedString = obfuscateString(originalString, RandomStringUtils.randomAlphanumeric(originalString.length).toByteArray())
        instructions.insert(targetInstruction, buildInstructionList {
            add(LdcInsnNode(obfuscatedString.first))
            add(LdcInsnNode(obfuscatedString.second.decodeToString()))
            add(decoderMethodByClass[targetClassNode]!!.invokeStatic())
        })
        instructions.remove(targetInstruction)
    }

    override fun buildDecryptionRoutine(obfuscationContext: ObfuscationContext, targetClassNode: ClassNode, classEligibilityPredicate: (ClassNode) -> Boolean) {
    }

    override fun obfuscateString(originalString: String, keyBytes: ByteArray): Pair<String, ByteArray> {
        return originalString xor keyBytes.decodeToString() to keyBytes
    }
}
