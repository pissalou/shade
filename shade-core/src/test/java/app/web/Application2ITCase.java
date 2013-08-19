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
public class Application2ITCase extends SeleneseTestBase {

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
        Stack<Action> actionStack = new Stack<Action>();
        actionStack.push(Action.open(APPLICATION_HOMEPAGE));
        testPageRecursively(actionStack);
    }

    /**
     * We should not test external links though.
     * @param actionStack to find your way back
     */
    public void testPageRecursively(final Stack<Action> actionStack) {
        log.info(showPadding(actionStack.size()) + "Testing page " + selenium.getLocation());
        testHtmlSourceRecursively("//body", actionStack);
    }

    public void testHtmlSourceRecursively(final String xpathPrefix, final Stack<Action> actionStack) {
        //clickAllJSLinks(); includes clickAllAjaxLinks();
        new ClickVerifyClick("(" + xpathPrefix + ")//*[@onclick]", actionStack).run();

        //clickAllNonJSLinks();
        new ClickVerifyClick("(" + xpathPrefix + ")//a[@href and not(@onclick)] | "
                + " (" + xpathPrefix + ")//form//input[@type='submit' and not(@onclick)]", actionStack, true).run();
    }

    public static class Action {
        public enum ActionType { NO_CHANGE, OPEN, CLICK_NEW_PAGE, CLICK_SAME_PAGE, CLICK_AJAX, CLICK_DOM_UPDATE }
        protected ActionType type;
        protected String target; // where you click on
        protected String[] value = new String[0]; // url or locator(s)

//        public Action(ActionType type) {
//            this(type, null, null);
//        }

//        public Action(ActionType type, String target) {
//            this(type, target, null);
//        }

        private Action(ActionType type, String target, String... value) {
            this.type = type;
            this.value = value;
            this.target = target;
        }

        public ActionType getType() {
            return type;
        }

        public String getValue() {
            return value[0];
        }

        public String[] getValues() {
            return value;
        }

        public String getTarget() {
            return target;
        }

        public String toString() {
            switch (type) {
                case OPEN:
                    return new StringBuilder(type.toString()).append(" ").append(value[0]).toString();
                default:
                    return new StringBuilder(type.toString()).append(" ").append(value != null ? Arrays.toString(value) : null).toString();
            }
        }

        public static Action open(String url) {
            return new Action(ActionType.OPEN, null, url);
        }

        public static Action noChange(String target) {
            return new Action(ActionType.NO_CHANGE, target);
        }

        public static Action navigationSamePage(String target) {
            return new Action(ActionType.CLICK_SAME_PAGE, target);
        }

        public static Action navigationNewPage(String target, String value) {
            return new Action(ActionType.CLICK_NEW_PAGE, target, value);
        }

        public static Action domUpdate(String target, String... value) {
            return new Action(ActionType.CLICK_DOM_UPDATE, target, value);
        }

        public static Action ajaxUpdate(String target, String value) {
            return new Action(ActionType.CLICK_AJAX, target, value);
        }
    }

    public abstract class AbstractClickCallback {
        String xpath;
        boolean waitForLoad;
        // int i; improvement? getLocator()?

        protected AbstractClickCallback(String xpath) {
            this(xpath, false);
        }

        protected AbstractClickCallback(String xpath, boolean waitForLoad) {
            this.xpath = xpath;
            this.waitForLoad = waitForLoad;
        }

        public abstract void executeAfterClick(Action result);

        public boolean isWaitForLoad() {
            return waitForLoad;
        }

        public void run(int padding) {
            String currentLocation = selenium.getLocation(); // rename testStartLocation
            int count = selenium.getXpathCount(xpath).intValue();
            log.info("There are " + count + " links to visit. Let's go!");
            for (int i = 1; i <= count; i++) {
                String locator = "xpath=(" + xpath + ")[" + i + "]";
                if (selenium.isVisible(locator)) {
                    log.info(showPadding(padding) + "Clicking '" + getFailSafeText(locator) + "'...");
                    Action result = clickAndWaitIfNecessary(locator, waitForLoad);
                    log.info(showPadding(padding) + "Result was " + result);
                    executeAfterClick(result);
                } else {
                    log.info(showPadding(padding) + "There was an invisible element we just did not triggger " + locator);
                }
            }
        }
    }

    public class ClickVerifyClick extends AbstractClickCallback {
        Stack<Action> actionStack;

        public ClickVerifyClick(String xpath, Stack<Action> actionStack) {
            super(xpath);
            this.actionStack = actionStack;
        }

        public ClickVerifyClick(String xpath, Stack<Action> actionStack, boolean waitForLoad) {
            super(xpath, waitForLoad);
            this.actionStack = actionStack;
        }

        @Override
        public void executeAfterClick(Action result) {
            verifyFalse(selenium.getTitle().contains("Error"));
            URL currentLocation = toLocationPath(selenium.getLocation());
            if ((result.getType() == Action.ActionType.CLICK_DOM_UPDATE || result.getType() == Action.ActionType.CLICK_AJAX)
                    && result.getValues() != null) {
                actionStack.push(result);
                for (String partialLocator : result.getValues()) {
                    testHtmlSourceRecursively(partialLocator, actionStack);
                    //FIXME: refresh by undoing last action
                }
            } else {
                if (!toLocationStack(actionStack).contains(currentLocation)) {
                    actionStack.push(result);
                    // recursion starts here
                    testPageRecursively(actionStack);
                } else {
                    actionStack.push(result);
                    log.info("We have already visited this page: we are not going to test it again.");
                }
            }
            restoreToPreviousAction(actionStack);
        }

        public void run() {
            super.run(actionStack.size());
        }
    }

    public void restoreToPreviousAction(Stack<Action> actionStack) {
        if (actionStack.size() == 1) {
            return; // No previous state
        }
        log.info("Restore " + actionStack.get(0).getValue());
        selenium.open(actionStack.get(0).getValue());
        actionStack.pop(); // remove last action
        for (int i = 1; i < actionStack.size(); i++) {
            Action action = actionStack.get(i);
            if (action.getType() == Action.ActionType.OPEN) {
            } else {
                log.info("Restore click " + action.getTarget());
                clickAndWaitIfNecessary(action.getTarget(), action.getType() != Action.ActionType.CLICK_DOM_UPDATE);
            }
        }
    }

    public Stack<URL> toLocationStack(Stack<Action> actionStack) {
        Stack<URL> locationStack = new Stack<URL>();
        for (Action action : actionStack) {
            if (action.getType() == Action.ActionType.OPEN || action.getType()  == Action.ActionType.CLICK_NEW_PAGE) {
                locationStack.push(toLocationPath(action.getValue()));
            }
        }
        return locationStack;
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

    public Action clickAndWaitIfNecessary(String locator, boolean forceWaitForLoad) {
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
            return currentLocation.equals(previousLocation) ? Action.navigationSamePage(locator) : Action.navigationNewPage(locator, currentLocation);
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
                    return Action.domUpdate(locator, xpathList.size() > 0 ? xpathList.toArray(new String[xpathList.size()]) : null);
                } else {
                    String ajaxContentId = compareHtmlSourceAfterAjaxLoad(previousHtmlSource, currentHtmlSource);
//                    log.info("Test new ajax content under id " + ajaxContentId);
                    return Action.ajaxUpdate(locator, "//*[@id='" + ajaxContentId + "']");
                }
            } else {
                return Action.noChange(locator);
            }
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