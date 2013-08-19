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

import org.junit.Test;

import java.util.Collection;
import java.util.Collections;

import static app.commons.CollectionFactory.*;
/**
 * @author pma
 */
public class CollectionFactoryTest {

    @Test
    public void testAll() {
        for (Order order : Order.values()) {
            for (ToleranceToNull toleranceToNull : ToleranceToNull.values()) {
                for (ToleranceToDuplicate toleranceToDuplicate : ToleranceToDuplicate.values()) {
                    Collection<String> col = CollectionFactory.newCollection(order, toleranceToNull, toleranceToDuplicate, "d", "b", "a", "c");
                    System.out.println((isSorted(col) ? "SORTED" : isSequential(col) ? "SEQUENTIAL" : "RANDOM") + " "
                            + (allowNull(col) ? "ALLOWS_NULL" : "PROHIBITS_NULL") + " "
                            + (allowsDuplicate(col) ? "ALLOWS_DUPLICATE" : "PROHIBITS_DUPLICATE") + " -> " + col.getClass().getSimpleName() + " " + col);
                }
            }
        }
    }
}
