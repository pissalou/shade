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

import java.util.*;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 *
 */
public class CollectionFactory {
    public enum Order { SORTED, SEQUENTIAL, RANDOM }
    public enum ToleranceToNull { ALLOWS_NULL, PROHIBITS_NULL }
    public enum ToleranceToDuplicate { ALLOWS_DUPLICATE, PROHIBITS_DUPLICATE }

    public static boolean allowNull(Collection col) {
        try {
            col.contains(null);
            return true;
        } catch (NullPointerException e) {
            return false;
        }
    }

    public static <T> boolean allowsDuplicate(Collection<T> col) {
        T element = col.iterator().next();
        boolean result = col.add(element);
        col.remove(element);
        return result;
    }

    public static boolean isSorted(Collection col) {
        return col instanceof SortedSet;
    }

    public static boolean isSequential(Collection col) {
        return col instanceof AbstractSequentialList;
    }

    public static <T extends Comparable> Collection<T> newCollection(Order order, ToleranceToNull toleranceToNull, ToleranceToDuplicate toleranceToDuplicate, T... elements) {
        switch (toleranceToDuplicate) {
            case ALLOWS_DUPLICATE:
                return newList(order, toleranceToNull, elements);
            default:
                return newSet(order, toleranceToNull, elements);
        }
    }

    public static <T> Set<T> newSet(Order order, ToleranceToNull toleranceToNull, T... elements) {
        switch (toleranceToNull) {
            case ALLOWS_NULL:
                return newSetAllowsNull(order, elements);
            default:
//                return new HashSet<T>();
//                throw new UnsupportedOperationException("newSetProhibitsNull(order, elements) not implemented yet");
                return newSetProhibitsNull(order, elements);
        }
    }

    private static <T> Set<T> newSetProhibitsNull(Order order, T... elements) {
        Set<T> col;
        switch (order) {
            case SORTED:
                col = new ConcurrentSkipListSet<T>();
                break;
            case SEQUENTIAL:
            default:
                col = new HashSet<T>() {
                    @Override
                    public boolean add(T element) {
                        if (element == null) {
                            throw new NullPointerException();
                        }
                        return super.add(element);
                    }
                };
        }
        Collections.addAll(col, elements);
        return col;
    }

    public static <T> Set<T> newSetAllowsNull(Order order, T... elements) {
        Set<T> col;
        switch (order) {
            case SORTED:
                col = new TreeSet<T>();
                break;
            case SEQUENTIAL:
            default:
                col = new HashSet<T>();
        }
        Collections.addAll(col, elements);
        return col;
    }

    public static <T extends Comparable> List<T> newList(Order order, ToleranceToNull toleranceToNull, T... elements) {
        switch (toleranceToNull) {
            case ALLOWS_NULL:
                return newListAllowsNull(order, elements);
            default:
//                return new ArrayList<T>();
//                throw new UnsupportedOperationException("newListProhibitsNull(order, elements) not implemented yet");
                return newListProhibitsNull(order, elements);
        }
    }

    public static <T extends Comparable> List<T> newListAllowsNull(Order order, T... elements) {
        List<T> col;
        switch (order) {
            case SEQUENTIAL:
                col = new LinkedList<T>();
                break;
            case SORTED:
                col = new ArrayList<T>() {
                        @Override
                        public boolean add(T element) {
                            boolean result = super.add(element);
                            Collections.sort(this);
                            return result;
                        }
                };
                break;
            default:
                col = new ArrayList<T>();
        }
        Collections.addAll(col, elements);
        return col;
    }

    private static <T> List<T> newListProhibitsNull(Order order, T... elements) {
        List<T> col;
        switch (order) {
            case SEQUENTIAL:
                col = new LinkedList<T>();
                break;
            case SORTED:
                col = new LinkedList<T>();
                break;
            default:
                col = new ArrayList<T>();
        }
        Collections.addAll(col, elements);
        return col;
    }

//    public static <T> List<T> newList(T... elements) {
        //return newCollection(ToleranceToDuplicate.ALLOWS_DUPLICATE, ToleranceToNull.ALLOWS_NULL, Order.SEQUENTIAL, elements);
//    }

    public static <T> Set<T> newSortedSet(T... elements) {
        return newSetAllowsNull(Order.SORTED, elements);
    }

    public static void main(String[] args) {
        //System.out.println(CollectionFactory.newList("a", "c", "d", "b"));
        System.out.println(CollectionFactory.newSortedSet("a", "c", "d", "b"));
    }
}