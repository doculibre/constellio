/**
 * Constellio
 * Copyright (C) 2010 DocuLibre inc.
 * <p>
 * This program is free software: you can redistribute it and/or modifyTo
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.constellio.sdk.tests.selenium.adapters.constellio;

import com.constellio.sdk.tests.selenium.adapters.base.WebElementAdapter;
import com.constellio.sdk.tests.selenium.adapters.base.WebElementFinder;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static com.constellio.data.utils.LangUtils.tabs;

public class ConstellioWebElement extends WebElementAdapter<ConstellioWebElement, ConstellioWebDriver> {

	private static final Logger LOGGER = LoggerFactory.getLogger(ConstellioWebElement.class);

	public ConstellioWebElement(ConstellioWebDriver webDriver, By by) {
		super(webDriver, by);
	}

	protected ConstellioWebElement(ConstellioWebDriver webDriver, WebElementFinder<WebElement> webElementFinder) {
		super(webDriver, webElementFinder);
	}

	public ConstellioWebElement(WebElementAdapter<?, ConstellioWebDriver> webElement, By by) {
		super(webElement, by);
	}

	public ConstellioWebElement(WebElementAdapter<ConstellioWebElement, ConstellioWebDriver> webElement) {
		super(webElement);
	}

	@Override
	protected ConstellioWebElement adapt(WebElementFinder<WebElement> adapted, ConstellioWebDriver webDriver) {
		return new ConstellioWebElement(webDriver, adapted);
	}

	public void clickAndWaitForPageReload() {
		clickAndWaitForPageReload(3);
	}

	public void clickAndWaitForPageReload(int timeoutInSeconds) {
		String lastPageDateString = getWebDriver().getPageLoadTimeAsString(100);
		click();
		getWebDriver().waitForPageReload(timeoutInSeconds, lastPageDateString);
	}

	public ConstellioWebTableElement findTableElement(By by) {
		return new ConstellioWebTableElement(this.findElement(by));
	}

	public void printHierarchy() {
		StringBuilder stringBuilder = new StringBuilder();
		printHierarchy(stringBuilder, 0);
		LOGGER.info(stringBuilder.toString());
	}

	protected void printHierarchy(StringBuilder stringBuilder, int indent) {

		List<ConstellioWebElement> elements = getChildren();

		stringBuilder.append(tabs(indent) + getTagName() + "[" + getClassNames() + "]");

		if (elements.isEmpty()) {
			String text = getText();

			if (text != null && StringUtils.isNotBlank(text.trim())) {
				stringBuilder.append(":" + text.trim().replace("\n", "\\n"));
			}

		}
		stringBuilder.append("\n");
		for (ConstellioWebElement element : elements) {
			element.printHierarchy(stringBuilder, indent + 1);
		}
	}

	public void clickAndWaitForRemoval() {
		click();
		waitForRemoval();
	}

	@Override
	public <X> X getScreenshotAs(OutputType<X> target) throws WebDriverException {
		return null;
	}

	@Override
	public Rectangle getRect() {
		return null;
	}
}
