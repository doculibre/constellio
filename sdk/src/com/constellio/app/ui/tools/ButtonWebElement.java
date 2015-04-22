/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package com.constellio.app.ui.tools;

import com.constellio.sdk.tests.selenium.adapters.base.WebElementAdapter;
import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebDriver;
import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebElement;

public class ButtonWebElement {
	private final ConstellioWebElement element;

	public ButtonWebElement(ConstellioWebElement element) {
		this.element = element;
	}

	public void clickAndWaitForPageReload() {
		element.clickAndWaitForPageReload();
	}

	public void clickAndWaitForPageReload(int timeoutInSeconds) {
		element.clickAndWaitForPageReload(timeoutInSeconds);
	}

	public void click() {
		element.click();
	}

	@Deprecated
	public void clickUsingJavascript() {
		element.clickUsingJavascript();
	}

	public boolean isEnabled() {
		return !element.getClassNames().contains("v-disabled");
	}

	public void clickAndWaitForElementRefresh(
			WebElementAdapter<ConstellioWebElement, ConstellioWebDriver> element) {
		this.element.clickAndWaitForElementRefresh(element);
	}

	public void clickAndWaitForElementRefresh(
			WebElementAdapter<ConstellioWebElement, ConstellioWebDriver> element,
			long timeoutInMS) {
		this.element.clickAndWaitForElementRefresh(element, timeoutInMS);
	}

	public boolean isDisplayed() {
		return element.isDisplayed();
	}

	public void scrollIntoView() {
		element.scrollIntoView();
	}

	public String getCaption(){
		return element.find("v-button-caption").getText();
	}
}
