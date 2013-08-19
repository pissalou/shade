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

import org.databene.feed4junit.Feeder;
import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.Assert.*;

/**
 * Auto-generate domain objects.
 * It should be possible to generate values with hints.
 */
@RunWith(Feeder.class)
public class DomainTest extends AbstractDomainAwareTest {

    @Test
    public void testUserInit() {
        User user = create(User.class);
        assertEquals(user, same(User.class));
        assertEquals(user.getPerson(), same(Person.class));
        assertNotSame(user, create(User.class));
        assertNotSame(user.getPerson(), create(Person.class));
    }

    @Test
    public void testUserInit(String username, String password) {
        create(User.class); //, username, password);
    }
}
