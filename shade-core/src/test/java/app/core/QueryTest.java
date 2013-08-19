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

package app.core;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author pma
 */
public class QueryTest extends AbstractPersistenceTest {

    @Test
    public void testAllQueries() {
        assertEquals(Db.<User>find("from User"), Db.getSession().getNamedQuery("ALL_USERS").list());
        new User("pascal", "mazars").save();
        assertEquals(1, Db.<User>find("from User").size());
        assertEquals(1, Db.getSession().getNamedQuery("ALL_USERS").list().size());
        assertEquals(Db.<User>find("from User"), Db.getSession().getNamedQuery("ALL_USERS").list());
    }
}
