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
import com.thoughtworks.selenium.SeleniumException;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.util.StringUtils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Crawler-like Selenium test that recurses through the application starting from the homepage and visiting all pages available from there
 * and interacting with all the elements a user can interact with.
 */
public class CrawlerITCase extends SeleneseTestBase {

    public static final String APPLICATION_HOMEPAGE = "http://localhost:8080/index.html"; //FIXME: read from web.xml
    public static final String DEFAULT_TIMEOUT = "28000"; //FIXME: reduce to a more sensible value, default 30s
    private static final Logger log = Logger.getLogger(CrawlerITCase.class);

    @Before
    public void setUp() throws Exception {
        selenium = new DefaultSelenium("localhost", 4444, "*chrome", "http://localhost:8080/");
        selenium.start();
        selenium.setTimeout(DEFAULT_TIMEOUT);
//        selenium.setSpeed("2000");
    }

    @Test
    public void testApplication() {
        //goToHomepage();
        Page homepage = new SeleniumPage(selenium, APPLICATION_HOMEPAGE);
        testPageRecursively(homepage);
    }

    /**
     * We should not test external links though.
     * @param page the page to test
     */
    public void testPageRecursively(Page page) {
        log.info(showPadding(page.getLocationStack().size()) + "Testing page " + page);
        testHtmlSourceRecursively(page, "//body");
    }

    public void testHtmlSourceRecursively(Page page, final String xpathPrefix) {
        //clickAllJSLinks(); includes clickAllAjaxLinks();
        new ClickVerifyClick(page, "(" + xpathPrefix + ")//*[@onclick]").run();

        //clickAllNonJSLinks();
        new ClickVerifyClick(page, "(" + xpathPrefix + ")//a[@href and not(@onclick)] | "
                + "(" + xpathPrefix + ")//form//input[@type='submit' and not(@onclick)]", true).run();
    }

    public abstract class AsbtractClickCallback {
        Page currentPage;
        String xpath;
        boolean waitForLoad;
        // int i; improvement? getLocator()?

        protected AsbtractClickCallback(Page page, String xpath) {
            this(page, xpath, false);
        }

        protected AsbtractClickCallback(Page page, String xpath, boolean waitForLoad) {
            this.currentPage = page;
            this.xpath = xpath;
            this.waitForLoad = waitForLoad;
        }

        public abstract void executeAfterClick(SeleniumPage.Action action);

        public boolean isWaitForLoad() {
            return waitForLoad;
        }

        public void run(int padding) {
//            String currentLocation = selenium.getLocation();
            int count = currentPage.getXpathCount(xpath).intValue();
            for (int i = 1; i <= count; i++) {
                String locator = "xpath=(" + xpath + ")[" + i + "]";
                if (currentPage.isVisible(locator)) {
                    log.info(showPadding(padding) + "Clicking '" + getFailSafeText(currentPage, locator) + "'...");
                    currentPage = currentPage.clickAndWaitForLoadIfNecessary(locator, waitForLoad);
                    Page.Action action = currentPage.getActionStack().lastElement();
                    log.info(showPadding(padding) + "Result was " + action);
                    executeAfterClick(action);
                    currentPage = restorePreviousState(currentPage);
                } else {
                    log.info(showPadding(padding) + "There was an invisible element we just did not triggger " + locator);
                }
            }
        }
    }

    public Page restorePreviousState(Page page) {
        Stack<Page.Action> actionStack = page.getActionStack();
        page = new SeleniumPage(selenium, actionStack.firstElement().getValue()); //open
        if (actionStack.size() > 1) {
            page.getActionStack().pop();
            for (int i = 1; i < actionStack.size(); i++) {
                page.clickAndWaitForLoadIfNecessary(actionStack.get(i).getTarget(), false);
            }
        }
//        return new SeleniumPage(selenium, page.getActionStack());
        return page;
    }

    public class ClickVerifyClick extends AsbtractClickCallback {

        public ClickVerifyClick(Page page, String xpath) {
            super(page, xpath);
        }

        public ClickVerifyClick(Page page, String xpath, boolean waitForLoad) {
            super(page, xpath, waitForLoad);
        }

        @Override
        public void executeAfterClick(SeleniumPage.Action action) {
            verifyFalse(currentPage.getTitle().contains("Error"));
            URL currentLocation = toLocationPath(currentPage.getLocation());
            if (action.getType() == SeleniumPage.Action.ActionType.CLICK_DOM_UPDATE || action.getType() == SeleniumPage.Action.ActionType.CLICK_AJAX) {
                for (String partialLocator : action.getValues()) {
                    testHtmlSourceRecursively(currentPage, partialLocator);
                    currentPage = restorePreviousState(currentPage);
                }
            } else {
                if (!hasBeenThereBefore(currentPage, currentLocation)) {
//                    recursion starts here
                    testPageRecursively(currentPage);
                    // Restore location
//                    selenium.open(locationStack.pop().toString()); //page.goBack();
                    currentPage = restorePreviousState(currentPage);
                } else {
                    log.info("We are not going to visit the link because it would create an infinite loop.");
                }
            }
        }

        public void run() {
            super.run(currentPage.getLocationStack().size());
        }
    }

    /**
     * FIXME move to Page
     * @param page
     * @param location
     * @return
     */
    private static boolean hasBeenThereBefore(Page page, URL location) {
        Stack<URL> locationStack = page.getLocationStack();
        URL currentLocation = locationStack.pop();
        boolean bool = locationStack.contains(location);
        locationStack.push(currentLocation);
        return bool;
    }

    private static String showPadding(int padding) {
        StringBuilder strBld = new StringBuilder(padding > 1 ? "|-" : "");
        for (int i = 1; i < padding;i++) {
            strBld.append("-");
        }
        return strBld.toString();
    }

    /**
     * Like assertFalse, but fails at the end of the test (during tearDown)
     */
    public void verifyFalse(String message, boolean b) {
        try {
            assertFalse(message, b);
        } catch (Error e) {
            verificationErrors.append(throwableToString(e));
        }
    }

    private static String throwableToString(Throwable t) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        t.printStackTrace(pw);
        return sw.toString();
    }

    public static URL toLocationPath(String location) {
        try {
            URL fullUrl = new URL(location);
            // We just throw the query string if any
            return new URL(fullUrl.getProtocol(), fullUrl.getHost(), fullUrl.getPort(), fullUrl.getPath());
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }


    public static String getFailSafeText(Page page, String locator) {
        String text = page.getText(locator);
        if (text.length() > 0) {
            return text;
        } else {
            try {
                return page.getValue(locator);
            } catch (SeleniumException e) {
                return e.getMessage();
            }
        }
    }

    @After
    public void tearDown() throws Exception {
        selenium.stop();
        super.tearDown(); // do not remove as this would swallow all verification errors.
    }
}