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
