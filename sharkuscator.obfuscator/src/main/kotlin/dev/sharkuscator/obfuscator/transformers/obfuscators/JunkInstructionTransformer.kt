package dev.sharkuscator.obfuscator.transformers.obfuscators

import dev.sharkuscator.obfuscator.configuration.transformers.TransformerConfiguration
import dev.sharkuscator.obfuscator.events.TransformerEvents
import dev.sharkuscator.obfuscator.transformers.BaseTransformer
import meteordevelopment.orbit.EventHandler
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.AbstractInsnNode
import org.objectweb.asm.tree.InsnList
import org.objectweb.asm.tree.InsnNode
import org.objectweb.asm.tree.IntInsnNode
import org.objectweb.asm.tree.LdcInsnNode
import kotlin.random.Random


object JunkInstructionTransformer : BaseTransformer<TransformerConfiguration>("JunkInstruction", TransformerConfiguration::class.java) {
    private const val LDC_BIPUSH_MIN_VALUE = -64
    private const val LDC_BIPUSH_EXCLUSIVE_UPPER_BOUND = 64

    private const val OTHER_BIPUSH_MIN_VALUE = -27
    private const val OTHER_BIPUSH_EXCLUSIVE_UPPER_BOUND = 37

    private const val NON_LDC_JUNK_CHECK_INTERVAL = 6
    private const val NON_LDC_PUSH_POP_CHANCE = 0.4f // 40% chance to insert PUSH/POP pair

    @EventHandler
    @Suppress("unused")
    private fun onMethodTransform(event: TransformerEvents.MethodTransformEvent) {
        if (transformed || exclusions.excluded(event.anytypeNode) || event.anytypeNode.isNative) {
            return
        }

        val methodNode = event.anytypeNode.node
        var nonLdcInstructionCounter = 0

        val originalInstructions = methodNode.instructions.toArray().toList()
        originalInstructions.forEach { instruction ->
            if (instruction is LdcInsnNode) {
                if (Random.nextBoolean()) {
                    insertBipushPopPair(methodNode.instructions, instruction, Random.nextInt(LDC_BIPUSH_MIN_VALUE, LDC_BIPUSH_EXCLUSIVE_UPPER_BOUND))
                }
            } else {
                if (nonLdcInstructionCounter % NON_LDC_JUNK_CHECK_INTERVAL == 0) {
                    if (Random.nextFloat() < NON_LDC_PUSH_POP_CHANCE) {
                        insertBipushPopPair(methodNode.instructions, instruction, Random.nextInt(OTHER_BIPUSH_MIN_VALUE, OTHER_BIPUSH_EXCLUSIVE_UPPER_BOUND))
                    } else {
                        methodNode.instructions.insertBefore(instruction, InsnNode(Opcodes.NOP))
                    }
                }
                nonLdcInstructionCounter++
            }
        }
    }

    private fun insertBipushPopPair(instructions: InsnList, beforeNode: AbstractInsnNode, value: Int) {
        instructions.insertBefore(beforeNode, IntInsnNode(Opcodes.BIPUSH, value))
        instructions.insertBefore(beforeNode, InsnNode(Opcodes.POP))
    }
}
