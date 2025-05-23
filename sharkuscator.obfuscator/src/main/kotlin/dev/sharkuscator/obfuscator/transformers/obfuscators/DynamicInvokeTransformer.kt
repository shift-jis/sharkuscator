package dev.sharkuscator.obfuscator.transformers.obfuscators

import dev.sharkuscator.obfuscator.ObfuscatorServices
import dev.sharkuscator.obfuscator.configuration.transformers.TransformerConfiguration
import dev.sharkuscator.obfuscator.events.ObfuscatorEvents
import dev.sharkuscator.obfuscator.events.TransformerEvents
import dev.sharkuscator.obfuscator.extensions.*
import dev.sharkuscator.obfuscator.transformers.BaseTransformer
import dev.sharkuscator.obfuscator.transformers.TransformerPriority
import dev.sharkuscator.obfuscator.utilities.BytecodeUtils
import meteordevelopment.orbit.EventHandler
import org.apache.commons.lang3.RandomStringUtils
import org.objectweb.asm.Handle
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import org.objectweb.asm.tree.*
import kotlin.random.Random


class DynamicInvokeTransformer : BaseTransformer<TransformerConfiguration>("DynamicInvoke", TransformerConfiguration::class.java) {
    private val returnOpcodes = arrayOf(Opcodes.POP, Opcodes.POP2, Opcodes.RETURN, Opcodes.IFNONNULL, Opcodes.IFNULL, Opcodes.CHECKCAST)
    private val invokeOpcodes = arrayOf(Opcodes.INVOKESTATIC, Opcodes.INVOKEVIRTUAL, Opcodes.INVOKEINTERFACE)

    private lateinit var bootstrapMethodHandle: Handle
    var generatedInvokerMethodName: String = ""
    var invokerHostClassName: String = ""

    @EventHandler
    @Suppress("unused")
    private fun onInitialization(event: ObfuscatorEvents.InitializationEvent) {
        val hostClassNode = event.context.jarContents.classContents.filter { !it.shouldSkipTransform() && !event.context.exclusions.excluded(it) }.random() ?: return
        invokerHostClassName = hostClassNode.name

        val methodDictionary = event.context.resolveDictionary(MethodNode::class.java)
        generatedInvokerMethodName = methodDictionary.generateNextName(null)

        val invokerMethodNode = createInvokerMethodNode(invokerHostClassName, generatedInvokerMethodName)
        hostClassNode.addMethod(org.mapleir.asm.MethodNode(invokerMethodNode, hostClassNode))

        bootstrapMethodHandle = Handle(Opcodes.H_INVOKESTATIC, invokerHostClassName, invokerMethodNode.name, invokerMethodNode.desc, false)
    }

    @EventHandler
    @Suppress("unused")
    private fun onMethodTransform(event: TransformerEvents.MethodTransformEvent) {
        if (transformed || event.anytypeNode.isNative || event.anytypeNode.isStaticInitializer() || event.anytypeNode.isConstructor()) {
            return
        }

        val classNode = event.anytypeNode.owner
        if (classNode.isDeclaredAsAnnotation() || classNode.isDeclaredAsInterface() || classNode.name == invokerHostClassName) {
            return
        }

        event.anytypeNode.node.instructions.filterIsInstance<MethodInsnNode>().filter { invokeOpcodes.contains(it.opcode) }.forEach { instruction ->
            val shouldApplyBasedOnChance = Random.nextInt(0, 100) <= 80
            if (instruction.owner.startsWith("[") || instruction.owner == "java/lang/invoke/MethodHandles" || !shouldApplyBasedOnChance) {
                return@forEach
            }

            var invokeDescriptor = instruction.desc
            if (instruction.opcode != Opcodes.INVOKESTATIC) {
                invokeDescriptor = if ((instruction.owner.startsWith('L') || instruction.owner.startsWith("["))) {
                    invokeDescriptor.replaceFirst("(", "(${instruction.owner}")
                } else {
                    invokeDescriptor.replaceFirst("(", "(L${instruction.owner};")
                }
            }

            val invokeReturnType = Type.getReturnType(invokeDescriptor)
            val castedReturnType = generalizeObjectType(invokeReturnType)
            val castedArgumentTypes = Type.getArgumentTypes(invokeDescriptor)
            for (index in castedArgumentTypes.indices) {
                if (instruction.opcode != Opcodes.INVOKESTATIC && index == 0) {
                    continue
                }
                castedArgumentTypes[index] = generalizeObjectType(castedArgumentTypes[index])
            }

            val classNameMapping = ObfuscatorServices.symbolRemapper.symbolMappings[instruction.owner] ?: instruction.owner.replace('/', '.')
            val methodNameMapping = ObfuscatorServices.symbolRemapper.mapMethodName(instruction.owner, instruction.name, instruction.desc)
            val descriptionMapping = ObfuscatorServices.symbolRemapper.applyMappingsToText(instruction.desc)

            invokeDescriptor = Type.getMethodDescriptor(castedReturnType, *castedArgumentTypes)
            event.anytypeNode.node.instructions.insertBefore(
                instruction, InvokeDynamicInsnNode(
                    RandomStringUtils.randomAlphabetic(14), invokeDescriptor, bootstrapMethodHandle,
                    instruction.opcode, classNameMapping, methodNameMapping, descriptionMapping
                )
            )

            if (invokeReturnType.sort == Type.OBJECT) {
                val checkCastNode = TypeInsnNode(Opcodes.CHECKCAST, invokeReturnType.internalName)
                if (!returnOpcodes.contains(instruction.next?.opcode) && checkCastNode.desc != Type.getInternalName(Any::class.java)) {
                    event.anytypeNode.node.instructions.insertBefore(instruction, checkCastNode)
                }
            }

            event.anytypeNode.node.instructions.remove(instruction)
        }
    }

