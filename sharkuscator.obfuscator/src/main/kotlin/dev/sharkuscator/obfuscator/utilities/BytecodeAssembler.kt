package dev.sharkuscator.obfuscator.utilities

import jdk.internal.org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.*

object BytecodeAssembler {
    fun findNonEmptyStrings(instructions: InsnList): List<Pair<LdcInsnNode, String>> {
        return instructions.filterIsInstance<LdcInsnNode>().filter { it.cst is String && (it.cst as String).isNotEmpty() }.map { Pair(it, it.cst as String) }
    }

    fun findNonZeroNumbers(instructions: InsnList): List<Pair<LdcInsnNode, Number>> {
        return instructions.filterIsInstance<LdcInsnNode>().filter { it.cst is Number && (it.cst as Number) != 0 }.map { Pair(it, it.cst as Number) }
    }

    fun buildInstructionList(vararg instructions: AbstractInsnNode): InsnList {
        return InsnList().apply {
            instructions.forEach { add(it) }
        }
    }

    fun createClassNode(name: String): ClassNode {
        return ClassNode().apply {
            this.superName = "java/lang/Object"
            this.version = Opcodes.V1_8

            this.access = Opcodes.ACC_PUBLIC
            this.name = name
        }
    }

    fun createMethodNode(access: Int, name: String, descriptor: String): MethodNode {
        return MethodNode(access, name, descriptor, null, null)
    }

    fun createInvokeStatic(owner: String, name: String, descriptor: String): MethodInsnNode {
        return MethodInsnNode(Opcodes.INVOKESTATIC, owner, name, descriptor)
    }
}
