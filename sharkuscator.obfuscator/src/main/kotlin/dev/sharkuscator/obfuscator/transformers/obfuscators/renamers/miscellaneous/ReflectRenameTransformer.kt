package dev.sharkuscator.obfuscator.transformers.obfuscators.renamers.miscellaneous

import dev.sharkuscator.obfuscator.ObfuscatorServices
import dev.sharkuscator.obfuscator.configuration.transformers.TransformerConfiguration
import dev.sharkuscator.obfuscator.events.TransformerEvents
import dev.sharkuscator.obfuscator.transformers.BaseTransformer
import dev.sharkuscator.obfuscator.transformers.TransformerPriority
import dev.sharkuscator.obfuscator.utilities.BytecodeUtils
import meteordevelopment.orbit.EventHandler
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import org.objectweb.asm.tree.AbstractInsnNode
import org.objectweb.asm.tree.LdcInsnNode
import org.objectweb.asm.tree.MethodInsnNode

class ReflectRenameTransformer : BaseTransformer<TransformerConfiguration>("ReflectRename", TransformerConfiguration::class.java) {
    @EventHandler
    @Suppress("unused")
    private fun onMethodTransform(event: TransformerEvents.MethodTransformEvent) {
        BytecodeUtils.findNonEmptyStrings(event.anytypeNode.node.instructions).forEach { (instruction, string) ->
            if (instruction.previous == null) {
                return@forEach
            }

            val reflectionMethodCall = findNextInvokeVirtualInstruction(instruction) ?: return@forEach
            when {
                reflectionMethodCall.name == "getDeclaredField" && instruction.previous.opcode == Opcodes.LDC -> {
                    val internalName = ((instruction.previous as LdcInsnNode).cst as Type).className.replace(".", "/")
                    instruction.cst = ObfuscatorServices.symbolRemapper.mapFieldName(internalName, string, "")
                }

                reflectionMethodCall.name == "getDeclaredMethod" && instruction.previous.opcode == Opcodes.LDC -> {
                    val internalName = ((instruction.previous as LdcInsnNode).cst as Type).className.replace(".", "/")
                    if (instruction.next.opcode == Opcodes.ACONST_NULL) {
                        instruction.cst = ObfuscatorServices.symbolRemapper.findClosestMethodMapping(internalName, string)
                    }
                }

//                reflectionMethodCall.name == "getMethod" && instruction.next.type == 0 -> {
//                    if (instruction.next.opcode == Opcodes.ICONST_0) {
//                        instruction.cst = ObfuscatorServices.symbolRemapper.findClosestMethodMapping(string, "")
//                    }
//                }
            }
        }
    }

    override fun getExecutionPriority(): Int {
        return TransformerPriority.LOW
    }

    private fun findNextInvokeVirtualInstruction(instruction: AbstractInsnNode): MethodInsnNode? {
        var currentNode: AbstractInsnNode? = instruction.next
        repeat(10) {
            if (currentNode == null) {
                return null
            }
            if (currentNode.opcode == Opcodes.INVOKEVIRTUAL) {
                return currentNode as MethodInsnNode
            }
            currentNode = currentNode.next
        }
        return null
    }
}
