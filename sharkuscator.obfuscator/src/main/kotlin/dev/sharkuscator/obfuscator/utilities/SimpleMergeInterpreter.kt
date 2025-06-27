package dev.sharkuscator.obfuscator.utilities

import org.objectweb.asm.ConstantDynamic
import org.objectweb.asm.Handle
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import org.objectweb.asm.tree.*
import org.objectweb.asm.tree.analysis.AnalyzerException
import org.objectweb.asm.tree.analysis.BasicValue
import org.objectweb.asm.tree.analysis.Interpreter


object SimpleMergeInterpreter : Interpreter<BasicValue>(Opcodes.ASM9) {
    private val nullType: Type = Type.getObjectType("null")

    override fun newValue(type: Type?): BasicValue? {
        if (type == null) {
            return BasicValue.UNINITIALIZED_VALUE
        }
        return when (type.sort) {
            Type.BOOLEAN, Type.CHAR, Type.BYTE, Type.SHORT, Type.INT -> BasicValue.INT_VALUE
            Type.DOUBLE -> BasicValue.DOUBLE_VALUE
            Type.FLOAT -> BasicValue.FLOAT_VALUE
            Type.LONG -> BasicValue.LONG_VALUE

            Type.ARRAY, Type.OBJECT -> BasicValue(type)
            Type.VOID -> null

            else -> throw AssertionError()
        }
    }

    override fun newOperation(instruction: AbstractInsnNode): BasicValue? {
        return when (instruction.opcode) {
            Opcodes.ICONST_M1, Opcodes.ICONST_0, Opcodes.ICONST_1, Opcodes.ICONST_2, Opcodes.ICONST_3,
            Opcodes.ICONST_4, Opcodes.ICONST_5, Opcodes.BIPUSH, Opcodes.SIPUSH -> BasicValue.INT_VALUE

            Opcodes.FCONST_0, Opcodes.FCONST_1, Opcodes.FCONST_2 -> BasicValue.FLOAT_VALUE
            Opcodes.DCONST_0, Opcodes.DCONST_1 -> BasicValue.DOUBLE_VALUE
            Opcodes.LCONST_0, Opcodes.LCONST_1 -> BasicValue.LONG_VALUE

            Opcodes.GETSTATIC -> newValue(Type.getType((instruction as FieldInsnNode).desc))
            Opcodes.NEW -> newValue(Type.getObjectType((instruction as TypeInsnNode).desc))

            Opcodes.ACONST_NULL -> newValue(nullType)
            Opcodes.JSR -> BasicValue.RETURNADDRESS_VALUE

            Opcodes.LDC -> {
                when (val value = (instruction as LdcInsnNode).cst) {
                    is Double -> BasicValue.DOUBLE_VALUE
                    is Float -> BasicValue.FLOAT_VALUE
                    is Long -> BasicValue.LONG_VALUE
                    is Int -> BasicValue.INT_VALUE

                    is ConstantDynamic -> newValue(Type.getType(value.descriptor))
                    is Handle -> newValue(Type.getObjectType("java/lang/invoke/MethodHandle"))
                    is String -> newValue(Type.getObjectType("java/lang/String"))

                    is Type -> when (value.sort) {
                        Type.OBJECT, Type.ARRAY -> newValue(Type.getObjectType("java/lang/Class"))
                        Type.METHOD -> newValue(Type.getObjectType("java/lang/invoke/MethodType"))
                        else -> throw AnalyzerException(instruction, "Illegal LDC value $value")
                    }

                    else -> throw AnalyzerException(instruction, "Illegal LDC value $value")
                }
            }

            else -> throw AssertionError()
        }
    }

    override fun copyOperation(instruction: AbstractInsnNode, value: BasicValue?): BasicValue? {
        return value
    }

