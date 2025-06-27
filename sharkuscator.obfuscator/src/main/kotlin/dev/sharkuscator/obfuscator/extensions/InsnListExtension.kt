package dev.sharkuscator.obfuscator.extensions

import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.*
import kotlin.random.Random
import kotlin.random.nextInt


fun InsnList.chunkedRandomly(sizeRange: IntRange): MutableList<MutableList<AbstractInsnNode>> {
    val instructionChunks = mutableListOf<MutableList<AbstractInsnNode>>()
    var nextChunkSize = Random.nextInt(sizeRange)

    for (instruction in this) {
        val instructionChunk = instructionChunks.lastOrNull()
        if (instructionChunk == null || instructionChunk.size >= nextChunkSize) {
            instructionChunks.add(mutableListOf(instruction))
            nextChunkSize = Random.nextInt(sizeRange)
        } else {
            instructionChunk.add(instruction)
        }
    }

    return instructionChunks
}

fun InsnList.partitionIntoBasicBlocks(): MutableList<MutableList<AbstractInsnNode>> {
    val leaders = mutableSetOf(first)
    for (instruction in this) {
        when (instruction) {
            is JumpInsnNode -> leaders.add(instruction.label)
            is TableSwitchInsnNode -> {
                leaders.add(instruction.dflt)
                instruction.labels.forEach { leaders.add(it) }
            }

            is LookupSwitchInsnNode -> {
                leaders.add(instruction.dflt)
                instruction.labels.forEach { leaders.add(it) }
            }
        }

        val isTerminator = when (instruction.opcode) {
            in Opcodes.IFEQ..Opcodes.JSR, in Opcodes.IRETURN..Opcodes.RETURN,
            Opcodes.RET, Opcodes.ATHROW, Opcodes.TABLESWITCH, Opcodes.LOOKUPSWITCH -> true

            else -> false
        }

        if (isTerminator && instruction.next != null) {
            instruction.next.let { leaders.add(it) }
        }
    }

    val basicBlocks = mutableListOf<MutableList<AbstractInsnNode>>()
    var currentBlock = mutableListOf<AbstractInsnNode>()

    for (instruction in this) {
        if (leaders.contains(instruction) && currentBlock.isNotEmpty()) {
            basicBlocks.add(currentBlock)
            currentBlock = mutableListOf()
        }
        currentBlock.add(instruction)
    }

    if (currentBlock.isNotEmpty()) {
        basicBlocks.add(currentBlock)
    }

    return basicBlocks
}
