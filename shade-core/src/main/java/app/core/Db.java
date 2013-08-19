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

import app.commons.ORMUtils;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.util.Assert;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import java.util.*;

/**
 * Central class that gives access to the database.
 */
public class Db {
    public static final String WHITESPACE = " ";
    public static final Object[] NO_PARAMS = new Object[0];
    static {
        // Create a sessionFactory if no Spring injection
//        sessionFactory = (SessionFactory) SingletonBeanFactoryLocator.getInstance("classpath:beanRefContext.xml").useBeanFactory("application").getFactory().getBean("sessionFactory");
//        sessionFactory = (SessionFactory) ApplicationContextProvider.getCtx().getBean("sessionFactory");
    }

    public static SessionFactory getSessionFactory() {
        return (SessionFactory) ApplicationContextProvider.getCtx().getBean("sessionFactory");
    }

    public static Session getSession() {
        SessionFactory sessionFactory = getSessionFactory();
        Assert.state(sessionFactory != null, "SessionFactory not injected");
        // Spring TransactionSynchronizationManager keeps the current session available for us via TransactionSynchronizationManager.getResource("sessionHolder").
//        Assert.isTrue(TransactionSynchronizationManager.isActualTransactionActive(), "No need to go any further if no transaction.");
        try {
            return sessionFactory.getCurrentSession();
        } catch (HibernateException e) {
            return sessionFactory.openSession();
        }
//        return (Session) em().getDelegate();
    }

    public static void flush() {
        // There must be a current session or else it is no point calling flush
        SessionFactory sessionFactory = getSessionFactory();
        sessionFactory.getCurrentSession().flush();
    }

    // TODO: rename
    public static Set<Class> getEntityClassList() {
        SortedSet<Class> classes = new TreeSet<Class>(new ORMUtils.DependencyComparator());
        SessionFactory sessionFactory = getSessionFactory();
        try {
            for (String className : sessionFactory.getAllClassMetadata().keySet()) {
                classes.add(Class.forName(className));
            }
        } catch (ClassNotFoundException e) {
            //log();
        }
        return classes;
    }

    public static String dump() {
        StringBuilder strBuf = new StringBuilder();
        for (Class clazz : Db.getEntityClassList()) {
            strBuf.append('\n').append(clazz.getSimpleName()).append('\n');
            strBuf.append(ORMUtils.toResultString(Db.getSession().createQuery("from" + WHITESPACE + clazz.getSimpleName()).list()));
        }
        return strBuf.toString();
    }

    public static <T extends PersistentObject> T get(Class<T> entityClass, Integer id) {
        return get(new ID<T>(entityClass, id));
    }

    public static <T extends PersistentObject> T get(ID<T> id) {
        return (T) getSession().get(id.getEntityClass(), id.getId());
    }

    public static <T extends PersistentObject> List<T> findAll(Class<T> entityClass) {
        return (List<T>) createQuery("from " + entityClass.getName()).list();
    }

    public static <T extends PersistentObject> List<T> find(Class<T> entityClass, int firstResultIndex, int maxResults) {
        return (List<T>) createQuery("from " + entityClass.getName()).setFirstResult(firstResultIndex).setMaxResults(maxResults).list();
    }

    /**
     * @deprecated use Criteria API instead
     */
    public static <T extends PersistentObject> List<T> find(String hql) {
        return (List<T>) find(hql, NO_PARAMS);
    }

    /**
     * @deprecated use Criteria API instead
     */
    public static <T extends PersistentObject> List<T> find(String queryName, int firstResultIndex, int maxResults) {
        return (List<T>) getSession().getNamedQuery(queryName).setFirstResult(firstResultIndex).setMaxResults(maxResults).list();
    }

    /**
     * @deprecated use Criteria API instead
     */
    public static <T extends PersistentObject> List<T> find(String hql, Object... params) {
        return (List<T>) createQuery(hql, params).list();
    }

    /**
     * @deprecated use Criteria API instead
     */
    public static <T extends PersistentObject> List<T> find(String hql, int firstResultIndex, int maxResults, Object... params) {
        return (List<T>) createQuery(hql, params).setFirstResult(firstResultIndex).setMaxResults(maxResults).list();
    }

    /**
     * @deprecated use Criteria API instead
     */
    public static <T extends PersistentObject> T findUnique(String hql, Object... params) {
        return (T) createQuery(hql, params).uniqueResult();
    }

    public static <T extends PersistentObject> void delete(Class<T> entityClass, Integer id) {
        get(new ID<T>(entityClass, id)).delete();
    }

    /**
     * @deprecated use Criteria API instead
     */
    protected static Query createQuery(String hql, Object... params) {
        Query query = getSession().createQuery(hql);
        int i = 0;
        for (Object param : params) {
            query.setParameter(i++, param);
        }
        return query;
    }
}
