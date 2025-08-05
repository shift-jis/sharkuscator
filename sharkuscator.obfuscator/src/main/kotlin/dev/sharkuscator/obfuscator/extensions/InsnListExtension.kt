package dev.sharkuscator.obfuscator.extensions

import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.InsnList
import org.objectweb.asm.tree.JumpInsnNode
import org.objectweb.asm.tree.LookupSwitchInsnNode
import org.objectweb.asm.tree.TableSwitchInsnNode
import kotlin.random.Random
import kotlin.random.nextInt


fun InsnList.chunkedRandomly(sizeRange: IntRange): MutableList<InsnList> {
    val instructionChunks = mutableListOf<InsnList>()
    var nextChunkSize = Random.nextInt(sizeRange)

    for (instruction in this) {
        val instructionChunk = instructionChunks.lastOrNull()
        if (instructionChunk == null || instructionChunk.size() >= nextChunkSize) {
            instructionChunks.add(InsnList().apply { add(instruction) })
            nextChunkSize = Random.nextInt(sizeRange)
        } else {
            instructionChunk.add(instruction)
        }
    }

    return instructionChunks
}

fun InsnList.partitionIntoBasicBlocks(): MutableList<InsnList> {
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

    val basicBlocks = mutableListOf<InsnList>()
    var currentBlock = InsnList()

    for (instruction in this) {
        if (leaders.contains(instruction) && currentBlock.isNotEmpty()) {
            basicBlocks.add(currentBlock)
            currentBlock = InsnList()
        }
        currentBlock.add(instruction)
    }

    if (currentBlock.isNotEmpty()) {
        basicBlocks.add(currentBlock)
    }

    return basicBlocks
}

fun InsnList.isNotEmpty(): Boolean = size() > 0
