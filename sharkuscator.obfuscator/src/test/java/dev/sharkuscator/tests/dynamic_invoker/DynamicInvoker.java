package dev.sharkuscator.tests.dynamic_invoker;

import java.lang.reflect.InvocationTargetException;

public class DynamicInvoker {
    public static void main(String[] args) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
    }

    public static void function(String string) {
        System.out.println("Hello World " + string);
    }
}
