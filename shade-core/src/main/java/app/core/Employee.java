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

import org.joda.time.DateMidnight;
import org.joda.time.Interval;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

/**
 * @author pma
 */
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorValue("E")
public class Employee extends Person {
    protected int employeeNo;
    protected Interval employmentInterval;

    protected Employee() { }

    public Employee(String firstName, String lastName) {
        super(firstName, lastName);
        this.employeeNo = 0;
        this.employmentInterval = new Interval(new DateMidnight(), new DateMidnight(2099, 1, 1));
    }

    public int getEmployeeNo() {
        return employeeNo;
    }

    public Interval getEmploymentInterval() {
        return employmentInterval;
    }
}
