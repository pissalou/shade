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
import org.hibernate.Session;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import java.util.List;

/**
 * Every domain object should inherit this class.
 */
@MappedSuperclass
public abstract class PersistentObject {
    @Id
    @GeneratedValue
    protected Integer id; // TODO: rename pk for primaryKey

    protected PersistentObject() {}

    public ID getId() {
        return id != null ? new ID(this.getClass(), id) : null;
    }

    @Override
    public int hashCode() {
        return ORMUtils.hashCode(this);
    }

    @Override
    public boolean equals(Object obj) {
        return ORMUtils.equals(this, obj);
    }

    /**
     * Save vs persist. Persist does not return id, it is a pain in the a**.
     * @return id
     */
    public ID save() {
        return new ID(this.getClass(), (Integer) Db.getSession().save(this));
    }

    public void update() {
        Db.getSession().update(this);
    }

    public void merge() {
        Db.getSession().merge(this);
    }

    public void refresh() {
        Db.getSession().refresh(this);
    }

    public void delete() {
        Session session = Db.getSession();
        session.delete(this);
    }

    public boolean isPersistent() {
        return id != null;
    }

    public String toString() {
        // TODO: toString based on NaturalId?
        return id != null ? getId().toString() : super.toString();
    }

    public String getNonAmbigiousString() {
        return ORMUtils.toNonAmbigiousString(this);
    }
}