    override fun unaryOperation(instruction: AbstractInsnNode, value: BasicValue?): BasicValue? {
        return when (instruction.opcode) {
            Opcodes.IFEQ, Opcodes.IFNE, Opcodes.IFLT, Opcodes.IFGE, Opcodes.IFGT, Opcodes.IFLE, Opcodes.TABLESWITCH, Opcodes.LOOKUPSWITCH, Opcodes.IRETURN, Opcodes.LRETURN,
            Opcodes.FRETURN, Opcodes.DRETURN, Opcodes.ARETURN, Opcodes.PUTSTATIC, Opcodes.MONITORENTER, Opcodes.MONITOREXIT, Opcodes.IFNULL, Opcodes.IFNONNULL, Opcodes.ATHROW -> null

            Opcodes.INEG, Opcodes.IINC, Opcodes.L2I, Opcodes.F2I, Opcodes.D2I, Opcodes.I2B, Opcodes.I2C, Opcodes.I2S, Opcodes.INSTANCEOF, Opcodes.ARRAYLENGTH -> BasicValue.INT_VALUE
            Opcodes.LNEG, Opcodes.I2L, Opcodes.F2L, Opcodes.D2L -> BasicValue.LONG_VALUE

            Opcodes.DNEG, Opcodes.I2D, Opcodes.L2D, Opcodes.F2D -> BasicValue.DOUBLE_VALUE
            Opcodes.FNEG, Opcodes.I2F, Opcodes.L2F, Opcodes.D2F -> BasicValue.FLOAT_VALUE

            Opcodes.GETFIELD -> newValue(Type.getType((instruction as FieldInsnNode).desc))
            Opcodes.NEWARRAY -> when ((instruction as IntInsnNode).operand) {
                Opcodes.T_BOOLEAN -> newValue(Type.getType("[Z"))
                Opcodes.T_CHAR -> newValue(Type.getType("[C"))
                Opcodes.T_BYTE -> newValue(Type.getType("[B"))
                Opcodes.T_SHORT -> newValue(Type.getType("[S"))
                Opcodes.T_INT -> newValue(Type.getType("[I"))
                Opcodes.T_FLOAT -> newValue(Type.getType("[F"))
                Opcodes.T_DOUBLE -> newValue(Type.getType("[D"))
                Opcodes.T_LONG -> newValue(Type.getType("[J"))
                else -> throw AnalyzerException(instruction, "Invalid array type")
            }

            Opcodes.ANEWARRAY -> newValue(Type.getType("[" + Type.getObjectType((instruction as TypeInsnNode).desc)))
            Opcodes.CHECKCAST -> newValue(Type.getObjectType((instruction as TypeInsnNode).desc))

            else -> throw java.lang.AssertionError()
        }
    }

    override fun binaryOperation(instruction: AbstractInsnNode, value1: BasicValue?, value2: BasicValue?): BasicValue? {
        return when (instruction.opcode) {
            Opcodes.IALOAD, Opcodes.BALOAD, Opcodes.CALOAD, Opcodes.SALOAD, Opcodes.IADD, Opcodes.ISUB, Opcodes.IMUL,
            Opcodes.IDIV, Opcodes.IREM, Opcodes.ISHL, Opcodes.ISHR, Opcodes.IUSHR, Opcodes.IAND, Opcodes.IOR, Opcodes.IXOR,
            Opcodes.LCMP, Opcodes.FCMPL, Opcodes.FCMPG, Opcodes.DCMPL, Opcodes.DCMPG -> BasicValue.INT_VALUE

            Opcodes.LALOAD, Opcodes.LADD, Opcodes.LSUB, Opcodes.LMUL, Opcodes.LDIV, Opcodes.LREM, Opcodes.LSHL, Opcodes.LSHR,
            Opcodes.LUSHR, Opcodes.LAND, Opcodes.LOR, Opcodes.LXOR -> BasicValue.LONG_VALUE

            Opcodes.DALOAD, Opcodes.DADD, Opcodes.DSUB, Opcodes.DMUL, Opcodes.DDIV, Opcodes.DREM -> BasicValue.DOUBLE_VALUE
            Opcodes.FALOAD, Opcodes.FADD, Opcodes.FSUB, Opcodes.FMUL, Opcodes.FDIV, Opcodes.FREM -> BasicValue.FLOAT_VALUE

            Opcodes.IF_ICMPEQ, Opcodes.IF_ICMPNE, Opcodes.IF_ICMPLT, Opcodes.IF_ICMPGE, Opcodes.IF_ICMPGT,
            Opcodes.IF_ICMPLE, Opcodes.IF_ACMPEQ, Opcodes.IF_ACMPNE, Opcodes.PUTFIELD -> null

            Opcodes.AALOAD -> BasicValue.REFERENCE_VALUE

            else -> throw java.lang.AssertionError()
        }
    }

    override fun ternaryOperation(instruction: AbstractInsnNode, p1: BasicValue?, p2: BasicValue?, p3: BasicValue?): BasicValue? {
        return null
    }

    override fun naryOperation(instruction: AbstractInsnNode, values: List<BasicValue?>?): BasicValue? {
        return when (instruction.getOpcode()) {
            Opcodes.MULTIANEWARRAY -> newValue(Type.getType((instruction as MultiANewArrayInsnNode).desc))
            Opcodes.INVOKEDYNAMIC -> newValue(Type.getReturnType((instruction as InvokeDynamicInsnNode).desc))
            else -> newValue(Type.getReturnType((instruction as MethodInsnNode).desc))
        }
    }

    override fun returnOperation(instruction: AbstractInsnNode, value: BasicValue?, expected: BasicValue?) {
    }

    override fun merge(value1: BasicValue, value2: BasicValue): BasicValue? {
        return if (value1 != value2) BasicValue.UNINITIALIZED_VALUE else value1
    }
}
