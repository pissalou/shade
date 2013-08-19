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
import org.hibernate.validator.constraints.Length;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

/**
 * This is all we should have to specify. The rest SHOULD BE auto-generated.
 */
@NamedQueries(
    @NamedQuery(name= "ALL_USERS", query = "from User this join fetch this.person")
)
@MappedSuperclass
public class UserBase extends PersistentObject {
    @NotNull @Length(min=3, max=16) @NaturalId
    protected String username;
    @NotNull @Length(min=5)
    protected String password;
    @NotNull @ManyToOne(cascade = CascadeType.ALL)
    protected Person person;

    protected UserBase() {}

    public UserBase(String firstName, String lastName) {
        this(new Person(firstName, lastName), toUsername(firstName, lastName), "secret");
    }

    public UserBase(Person person, String username, String password) {
        this.person = person;
        this.username = username;
        this.password = password;
    }

    protected static String toUsername(String firstName, String lastName) {
        try {
            return firstName.substring(0, 1) + lastName.substring(0, 2);
        } catch (Exception e) {
            return null;
        }
    }
}
