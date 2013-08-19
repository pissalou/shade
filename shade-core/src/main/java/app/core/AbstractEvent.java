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

import org.joda.time.Interval;

import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;

/**
 * @author pma
 */
@MappedSuperclass
public abstract class AbstractEvent extends PersistentObject implements Comparable {
    @ManyToOne
    private Employee employee;
    private Interval interval;

    protected AbstractEvent(Employee employee, Interval interval) {
        this.employee = employee;
        this.interval = interval;
    }

    public Employee getEmployee() {
        return employee;
    }

    public Interval getInterval() {
        return interval;
    }

    public abstract int getPriority();

    @Override
    public int compareTo(Object o) {
        if (o instanceof AbstractEvent)
            return new EventComparator().compare(this, (AbstractEvent) o);
        return this.getPriority();
    }
}
