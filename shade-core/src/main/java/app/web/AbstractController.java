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

import app.commons.ConfigurationUtils;
import app.commons.EmailUtils;
import app.core.Db;
import app.core.Employee;
import app.core.ID;
import app.core.PersistentObject;
import org.joda.time.Interval;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;

import java.beans.PropertyEditorSupport;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

/**
 *
 */
//@RequestMapping("{controller}/{action}")
public abstract class AbstractController extends MultiActionController {
    public static final DateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy");

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        //IMPORTANT
        binder.initDirectFieldAccess();
        binder.registerCustomEditor(PersistentObject.class, new PropertyEditorSupport() {
            @Override
            public void setAsText(String s) throws IllegalArgumentException {
                setValue(Db.get(ID.parse(s)));
            }

            @Override
            public String getAsText() {
                return getValue() != null ? ((PersistentObject) getValue()).getId().toString() : null;
            }
        });
        binder.registerCustomEditor(Date.class, new PropertyEditorSupport() {
            @Override
            public void setAsText(String s) throws IllegalArgumentException {
                try {
                    setValue(DATE_FORMAT.parse(s));
                } catch (ParseException e) {
                    throw new IllegalArgumentException(e);
                }
            }

            @Override
            public String getAsText() {
                return getValue() != null ? DATE_FORMAT.format(getValue()) : null;
            }
        });
    }

    @ExceptionHandler(Exception.class)
    public ModelMap handleAllExceptions(Exception e) {
        if (!ConfigurationUtils.getString("mode").startsWith("DEV")) {
            EmailUtils.send(e.getMessage(), Arrays.toString(e.getStackTrace()).replaceAll(", ", "\n"));
        }
        return new ModelMap("exception", e);
    }
}
