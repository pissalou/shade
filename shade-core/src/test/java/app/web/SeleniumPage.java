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

import com.thoughtworks.selenium.Selenium;
import com.thoughtworks.selenium.SeleniumException;
import org.openqa.selenium.internal.seleniumemulation.Open;
import org.springframework.util.StringUtils;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Object-oriented flavoured testing.
 */
public class SeleniumPage implements Page {
    protected Selenium selenium;
//    protected Stack<URL> locationStack;
    protected Stack<Action> actionStack;
    protected static Page currentPage;
    protected int inputCounter; // Maybe better if we could have tabIndex instead
    protected int selectCounter;// Maybe better if we could have tabIndex instead
    public static final Integer TIMEOUT = 300000;
    public static int PAGE_LOAD_MAX_WAIT = 2000;

    public SeleniumPage(Selenium selenium, final String url) {
        this(selenium, url, new Stack<Action>());
        this.selenium.open(url);
        currentPage = this;
    }

//    public static SeleniumPage newSeleniumPage(SeleniumPage page, Action action) {
//        page.getActionStack().push(action);
//        return new SeleniumPage(page.selenium, page.getActionStack());
//    }

//    public SeleniumPage(Selenium selenium, Stack<Action> actionStack) {
//        this(selenium, actionStack.firstElement().getValue()); //open
//        for (int i = 1; i < actionStack.size(); i++) {
//            this.clickAndWaitForLoadIfNecessary(actionStack.get(i).getTarget(), false);
//        }
//    }

    /**
     * This method is used internally only.
     * @param selenium seleniumBrowser
     * @param url the new current URL
     * @param actionStack the current action stack (history of past actions)
     */
    private SeleniumPage(Selenium selenium, final String url, Stack<Action> actionStack) {
        this.selenium = selenium;
        this.actionStack = new Stack<Action>();
        if (actionStack.isEmpty()) {
            this.actionStack.push(Action.open(url));
        } else {
            this.actionStack.addAll(actionStack);
//            this.actionStack.push(Action.navigationNewPage("#target", url));
        }
        currentPage = this;
    }

    public static Page getCurrentPage() {
       return currentPage;
    }

    public boolean isCurrentPage() {
        // Perhaps we need to implement equals/hashCode
        return this.equals(currentPage);
    }

    public void assertIsCurrentPage() {
        if (!isCurrentPage())
            throw new IllegalStateException(this + " is not the current page");
    }

//    /**
//     * Merge with click...?
//     * @param action
//     * @return
//     */
//    @Override
//    public Page registerAction(Action action) {
////        Action previousAction = actionStack.pop();
////        if (previousAction.getType() == Action.ActionType.CLICK_DOM_UPDATE) {
////            throw new IllegalStateException("previousAction.getType()=" + previousAction.getType());
////        }
//        actionStack.push(action);
////        if (action.getType() == Action.ActionType.CLICK_NEW_PAGE || action.getType() == Action.ActionType.CLICK_SAME_PAGE) {
////            return new SeleniumPage(selenium, actionStack);
////        }
//        return this;
//    }

    @Override
    public Stack<Action> getActionStack() {
        return actionStack;
    }

    @Override
    public Stack<URL> getLocationStack() {
        Stack<URL> locationStack = new Stack<URL>();
        for (Action action : actionStack) {
            if (action.getType() == Action.ActionType.OPEN || action.getType()  == Action.ActionType.CLICK_NEW_PAGE) {
                locationStack.push(toLocationPath(action.getValue()));
            }
        }
        return locationStack;
    }

    @Override
    public SeleniumPage click(String locator) {
        assertIsCurrentPage();
        assertUniqueElement(locator);
        selenium.click(locator);
//        if (actionStack == null)
//            actionStack = new Stack<Action>();
//        actionStack.push(Action.domUpdate(locator));
        return this;
    }

