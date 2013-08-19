/*
 * Copyright (C) 2013 Pascal Mazars
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package app.commons;

import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Helper class for the Reflection API.
 */
public class ReflectionUtils {

    private static final Class[] NO_PARAMS = new Class[0];
    private static final Object[] NO_ARGS = new Object[0];

    /**
     * Retrieve the value of a field using Reflection API.
     * The class the instance is an 'instanceof' is evaluated at runtime.
     * @param instance the instance to get the field value from
     * @param fieldName the name of the field
     * @param <T> the type of the field
     * @return the field value cast to the expectedType
     */
    public static <T> T getFieldValue(Object instance, String fieldName, Class<T> expectedType) {
        return getFieldValue(instance.getClass(), instance, fieldName, expectedType);
    }

    /**
     * Retrieve the value of a field using Reflection API.
     * @param clazz the class the instance is an 'instanceof'
     * @param instance the instance to get the field value from
     * @param fieldName the name of the field
     * @param <T> the type of the field
     * @return the field value cast to the expectedType
     */
    public static <T> T getFieldValue(Class clazz, Object instance, String fieldName, Class<T> expectedType) {
          try {
              return (T) getField(clazz, fieldName).get(instance);
          } catch (IllegalAccessException e) {
              throw new RuntimeException(e);
          }
    }

    /**
     * Retrieve the value of a static field using Reflection API.
     * @param clazz the class the instance is an 'instanceof'
     * @param fieldName the name of the field
     * @param <T> the type of the field
     * @return the field value cast to the expectedType
     */
    public static <T> T getStaticValue(Class clazz, String fieldName, Class<T> expectedType) {
        return getFieldValue(clazz, null, fieldName, expectedType);
    }

    /**
     * We cannot use <code>clazz.getField(String fieldName)</code> because it only returns public fields.
     */
    public static Field getField(Class clazz, String fieldName) {
        Class currentClass = clazz;
        while (currentClass != null) {
           try {
               Field field = currentClass.getDeclaredField(fieldName);
               field.setAccessible(true);
                return field;
               } catch (NoSuchFieldException e) {
                currentClass = currentClass.getSuperclass();
               }
           }
        throw new RuntimeException(new NoSuchFieldException(fieldName));
    }

    public static Field[] getAllDeclaredFields(Class clazz) {
        Class currentClass = clazz;
        List<Field> fields = new ArrayList<Field>();
        while (currentClass != null) {
           fields.addAll(Arrays.asList(currentClass.getDeclaredFields()));
            currentClass = currentClass.getSuperclass();
       }
        return fields.toArray(new Field[fields.size()]);
    }

    /**
     * Sets the value of a field using Reflection API.
     * The class the instance is an 'instanceof' is evaluated at runtime.
     * @param instance the instance to get the field value from
     * @param fieldName the name of the field
     * @param value the new value to be set for the field
     * @param <T> the type of the field
     */
    public static <T> void setFieldValue(Object instance, String fieldName, T value) {
        setFieldValue(instance.getClass(), instance, fieldName, value);
    }

