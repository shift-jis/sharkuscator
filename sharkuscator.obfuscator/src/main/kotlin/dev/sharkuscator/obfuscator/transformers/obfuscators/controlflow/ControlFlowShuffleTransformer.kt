package dev.sharkuscator.obfuscator.transformers.obfuscators.controlflow

import dev.sharkuscator.obfuscator.configuration.transformers.TransformerConfiguration
import dev.sharkuscator.obfuscator.events.TransformerEvents
import dev.sharkuscator.obfuscator.transformers.BaseTransformer
import dev.sharkuscator.obfuscator.transformers.TransformerStrength
import meteordevelopment.orbit.EventHandler
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.*


object ControlFlowShuffleTransformer : BaseTransformer<TransformerConfiguration>("ControlFlowShuffle", TransformerConfiguration::class.java) {
    private const val MIN_METHOD_SIZE_FOR_SHUFFLE = 5
    private const val MAX_RECURSION_DEPTH = 10

    @EventHandler
    @Suppress("unused")
    private fun onMethodTransform(event: TransformerEvents.MethodTransformEvent) {
        if (!isEligibleForExecution() || !shouldTransformMethod(event.obfuscationContext, event.anytypeNode)) {
            return
        }

        shuffleControlFlow(event.anytypeNode.node)
    }

    override fun transformerStrength(): TransformerStrength {
        return TransformerStrength.MODERATE
    }

    private fun shuffleControlFlow(targetMethodNode: MethodNode, recursionDepth: Int = 0) {
        val instructions = targetMethodNode.instructions
        if (instructions.size() <= MIN_METHOD_SIZE_FOR_SHUFFLE || recursionDepth >= MAX_RECURSION_DEPTH) {
            return
        }

        val firstBlockLabel = LabelNode()
        val secondBlockLabel = LabelNode()

        val splitInstruction = instructions.get(instructions.size() / 2)
        val lastInstruction = instructions.last

        val splitIndex = instructions.indexOf(splitInstruction)
        if (targetMethodNode.tryCatchBlocks.any { blockNode -> splitIndex > instructions.indexOf(blockNode.start) && splitIndex < instructions.indexOf(blockNode.end) }) {
            return
        }

        val instructionsToMove = mutableListOf<AbstractInsnNode>()
        var currentInstruction = instructions.first
        while (currentInstruction != null && currentInstruction != splitInstruction) {
            instructionsToMove.add(currentInstruction)
            currentInstruction = currentInstruction.next
        }

        val firstBlockInstructions = InsnList()
        instructionsToMove.forEach { instruction ->
            instructions.remove(instruction)
            firstBlockInstructions.add(instruction)
        }

        firstBlockInstructions.insert(firstBlockLabel)
        firstBlockInstructions.add(JumpInsnNode(Opcodes.GOTO, secondBlockLabel))

        instructions.insert(lastInstruction, firstBlockInstructions)
        instructions.insertBefore(splitInstruction, JumpInsnNode(Opcodes.GOTO, firstBlockLabel))
        instructions.insertBefore(splitInstruction, secondBlockLabel)

        targetMethodNode.localVariables?.removeIf { variableNode ->
            instructions.indexOf(variableNode.end) < instructions.indexOf(variableNode.start)
        }
        shuffleControlFlow(targetMethodNode, recursionDepth + 1)
    }
}