    @Override
    public Page clickAndWait(String locator) {
        return clickAndWaitForLoadIfNecessary(locator, true);
    }

//    public Page clickAndWaitForLoadIfNecessary(String locator, boolean forceWaitForLoad) {
//        click(locator);
//        return waitForLoadIfNecessary(forceWaitForLoad);
//    }

    private boolean waitForLoadIfNecessary(boolean forceWaitForLoad) {
        if (forceWaitForLoad) {
            waitForPageToLoad(Integer.toString(PAGE_LOAD_MAX_WAIT));
//            actionStack.pop(); // pop domUpdate
            return true; //newSeleniumPage(this, null);
        } else {
            long initTime = System.currentTimeMillis();
            while (System.currentTimeMillis() - initTime < PAGE_LOAD_MAX_WAIT) {
                try {
                    waitForPageToLoad("10");
//                    actionStack.pop(); // pop domUpdate
                    return true; //newSeleniumPage(this, null);
                } catch (SeleniumException e) {
                    // We do not want to fail if there was no page reload.
                }
            }
            return false; //this;
        }
    }

    public Page clickAndWaitForLoadIfNecessary(String locator, boolean forceWaitForLoad) {
        String previousHtmlSource = this.getHtmlSource();
        String previousLocation = this.getLocation();
        boolean[] previousVisibilityArray = getVisibilityArray();
        this.click(locator);
        boolean isNewPageLoaded = this.waitForLoadIfNecessary(forceWaitForLoad);
//        this.getActionStack().lastElement().getType() == Page.Action.ActionType.CLICK_NEW_PAGE || this.getActionStack().lastElement().getType() == Page.Action.ActionType.CLICK_SAME_PAGE;
        Page.Action action;
        if (isNewPageLoaded) {
            String currentLocation = this.getLocation();
            action = currentLocation.equals(previousLocation) ? Page.Action.navigationSamePage(locator) : Page.Action.navigationNewPage(locator, currentLocation);
        } else {
            String currentHtmlSource = this.getHtmlSource();
            if (!currentHtmlSource.equals(previousHtmlSource)) {
                // Compare the number of tags
                if (StringUtils.countOccurrencesOf(currentHtmlSource, "<") == StringUtils.countOccurrencesOf(previousHtmlSource, "<")) {
                    List<String> xpathList = new ArrayList<String>();
                    for (int xpathIndex : compareVisibilityArray(previousVisibilityArray, this.getVisibilityArray())) {
                        xpathList.add("(//*)[" + (xpathIndex + 1) + "]"); //array 0-based, xpath 1-based
                    }
//                    if (!xpathList.isEmpty())
//                        log.info("Test newly visible content"); // '" + getFailSafeText(xpathList.get(0)) + "'");
                    action = Page.Action.domUpdate(locator, xpathList.size() > 0 ? xpathList.toArray(new String[xpathList.size()]) : null);
                } else {
                    String ajaxContentId = compareHtmlSourceAfterAjaxLoad(previousHtmlSource, currentHtmlSource);
//                    log.info("Test new ajax content under id " + ajaxContentId);
                    action = Page.Action.ajaxUpdate(locator, "//*[@id='" + ajaxContentId + "']");
                }
            } else {
                action = Page.Action.noChange(locator);
            }
        }
        this.getActionStack().push(action);
        return this;
    }

    private boolean[] getVisibilityArray() {
        boolean[] visibilityArray = new boolean[this.getXpathCount("//*").intValue()];
        for (int i = 0; i < visibilityArray.length; i++) {
            // array is 0-zero, xpath is 1-based
            visibilityArray[i] = this.isVisible("xpath=(//*)[" + (i + 1) + "]");
        }
        return visibilityArray;
    }

