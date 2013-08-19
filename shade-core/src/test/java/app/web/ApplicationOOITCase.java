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

import com.thoughtworks.selenium.DefaultSelenium;
import com.thoughtworks.selenium.SeleneseTestBase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.internal.seleniumemulation.IsTextPresent;
import org.openqa.selenium.internal.seleniumemulation.WaitForPageToLoad;

import java.util.logging.Logger;

/**
 * Test using a better API than the one provided by Selenium.
 */
public class ApplicationOOITCase extends SeleneseTestBase {

    public static final String APPLICATION_HOMEPAGE = "http://localhost:8080/index.html"; //FIXME: read from web.xml
    public static final Integer TIMEOUT = 28000; //FIXME: reduce to a more sensible value, default 30s
    private static final Logger log = Logger.getLogger(ApplicationITCase.class.getName());

    @Before
    public void setUp() throws Exception {
        selenium = new DefaultSelenium("localhost", 4444, "*chrome", "http://localhost:8080/");
        selenium.start();
        selenium.setTimeout(TIMEOUT.toString());
//        selenium.setSpeed("2000");

//        Page interpreter = homepage.clickLinkAndWait("Groovy Interpreter");
//        interpreter.submitForm();
//        homepage = new SeleniumPage(selenium, APPLICATION_HOMEPAGE);
    }

    @Test
    public void testCreateKO() {
        Page userNew = new SeleniumPage(selenium, "http://localhost:8080/user/create"); // do better, relative urls
        userNew.submitForm();
        verifyTrue(userNew.isCurrentPage());
        verifyTrue(validationErrorPresent());
    }

    @Test
    public void testCreateOK() {
        Page homepage = new SeleniumPage(selenium, APPLICATION_HOMEPAGE);
        Page userOverview = homepage.clickLinkAndWait("User overview");
        int userCount = userOverview.getXpathCount("//table/tbody/tr").intValue();
        homepage = userOverview.clickLinkAndWait("Return to homepage");
        Page userNew = homepage.clickLinkAndWait("User new");
        userNew.selectAny().input("caca").input("prout").submitForm();
        verifyTrue(userOverview.isCurrentPage());
        verifyEquals(userCount + 1, userOverview.getXpathCount("//table/tbody/tr").intValue());
    }

    private boolean validationErrorPresent() {
        return selenium.isTextPresent("must be");
    }

    @After
    public void tearDown() throws Exception {
        selenium.stop();
        super.tearDown(); // do not remove as this would swallow all verification errors.
    }
}