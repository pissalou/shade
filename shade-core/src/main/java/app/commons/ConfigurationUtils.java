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

package app.commons;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Use Spring injection wherever it is available.
 */
public class ConfigurationUtils {
    private static Properties applicationProperties = new Properties();

    static {
        try {
            //InputStream is = new FileInputStream("/application.properties");
            InputStream is = ConfigurationUtils.class.getResourceAsStream("/application.properties");
            applicationProperties.load(is);
            is.close();
        } catch (IOException e) {
            System.exit(1);
        }
    }

    public static Object getObject(String key) {
        return applicationProperties.get(key);
    }

    public static String getString(String key) {
        return (String) getObject(key);
    }
}
