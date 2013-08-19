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

package app.web.velocity;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import org.apache.velocity.util.introspection.*;

/**
 * Modified version of Shinobu's PublicFieldUberspect.java.
 * @author: pma
 */

/**
 * Uberspect implementation that exposes public fields.
 * Also exposes the explicit "length" field of arrays.
 *
 * <p>To use, tell Velocity to use this class for introspection
 * by adding the following to your velocity.properties:<br />
 *
 * <code>
 * runtime.introspector.uberspect = app.web.velocity.FieldUberspect
 * </code>
 * </p>
 *
 * @author <a href="mailto:shinobu@ieee.org">Shinobu Kawai</a>
 * @version $Id: $
 */
public class FieldUberspect extends UberspectImpl {

    /**
     * Default constructor.
     */
    public FieldUberspect()
    {
    }

    /**
     * Property getter - returns VelPropertyGet appropos for #set($foo = $bar.woogie).
     * <br />
     * Returns a special {@link VelPropertyGet} for the <code>length</code> property of arrays.
     * Otherwise tries the regular routine.  If a getter was not found,
     * returns a {@link VelPropertyGet} that gets from public fields.
     *
     * @param obj the object
     * @param identifier the name of the property
     * @param i a bunch of information.
     * @return a valid <code>VelPropertyGet</code>, if it was found.
     * @throws Exception failed to create a valid <code>VelPropertyGet</code>.
     */
    public VelPropertyGet getPropertyGet(Object obj, String identifier, Info i)
            throws Exception
    {
        Class clazz = obj.getClass();
        boolean isArray = clazz.isArray();
        boolean isLength = identifier.equals("length");
        if (isArray && isLength)
        {
            return new ArrayLengthGetter();
        }

        VelPropertyGet getter = super.getPropertyGet(obj, identifier, i);
        // there is no clean way to see if super succeeded
        // @see http://issues.apache.org/bugzilla/show_bug.cgi?id=31742
        try
        {
            getter.getMethodName();
            return getter;
        }
        catch (NullPointerException notFound)
        {
        }
        // FIXME: not only public fields
//        Field field = obj.getClass().getField(identifier);
        try {
            Field field = getField(obj.getClass(), identifier);
            if (field != null)
            {
                return new PublicFieldGetter(field);
            }
        } catch (NoSuchFieldException e) {}

        return super.getPropertyGet(obj, identifier, i);
    }

    public static Field getField(Class clazz, String fieldName) throws NoSuchFieldException{
        Class currentClass = clazz;
        while (currentClass != null) {
           try {
               Field field = currentClass.getDeclaredField(fieldName);
               field.setAccessible(true);
                return field;
               } catch (NoSuchFieldException e) {
                currentClass = currentClass.getSuperclass();
               }
           }
        throw new NoSuchFieldException(fieldName);
    }

    /**
     * Property setter - returns VelPropertySet appropos for #set($foo.bar = "geir").
     * <br />
     * First tries the regular routine.  If a setter was not found,
     * returns a {@link VelPropertySet} that sets to public fields.
     *
     * @param obj the object
     * @param identifier the name of the property
     * @param arg the value to set to the property
     * @param i a bunch of information.
     * @return a valid <code>VelPropertySet</code>, if it was found.
     * @throws Exception failed to create a valid <code>VelPropertySet</code>.
     */
    public VelPropertySet getPropertySet(Object obj, String identifier,
            Object arg, Info i) throws Exception
    {
        VelPropertySet setter = super.getPropertySet(obj, identifier, arg, i);
        if (setter != null)
        {
            return setter;
        }

        Field field = obj.getClass().getField(identifier);
        if (field != null)
        {
            return new PublicFieldSetter(field);
        }

        return null;
    }

    /**
     * Implementation of {@link VelPropertyGet} that gets from public fields.
     *
     * @author <a href="mailto:shinobu@ieee.org">Shinobu Kawai</a>
     * @version $Id: $
     */
    protected class PublicFieldGetter implements VelPropertyGet
    {
        /** The <code>Field</code> object representing the property. */
        private Field field = null;

        /**
         * Constructor.
         *
         * @param field The <code>Field</code> object representing the property.
         */
        public PublicFieldGetter(Field field)
        {
            this.field = field;
        }

        /**
         * Returns the value of the public field.
         *
         * @param o the object
         * @return the value
         * @throws Exception failed to get the value from the object
         */
        public Object invoke(Object o) throws Exception
        {
            return this.field.get(o);
        }

        /**
         * This class is cacheable, so it returns <code>true</code>.
         *
         * @return <code>true</code>.
         */
        public boolean isCacheable()
        {
            return true;
        }

        /**
         * Returns <code>"public field getter"</code>, since there is no method.
         *
         * @return <code>"public field getter"</code>
         */
        public String getMethodName()
        {
            return "public field getter";
        }
    }

    /**
     * Implementation of {@link VelPropertyGet} that gets length from arrays.
     *
     * @author <a href="mailto:shinobu@ieee.org">Shinobu Kawai</a>
     * @version $Id: $
     */
    protected class ArrayLengthGetter implements VelPropertyGet
    {
        /**
         * Constructor.
         */
        public ArrayLengthGetter()
        {
        }

        /**
         * Returns the length of the array.
         *
         * @param o the array
         * @return the length
         * @throws Exception failed to get the length from the array
         */
        public Object invoke(Object o) throws Exception
        {
            // Thanks to Eric Fixler for this refactor.
            return new Integer(Array.getLength(o));
        }

        /**
         * This class is cacheable, so it returns <code>true</code>.
         *
         * @return <code>true</code>.
         */
        public boolean isCacheable()
        {
            return true;
        }

        /**
         * Returns <code>"array length getter"</code>, since there is no method.
         *
         * @return <code>"array length getter"</code>
         */
        public String getMethodName()
        {
            return "array length getter";
        }
    }

    /**
     * Implementation of {@link VelPropertySet} that sets to public fields.
     *
     * @author <a href="mailto:shinobu@ieee.org">Shinobu Kawai</a>
     * @version $Id: $
     */
    protected class PublicFieldSetter implements VelPropertySet
    {
        /** The <code>Field</code> object representing the property. */
        private Field field = null;

        /**
         * Constructor.
         *
         * @param field The <code>Field</code> object representing the property.
         */
        public PublicFieldSetter(Field field)
        {
            this.field = field;
        }

        /**
         * Sets the value to the public field.
         *
         * @param o the object
         * @param value the value to set
         * @return always <code>null</code>
         * @throws Exception failed to set the value to the object
         */
        public Object invoke(Object o, Object value) throws Exception
        {
            this.field.set(o, value);
            return null;
        }

        /**
         * This class is cacheable, so it returns <code>true</code>.
         *
         * @return <code>true</code>.
         */
        public boolean isCacheable()
        {
            return true;
        }

        /**
         * Returns <code>"public field setter"</code>, since there is no method.
         *
         * @return <code>"public field setter"</code>
         */
        public String getMethodName()
        {
            return "public field setter";
        }
    }
}
