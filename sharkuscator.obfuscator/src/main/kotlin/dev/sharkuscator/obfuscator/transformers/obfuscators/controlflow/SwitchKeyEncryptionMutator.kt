package dev.sharkuscator.obfuscator.transformers.obfuscators.controlflow

import dev.sharkuscator.commons.AssemblyHelper.buildInstructionList
import dev.sharkuscator.commons.AssemblyHelper.integerPushInstruction
import dev.sharkuscator.obfuscator.transformers.TransformerStrength
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.*
import kotlin.random.Random

object SwitchKeyEncryptionMutator : ControlFlowMangleMutator {
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

        val newSwitchDefaultHandlerLabel = LabelNode()
        val originalDefaultTargetLabel = when (targetInstruction) {
            is LookupSwitchInsnNode -> targetInstruction.dflt
            is TableSwitchInsnNode -> targetInstruction.dflt
            else -> throw IllegalStateException("Unreachable: originalSwitchNode type already checked")
        }

        val newSwitchLabels = obfuscatedCaseData.map { it.second }.toTypedArray()
        val newSwitchKeys = obfuscatedCaseData.map { it.first }.toIntArray()

        instructions.insert(targetInstruction, buildInstructionList {
            add(integerPushInstruction(xorOperand))
            add(InsnNode(Opcodes.IXOR))
            add(LookupSwitchInsnNode(newSwitchDefaultHandlerLabel, newSwitchKeys, newSwitchLabels))
            obfuscatedCaseData.forEach { (_, intermediateLabel, originalCaseTargetLabel) ->
                add(intermediateLabel)
                add(JumpInsnNode(Opcodes.GOTO, originalCaseTargetLabel))
            }
            add(newSwitchDefaultHandlerLabel)
            add(JumpInsnNode(Opcodes.GOTO, originalDefaultTargetLabel))
        })
        instructions.remove(targetInstruction)
    }

    override fun isApplicableFor(instruction: AbstractInsnNode, applicationChancePercentage: Int): Boolean {
        val shouldApplyBasedOnChance = Random.nextInt(0, 100) <= applicationChancePercentage
        return (instruction is LookupSwitchInsnNode || instruction is TableSwitchInsnNode) && shouldApplyBasedOnChance
    }

    private fun extractSwitchCases(switchNode: AbstractInsnNode): MutableList<Pair<Int, LabelNode>> {
        return when (switchNode) {
            is LookupSwitchInsnNode -> {
                switchNode.keys.indices.mapTo(mutableListOf()) { index ->
                    switchNode.keys[index] to switchNode.labels[index]
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

    override fun transformerStrength(): TransformerStrength {
        return TransformerStrength.MODERATE
    }
}
