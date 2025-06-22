package dev.sharkuscator.obfuscator.transformers.obfuscators

import dev.sharkuscator.obfuscator.ObfuscatorServices
import dev.sharkuscator.obfuscator.configuration.transformers.TransformerConfiguration
import dev.sharkuscator.obfuscator.events.ObfuscatorEvents
import dev.sharkuscator.obfuscator.events.TransformerEvents
import dev.sharkuscator.obfuscator.extensions.*
import dev.sharkuscator.obfuscator.transformers.BaseTransformer
import dev.sharkuscator.obfuscator.transformers.TransformerPriority
import dev.sharkuscator.obfuscator.transformers.TransformerStrength
import dev.sharkuscator.obfuscator.utilities.AssemblyHelper.buildInstructionList
import dev.sharkuscator.obfuscator.utilities.AssemblyHelper.createMethodNode
import meteordevelopment.orbit.EventHandler
import org.apache.commons.lang3.RandomStringUtils
import org.mapleir.asm.ClassNode
import org.objectweb.asm.Handle
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import org.objectweb.asm.tree.*
import kotlin.random.Random


object DynamicInvokeTransformer : BaseTransformer<TransformerConfiguration>("DynamicInvoke", TransformerConfiguration::class.java) {
    private val returnOpcodes = arrayOf(Opcodes.POP, Opcodes.POP2, Opcodes.RETURN, Opcodes.IFNONNULL, Opcodes.IFNULL, Opcodes.CHECKCAST)
    private val invokeOpcodes = arrayOf(Opcodes.INVOKESTATIC, Opcodes.INVOKEVIRTUAL, Opcodes.INVOKEINTERFACE)

    private lateinit var bootstrapMethodHandle: Handle
    var generatedInvokerMethodName: String = ""
    var invokerHostClassName: String = ""

    @EventHandler
    @Suppress("unused")
    private fun onInitialization(event: ObfuscatorEvents.InitializationEvent) {
        if (!isEligibleForExecution()) {
            return
        }

        val selectedHostClassNode = event.context.classSource.iterate().filter { !it.shouldSkipTransform() && !event.context.exclusions.excluded(it) }.random() ?: return
        invokerHostClassName = selectedHostClassNode.name

        val invokerMethodNameGenerator = event.context.resolveDictionary<org.mapleir.asm.MethodNode, ClassNode>(org.mapleir.asm.MethodNode::class.java)
        generatedInvokerMethodName = invokerMethodNameGenerator.generateNextName(null)

        val newInvokerMethodAsmNode = createInvokerMethodNode(invokerHostClassName, generatedInvokerMethodName)
        selectedHostClassNode.addMethod(org.mapleir.asm.MethodNode(newInvokerMethodAsmNode, selectedHostClassNode))

        bootstrapMethodHandle = Handle(Opcodes.H_INVOKESTATIC, invokerHostClassName, newInvokerMethodAsmNode.name, newInvokerMethodAsmNode.desc, false)
    }

