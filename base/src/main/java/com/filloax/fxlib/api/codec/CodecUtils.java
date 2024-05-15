package com.filloax.fxlib.api.codec;

import com.mojang.serialization.Codec;
import kotlin.reflect.KCallable;
import kotlin.reflect.KClass;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;
import java.util.*;

public class CodecUtils {
    public static <E> Codec<Set<E>> setOf(Codec<E> elementCodec)
    {
        return elementCodec.listOf().xmap(HashSet::new, ArrayList::new);
    }

    public interface ConstructorProxy<T> {
        T newInstance(Object... args);
    }

    /**
     * Java version of {@link com.filloax.fxlib.api.codec.CodecUtilsKt#constructorWithOptionals(KClass)} to
     * work with mappings, as kotlin reflections seem to have issues with those at runtime.
     * Needs to have all parameters specified even if some would be optional, as a side effect.
     */
    public static <T> ConstructorProxy<T> constructorWithOptionals(@NotNull Constructor<T> constructor) {
        Parameter[] parameters = constructor.getParameters();
        boolean[] isPrimitiveType = new boolean[parameters.length];

        for (int i = 0; i < parameters.length; i++) {
            isPrimitiveType[i] = parameters[i].getType().isPrimitive();
        }

        return args -> {
            if (args.length != parameters.length) {
                throw new IllegalArgumentException("Incorrect number of arguments: is " + args.length
                        + ", should be " + parameters.length);
            }

            Object[] processedArgs = new Object[args.length];
            for (int i = 0; i < args.length; i++) {
                Object arg = args[i];
                if (arg instanceof Optional<?> o) {
                    arg = o.orElse(null);
                }
                processedArgs[i] = arg;
            }

            // Check types
            for (int i = 0; i < processedArgs.length; i++) {
                Object arg = processedArgs[i];
                if (arg == null) continue; // Hopefully it's supposed to be nullable

                Parameter param = parameters[i];
                Class<?> argType = arg.getClass();
                Class<?> expectedType = param.getType();

                if (isPrimitiveType[i]) {
                    argType = wrapperToPrimitive.getOrDefault(argType, argType);
                } else {
                    argType = primitiveToWrapper.getOrDefault(argType, argType);
                }

                if (!expectedType.isAssignableFrom(argType)) {
                    throw new IllegalArgumentException("Type for arg " + i + " \"" + param.getName() +
                            "\" is wrong: is " + arg.getClass() + ", should be " + expectedType);
                }
            }

            try {
                return constructor.newInstance(processedArgs);
            } catch (InstantiationException | IllegalAccessException | java.lang.reflect.InvocationTargetException e) {
                e.printStackTrace();
                return null;
            }
        };
    }

    private static final Map<Class<?>, Class<?>> primitiveToWrapper = Map.of(
            int.class, Integer.class,
            long.class, Long.class,
            float.class, Float.class,
            double.class, Double.class,
            short.class, Short.class,
            byte.class, Byte.class,
            char.class, Character.class,
            boolean.class, Boolean.class
    );

    private static final Map<Class<?>, Class<?>> wrapperToPrimitive = Map.of(
            Integer.class, int.class,
            Long.class, long.class,
            Float.class, float.class,
            Double.class, double.class,
            Short.class, short.class,
            Byte.class, byte.class,
            Character.class, char.class,
            Boolean.class, boolean.class
    );
}
