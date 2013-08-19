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

import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;

/**
 * Subclasses should rollback their transaction.
 */
@ContextConfiguration("classpath:application-config.xml")
public abstract class AbstractPersistenceTest extends AbstractTransactionalJUnit4SpringContextTests {

    protected <T extends PersistentObject> T createTestInstance(Class<T> entityClass) {
        return AbstractDomainAwareTest.create(entityClass);
    }

    protected <T extends PersistentObject> T anyInstance(Class<T> entityClass) {
        return Db.find(entityClass, 0, 1).get(0);
    }
}
