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

import java.io.Serializable;

/**
 * The primary key.
 */
public class ID<T extends PersistentObject> implements Serializable {
    private Class<T> clazz;
    private Integer id;

    public ID(Class<T> clazz, Integer id) {
        this.clazz = clazz;
        this.id = id;
    }

    public final Class<T> getEntityClass() {
        return clazz;
    }

    public Integer getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ID id1 = (ID) o;
        return clazz.equals(id1.clazz) && id.equals(id1.id);
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public String toString(){
        return clazz.getSimpleName() + "#" + id;
    }

    public static ID parse(String id){
        try {
            Class clazz = Class.forName("app.core." + id.split("#")[0]); // TODO: use Hibernate stats
            return new ID(clazz, Integer.parseInt(id.split("#")[1]));
        } catch (Exception e) {
            return null;
        }
    }
}