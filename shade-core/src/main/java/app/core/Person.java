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

import org.hibernate.annotations.NaturalId;

import javax.persistence.Entity;
//import javax.validation.constraints.NotNull;
import java.util.Date;

@Entity
public class Person extends PersistentObject {
//    @NotNull
    @NaturalId
    protected String firstName;
//    @NotNull
    @NaturalId
    public String lastName;
    protected String nickName;
//    @NotNull
    @NaturalId
    protected Date dateOfBirth;
//    @Embedded
//    protected Address address;

    public Person() { }

    public Person(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
    }

    // FIXME: So dirty, use field injection.
//    public void setFirstName(String firstName) {
//        this.firstName = firstName;
//    }

    // FIXME: So dirty, use field injection.
//    public void setLastName(String lastName) {
//        this.lastName = lastName;
//    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public Date getDateOfBirth() {
        return dateOfBirth;
    }

    //Package-access
//    void setDateOfBirth(Date dateOfBirth) {
//        this.dateOfBirth = dateOfBirth;
//    }

    public User getUser() {
        return Db.findUnique("from User where person = ?", this);
    }
}
