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

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.AbstractTransactionalSpringContextTests;

import java.util.Arrays;

/**
 * Test that the spring configuration is correct.
 */
//@ContextConfiguration
public class ApplicationContextTest extends AbstractTransactionalSpringContextTests {

    public void testLoadOk() {
        ConfigurableApplicationContext appCtx = super.getApplicationContext();
        assertTrue(appCtx.isActive());
        assertTrue(Arrays.toString(appCtx.getBeanDefinitionNames()), appCtx.getBeanDefinitionCount() > 9);
        for (String beanDefinitionName : appCtx.getBeanDefinitionNames()) {
            // Load all beans to make sure no initialization fails
            assertNotNull(appCtx.getBean(beanDefinitionName));
        }

        assertEquals(appCtx, ((ApplicationContextProvider) appCtx.getBean("applicationContextProvider")).getCtx());
//        assertNotNull(((Dao) appCtx.getBean("dao")).getSessionFactory());
//        ((Db) appCtx.getBean("db")).getSessionFactory();
        assertNotNull(Db.getSession());
    }

    @Override
    protected String[] getConfigLocations() {
        return new String[] { "classpath:application-config.xml" };
//        return new String[] { "classpath:servlet-context.xml" };
    }
}
