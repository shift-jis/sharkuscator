package dynamic_invoke;

import org.objectweb.asm.Opcodes;

import java.lang.invoke.ConstantCallSite;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Base64;

public class DynamicInvokeDecode {
    private static final MethodHandle decodeHandle;
    private static final MethodHandle returnHandle;

    public static void main(String[] args) {
        try {
            DynamicInvokeDecode dynamicInvokeDecode = new DynamicInvokeDecode();
            System.out.println((String) bootstrap(MethodHandles.lookup(), "test", MethodType.methodType(String.class, DynamicInvokeDecode.class, String.class, String.class, String.class, String.class), Opcodes.INVOKEVIRTUAL,
                    "dynamic_invoke.DynamicInvokeDecode",
                    "test", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;").dynamicInvoker().invoke(dynamicInvokeDecode, "1", "2", "3", "4"));
//            System.out.println(new String(((byte[]) decodeHandle.invoke(Base64.getDecoder(), "xZLDvMOow6nigqzDrMOgw7I=".getBytes()))));
//            System.out.println((String) returnHandle.invoke(dynamicInvokeDecode, "1", "2", "3", "4"));
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    private String test(String arg1, String arg2, String arg3, String arg4) {
        return arg1 + arg2 + arg3 + arg4;
    }

    static {
        try {
            decodeHandle = MethodHandles.lookup().findVirtual(Base64.Decoder.class, "decode", MethodType.methodType(byte[].class, byte[].class));
            returnHandle = MethodHandles.lookup().findVirtual(DynamicInvokeDecode.class, "test", MethodType.methodType(String.class, String.class, String.class, String.class, String.class));
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static ConstantCallSite bootstrap(MethodHandles.Lookup lookup, String a, MethodType newType, int opcode, String className, String name, String descriptor) throws ClassNotFoundException, NoSuchMethodException,
            IllegalAccessException {
        MethodType methodType = MethodType.fromMethodDescriptorString(descriptor, DynamicInvokeDecode.class.getClassLoader());
        Class<?> targetClass = Class.forName(className);

        MethodHandle targetMethodHandle;
        if (opcode == 184) { // INVOKESTATIC
            targetMethodHandle = lookup.findStatic(targetClass, name, methodType);
        } else if (opcode == 182) { // INVOKEVIRTUAL
            targetMethodHandle = lookup.findVirtual(targetClass, name, methodType);
        } else if (opcode == 185) { // INVOKEINTERFACE
            targetMethodHandle = lookup.findVirtual(targetClass, name, methodType); // Assuming interface is also handled by findVirtual in this context
        } else {
            throw new IllegalArgumentException("Unsupported opcode: " + opcode); // Or handle other opcodes if needed
        }

        return new ConstantCallSite(targetMethodHandle.asType(newType));
    }
}
