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

import org.hibernate.Session;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.ExpectedException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.junit.Assert;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;

/**
 * @author pma
 **/
@ContextConfiguration("classpath:application-config.xml")
public class ConcurrencyTest extends AbstractTransactionalJUnit4SpringContextTests {
//    @Autowired
//    protected Dao dao;

    @Test
    public void testGetSessionFactorySameInstanceAsInjectedSessionFactory() {
//        Assert.assertEquals(Db.getSessionFactory(), dao.getSessionFactory());
//        Assert.assertEquals(dao.sessionFactory, Db.getSessionFactory());
    }

    @Test
    public void testGetSessionSameInstanceAsInjectedSession() {
//        dao.save(new User("caca", "prout"));
//        Assert.assertEquals(dao.getSessionFactory().getCurrentSession(), Db.getSession());
    }

    /**
     * Only true if the test is transactional.
     */
    @Test
    public void testGetSessionReturnsSameInstanceIfSameThread() {
        Session session = Db.getSession();
        Assert.assertEquals(session, Db.getSession());
    }

    static Session session;
    static Session session2;

    @Test
    public void testGetSessionConcurrentThreadsGetDifferentSessions() {
        new Thread() {
            @Override
            public void run() {
                session = Db.getSession();
            }
        }.start();
        session2 = Db.getSession();
        Assert.assertFalse(session2.equals(session));
    }

}
