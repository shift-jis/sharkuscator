package dev.sharkuscator.obfuscator.transformers.obfuscators.constants.strategies

import dev.sharkuscator.obfuscator.ObfuscatorServices
import dev.sharkuscator.obfuscator.transformers.strategies.StringConstantObfuscationStrategy
import dev.sharkuscator.obfuscator.utilities.BytecodeUtils
import org.apache.commons.lang3.RandomStringUtils
import org.mapleir.asm.ClassNode
import org.mapleir.asm.MethodNode
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.AbstractInsnNode
import org.objectweb.asm.tree.InsnList
import java.nio.charset.StandardCharsets
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.DESKeySpec
import javax.crypto.spec.IvParameterSpec

class DESStringObfuscationStrategy : StringConstantObfuscationStrategy {
    private val methodDescriptor = "(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;"
    private val methodAccess = Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC + Opcodes.ACC_BRIDGE + Opcodes.ACC_SYNTHETIC
    private val decodeMethodNodeCache = mutableMapOf<ClassNode, MethodNode>()

    private val desCipher = Cipher.getInstance("DES/CBC/PKCS5Padding")
    private val keyFactory = SecretKeyFactory.getInstance("DES")
    private val ivSpec = IvParameterSpec(ByteArray(8))

    override fun prepareDecoderMethod(classNode: ClassNode, decoderMethodName: String): MethodNode {
        val builtMethodNode = BytecodeUtils.createMethodNode(methodAccess, decoderMethodName, methodDescriptor).apply {
        }

        return MethodNode(builtMethodNode, classNode).also {
            decodeMethodNodeCache.putIfAbsent(classNode, it)
            classNode.addMethod(it)
        }
    }

    override fun replaceInstructions(preparedDecoder: MethodNode, instructions: InsnList, targetInstruction: AbstractInsnNode, originalString: String) {
        val resultPair = obfuscateString(originalString, RandomStringUtils.randomAlphanumeric(32))
        ObfuscatorServices.sharkLogger.info("${resultPair.first} ${resultPair.second.decodeToString()}")
    }

    override fun obfuscateString(originalString: String, keyBytes: ByteArray): Pair<String, ByteArray> {
        desCipher.init(Cipher.ENCRYPT_MODE, keyFactory.generateSecret(DESKeySpec(keyBytes)), ivSpec)
        return Pair(String(desCipher.doFinal(originalString.toByteArray(StandardCharsets.ISO_8859_1)), StandardCharsets.ISO_8859_1), keyBytes)
    }
}
