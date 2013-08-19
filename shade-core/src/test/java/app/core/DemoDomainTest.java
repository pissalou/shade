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

import static junit.framework.Assert.assertEquals;

/**
 * @author pma
 */
public class DemoDomainTest extends AbstractPersistenceTest {

    @Test
    public void testAnyUserSave() {
        assertEquals(0, Db.findAll(User.class).size());
        new User("caca", "prout").save();
        assertEquals(1, Db.findAll(User.class).size());
        User user = anyInstance(User.class);
        assertEquals(1, Db.findAll(User.class).size());
        user.delete();
        assertEquals(0, Db.findAll(User.class).size());
    }
}
