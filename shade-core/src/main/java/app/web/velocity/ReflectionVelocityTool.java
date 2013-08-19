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

package app.web.velocity;

import app.commons.ReflectionUtils;
import org.hibernate.annotations.NaturalId;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Arrays;

/**
 * @author Pma
 */
public class ReflectionVelocityTool {

    public Field[] getFields(Object obj) throws NoSuchFieldException {
        return obj != null ? obj.getClass().getDeclaredFields() : null;
    }

    public Object getFieldValue(Field field, Object instance) {
        return ReflectionUtils.getFieldValue(instance, field.getName(), Object.class);
    }

    public Annotation[] getFieldAnnotations(Object obj, String fieldName) throws NoSuchFieldException {
        return getFieldAnnotations(obj.getClass().getField(fieldName));
    }

    public Annotation[] getFieldAnnotations(Field field) throws NoSuchFieldException {
        return field.getAnnotations();
    }

    public boolean isRequired(Field field) throws NoSuchFieldException {
        for (Annotation annotation : getFieldAnnotations(field)) {
            if (annotation instanceof NotNull || annotation instanceof NotEmpty) {
                return true;
            }
        }
        return false;
    }

    public boolean isNumber(Field field) throws NoSuchFieldException {
        Class fieldClass = field.getType();
        return fieldClass == Integer.class || fieldClass == Long.class
                || fieldClass == Integer.TYPE || fieldClass == Long.TYPE;
    }

    public boolean isReadOnly(Field field) throws NoSuchFieldException {
        for (Annotation annotation : getFieldAnnotations(field)) {
            if (annotation instanceof NaturalId) {
                return true;
            }
        }
        return false;
    }
}
