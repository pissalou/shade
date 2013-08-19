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

package app.web;

import app.core.Db;
import app.core.ID;
import app.core.PersistentObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.validation.Valid;
import java.lang.reflect.ParameterizedType;

/**
 * @author Pma
 */
public abstract class AbstractCrudController<T extends PersistentObject> extends AbstractController {
    @Autowired
    PlatformTransactionManager transactionManager;

    @RequestMapping("/*")
    public ModelAndView index() {
        Class<T> clazz = getTypeClass(); // Person.class or something else
        return new ModelAndView(clazz.getSimpleName().toLowerCase() + "/index", "items", Db.<T>findAll(clazz));
    }

    @RequestMapping(value = {"/create", "/new"}, method = RequestMethod.GET)
    public ModelAndView getForm() {
        Class<T> clazz = getTypeClass();
        // TODO: find out why the model path must be the classname
        String simpleClassName = clazz.getSimpleName().toLowerCase();
        return new ModelAndView(simpleClassName + "/form", simpleClassName, getNewInstance(clazz));
    }

    @RequestMapping(value = "/edit", method = RequestMethod.GET)
    public ModelAndView getForm(@RequestParam int id) {
        Class<T> clazz = getTypeClass();
        T instance = Db.get(clazz, id);
        if (instance == null)
            throw new IllegalArgumentException("No entity found in db for " + new ID<T>(clazz, id));
        // TODO: find out why the model path must be the classname
        String simpleClassName = clazz.getSimpleName().toLowerCase();
        return new ModelAndView(simpleClassName + "/form", simpleClassName, instance);
    }

    @RequestMapping(value = {"/create", "/edit"}, method = RequestMethod.POST)
    public String sendForm(@Valid final T form, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            Class<T> clazz = getTypeClass();
            return clazz.getSimpleName().toLowerCase() + "/form"; // stay on the same view so we can display the validation errors
        }
        if (form.isPersistent()) {
            new TransactionTemplate(transactionManager).execute(new TransactionCallbackWithoutResult() {
                protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
                    form.update();
                }
            });
        } else {
            form.save(); // no need for a transaction?
        }
        // FIXME: find out why the item must be called "form"
        return "redirect:.";
    }

    @RequestMapping(value = "/delete") // TODO: , method = RequestMethod.POST)
    public String delete(@RequestParam final int id) {
        new TransactionTemplate(transactionManager).execute(new TransactionCallbackWithoutResult() {
            protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
                Db.delete(getTypeClass(), id);
            }
        });
        return "redirect:.";
    }

    protected Class<T> getTypeClass() {
        return (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0]; // Person.class
    }

    protected T getNewInstance(Class<T> clazz) {
        try {
            return clazz.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}