package dev.sharkuscator.obfuscator.transformers.obfuscators.controlflow

import dev.sharkuscator.obfuscator.ObfuscatorServices
import dev.sharkuscator.obfuscator.transformers.TransformerStrength
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.AbstractInsnNode
import org.objectweb.asm.tree.InsnList
import org.objectweb.asm.tree.JumpInsnNode

object GotoChainOptimizeMutator : ControlFlowMangleMutator {
    override fun processInstruction(instructions: InsnList, targetInstruction: AbstractInsnNode) {
        if (targetInstruction !is JumpInsnNode) {
            ObfuscatorServices.sharkLogger.error("GotoChainOptimizeMutator received an instruction it cannot process, despite canProcess being true.")
            return
        }

        val instructionAfterTarget = targetInstruction.label.next
        if (instructionAfterTarget != null && instructionAfterTarget.opcode == Opcodes.GOTO) {
            val chainedGotoInstruction = instructionAfterTarget as JumpInsnNode
            targetInstruction.label = chainedGotoInstruction.label
        }
    }

    override fun isApplicableFor(instruction: AbstractInsnNode, applicationChancePercentage: Int): Boolean {
//        val shouldApplyBasedOnChance = Random.nextInt(0, 100) <= applicationChancePercentage
        return instruction is JumpInsnNode && instruction.opcode == Opcodes.GOTO // && shouldApplyBasedOnChance
    }

    override fun transformerStrength(): TransformerStrength {
        return TransformerStrength.LIGHT
    }
}
