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

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Static reference to Spring ApplicationContext so we can access Spring beans from anywhere.
 */
@Component
@Scope("singleton")
public class ApplicationContextProvider implements ApplicationContextAware {
    private static ApplicationContext ctx;

    /**
     * This one is for Spring.
     * @param ctx
     * @throws BeansException
     */
    public void setApplicationContext(ApplicationContext ctx) throws BeansException {
        setCtx(ctx);
    }

    /**
     * This one is for calling programmatically.
     * @param ctx
     * @throws BeansException
     */
    public static void setCtx(ApplicationContext ctx) throws BeansException {
        ApplicationContextProvider.ctx = ctx;
    }

    public static ApplicationContext getCtx() {
        return ctx;
    }
}