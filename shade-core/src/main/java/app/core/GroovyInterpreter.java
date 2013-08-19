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

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import org.springframework.transaction.annotation.Transactional;

/**
 * Groovy!
 */
public class GroovyInterpreter {
    private static final String IMPORTS = "import app.core.*\n";

    @Transactional
    public static Object run(String script) {
        Binding binding = new Binding();
        GroovyShell shell = new GroovyShell(binding);
        Object result = shell.evaluate(IMPORTS + script);
        return result;
    }

    public static void main(String[] args) {
        Db.getSession().beginTransaction();
        User user = (User) run("user = new User('Groovy', 'Rocks'); user2 = new User('Richfaces', 'Sucks'); user.persist(); user2.persist(); return user");
        Db.getSession().flush();
        System.out.println(Db.dump());
    }
}
