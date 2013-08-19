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

import javax.persistence.*;
import java.util.List;

/**
 * This entity-class CAN BE auto-generated.
 */
@Entity
public class User extends UserBase {

//    private static transient UserFinder userFinder;

    public User() { }

    public User(String firstName, String lastName) {
        super(firstName, lastName);
    }

    public User(Person person, String username, String password) {
        super(person, username, password);
    }

    public String getPassword() {
        return password;
    }

    public String getUsername() {
        return username;
    }

    public Person getPerson() {
        return person;
    }

    /* Not that good to have public setters here. All because of Spring MVC. */
    /* See if we can provide a better BeanWrapper that can inject the field directly bypassing the setter */
    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

//    public static UserFinder repository() {
//        // lazy-loading
//        if (userFinder == null)
//            userFinder = new UserFinder();
//        return userFinder;
//    }

    public static List<User> find(String hql, Object... params) {
//        return repository().find(hql, params);
        return Db.find(hql, params);
    }

    public static User get(ID<User> id) {
//        return repository().get(id);
        return Db.get(id);
    }

    public static List<User> findAll() {
        // Not that good, find a better solution. How does hibernateTemplate do this?
//        return find("from User");
        return Db.getSession().getNamedQuery("ALL_USERS").list();
    }
}