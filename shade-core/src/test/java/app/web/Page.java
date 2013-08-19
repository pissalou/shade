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

import java.net.URL;
import java.util.Arrays;
import java.util.Stack;

/**
 * Page under testing.
 */
public interface Page {

    boolean isCurrentPage();

//    Page registerAction(SeleniumPage.Action action);

    Stack<SeleniumPage.Action> getActionStack();

    Stack<URL> getLocationStack();

    Page click(String locator);

    Page clickAndWait(String locator);

    Page clickLinkAndWait(String linkText);

    Page clickAndWaitForLoadIfNecessary(String locator, boolean forceWaitForLoad);

    /* Automatically generated */

    void setExtensionJs(String extensionJs);

    void showContextualBanner();

    void showContextualBanner(String className, String methodName);

    void doubleClick(String locator);

    void contextMenu(String locator);

    void clickAt(String locator, String coordString);

    void doubleClickAt(String locator, String coordString);

    void contextMenuAt(String locator, String coordString);

    void fireEvent(String locator, String eventName);

    void focus(String locator);

    void keyPress(String locator, String keySequence);

    void shiftKeyDown();

    void shiftKeyUp();

    void metaKeyDown();

    void metaKeyUp();

    void altKeyDown();

    void altKeyUp();

    void controlKeyDown();

    void controlKeyUp();

    void keyDown(String locator, String keySequence);

    void keyUp(String locator, String keySequence);

    void mouseOver(String locator);

    void mouseOut(String locator);

    void mouseDown(String locator);

    void mouseDownRight(String locator);

    void mouseDownAt(String locator, String coordString);

    void mouseDownRightAt(String locator, String coordString);

    void mouseUp(String locator);

    void mouseUpRight(String locator);

    void mouseUpAt(String locator, String coordString);

    void mouseUpRightAt(String locator, String coordString);

    void mouseMove(String locator);

    void mouseMoveAt(String locator, String coordString);

    Page type(String locator, String value);

    Page input(int inputIndex, String value);

    Page input(String value);

    void typeKeys(String locator, String value);

    void setSpeed(String value);

    String getSpeed();

    String getLog();

    Page check(String locator);

    Page uncheck(String locator);

    Page selectAny();

    Page select(int index);

    Page select(String selectLocator, String optionLabel);

    Page select(String selectLocator, int optionIndex);

    Page addSelection(String locator, String optionLocator);

    Page removeSelection(String locator, String optionLocator);

    Page removeAllSelections(String locator);

    Page submitForm();

    Page submit(String formLocator);

    void selectWindow(String windowID);

    void selectPopUp(String windowID);

    void deselectPopUp();

    void selectFrame(String locator);

    boolean getWhetherThisFrameMatchFrameExpression(String currentFrameString, String target);

    boolean getWhetherThisWindowMatchWindowExpression(String currentWindowString, String target);

    void waitForPopUp(String windowID, String timeout);

    void chooseCancelOnNextConfirmation();

    void chooseOkOnNextConfirmation();

    void answerOnNextPrompt(String answer);

    void goBack();

    void refresh();

    void close();

    boolean isAlertPresent();

    boolean isPromptPresent();

    boolean isConfirmationPresent();

    String getAlert();

    String getConfirmation();

    String getPrompt();

    String getLocation();

    String getTitle();

    String getBodyText();

    String getValue(String locator);

    String getText(String locator);

    void highlight(String locator);

    String getEval(String script);

    boolean isChecked(String locator);

    String getTable(String tableCellAddress);

    String[] getSelectedLabels(String selectLocator);

    String getSelectedLabel(String selectLocator);

    String[] getSelectedValues(String selectLocator);

    String getSelectedValue(String selectLocator);

    String[] getSelectedIndexes(String selectLocator);

    String getSelectedIndex(String selectLocator);

    String[] getSelectedIds(String selectLocator);

    String getSelectedId(String selectLocator);

    boolean isSomethingSelected(String selectLocator);

    String[] getSelectOptions(String selectLocator);

    String getAttribute(String attributeLocator);

    boolean isTextPresent(String pattern);

    boolean isElementPresent(String locator);

    boolean isVisible(String locator);

    boolean isEditable(String locator);

    String[] getAllButtons();

    String[] getAllLinks();

    String[] getAllFields();

    String[] getAttributeFromAllWindows(String attributeName);

    void dragdrop(String locator, String movementsString);

    void setMouseSpeed(String pixels);

    Number getMouseSpeed();

    void dragAndDrop(String locator, String movementsString);

    void dragAndDropToObject(String locatorOfObjectToBeDragged, String locatorOfDragDestinationObject);

    void windowFocus();

    void windowMaximize();

    String[] getAllWindowIds();

    String[] getAllWindowNames();

    String[] getAllWindowTitles();

    String getHtmlSource();

    void setCursorPosition(String locator, String position);

    Number getElementIndex(String locator);

    boolean isOrdered(String locator1, String locator2);

    Number getElementPositionLeft(String locator);

    Number getElementPositionTop(String locator);

    Number getElementWidth(String locator);

    Number getElementHeight(String locator);

    Number getCursorPosition(String locator);

    String getExpression(String expression);

    Number getXpathCount(String xpath);

    Number getCssCount(String css);

    void assignId(String locator, String identifier);

    void allowNativeXpath(String allow);

    void ignoreAttributesWithoutValue(String ignore);

    void waitForCondition(String script, String timeout);

    void setTimeout(String timeout);

    void waitForPageToLoad(String timeout);

    void waitForFrameToLoad(String frameAddress, String timeout);

    String getCookie();

    String getCookieByName(String name);

    boolean isCookiePresent(String name);

    void createCookie(String nameValuePair, String optionsString);

    void deleteCookie(String name, String optionsString);

    void deleteAllVisibleCookies();

    void setBrowserLogLevel(String logLevel);

    void runScript(String script);

    void addLocationStrategy(String strategyName, String functionDefinition);

    void captureEntirePageScreenshot(String filename, String kwargs);

    void rollup(String rollupName, String kwargs);

    void addScript(String scriptContent, String scriptTagId);

    void removeScript(String scriptTagId);

    void useXpathLibrary(String libraryName);

    void setContext(String context);

    void attachFile(String fieldLocator, String fileLocator);

    void captureScreenshot(String filename);

    String captureScreenshotToString();

    String captureNetworkTraffic(String type);

    void addCustomRequestHeader(String key, String value);

    String captureEntirePageScreenshotToString(String kwargs);

    void shutDownSeleniumServer();

    String retrieveLastRemoteControlLogs();

    void keyDownNative(String keycode);

    void keyUpNative(String keycode);

    void keyPressNative(String keycode);

//    Page clickAndWaitIfNecessary(String locator, boolean waitForLoad);

    public static class Action {
        public enum ActionType { NO_CHANGE, OPEN, CLICK_NEW_PAGE, CLICK_SAME_PAGE, CLICK_AJAX, CLICK_DOM_UPDATE }
        protected ActionType type;
        protected String target; // where you click on
        protected String[] value; // url or locator(s)

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
}