    public static List<Integer> compareVisibilityArray(boolean[] arr1, boolean[] arr2) {
//        assertEquals(arr1.length, arr2.length);
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

    @Override
    public Page clickLinkAndWait(String linkText) {
        String xpath = "//a[text()='" + linkText + "']";
        return clickAndWait(xpath);
    }

    private void assertUniqueElement(String xpath) {
        int xpathCount = getXpathCount(xpath).intValue();
        if (xpathCount != 1)
            throw new IllegalArgumentException(xpathCount > 1 ? xpath + " locator does not match an unique element. Found " + xpathCount
                    : xpath + " not found on " + this);
    }

    /* Delegating to selenium */

    @Override
    public void setExtensionJs(String extensionJs) {
        selenium.setExtensionJs(extensionJs);
    }

//    @Override
//    public void start() {
//        selenium.start();
//    }

//    @Override
//    public void start(String optionsString) {
//        selenium.start();
//    }
//
//    @Override
//    public void start(Object optionsObject) {
//        selenium.start();
//    }
//
//    @Override
//    public void stop() {
//        selenium.stop();
//    }

    @Override
    public void showContextualBanner() {
        selenium.showContextualBanner();
    }

    @Override
    public void showContextualBanner(String className, String methodName) {
        selenium.showContextualBanner(className, methodName);
    }

    @Override
    public void doubleClick(String locator) {
        selenium.doubleClick(locator);
    }

    @Override
    public void contextMenu(String locator) {
        selenium.contextMenu(locator);
    }

    @Override
    public void clickAt(String locator, String coordString) {
        selenium.clickAt(locator, coordString);
    }

    @Override
    public void doubleClickAt(String locator, String coordString) {
        selenium.doubleClickAt(locator, coordString);
    }

    @Override
    public void contextMenuAt(String locator, String coordString) {
        selenium.contextMenuAt(locator, coordString);
    }

    @Override
    public void fireEvent(String locator, String eventName) {
        selenium.fireEvent(locator, eventName);
    }

    @Override
    public void focus(String locator) {
        selenium.focus(locator);
    }

    @Override
    public void keyPress(String locator, String keySequence) {
        selenium.keyPress(locator, keySequence);
    }

    @Override
    public void shiftKeyDown() {
        selenium.shiftKeyDown();
    }

    @Override
    public void shiftKeyUp() {
        selenium.shiftKeyUp();
    }

    @Override
    public void metaKeyDown() {
        selenium.metaKeyDown();
    }

    @Override
    public void metaKeyUp() {
        selenium.metaKeyUp();
    }

    @Override
    public void altKeyDown() {
        selenium.altKeyDown();
    }

    @Override
    public void altKeyUp() {
        selenium.altKeyUp();
    }

    @Override
    public void controlKeyDown() {
        selenium.controlKeyDown();
    }

    @Override
    public void controlKeyUp() {
        selenium.controlKeyUp();
    }

    @Override
    public void keyDown(String locator, String keySequence) {
        selenium.keyDown(locator, keySequence);
    }

    @Override
    public void keyUp(String locator, String keySequence) {
        selenium.keyUp(locator, keySequence);
    }

    @Override
    public void mouseOver(String locator) {
        selenium.mouseOver(locator);
    }

    @Override
    public void mouseOut(String locator) {
        selenium.mouseOut(locator);
    }

    @Override
    public void mouseDown(String locator) {
        selenium.mouseDown(locator);
    }

    @Override
    public void mouseDownRight(String locator) {
        selenium.mouseDownRight(locator);
    }

    @Override
    public void mouseDownAt(String locator, String coordString) {
        selenium.mouseDownAt(locator, coordString);
    }

    @Override
    public void mouseDownRightAt(String locator, String coordString) {
        selenium.mouseDownRightAt(locator, coordString);
    }

    @Override
    public void mouseUp(String locator) {
        selenium.mouseUp(locator);
    }

    @Override
    public void mouseUpRight(String locator) {
        selenium.mouseUpRight(locator);
    }

    @Override
    public void mouseUpAt(String locator, String coordString) {
        selenium.mouseUpAt(locator, coordString);
    }

    @Override
    public void mouseUpRightAt(String locator, String coordString) {
        selenium.mouseUpRightAt(locator, coordString);
    }

    @Override
    public void mouseMove(String locator) {
        selenium.mouseMove(locator);
    }

    @Override
    public void mouseMoveAt(String locator, String coordString) {
        selenium.mouseMoveAt(locator, coordString);
    }

    @Override
    public Page input(String value) {
        return input(++inputCounter, value);
    }

    @Override
    public Page input(int inputIndex, String value) {
        return type("//input[" +  inputIndex + "]", value);
    }

    @Override
    public Page type(String locator, String value) {
        selenium.type(locator, value);
        return this;
    }

    @Override
    public void typeKeys(String locator, String value) {
        selenium.typeKeys(locator, value);
    }

    @Override
    public void setSpeed(String value) {
        selenium.setSpeed(value);
    }

    @Override
    public String getSpeed() {
        return selenium.getSpeed();
    }

    @Override
    public String getLog() {
        return selenium.getLog();
    }

    @Override
    public Page check(String locator) {
        selenium.check(locator);
        return this;
    }

    @Override
    public Page uncheck(String locator) {
        selenium.uncheck(locator);
        return this;
    }

    @Override
    public Page select(String selectLocator, String optionLabel) {
        assertUniqueElement(selectLocator);
        selenium.select(selectLocator, "label=" + optionLabel);
        return this;
    }

    @Override
    public Page select(String selectLocator, int index) {
        assertUniqueElement(selectLocator);
        selenium.select(selectLocator, "index=" + index);
        return this;
    }

    private Page select(int selectIndex, int index) {
        selenium.select("//select[" + selectIndex + "]", "index=" + index);
        return this;
    }

    @Override
    public Page select(int index) {
        selenium.select("//select[" + ++selectCounter + "]", "index=" + index);
        return this;
    }

    @Override
    public Page selectAny() {
        int selectIndex = ++selectCounter;
        int optionIndex = randomInt(1, getXpathCount("//select[" + selectIndex + "]/option[@value!='']").intValue());
        return select(selectIndex, optionIndex);
    }

    protected static int randomInt(int minInclusive, int maxExclusive) {
        return new Random(System.currentTimeMillis()).nextInt(maxExclusive) + minInclusive;
    }

    @Override
    public Page addSelection(String locator, String optionLocator) {
        selenium.addSelection(locator, optionLocator);
        return this;
    }

    @Override
    public Page removeSelection(String locator, String optionLocator) {
        selenium.removeSelection(locator, optionLocator);
        return this;
    }

    @Override
    public Page removeAllSelections(String locator) {
        selenium.removeAllSelections(locator);
        return this;
    }

    @Override
    public Page submitForm() {
//        return submit("//form");
        return clickAndWait("//form//input[@type='submit']");
    }

    @Override
    public Page submit(String formLocator) {
        assertUniqueElement(formLocator);
        selenium.submit(formLocator);
        return this;
    }

//    @Override
//    public void open(String url, String ignoreResponseCode) {
//        selenium.open(url, ignoreResponseCode);
//    }

//    @Override
//    public void open(String url) {
//        selenium.open();
//    }

//    @Override
//    public void openWindow(String url, String windowID) {
//        selenium.openWindow();
//    }

    @Override
    public void selectWindow(String windowID) {
        selenium.selectWindow(windowID);
    }

    @Override
    public void selectPopUp(String windowID) {
        selenium.selectPopUp(windowID);
    }

    @Override
    public void deselectPopUp() {
        selenium.deselectPopUp();
    }

    @Override
    public void selectFrame(String locator) {
        selenium.selectFrame(locator);
    }

    @Override
    public boolean getWhetherThisFrameMatchFrameExpression(String currentFrameString, String target) {
        return selenium.getWhetherThisFrameMatchFrameExpression(currentFrameString, target);
    }

    @Override
    public boolean getWhetherThisWindowMatchWindowExpression(String currentWindowString, String target) {
        return selenium.getWhetherThisWindowMatchWindowExpression(currentWindowString, target);
    }

    @Override
    public void waitForPopUp(String windowID, String timeout) {
        selenium.waitForPopUp(windowID, timeout);
    }

    @Override
    public void chooseCancelOnNextConfirmation() {
        selenium.chooseCancelOnNextConfirmation();
    }

    @Override
    public void chooseOkOnNextConfirmation() {
        selenium.chooseOkOnNextConfirmation();
    }

    @Override
    public void answerOnNextPrompt(String answer) {
        selenium.answerOnNextPrompt(answer);
    }

    @Override
    public void goBack() {
        selenium.goBack();
    }

    @Override
    public void refresh() {
        selenium.refresh();
    }

    @Override
    public void close() {
        selenium.close();
    }

    @Override
    public boolean isAlertPresent() {
        return selenium.isAlertPresent();
    }

    @Override
    public boolean isPromptPresent() {
        return selenium.isPromptPresent();
    }

    @Override
    public boolean isConfirmationPresent() {
        return selenium.isConfirmationPresent();
    }

    @Override
    public String getAlert() {
        return selenium.getAlert();
    }

    @Override
    public String getConfirmation() {
        return selenium.getConfirmation();
    }

    @Override
    public String getPrompt() {
        return selenium.getPrompt();
    }

    @Override
    public String getLocation() {
        return selenium.getLocation();
    }

    @Override
    public String getTitle() {
        return selenium.getTitle();
    }

    @Override
    public String getBodyText() {
        return selenium.getBodyText();
    }

    @Override
    public String getValue(String locator) {
        return selenium.getValue(locator);
    }

    @Override
    public String getText(String locator) {
        return selenium.getText(locator);
    }

    @Override
    public void highlight(String locator) {
        selenium.highlight(locator);
    }

    @Override
    public String getEval(String script) {
        return selenium.getEval(script);
    }

    @Override
    public boolean isChecked(String locator) {
        return selenium.isChecked(locator);
    }

    @Override
    public String getTable(String tableCellAddress) {
        return selenium.getTable(tableCellAddress);
    }

    @Override
    public String[] getSelectedLabels(String selectLocator) {
        return selenium.getSelectedLabels(selectLocator);
    }

    @Override
    public String getSelectedLabel(String selectLocator) {
        return selenium.getSelectedLabel(selectLocator);
    }

    @Override
    public String[] getSelectedValues(String selectLocator) {
        return selenium.getSelectedValues(selectLocator);
    }

    @Override
    public String getSelectedValue(String selectLocator) {
        return selenium.getSelectedValue(selectLocator);
    }

    @Override
    public String[] getSelectedIndexes(String selectLocator) {
        return selenium.getSelectedIndexes(selectLocator);
    }

    @Override
    public String getSelectedIndex(String selectLocator) {
        return selenium.getSelectedIndex(selectLocator);
    }

    @Override
    public String[] getSelectedIds(String selectLocator) {
        return selenium.getSelectedIds(selectLocator);
    }

    @Override
    public String getSelectedId(String selectLocator) {
        return selenium.getSelectedId(selectLocator);
    }

    @Override
    public boolean isSomethingSelected(String selectLocator) {
        return selenium.isSomethingSelected(selectLocator);
    }

    @Override
    public String[] getSelectOptions(String selectLocator) {
        return  selenium.getSelectOptions(selectLocator);
    }

    @Override
    public String getAttribute(String attributeLocator) {
        return selenium.getAttribute(attributeLocator);
    }

    @Override
    public boolean isTextPresent(String pattern) {
        return selenium.isTextPresent(pattern);
    }

    @Override
    public boolean isElementPresent(String locator) {
        return selenium.isElementPresent(locator);
    }

    @Override
    public boolean isVisible(String locator) {
        return selenium.isVisible(locator);
    }

    @Override
    public boolean isEditable(String locator) {
        return selenium.isEditable(locator);
    }

    @Override
    public String[] getAllButtons() {
        return selenium.getAllButtons();
    }

    @Override
    public String[] getAllLinks() {
        return selenium.getAllLinks();
    }

    @Override
    public String[] getAllFields() {
        return selenium.getAllFields();
    }

    @Override
    public String[] getAttributeFromAllWindows(String attributeName) {
        return selenium.getAttributeFromAllWindows(attributeName);
    }

    @Override
    public void dragdrop(String locator, String movementsString) {
        selenium.dragdrop(locator, movementsString);
    }

    @Override
    public void setMouseSpeed(String pixels) {
        selenium.setMouseSpeed(pixels);
    }

    @Override
    public Number getMouseSpeed() {
        return selenium.getMouseSpeed();
    }

    @Override
    public void dragAndDrop(String locator, String movementsString) {
        selenium.dragAndDrop(locator, movementsString);
    }

    @Override
    public void dragAndDropToObject(String locatorOfObjectToBeDragged, String locatorOfDragDestinationObject) {
        selenium.dragAndDropToObject(locatorOfObjectToBeDragged, locatorOfDragDestinationObject);
    }

    @Override
    public void windowFocus() {
        selenium.windowFocus();
    }

    @Override
    public void windowMaximize() {
        selenium.windowMaximize();
    }

    @Override
    public String[] getAllWindowIds() {
        return selenium.getAllWindowIds();
    }

    @Override
    public String[] getAllWindowNames() {
        return selenium.getAllWindowNames();
    }

    @Override
    public String[] getAllWindowTitles() {
        return selenium.getAllWindowTitles();
    }

    @Override
    public String getHtmlSource() {
        return selenium.getHtmlSource();
    }

    @Override
    public void setCursorPosition(String locator, String position) {
        selenium.setCursorPosition(locator, position);
    }

    @Override
    public Number getElementIndex(String locator) {
        return selenium.getElementIndex(locator);
    }

    @Override
    public boolean isOrdered(String locator1, String locator2) {
        return selenium.isOrdered(locator1, locator2);
    }

    @Override
    public Number getElementPositionLeft(String locator) {
        return selenium.getElementPositionLeft(locator);
    }

    @Override
    public Number getElementPositionTop(String locator) {
        return selenium.getElementPositionTop(locator);
    }

    @Override
    public Number getElementWidth(String locator) {
        return selenium.getElementWidth(locator);
    }

    @Override
    public Number getElementHeight(String locator) {
        return selenium.getElementHeight(locator);
    }

    @Override
    public Number getCursorPosition(String locator) {
        return selenium.getCursorPosition(locator);
    }

    @Override
    public String getExpression(String expression) {
        return selenium.getExpression(expression);
    }

    @Override
    public Number getXpathCount(String xpath) {
        assertIsCurrentPage();
        return selenium.getXpathCount(xpath.replaceFirst("xpath=", "")); // getXPathCount does not accept 'xpath=' prefix
    }

    @Override
    public Number getCssCount(String css) {
        return selenium.getCssCount(css);
    }

    @Override
    public void assignId(String locator, String identifier) {
        selenium.assignId(locator, identifier);
    }

    @Override
    public void allowNativeXpath(String allow) {
        selenium.allowNativeXpath(allow);
    }

    @Override
    public void ignoreAttributesWithoutValue(String ignore) {
        selenium.ignoreAttributesWithoutValue(ignore);
    }

    @Override
    public void waitForCondition(String script, String timeout) {
        selenium.waitForCondition(script, timeout);
    }

    @Override
    public void setTimeout(String timeout) {
        selenium.setTimeout(timeout);
    }

    @Override
    public void waitForPageToLoad(String timeout) {
        selenium.waitForPageToLoad(timeout);
    }

    @Override
    public void waitForFrameToLoad(String frameAddress, String timeout) {
        selenium.waitForFrameToLoad(frameAddress, timeout);
    }

    @Override
    public String getCookie() {
        return selenium.getCookie();
    }

    @Override
    public String getCookieByName(String name) {
        return selenium.getCookieByName(name);
    }

    @Override
    public boolean isCookiePresent(String name) {
        return selenium.isCookiePresent(name);
    }

    @Override
    public void createCookie(String nameValuePair, String optionsString) {
        selenium.createCookie(nameValuePair, optionsString);
    }

    @Override
    public void deleteCookie(String name, String optionsString) {
        selenium.deleteCookie(name, optionsString);
    }

    @Override
    public void deleteAllVisibleCookies() {
        selenium.deleteAllVisibleCookies();
    }

    @Override
    public void setBrowserLogLevel(String logLevel) {
        selenium.setBrowserLogLevel(logLevel);
    }

    @Override
    public void runScript(String script) {
        selenium.runScript(script);
    }

    @Override
    public void addLocationStrategy(String strategyName, String functionDefinition) {
        selenium.addLocationStrategy(strategyName, functionDefinition);
    }

    @Override
    public void captureEntirePageScreenshot(String filename, String kwargs) {
        selenium.captureEntirePageScreenshot(filename, kwargs);
    }

    @Override
    public void rollup(String rollupName, String kwargs) {
        selenium.rollup(rollupName, kwargs);
    }

    @Override
    public void addScript(String scriptContent, String scriptTagId) {
        selenium.addScript(scriptContent, scriptTagId);
    }

    @Override
    public void removeScript(String scriptTagId) {
        selenium.removeScript(scriptTagId);
    }

    @Override
    public void useXpathLibrary(String libraryName) {
        selenium.useXpathLibrary(libraryName);
    }

    @Override
    public void setContext(String context) {
        selenium.setContext(context);
    }

    @Override
    public void attachFile(String fieldLocator, String fileLocator) {
        selenium.attachFile(fieldLocator, fileLocator);
    }

    @Override
    public void captureScreenshot(String filename) {
        selenium.captureScreenshot(filename);
    }

    @Override
    public String captureScreenshotToString() {
        return selenium.captureScreenshotToString();
    }

    @Override
    public String captureNetworkTraffic(String type) {
        return selenium.captureNetworkTraffic(type);
    }

    @Override
    public void addCustomRequestHeader(String key, String value) {
        selenium.addCustomRequestHeader(key, value);
    }

    @Override
    public String captureEntirePageScreenshotToString(String kwargs) {
        return selenium.captureEntirePageScreenshotToString(kwargs);
    }

    @Override
    public void shutDownSeleniumServer() {
        selenium.shutDownSeleniumServer();
    }

    @Override
    public String retrieveLastRemoteControlLogs() {
        return selenium.retrieveLastRemoteControlLogs();
    }

    @Override
    public void keyDownNative(String keycode) {
        selenium.keyDownNative(keycode);
    }

    @Override
    public void keyUpNative(String keycode) {
        selenium.keyUpNative(keycode);
    }

    @Override
    public void keyPressNative(String keycode) {
        selenium.keyPressNative(keycode);
    }

    @Override
    public String toString() {
        Stack<URL> locationStack = getLocationStack();
        return locationStack.get(locationStack.size() - 1).toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SeleniumPage that = (SeleniumPage) o;
        Stack<URL> locationStack = getLocationStack();
        return locationStack.lastElement().equals(that.getLocationStack().lastElement());
    }

    @Override
    public int hashCode() {
        Stack<URL> locationStack = getLocationStack();
        return locationStack.lastElement().hashCode();
    }

    /* End of generated delegation code */

    public static URL toLocationPath(String location) {
        try {
            URL fullUrl = new URL(location);
            // We just throw the query string if any
            return new URL(fullUrl.getProtocol(), fullUrl.getHost(), fullUrl.getPort(), fullUrl.getPath());
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }
}
