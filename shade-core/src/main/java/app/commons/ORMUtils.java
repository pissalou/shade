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

import app.core.PersistentObject;
import org.hibernate.annotations.NaturalId;

import java.lang.reflect.Field;
import java.util.*;

/**
 * Everything you need to make ORM more Play!ful.
 */
public class ORMUtils {

    /**
     * We cannot use <code>clazz.getField(String fieldName)</code> because it only returns public fields.
     */
    private static Collection<Field> getFields(Class clazz) {
        List<Field> fields = new ArrayList<Field>();
        Class currentClass = clazz;
        while (currentClass != null) {
            Field[] fieldArray = currentClass.getDeclaredFields();
            for (Field field : fieldArray) {
                field.setAccessible(true);
            }
            fields.addAll(0, Arrays.asList(fieldArray));
            currentClass = currentClass.getSuperclass();
        }
        return fields;
    }

    public static int hashCode(PersistentObject obj) {
        if (obj.isPersistent()) {
            return obj.getClass().hashCode() + obj.getId().getId(); // TODO: implement in ID
        }
        Class clazz = obj.getClass();
        PrimeNumberGenerator primeNumberGenerator = new PrimeNumberGenerator();
        int hashCode = -primeNumberGenerator.next();
        try {
            for (Field field : getFields(clazz)) {
                if (field.getAnnotation(NaturalId.class) != null) { //TODO: implement if no field is NaturalId
                    Object fieldValue = field.get(obj);
                    if (fieldValue != null)
                        hashCode -= primeNumberGenerator.next() * fieldValue.hashCode();
                }
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException("error in hashCode()");
        }
        return hashCode;
    }

    public static boolean equals(PersistentObject obj1, Object obj2) {
        if (obj1 == null) {
            return obj2 == null;
        } else {
            Class class1 = obj1.getClass();
            Class class2 = obj2.getClass();
            if (class1 != class2) {
                return false;
            }
            if (obj1.isPersistent() && ((PersistentObject) obj2).isPersistent()) {
                // both persisted -> compare primary key.
                return obj1.getId().equals(((PersistentObject) obj2).getId());
            } else if (obj1.isPersistent() || ((PersistentObject) obj2).isPersistent()) {
                // only one persisted -> they cannot be the same
                return false;
            } else {
                // none persisted -> compare natural keys.
                try {
                    for (Field field : getFields(class1)) {
                        if (field.getAnnotation(NaturalId.class) != null) {
                            Object val1 = field.get(obj1);
                            Object val2 = field.get(obj2);
                            if (val1 == null && val2 != null || val1 != null && !val1.equals(val2)) {
                                return false;
                            }
                        }
                    }
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
                return true;
            }
        }
    }

    public static Collection<String> toTableHeaders(Class clazz) {
        Collection<String> headers = new ArrayList<String>();
        for (Field field : getFields(clazz)) {
            headers.add(field.getName());
        }
        return headers;
    }

    public static String toTableHeaderString(Class clazz, Integer... columnWidth) {
        StringBuilder strBuf = new StringBuilder();
        int i = 0;
        for (String header : toTableHeaders(clazz)) {
            try {
                strBuf.append(formatColumn(header, columnWidth[i++]));
            } catch (ArrayIndexOutOfBoundsException e) {
                strBuf.append(formatColumn(header));
            }
        }
        strBuf.append(" |");
        return strBuf.toString();
    }

    private static String formatColumn(Object value) {
        return formatColumn(value, -1);
    }

    /**
     * FIXME: use a TextFormat.
     * @param value
     * @param width
     * @return
     */
    private static String formatColumn(Object value, int width) {
        String column;
        try {
            column = "| " + (width != -1 ? value.toString().substring(0, width) : value) + " ";
        } catch (StringIndexOutOfBoundsException e) {
            width = Integer.parseInt(e.getMessage().substring(e.getMessage().lastIndexOf(":") + 2, e.getMessage().length()));
            column = "| " + value + " ";
            for (int j = 0; j <= width - value.toString().length(); j++) { column +=(" "); }
        } catch (NullPointerException e) {
            column = "| NULL ";
            for (int j = 0; j <= width - 4; j++) { column +=(" "); }
        }
        return column;
    }

    public static Collection<Object> toColumns(PersistentObject obj) {
        Collection<Object> columns = new ArrayList<Object>();
        Class clazz = obj.getClass();
        int i = 0;
        for (Field field : getFields(clazz)) {
            field.setAccessible(true);
            try {
                columns.add(field.get(obj));
            } catch (IllegalAccessException e2) {
            }
        }
        return columns;
    }

    public static String toResultString(PersistentObject obj, Integer... columnWidth) {
        StringBuilder strBuf = new StringBuilder();
        int i = 0;
        for (Object columnValue : toColumns(obj)) {
            try {
                strBuf.append(formatColumn(columnValue, columnWidth[i++]));
            } catch (ArrayIndexOutOfBoundsException e) {
                strBuf.append(formatColumn(columnValue));
            }
        }
        strBuf.append(" |");
        return strBuf.toString();
    }

    private static Integer[] calculateColumnWidth(String tableDump) {
        List<Integer> results = new ArrayList<Integer>();
        int currentIndex = 0;
        while (currentIndex != -1 && currentIndex < tableDump.indexOf('\n')) {
            int nextIndex = tableDump.indexOf("|", currentIndex + 1);
            results.add(nextIndex - currentIndex);
            currentIndex = nextIndex;
        }
        return results.toArray(new Integer[results.size()]);
    }

    public static String toResultString(Collection<? extends PersistentObject> results) {
        //1st pass
        StringBuilder strBuf = new StringBuilder();
        Iterator<? extends PersistentObject> iter = results.iterator();
        if (!iter.hasNext()) {
            return "empty table";
        }
        strBuf.append(toTableHeaderString(iter.next().getClass())).append('\n');
        for (PersistentObject obj : results) {
            strBuf.append(toResultString(obj));
        }
        String tableDump = strBuf.toString();
        Integer[] columnWidths = calculateColumnWidth(tableDump); //new Integer[] { 12, 12 , 12};
        int totalWidth = 0;
        for (int width : columnWidths) {
            totalWidth += width;
        }
        //2nd pass
        strBuf = new StringBuilder();
        strBuf.append(toTableHeaderString(results.iterator().next().getClass(), columnWidths)).append('\n');
        for (PersistentObject obj : results) {
            strBuf.append(toResultString(obj, columnWidths)).append('\n');
        }
        return strBuf.toString();
    }

    public static String toNonAmbigiousString(PersistentObject obj) {
        StringBuilder strBld = new StringBuilder();
        Class clazz = obj.getClass();
        try {
            for (Field field : getFields(clazz)) {
                if (field.getAnnotation(NaturalId.class) != null) {
                    Object fieldValue = field.get(obj);
                    if (fieldValue != null)
                        strBld.append(fieldValue).append(", ");
                }
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException("error in toNonAmbigiousString()");
        }
        return strBld.toString();
    }

    private static final class PrimeNumberGenerator {
        int i = 0;
        private static final int[] PRIME_NUMBER = new int[] {3, 7, 11, 13, 17, 19, 23, 29, 31};

        public int next() {
            return PRIME_NUMBER[i++];
        }
    }

    /**
     * Compare classes based on the possible dependency between them.
     */
    public static final class DependencyComparator implements Comparator<Class> {

        @Override
        public int compare(Class clazz, Class otherClazz) {
            Set<Class> fieldTypes = new HashSet<Class>();
            Set<Class> otherFieldTypes = new HashSet<Class>();
            for (Field field : ReflectionUtils.getAllDeclaredFields(clazz)) {
                fieldTypes.add(field.getType());
            }
            for (Field field : ReflectionUtils.getAllDeclaredFields(otherClazz)) {
                otherFieldTypes.add(field.getType());
            }
            if (fieldTypes.contains(otherClazz))
                return 2;
            else if (otherFieldTypes.contains(clazz))
                return -1;
            else
                return 1; // TODO: go recursive on this one
        }
    }
}