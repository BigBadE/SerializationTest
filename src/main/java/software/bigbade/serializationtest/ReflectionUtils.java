package software.bigbade.serializationtest;

import com.google.common.collect.Lists;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

/**
 * Copyright © 2020 Property of HQGAMING STUDIO, LLC
 * All rights reserved. No part of this publication may be reproduced, distributed, or
 * transmitted in any form or by any means, including photocopying, recording, or other
 * electronic or mechanical methods, without the prior written permission of the publisher,
 * except in the case of brief quotations embodied in critical reviews and certain other
 * noncommercial uses permitted by copyright law.
 */

public class ReflectionUtils {
    private ReflectionUtils() {}

    public static Iterable<Field> getFieldsUpTo(@Nonnull Class<?> startClass,
                                                @Nullable Class<?> exclusiveParent) {
        List<Field> currentClassFields = Lists.newArrayList(startClass.getDeclaredFields());
        Class<?> parentClass = startClass.getSuperclass();

        if (parentClass != null &&
                (exclusiveParent == null || !(parentClass.equals(exclusiveParent)))) {
            List<Field> parentClassFields =
                    (List<Field>) getFieldsUpTo(parentClass, exclusiveParent);
            currentClassFields.addAll(parentClassFields);
        }

        return currentClassFields;
    }

    public static Method getMethod(String name, Class<?> clazz, Class<?> params) {
        try {
            return clazz.getDeclaredMethod(name, params);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Class<?> getClass(String name) {
        try {
            return Class.forName(name);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Object callMethod(Method method, Object... arguments) {
        try {
            return method.invoke(null, arguments);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }
}
