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

import static app.web.ApplicationITCase.ClickEventResult.*;
import static app.web.ApplicationITCase.ClickEventResult.EventType.*;
import com.thoughtworks.selenium.DefaultSelenium;
import com.thoughtworks.selenium.SeleneseTestBase;
import com.thoughtworks.selenium.Selenium;
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
public class ApplicationITCase extends SeleneseTestBase {

    public static final String APPLICATION_HOMEPAGE = "http://localhost:8080/index.html"; //FIXME: read from web.xml
    public static final String DEFAULT_TIMEOUT = "28000"; //FIXME: reduce to a more sensible value, default 30s
    private static final Logger log = Logger.getLogger(ApplicationITCase.class);

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
        selenium.open(APPLICATION_HOMEPAGE);
        Stack<URL> locationStack = new Stack<URL>();
        locationStack.push(toLocationPath(APPLICATION_HOMEPAGE));
        testPageRecursively(locationStack);
    }

    /**
     * We should not test external links though.
     * @param locationStack to find your way back
     */
    public void testPageRecursively(final Stack<URL> locationStack) {
        log.info(showPadding(locationStack.size()) + "Testing page " + selenium.getLocation());
        testHtmlSourceRecursively("//body", locationStack);
    }

    public void testHtmlSourceRecursively(final String xpathPrefix, final Stack<URL> locationStack) {
        //clickAllJSLinks(); includes clickAllAjaxLinks();
        new ClickVerifyClick("(" + xpathPrefix + ")//*[@onclick]", locationStack).run();

        //clickAllNonJSLinks();
        new ClickVerifyClick("(" + xpathPrefix + ")//a[@href and not(@onclick)] | "
                + " (" + xpathPrefix + ")//form//input[@type='submit' and not(@onclick)]", locationStack, true).run();
    }

    public abstract class AsbtractClickCallback {
        String xpath;
        boolean waitForLoad;
        // int i; improvement? getLocator()?

        protected AsbtractClickCallback(String xpath) {
            this(xpath, false);
        }

        protected AsbtractClickCallback(String xpath, boolean waitForLoad) {
            this.xpath = xpath;
            this.waitForLoad = waitForLoad;
        }

        public abstract void executeAfterClick(ClickEventResult result);

        public boolean isWaitForLoad() {
            return waitForLoad;
        }

        public void run(int padding) {
            String currentLocation = selenium.getLocation();
            int count = selenium.getXpathCount(xpath).intValue();
            for (int i = 1; i <= count; i++) {
                String locator = "xpath=(" + xpath + ")[" + i + "]";
                if (selenium.isVisible(locator)) {
                    log.info(showPadding(padding) + "Clicking '" + getFailSafeText(locator) + "'...");
                    ClickEventResult result = clickAndWaitIfNecessary(locator, waitForLoad);
                    log.info(showPadding(padding) + "Result was " + result);
                    executeAfterClick(result);
                    selenium.open(currentLocation); // Refresh page
                } else {
                    log.info(showPadding(padding) + "There was an invisible element we just did not triggger " + locator);
                }
            }
        }
    }

    public class ClickVerifyClick extends AsbtractClickCallback {
        Stack<URL> locationStack;

        public ClickVerifyClick(String xpath, Stack<URL> locationStack) {
            super(xpath);
            this.locationStack = locationStack;
        }

        public ClickVerifyClick(String xpath, Stack<URL> locationStack, boolean waitForLoad) {
            super(xpath, waitForLoad);
            this.locationStack = locationStack;
        }

        @Override
        public void executeAfterClick(ClickEventResult result) {
            verifyFalse(selenium.getTitle().contains("Error"));
            URL currentLocation = toLocationPath(selenium.getLocation());
            if (result.getEventType() == DOM_UPDATE || result.getEventType() == AJAX_UPDATE) {
                for (String partialLocator : result.getLocators()) {
                    testHtmlSourceRecursively(partialLocator, locationStack);
                    //refresh by undoing last action
                }
            } else {
                if (!locationStack.contains(currentLocation)) {
                    locationStack.push(currentLocation);
                    // recursion starts here
                    testPageRecursively(locationStack);
                    // Restore location
                    selenium.open(locationStack.pop().toString());
                } else {
                    log.info("We are not going to visit the link because it would create an infinite loop.");
                }
            }
        }

        public void run() {
            super.run(locationStack.size());
        }
    }

    private String showPadding(int padding) {
        StringBuilder strBld = new StringBuilder(padding > 1 ? "|-" : "");
        for (int i = 1; i < padding;i++) {
            strBld.append("-");
        }
        return strBld.toString();
    }

    public static int PAGE_LOAD_MAX_WAIT = 2000;
    private static boolean[] getVisibilityArray(Selenium selenium) {
        boolean[] visibilityArray = new boolean[selenium.getXpathCount("//*").intValue()];
        for (int i = 0; i < visibilityArray.length; i++) {
            // array is 0-zero, xpath is 1-based
            visibilityArray[i] = selenium.isVisible("xpath=(//*)[" + (i + 1) + "]");
        }
        return visibilityArray;
    }

    public List<Integer> compareVisibilityArray(boolean[] arr1, boolean[] arr2) {
        assertEquals(arr1.length, arr2.length);
        List<Integer> results = new ArrayList<Integer>();
        for (int i = 0; i < arr1.length; i++) {
            if (!arr1[i] && arr2[i]) {
                results.add(i);
                break; // Return only the first mismatch for now
            }
        }
        return results;
    }

    /**
     * Compares before and after ajax call.
     * @param previousHtmlSource html before ajax
     * @param currentHtmlSource html after ajax (supposedly containing extra html code)
     * @return the id of the parent which had ajax content appended to.
     */
    private static String compareHtmlSourceAfterAjaxLoad(String previousHtmlSource, String currentHtmlSource) {
        String[] previousSplit = previousHtmlSource.split("(?<=>)");
        String[] currentSplit = currentHtmlSource.split("(?<=>)");
        int i = 0;
        while (previousSplit.length > i && (currentSplit[i].equals(previousSplit[i]) || (i == 0 || extractIdFromTag(currentSplit[i-1]) == null))) {
            // continue incrementing if no mismatch in html code OR previous tag does not have an id attribute.
            i++;
        }
        return extractIdFromTag(currentSplit[i-1]);
    }

    public static final Pattern ID_ATTR_EXTRACTION_PATTERN = Pattern.compile("\\sid=\"(.*?)\"");
    private static String extractIdFromTag(String htmlTag) {
        Matcher m = ID_ATTR_EXTRACTION_PATTERN.matcher(htmlTag);
        String id = null;
        while (m.find()) {
             id = m.group(1);
        }
        return id;
    }

    public ClickEventResult clickAndWaitIfNecessary(String locator, boolean forceWaitForLoad) {
        String previousHtmlSource = selenium.getHtmlSource();
        String previousLocation = selenium.getLocation();
        boolean[] previousVisibilityArray = getVisibilityArray(selenium);
        selenium.click(locator);
        if (selenium.isConfirmationPresent()) {
            // consume confirmation or else next text will fail
            selenium.getConfirmation();
        } else if (selenium.isAlertPresent()) {
            // consume alert or else next text will fail
            selenium.getAlert();
        }
        boolean isNewPageLoaded = waitForLoadIfNecessary(forceWaitForLoad);
        if (isNewPageLoaded) {
            String currentLocation = selenium.getLocation();
            return currentLocation.equals(previousLocation) ? navigationSamePage() : navigationNewPage(currentLocation);
        } else {
            String currentHtmlSource = selenium.getHtmlSource();
            if (!currentHtmlSource.equals(previousHtmlSource)) {
                // Compare the number of tags
                if (StringUtils.countOccurrencesOf(currentHtmlSource, "<") == StringUtils.countOccurrencesOf(previousHtmlSource, "<")) {
                    List<String> xpathList = new ArrayList<String>();
                    for (int xpathIndex : compareVisibilityArray(previousVisibilityArray, getVisibilityArray(selenium))) {
                        xpathList.add("(//*)[" + (xpathIndex + 1) + "]"); //array 0-based, xpath 1-based
                    }
//                    if (!xpathList.isEmpty())
//                        log.info("Test newly visible content"); // '" + getFailSafeText(xpathList.get(0)) + "'");
                    return domUpdate(xpathList.size() > 0 ? xpathList.toArray(new String[xpathList.size()]) : null);
                } else {
                    String ajaxContentId = compareHtmlSourceAfterAjaxLoad(previousHtmlSource, currentHtmlSource);
//                    log.info("Test new ajax content under id " + ajaxContentId);
                    return ajaxUpdate("//*[@id='" + ajaxContentId +  "']");
                }
            } else {
                return noChange();
            }
        }
    }

    public static class ClickEventResult {
        public enum EventType {NO_CHANGE, DOM_UPDATE, AJAX_UPDATE, NAVIGATION_SAME_PAGE, NAVIGATION_NEW_PAGE}
        private EventType eventType;
        private String[] location; // or locators depending on the event type

        private ClickEventResult(EventType eventType, String... location) {
            this.eventType = eventType;
            this.location = location;
        }

        public EventType getEventType() {
            return eventType;
        }

        public String getLocation() {
            assertEquals(1, location.length);
            return location[0];
        }

        public String[] getLocators() {
            return location != null ? location : new String[0]; // fail-safe method
        }

        public static ClickEventResult noChange() {
            return new ClickEventResult(EventType.NO_CHANGE);
        }

        public static ClickEventResult domUpdate(String... location) {
            return new ClickEventResult(EventType.DOM_UPDATE, location);
        }

        public static ClickEventResult ajaxUpdate(String location) {
            return new ClickEventResult(EventType.DOM_UPDATE, location);
        }

        public static ClickEventResult navigationSamePage() {
            return new ClickEventResult(EventType.NAVIGATION_SAME_PAGE);
        }

        public static ClickEventResult navigationNewPage(String location) {
            return new ClickEventResult(EventType.NAVIGATION_NEW_PAGE, location);
        }

        public String toString() {
            return eventType + (location != null ? Arrays.toString(location) : "");
        }
    }


    public boolean waitForLoadIfNecessary(boolean forceWaitForLoad) {
        if (forceWaitForLoad) {
            selenium.waitForPageToLoad(Integer.toString(PAGE_LOAD_MAX_WAIT));
            return true;
        } else {
            long initTime = System.currentTimeMillis();
            while (System.currentTimeMillis() - initTime < PAGE_LOAD_MAX_WAIT) {
                try {
                    selenium.waitForPageToLoad("10");
                    return true;
                } catch (SeleniumException e) {
                    // We do not want to fail if there was no page reload.
                }
            }
            return false;
        }
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


    public String getFailSafeText(String locator) {
        String text = selenium.getText(locator);
        if (text.length() > 0) {
            return text;
        } else {
            try {
                return selenium.getValue(locator);
            } catch (SeleniumException e) {
                return e.getMessage();
            }
        }
    }

    @Deprecated
    private String getReadyState() {
        return selenium.getEval("selenium.browserbot.getCurrentWindow().document.readyState");
    }

    @Deprecated
    private boolean isAjaxActive() {
        return !selenium.getEval("selenium.browserbot.getCurrentWindow().jQuery.active").equals("0");
    }

    @After
    public void tearDown() throws Exception {
        selenium.stop();
        super.tearDown(); // do not remove as this would swallow all verification errors.
    }
}