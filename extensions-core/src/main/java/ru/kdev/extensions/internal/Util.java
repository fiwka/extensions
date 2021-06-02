package ru.kdev.extensions.internal;

import org.objectweb.asm.Type;
import sun.misc.Unsafe;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public final class Util {

    public static MethodHandles.Lookup TRUSTED_LOOKUP;
    public static Unsafe UNSAFE;

    public static String getMethodDescriptor(Method m) {
        return Type.getMethodDescriptor(m);
    }

    static {
        try {
            Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
            theUnsafe.setAccessible(true);
            UNSAFE = (Unsafe) theUnsafe.get(null);
            Field implLookup = MethodHandles.Lookup.class.getDeclaredField("IMPL_LOOKUP");
            TRUSTED_LOOKUP = (MethodHandles.Lookup) UNSAFE.getObjectVolatile(UNSAFE.staticFieldBase(implLookup), UNSAFE.staticFieldOffset(implLookup));
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}