    /**
     * Sets the value of a field using Reflection API.
     * @param clazz the class the instance is an 'instanceof'
     * @param instance the instance to get the field value from
     * @param fieldName the name of the field
     * @param value the new value to be set for the field
     * @param <T> the type of the field
     */
    public static <T> void setFieldValue(Class clazz, Object instance, String fieldName, T value) {
        try {
            getField(clazz, fieldName).set(instance, value);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Invoke a no-arg method using Reflection API.
     * The class the instance is an 'instanceof' is evaluated at runtime.
     * The method signature is evaluated at runtime.
     * @param instance the instance to get the field value from
     * @param methodName the name of the method to invoke
     * @param <T> the return type of the method
     * @return the return value cast to the expectedType
     */
    public static <T> T invokeMethod(Object instance, String methodName, Class<T> expectedReturnType) {
        return invokeMethod(instance.getClass(), instance, methodName, NO_PARAMS, NO_ARGS, expectedReturnType);
    }

    /**
     * Invoke a no-arg method using Reflection API.
     * The class the instance is an 'instanceof' is evaluated at runtime.
     * The method signature is evaluated at runtime.
     * @param instance the instance to get the field value from
     * @param methodName the name of the method to invoke
     * @param <T> the return type of the method
     * @return the return value cast to the expectedType
     */
    public static <T> T invokeMethod(Object instance, String methodName, Object arg, Class<T> expectedReturnType) {
        return invokeMethod(instance, methodName, new Object[] {arg}, expectedReturnType);
    }

    /**
     * Invoke a method using Reflection API.
     * The class the instance is an 'instanceof' is evaluated at runtime.
     * The method signature is evaluated at runtime.
     * @param instance the instance to get the field value from
     * @param methodName the name of the method to invoke
     * @param args the arguments to invoke the method with
     * @param <T> the return type of the method
     * @return the return value cast to the expectedType
     */
    public static <T> T invokeMethod(Object instance, String methodName, Object[] args, Class<T> expectedReturnType) {
        return invokeMethod(instance.getClass(), instance, methodName, argsToMethodSignature(args), args, expectedReturnType);
    }

    // FIXME: handle primitive type
    public static Class[] argsToMethodSignature(Object[] args) {
        Class[] methodSignature = new Class[args.length];
        for (int i = 0; i < args.length; i++) {
            //FIXME: should not be String.class here
            methodSignature[i] = args[i] != null ? args[i].getClass() : String.class;
        }
        return methodSignature;
    }

    /**
     * We cannot use <code>clazz.getMethod(String name, Class<?>... parameterTypes)</code> because it only returns public methods.
     */
    private static Method getMethod(Class clazz, String methodName, Class<?>... parameterTypes) {
        Class currentClass = clazz;
        while (currentClass != null) {
           try {
               Method method = currentClass.getDeclaredMethod(methodName, parameterTypes);
               method.setAccessible(true);
                return method;
               } catch (NoSuchMethodException e) {
                currentClass = currentClass.getSuperclass();
               }
           }
        throw new RuntimeException(new NoSuchMethodException(methodName));
    }

    /**
     * Invoke a void method using Reflection API.
     * The class the instance is an 'instanceof' is evaluated at runtime.
     * The method signature is evaluated at runtime.
     * @param instance the instance to get the field value from
     * @param methodName the name of the method to invoke
     * @param args the arguments to invoke the method with
     */
    public static <T> T invokeMethod(Object instance, String methodName, Object... args) {
        return (T) invokeMethod(instance, methodName, argsToMethodSignature(args), args);
    }

    /**
     * Invoke a void method using Reflection API.
     * The class the instance is an 'instanceof' is evaluated at runtime.
     * @param instance the instance to get the field value from
     * @param methodName the name of the method to invoke
     * @param methodSignature the method signature
     * @param args the arguments to invoke the method with
     */
    public static <T> T invokeMethod(Object instance, String methodName, Class[] methodSignature, Object[] args) {
        return (T) invokeMethod(instance.getClass(), instance, methodName, methodSignature, args, Void.class);
    }

//    public static <T> T invokeMethod(Class clazz, Object instance, String methodName, Class[] methodSignature, Object[] args, Class<T> expectedReturnType) {
//        return invokeMethod(clazz, instance, methodName, methodSignature, args, expectedReturnType);
//    }

    /**
     * Invoke a method using Reflection API.
     * @param clazz the class the instance is an 'instanceof'
     * @param instance the instance to get the field value from
     * @param methodName the name of the method to invoke
     * @param methodSignature the method signature
     * @param args the arguments to invoke the method with
     * @param <T> the return type of the method
     * @return the return value cast to the expectedType
     */
    public static <T> T invokeMethod(Class clazz, Object instance, String methodName, Class[] methodSignature, Object[] args, Class<T> expectedReturnType) {
          try {
              return (T) getMethod(clazz, methodName, methodSignature).invoke(instance, args);
          } catch (InvocationTargetException e) {
              throw new RuntimeException(e);
          } catch (IllegalAccessException e) {
              throw new RuntimeException(e);
          }
    }

    public static <T> T invokeStaticMethod(Class clazz, String methodName) {
        return (T) invokeMethod(clazz, null, methodName);
    }

    public static <T> T invokeGetter(Object instance, String fieldName) {
        return (T) invokeMethod(instance, "get" + StringUtils.capitalize(fieldName));
    }
}