    override fun getExecutionPriority(): Int {
        return TransformerPriority.SIXTY_FIVE
    }

    private fun createInvokerMethodNode(className: String, methodName: String): MethodNode {
        val invokerDescriptor = "(Ljava/lang/invoke/MethodHandles\$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;ILjava/lang/String;Ljava/lang/String;Ljava/lang/String;)Ljava/lang/Object;"
        val methodHandleDescriptor = "(Ljava/lang/Class;Ljava/lang/String;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/MethodHandle;"
        val methodTypeDescriptor = "(Ljava/lang/String;Ljava/lang/ClassLoader;)Ljava/lang/invoke/MethodType;"

        // Labels for control flow
        val labelCheckVirtual = LabelNode()
        val labelCheckInterface = LabelNode()
        val labelReturnCallSite = LabelNode()
        val labelUnsupportedOpcode = LabelNode()

        return BytecodeUtils.createMethodNode(Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC, methodName, invokerDescriptor).apply {
            instructions = BytecodeUtils.buildInstructionList(
                // Load and resolve method type
                VarInsnNode(Opcodes.ALOAD, 6),
                LdcInsnNode(Type.getObjectType(className)),
                MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/Class", "getClassLoader", "()Ljava/lang/ClassLoader;", false),
                MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/invoke/MethodType", "fromMethodDescriptorString", methodTypeDescriptor, false),
                VarInsnNode(Opcodes.ASTORE, 7),

                // Load target class
                VarInsnNode(Opcodes.ALOAD, 4),
                MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/Class", "forName", "(Ljava/lang/String;)Ljava/lang/Class;", false),
                VarInsnNode(Opcodes.ASTORE, 8),

                // Check for static method opcode (184)
                VarInsnNode(Opcodes.ILOAD, 3),
                IntInsnNode(Opcodes.SIPUSH, 184),
                JumpInsnNode(Opcodes.IF_ICMPNE, labelCheckVirtual),
                VarInsnNode(Opcodes.ALOAD, 0),
                VarInsnNode(Opcodes.ALOAD, 8),
                VarInsnNode(Opcodes.ALOAD, 5),
                VarInsnNode(Opcodes.ALOAD, 7),
                MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/invoke/MethodHandles\$Lookup", "findStatic", methodHandleDescriptor, false),
                VarInsnNode(Opcodes.ASTORE, 9),
                JumpInsnNode(Opcodes.GOTO, labelReturnCallSite),

                // Check for virtual method opcode (182)
                labelCheckVirtual,
                VarInsnNode(Opcodes.ILOAD, 3),
                IntInsnNode(Opcodes.SIPUSH, 182),
                JumpInsnNode(Opcodes.IF_ICMPNE, labelCheckInterface),
                VarInsnNode(Opcodes.ALOAD, 0),
                VarInsnNode(Opcodes.ALOAD, 8),
                VarInsnNode(Opcodes.ALOAD, 5),
                VarInsnNode(Opcodes.ALOAD, 7),
                MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/invoke/MethodHandles\$Lookup", "findVirtual", methodHandleDescriptor, false),
                VarInsnNode(Opcodes.ASTORE, 9),
                JumpInsnNode(Opcodes.GOTO, labelReturnCallSite),

                // Check for interface method opcode (185)
                labelCheckInterface,
                VarInsnNode(Opcodes.ILOAD, 3),
                IntInsnNode(Opcodes.SIPUSH, 185),
                JumpInsnNode(Opcodes.IF_ICMPNE, labelUnsupportedOpcode),
                VarInsnNode(Opcodes.ALOAD, 0),
                VarInsnNode(Opcodes.ALOAD, 8),
                VarInsnNode(Opcodes.ALOAD, 5),
                VarInsnNode(Opcodes.ALOAD, 7),
                MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/invoke/MethodHandles\$Lookup", "findVirtual", methodHandleDescriptor, false),
                VarInsnNode(Opcodes.ASTORE, 9),

                // Return ConstantCallSite
                labelReturnCallSite,
                TypeInsnNode(Opcodes.NEW, "java/lang/invoke/ConstantCallSite"),
                InsnNode(Opcodes.DUP),
                VarInsnNode(Opcodes.ALOAD, 9),
                VarInsnNode(Opcodes.ALOAD, 2),
                MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/invoke/MethodHandle", "asType", "(Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/MethodHandle;", false),
                MethodInsnNode(Opcodes.INVOKESPECIAL, "java/lang/invoke/ConstantCallSite", "<init>", "(Ljava/lang/invoke/MethodHandle;)V", false),
                InsnNode(Opcodes.ARETURN),

                // Unsupported opcode handling
                labelUnsupportedOpcode,
                TypeInsnNode(Opcodes.NEW, "java/lang/IllegalArgumentException"),
                InsnNode(Opcodes.DUP),
                TypeInsnNode(Opcodes.NEW, "java/lang/StringBuilder"),
                InsnNode(Opcodes.DUP),
                MethodInsnNode(Opcodes.INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "()V", false),
                LdcInsnNode("Unsupported opcode: "),
                MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false),
                VarInsnNode(Opcodes.ILOAD, 3),
                MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(I)Ljava/lang/StringBuilder;", false),
                MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false),
                MethodInsnNode(Opcodes.INVOKESPECIAL, "java/lang/IllegalArgumentException", "<init>", "(Ljava/lang/String;)V", false),
                InsnNode(Opcodes.ATHROW)
            )
        }
    }

    private fun generalizeObjectType(type: Type): Type {
        return when {
            type.sort == Type.ARRAY && type.elementType.sort == Type.OBJECT -> type
            type.sort == Type.OBJECT -> Type.getType(Any::class.java)
            else -> type
        }
    }
}
