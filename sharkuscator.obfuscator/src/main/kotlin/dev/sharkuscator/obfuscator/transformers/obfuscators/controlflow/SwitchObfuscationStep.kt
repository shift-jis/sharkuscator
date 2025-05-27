package dev.sharkuscator.obfuscator.transformers.obfuscators.controlflow

import dev.sharkuscator.obfuscator.utilities.BytecodeUtils.complexIntegerPushInstruction
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.*
import kotlin.random.Random

class SwitchObfuscationStep : ControlFlowObfuscationStep {
    override fun processInstruction(instructions: InsnList, targetInstruction: AbstractInsnNode) {
        if (targetInstruction !is LookupSwitchInsnNode && targetInstruction !is TableSwitchInsnNode) {
            return
        }

        val originalSwitchCases = extractSwitchCases(targetInstruction)
        val xorOperand = Random.nextInt()

        val obfuscatedCaseData = mutableListOf<Triple<Int, LabelNode, LabelNode>>()
        originalSwitchCases.forEach { (originalKey, originalTargetLabel) ->
            val obfuscatedKey = originalKey xor xorOperand
            obfuscatedCaseData.add(Triple(obfuscatedKey, LabelNode(), originalTargetLabel))
        }
        obfuscatedCaseData.sortBy { it.first }

        val newInstructionSequence = InsnList()

        val obfuscatedSwitchEntryPoint = LabelNode()
        val newSwitchDefaultHandlerLabel = LabelNode()
        val originalDefaultTargetLabel = when (targetInstruction) {
            is LookupSwitchInsnNode -> targetInstruction.dflt
            is TableSwitchInsnNode -> targetInstruction.dflt
            else -> throw IllegalStateException("Unreachable: originalSwitchNode type already checked")
        }

        newInstructionSequence.add(JumpInsnNode(Opcodes.GOTO, obfuscatedSwitchEntryPoint))

        newInstructionSequence.add(newSwitchDefaultHandlerLabel)
        newInstructionSequence.add(JumpInsnNode(Opcodes.GOTO, originalDefaultTargetLabel))

        newInstructionSequence.add(obfuscatedSwitchEntryPoint)
        newInstructionSequence.add(complexIntegerPushInstruction(xorOperand))
        newInstructionSequence.add(InsnNode(Opcodes.IXOR))

        obfuscatedCaseData.forEach { (_, intermediateLabel, originalCaseTargetLabel) ->
            newInstructionSequence.add(intermediateLabel)
            newInstructionSequence.add(JumpInsnNode(Opcodes.GOTO, originalCaseTargetLabel))
        }

        instructions.insert(targetInstruction, newInstructionSequence)
        instructions.remove(targetInstruction)
    }

    override fun isApplicableFor(instruction: AbstractInsnNode, applicationChancePercentage: Int): Boolean {
        val shouldApplyBasedOnChance = Random.nextInt(0, 100) <= applicationChancePercentage
        return (instruction is LookupSwitchInsnNode || instruction is TableSwitchInsnNode) && shouldApplyBasedOnChance
    }

    private fun extractSwitchCases(switchNode: AbstractInsnNode): MutableList<Pair<Int, LabelNode>> {
        return when (switchNode) {
            is LookupSwitchInsnNode -> {
                switchNode.keys.indices.mapTo(mutableListOf()) { i ->
                    switchNode.keys[i] to switchNode.labels[i]
                }
            }

            is TableSwitchInsnNode -> {
                (switchNode.min..switchNode.max).mapIndexedTo(mutableListOf()) { index, key ->
                    key to switchNode.labels[index]
                }
            }

            else -> throw IllegalStateException("Invalid instruction type passed to extractSwitchCases. Expected LookupSwitchInsnNode or TableSwitchInsnNode, got ${switchNode::class.simpleName}")
        }
    }

    override fun getObfuscationStrength(): ObfuscationStrength {
        return ObfuscationStrength.MODERATE
    }
}
