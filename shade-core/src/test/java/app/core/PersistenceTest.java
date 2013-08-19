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

//import org.databene.feed4junit.Feeder;
import app.commons.ReflectionUtils;
import org.hibernate.validator.constraints.Length;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Set;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

/**
 * JUnit!
 */
//@RunWith(SpringJUnit4ClassRunner.class)
public class PersistenceTest extends AbstractPersistenceTest {
    @Autowired
    protected JdbcTemplate jdbc;
//    @Autowired
//    protected Dao dao;

    @Test
    public void testEquals() {
        //NAIVE: inject random, limit values here
        User user1 = createTestInstance(User.class);
        User user2 = new User(new Person("caca", "prout"), "caca", "prout");
        assertTrue(user1.equals(user1));
        assertTrue(user2.equals(user2));
        assertFalse(user2.equals(user1));
        assertFalse(user1.equals(user2));
    }

    @Test
    public void testHashCode() {
        User user1 = new User(new Person("caca", "boudin"), "caca", "boudin");
        User user2 = new User(new Person("caca", "prout"), "caca", "boudin");
        assertEquals(user1.hashCode(), user1.hashCode());
        assertEquals(user2.hashCode(), user2.hashCode());
        assertNotSame(user1.hashCode(), user2.hashCode());
        assertEquals(user1.hashCode(), new User(new Person("caca", "boudin"), "caca", "boudin").hashCode());
    }

    @Test
    @SuppressWarnings("Unchecked")
    public void testSaveUser() throws Exception {
        assertEquals(0, countRowsInTable("User"));
        new User("caca", "popo").save(); //persistentObject.save();
        assertEquals(1, countRowsInTable("User"));
    }

    @Test
    @SuppressWarnings("Unchecked")
    public void testSaveUser2() throws Exception {
        assertEquals(0, countRowsInTable("User"));
        new User("caca2", "popo2").save(); //persistentObject.save();
        assertEquals(1, countRowsInTable("User"));
    }

    @Test
    @SuppressWarnings("Unchecked")
    public void testSave() throws Exception {
        for (Class entityClass : Db.getEntityClassList()) {
            assertEquals("Table " + entityClass.getSimpleName() + " was not empty", 0, countRowsInTable(entityClass.getSimpleName()));
            PersistentObject persistentObject = newInstance(entityClass);
            assertEquals(newInstance(entityClass), persistentObject);
            assertEquals(newInstance(entityClass).hashCode(), persistentObject.hashCode());
            ID id = persistentObject.save();
            Db.get(entityClass, id.getId());
            assertEquals(id.getId().intValue(), Db.getSession().createQuery("select max(id) from " + entityClass.getSimpleName()).uniqueResult());
            assertNotSame(entityClass.newInstance(), persistentObject);
            assertNotSame(entityClass.newInstance().hashCode(), persistentObject.hashCode());
            assertNotSame(entityClass.newInstance(), Db.get(id));
            assertNotSame(entityClass.newInstance().hashCode(), Db.get(id).hashCode());
        }
    }

    public static <T extends PersistentObject> T newInstance(Class<T> clazz) throws Exception {
        return buildRecursively(clazz.newInstance());
    }

    private static <T extends PersistentObject> T buildRecursively(T newInstance) throws Exception {
        Set<ConstraintViolation<T>> constraintViolations = validate(newInstance);
        if (constraintViolations.isEmpty())
            return newInstance; // the instance is finished building
        for (ConstraintViolation constraintViolation : constraintViolations) {
            Object missingProperty;
            Field violatedField = ReflectionUtils.getField(constraintViolation.getRootBeanClass(), constraintViolation.getPropertyPath().toString());
            if (NotNull.class.isAssignableFrom(constraintViolation.getConstraintDescriptor().getAnnotation().getClass())) {
                if (PersistentObject.class.isAssignableFrom(violatedField.getType())) { // similar to instanceof
                    missingProperty = newInstance((Class<PersistentObject>) violatedField.getType()); //recursion here
                    ((PersistentObject) missingProperty).save(); // save the dependency
                } else {
                    missingProperty = violatedField.getType().newInstance(); // just create a non-null property
                }
                ReflectionUtils.setFieldValue(newInstance, violatedField.getName(), missingProperty);
            } else if (Length.class.isAssignableFrom(constraintViolation.getConstraintDescriptor().getAnnotation().getClass())) {
                // TODO: implement correctly
                ReflectionUtils.setFieldValue(newInstance, violatedField.getName(), "0123456789");
            }
            // TODO: support more annotations
        }
        return buildRecursively(newInstance);
    }

    public static <T extends PersistentObject> Set<ConstraintViolation<T>> validate(T object) {
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        return validator.validate(object);
    }

    @Deprecated
    public static <T extends PersistentObject> ID<T> nonFailingSave(T persistentObject) {
        try {
            return persistentObject.save();
        } catch (ConstraintViolationException e) {
            for (ConstraintViolation constraintViolation : e.getConstraintViolations()) {
                Field violatedField = ReflectionUtils.getField(constraintViolation.getRootBeanClass(), constraintViolation.getPropertyPath().toString());
                PersistentObject missingProperty = new Person(); //ReflectionUtils.instantiate(violatedField.getType()); // TODO
                missingProperty.save();
                ReflectionUtils.setFieldValue(persistentObject, "person", missingProperty);
            }
            // Do we validate first?
            // or
            // How do we clean up the constraint violations??
            return persistentObject.save(); //nonFailingSave(persistentObject);  //recursive
        }
    }

    @Test
    @SuppressWarnings("Unchecked")
    public void testDelete() throws Exception {
        for (Class entityClass : Db.getEntityClassList()) {
            assertEquals(0, countRowsInTable(entityClass.getSimpleName()));
            PersistentObject persistentObject = newInstance(entityClass);
            ID id = persistentObject.save();
            assertNotNull(Db.get(id));
            persistentObject.delete();
            assertNull(Db.get(id));
        }
    }

    /**
     * The original method does not work with in-memory HSQLDB.
     * @param tableName
     * @return
     */
    @Override
    public int countRowsInTable(String tableName) {
        return ((Long) Db.getSession().createQuery("select count(*) from " + tableName).uniqueResult()).intValue();
    }
}