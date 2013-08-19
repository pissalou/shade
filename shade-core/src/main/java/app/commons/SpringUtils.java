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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Helps you debug Spring ApplicationContext.
 * FIXME: What about JDK dynamic proxies?
 */
public class SpringUtils {

    public static final String PROXY_CLASS_PATTERN = "$$EnhancerByCGLIB$$";
    public static final String PROXY_STRING = "CGLIB";

    public static Map<String, Object> getDependencies(Object obj) {
        Map<String, Object> dependencies = new HashMap<String, Object>();
        for (Field field : getFields(obj.getClass())) {
            String fieldName = field.getName();
            if (!fieldName.contains(PROXY_STRING)) {
                Object dependency;
                try {
                    dependency = field.get(obj);
                    // In case CGLIG has proxied the field.
                    if (dependency == null) {
                        try {
                            dependency = ReflectionUtils.invokeGetter(obj, fieldName);
                        } catch (Exception e) {
                            // silent
                        }
                    }
                    dependencies.put(fieldName, dependency);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return dependencies;
    }

    /**
     * We cannot use <code>clazz.getField(String fieldName)</code> because it only returns public fields.
     */
    protected static Collection<Field> getFields(Class clazz) {
        Class currentClass = clazz;
        Collection<Field> fields = new ArrayList<Field>();
        while (currentClass != null) {
           for (Field field : currentClass.getDeclaredFields()) {
               field.setAccessible(true);
               fields.add(field);
           }
           currentClass = currentClass.getSuperclass();
        }
        return fields;
    }
}