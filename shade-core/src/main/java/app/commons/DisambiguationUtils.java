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

import app.commons.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.*;

public class DisambiguationUtils {

    @Deprecated
    public static <T> String toDisambiguatedString(T obj, T[] others, Field... excludedFields) {
        StringBuilder strBuf = new StringBuilder();
        if (obj.getClass().isArray()) {
            throw new RuntimeException("Arrays not supported");
        } else {
            Collection<DisambiguationField> disambiguationFields = getDisambiguationFields(obj, others, excludedFields);
            strBuf.append(obj.toString()).append(toDisambiguatedString(obj, disambiguationFields)).append("\n");
            for (T other : others) {
                strBuf.append(other.toString()).append(toDisambiguatedString(other, disambiguationFields)).append("\n");
            }
        }
        return strBuf.toString();
    }

    public static <T> String toDisambiguatedString(T obj, Collection<DisambiguationField> disambiguationFields) {
        StringBuilder strBuf = new StringBuilder();
        if (disambiguationFields.size() > 0) {
            strBuf.append(" (");
        }
        for (DisambiguationField disambiguationField : disambiguationFields) {
//            strBuf.append(disambiguationField.field.getName()).append(": ").append(ReflectionUtils.getFieldValue(obj, disambiguationField.path)).append(", ");
        }
        if (disambiguationFields.size() > 0) {
            strBuf.delete(strBuf.length() - 2, strBuf.length());
            strBuf.append(" )");
        }
        return strBuf.toString();
    }

    public static <T> Set<DisambiguationField> getDisambiguationFields(T obj, T[] others, Field... excludedFields) {
        Set<DisambiguationField> disambiguationFields = new HashSet<DisambiguationField>();
        for (T other : others) {
            disambiguationFields.addAll(getDisambiguationFields(obj.getClass(), obj, other, excludedFields));
        }
        return disambiguationFields;
    }

    private static <T> Set<DisambiguationField> getDisambiguationFields(T obj, T obj2, Field... excludedFields) {
        return getDisambiguationFields(obj.getClass(), obj, obj2, excludedFields);
    }


    private static <T> Set<DisambiguationField> getDisambiguationFields(Class type, T obj, T obj2, Field... excludedFields) {
        return getDisambiguationFields(type, obj, obj2, "", excludedFields);
    }

    private static String appendFieldNameToPath(String path, Field field) {
        return path + (path.length() > 0 ? "." : "") + field.getName();
    }

    private static <T> Set<DisambiguationField> getDisambiguationFields(Class type, T obj, T obj2, String path, Field... excludedFields) {
        Set<DisambiguationField> disambiguationFields = new HashSet<DisambiguationField>();
        Field[] fieldArray = type.getDeclaredFields();
        try {
            for (Field field : fieldArray) {
                if (!Arrays.asList(excludedFields).contains(field)) {
                    field.setAccessible(true);
                    if (obj != null) {
                        try {
                            if (compare((Comparable) field.get(obj), (Comparable) field.get(obj2)) != 0) {
                                disambiguationFields.add(new DisambiguationField(field, appendFieldNameToPath(path, field)));
                            }
                        } catch (ClassCastException e) {
                            //recursion
                            disambiguationFields.addAll(getDisambiguationFields(field.getType(), field.get(obj), field.get(obj2), appendFieldNameToPath(path, field)));
                        }
                    } else {
                        if (obj2 != null) {
                            disambiguationFields.add(new DisambiguationField(field, appendFieldNameToPath(path, field)));
                        }
                    }
                }
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        return disambiguationFields;
    }

    private static String toFieldDisambiguatedString(Field field, Object val, Object val2) {
        return new StringBuilder(field.getName()).append(": ").append(val).append(", ").toString();
    }

    public static boolean isComparable(Class clazz) {
        return clazz.isPrimitive() || Arrays.asList(clazz.getInterfaces()).contains(Comparable.class);
    }

    public static <T> int compare(Field comparableField, T obj, T obj2) {
        try {
           return ((Comparable) comparableField.get(obj)).compareTo(comparableField.get(obj2));
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> int compare(Comparable obj, Comparable obj2) {
        if (obj == null) {
            return obj2 == null ? 0 : -1;
        } else {
            return obj.compareTo(obj2);
        }
    }

    public static final class DisambiguationField {
        public Field field;
        public String path;

        public DisambiguationField(Field field, String path) {
            this.field = field;
            this.path = path;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            DisambiguationField that = (DisambiguationField) o;

            if (field != null ? !field.equals(that.field) : that.field != null) return false;
            if (path != null ? !path.equals(that.path) : that.path != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = field != null ? field.hashCode() : 0;
            result = 31 * result + (path != null ? path.hashCode() : 0);
            return result;
        }

        @Override
        public String toString() {
            return field + ", path=" + path + '\n';
        }
    }

    static class Person {
        String firstName;
        String lastName;
        boolean isMale;
        Date dateOfBirth;

        Person(String firstName, String lastName, boolean male, Date dateOfBirth) {
            this.firstName = firstName;
            this.lastName = lastName;
            isMale = male;
            this.dateOfBirth = dateOfBirth;
        }

        @Override
        public String toString() {
            return firstName + " " + lastName; // + " " + (isMale ? "M" : "F") + " born " + dateOfBirth;
        }
    }

    public static void main(String[] args) throws Exception {
        Field[] excludedFields = new Field[] {ReflectionUtils.getField(Person.class, "firstName"), ReflectionUtils.getField(Person.class, "lastName")};
//        System.out.println(toDisambiguatedString(
//                new Person("Robert", "Lafondue", true, null),
//                new Person[]{
//                        new Person("Robert", "Lafondue", true, new DateMidnight("1974-01-01")),
//                        new Person("Robert", "Lafondue", true, new DateMidnight("1975-01-01")),
//                        new Person("Robert", "Lafondue", true, new DateMidnight("1976-01-01"))
//                },
//                excludedFields));
        System.out.println(toDisambiguatedString(new String[] {"a", "b", "c"}, new String[][] { {"a", "b", "d" } }));
    }
}