    @EventHandler
    @Suppress("unused")
    private fun onMethodTransform(event: TransformerEvents.MethodTransformEvent) {
        if (!isEligibleForExecution() || !shouldTransformMethod(event.context, event.anytypeNode) || event.anytypeNode.isStaticInitializer() || event.anytypeNode.isConstructor()) {
            return
        }

        val targetClassNode = event.anytypeNode.owner
        if (targetClassNode.isDeclaredAsAnnotation() || targetClassNode.isDeclaredAsInterface() || targetClassNode.name == invokerHostClassName) {
            return
        }

        event.anytypeNode.node.instructions.filterIsInstance<MethodInsnNode>().filter { invokeOpcodes.contains(it.opcode) }.forEach { instruction ->
            val shouldApplyBasedOnChance = Random.nextInt(0, 100) <= 80
            val isArrayOrInnerOrJavaPackage = instruction.owner.startsWith("[") || instruction.owner.contains("$") || instruction.owner.startsWith("java/")
            if (event.context.classSource.findClassNode(instruction.owner)?.isSpongeMixin() ?: false || exclusions.excluded(instruction.owner) || isArrayOrInnerOrJavaPackage || !shouldApplyBasedOnChance) {
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

            val classNameMapping = ObfuscatorServices.symbolRemapper.symbolMappings[instruction.owner]?.replace('/', '.') ?: instruction.owner.replace('/', '.')
            val methodNameMapping = ObfuscatorServices.symbolRemapper.mapMethodName(instruction.owner, instruction.name, instruction.desc)
            val descriptionMapping = ObfuscatorServices.symbolRemapper.applyMappingsToText(instruction.desc)

            invokeDescriptor = Type.getMethodDescriptor(castedReturnType, *castedArgumentTypes)
            event.anytypeNode.node.instructions.insertBefore(
                instruction, InvokeDynamicInsnNode(
                    RandomStringUtils.randomAlphabetic(4), invokeDescriptor, bootstrapMethodHandle,
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

    override fun transformerStrength(): TransformerStrength {
        return TransformerStrength.STRONG
    }

    override fun executionPriority(): Int {
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

        return createMethodNode(Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC, methodName, invokerDescriptor).apply {
            instructions = buildInstructionList {
                // Load and resolve method type
                add(VarInsnNode(Opcodes.ALOAD, 6))
                add(LdcInsnNode(Type.getObjectType(className)))
                add(MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/Class", "getClassLoader", "()Ljava/lang/ClassLoader;", false))
                add(MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/invoke/MethodType", "fromMethodDescriptorString", methodTypeDescriptor, false))
                add(VarInsnNode(Opcodes.ASTORE, 7))

                // Load target class
                add(VarInsnNode(Opcodes.ALOAD, 4))
                add(MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/Class", "forName", "(Ljava/lang/String;)Ljava/lang/Class;", false))
                add(VarInsnNode(Opcodes.ASTORE, 8))

                // Check for static method opcode (184)
                add(VarInsnNode(Opcodes.ILOAD, 3))
                add(IntInsnNode(Opcodes.SIPUSH, 184))
                add(JumpInsnNode(Opcodes.IF_ICMPNE, labelCheckVirtual))
                add(VarInsnNode(Opcodes.ALOAD, 0))
                add(VarInsnNode(Opcodes.ALOAD, 8))
                add(VarInsnNode(Opcodes.ALOAD, 5))
                add(VarInsnNode(Opcodes.ALOAD, 7))
                add(MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/invoke/MethodHandles\$Lookup", "findStatic", methodHandleDescriptor, false))
                add(VarInsnNode(Opcodes.ASTORE, 9))
                add(JumpInsnNode(Opcodes.GOTO, labelReturnCallSite))

                // Check for virtual method opcode (182)
                add(labelCheckVirtual)
                add(VarInsnNode(Opcodes.ILOAD, 3))
                add(IntInsnNode(Opcodes.SIPUSH, 182))
                add(JumpInsnNode(Opcodes.IF_ICMPNE, labelCheckInterface))
                add(VarInsnNode(Opcodes.ALOAD, 0))
                add(VarInsnNode(Opcodes.ALOAD, 8))
                add(VarInsnNode(Opcodes.ALOAD, 5))
                add(VarInsnNode(Opcodes.ALOAD, 7))
                add(MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/invoke/MethodHandles\$Lookup", "findVirtual", methodHandleDescriptor, false))
                add(VarInsnNode(Opcodes.ASTORE, 9))
                add(JumpInsnNode(Opcodes.GOTO, labelReturnCallSite))

                // Check for interface method opcode (185)
                add(labelCheckInterface)
                add(VarInsnNode(Opcodes.ILOAD, 3))
                add(IntInsnNode(Opcodes.SIPUSH, 185))
                add(JumpInsnNode(Opcodes.IF_ICMPNE, labelUnsupportedOpcode))
                add(VarInsnNode(Opcodes.ALOAD, 0))
                add(VarInsnNode(Opcodes.ALOAD, 8))
                add(VarInsnNode(Opcodes.ALOAD, 5))
                add(VarInsnNode(Opcodes.ALOAD, 7))
                add(MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/invoke/MethodHandles\$Lookup", "findVirtual", methodHandleDescriptor, false))
                add(VarInsnNode(Opcodes.ASTORE, 9))

                // Return ConstantCallSite
                add(labelReturnCallSite)
                add(TypeInsnNode(Opcodes.NEW, "java/lang/invoke/ConstantCallSite"))
                add(InsnNode(Opcodes.DUP))
                add(VarInsnNode(Opcodes.ALOAD, 9))
                add(VarInsnNode(Opcodes.ALOAD, 2))
                add(MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/invoke/MethodHandle", "asType", "(Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/MethodHandle;", false))
                add(MethodInsnNode(Opcodes.INVOKESPECIAL, "java/lang/invoke/ConstantCallSite", "<init>", "(Ljava/lang/invoke/MethodHandle;)V", false))
                add(InsnNode(Opcodes.ARETURN))

                // Unsupported opcode handling
                add(labelUnsupportedOpcode)
                add(TypeInsnNode(Opcodes.NEW, "java/lang/IllegalArgumentException"))
                add(InsnNode(Opcodes.DUP))
                add(TypeInsnNode(Opcodes.NEW, "java/lang/StringBuilder"))
                add(InsnNode(Opcodes.DUP))
                add(MethodInsnNode(Opcodes.INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "()V", false))
                add(LdcInsnNode("Unsupported opcode: "))
                add(MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false))
                add(VarInsnNode(Opcodes.ILOAD, 3))
                add(MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(I)Ljava/lang/StringBuilder;", false))
                add(MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false))
                add(MethodInsnNode(Opcodes.INVOKESPECIAL, "java/lang/IllegalArgumentException", "<init>", "(Ljava/lang/String;)V", false))
                add(InsnNode(Opcodes.ATHROW))
            }
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
