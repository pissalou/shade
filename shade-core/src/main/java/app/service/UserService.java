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

package app.service;

import app.commons.ORMUtils;
import app.core.*;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 */
@Service
public class UserService extends AbstractCRUDService<User> { // FIXME: extends AbstractCRUDService<User> does not work with @Transactional!!

    public void doStuff() {
        Session session = Db.getSession();
        session.beginTransaction();

        User user = new User(new Person("Pascal", "Mazars"), "pma", "cacaprout");
        System.out.println("Hello " + user);
        ID<User> id = user.save();
        System.out.println("Persisted with id " + id);
        System.out.println("Persisted with id " + new User(new Person("Daddy", "Longleg"), "dlg", "cacaprout").save());

//        User foundUser = UserFinder.getInstance().findUnique("from app.core.User where person.firstName = 'Pascal'");
//        System.out.println("Found unique with firstName = 'Pascal': " + foundUser.toString());

//        User foundUser2 = new IDFinder<User>().get(id);
//        System.out.println("Found with id " + id + ": " + foundUser2.toString());

        //foundUser2.getPerson().setDateOfBirth(new Date());

//        User foundUser3 = foundUser2.getPerson().getUser();
//        System.out.println("Found with getPerson().getUser(): " + foundUser3.toString());

        List<User> userList =  User.findAll(); //find("from app.core.User");
        System.out.println("List<app.core.User>: \n" + ORMUtils.toResultString(userList));

        System.out.println("Db dump: " + Db.dump());

        session.flush();
        throw ApplicationError.somethingWentTerriblyWrong();
    }

    public User create(String firstName, String lastName, String username, String password) {
        Person person = new Person(firstName, lastName);
        person.save();

        User user = new User(new Person(firstName, lastName), username, password);
        user.save();
        return user;
    }
}