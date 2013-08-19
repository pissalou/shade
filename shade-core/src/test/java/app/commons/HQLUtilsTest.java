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

import app.core.AbstractDomainAwareTest;
import app.core.AbstractPersistenceTest;
import app.core.Db;
import app.core.User;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author pma
 */
public class HQLUtilsTest extends AbstractPersistenceTest {

    @Test
    public void testParseEntityClassFromNamedQuery() {
        String queryName = "ALL_USERS";
//        return parseEntityClass(Db.getSession().getNamedQuery(queryName).getQueryString());
        assertEquals(User.class, HQLUtils.parseEntityClassFromNamedQuery(queryName));
    }
}
