/**
 * Constellio
 * Copyright (C) 2010 DocuLibre inc.
 *
 * This program is free software: you can redistribute it and/or modifyTo
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
package com.constellio.sdk.tests.selenium.adapters.base;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

import com.constellio.sdk.tests.selenium.conditions.ConditionWithTimeout;

@SuppressWarnings({ "rawtypes", "unchecked" })
public abstract class WebElementAdapter<WE extends WebElement, WD extends WebDriverAdapter<WE>> implements WebElement {

	private WebElementFinder<WebElement> adaptedElementFinder;

	private WD webDriver;

	public WebElementAdapter(WD driver, final By by) {
		super();
		this.webDriver = driver;
		this.adaptedElementFinder = new WebElementFinder<WebElement>() {

			@Override
			public WebElement get() {
				webDriver.ensureNoApplicationException();
				return WebElementAdapter.this.webDriver.findElement(by);
			}

			@Override
			public String getOperationDescription() {
				return "webDriver.find(" + by.toString() + ")";
			}

		};
		this.adaptedElementFinder.getUsingCache();

	}

	protected WebElementAdapter(WD webDriver, WebElementFinder<WebElement> webElementFinder) {
		this.webDriver = webDriver;
		this.adaptedElementFinder = webElementFinder;
		this.adaptedElementFinder.getUsingCache();
	}

	public WebElementAdapter(final WebElementAdapter<?, WD> webElement, final By by) {
		super();
		this.webDriver = webElement.webDriver;
		this.adaptedElementFinder = new WebElementFinder<WebElement>() {

			@Override
			public WebElement get() {
				webDriver.ensureNoApplicationException();
				return webElement.findElement(by);
			}

			@Override
			public String getOperationDescription() {
				return webElement.toString() + ".find(" + by.toString() + ")";
			}
		};
		this.adaptedElementFinder.getUsingCache();
	}

	public WebElementAdapter(WebElementAdapter<WE, WD> webElement) {
		super();
		this.webDriver = webElement.webDriver;
		this.adaptedElementFinder = webElement.adaptedElementFinder;
	}

	protected abstract WE adapt(WebElementFinder<WebElement> adapted, WD webDriver);

	public void changeValueTo(String newValue) {
		clear();
		try {
			Thread.sleep(10);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		click();
		sendKeysWithoutOnchangeEvent(newValue);
	}

	@Override
	public void clear() {
		getAdaptedElement().clear();
	}

	@Override
	public void click() {
		scrollIntoView();
		getAdaptedElement().click();
	}
	
	public void rightClick() {
        WebElement webElement = getAdaptedSeleniumElement();
        Actions oAction = new Actions(webDriver.getAdaptedDriver());
        oAction.moveToElement(webElement);
        oAction.contextClick(webElement).build().perform();
    }

	@Deprecated
	public void clickUsingJavascript() {
		try {
			scrollIntoView();
			webDriver.executeScript("arguments[0].scrollIntoView();arguments[0].click();", getAdaptedSeleniumElement());
		} catch (Exception e) {
			getAdaptedSeleniumElement().click();
		}
	}

	public void clickAndWaitForElementRefresh(final WebElementAdapter<WE, WD> element) {
		clickAndWaitForElementRefresh(element, 10000);
	}

	public void clickAndWaitForElementRefresh(final WebElementAdapter<WE, WD> element, long timeoutInMS) {
		scrollIntoView();
		final long initialVersion = element.getVersion();
		getAdaptedElement().click();
		new ConditionWithTimeout() {

			@Override
			protected boolean evaluate() {
				return element.getVersion() > initialVersion;
			}
		}.waitForTrue(timeoutInMS);
	}

	public List<WE> findAdaptElements(final By by) {
		List<WE> adapters = new ArrayList<WE>();
		int size = getAdaptedElement().findElements(by).size();
		for (int i = 0; i < size; i++) {
			adapters.add(findElementAtIndex(by, i));
		}
		return Collections.unmodifiableList(adapters);
	}

	public WE find(String className) {
		return findElement(By.className(className));
	}

	@Override
	public WE findElement(final By by) {
		WebElementFinder<WebElement> factory = new WebElementFinder<WebElement>() {

			@Override
			public WebElement get() {
				webDriver.ensureNoApplicationException();
				return getAdaptedElement().findElement(by);
			}

			@Override
			public String getOperationDescription() {
				return getAdaptedElement().toString() + ".find(" + by.toString() + ")";
			}
		};
		factory.getUsingCache();
		return adapt(factory, webDriver);
	}

	public WE findElementAtIndex(final By by, final int index) {
		WebElementFinder<WebElement> factory = new WebElementFinder<WebElement>() {

			@Override
			public WebElement get() {
				webDriver.ensureNoApplicationException();
				return getAdaptedElement().findElements(by).get(index);
			}

			@Override
			public String getOperationDescription() {
				return WebElementAdapter.this.toString() + ".findElements(" + by.toString() + ")";
			}
		};
		factory.getUsingCache();
		return adapt(factory, webDriver);
	}

	@Override
	public List<WebElement> findElements(By by) {
		return (List<WebElement>) findAdaptElements(by);
	}

	public WebElement getAdaptedElement() {
		WebElement adapted = adaptedElementFinder.getUsingCache();
		return adapted;
	}

	public WebElement getAdaptedSeleniumElement() {
		WebElement adapted = adaptedElementFinder.getUsingCache();
		if (adapted instanceof WebElementAdapter) {
			return ((WebElementAdapter) adapted).getAdaptedSeleniumElement();
		} else {
			return adapted;
		}
	}

	public List<WE> getChildren() {
		return findAdaptElements(By.xpath("*"));
	}

	public String getClassNames() {
		return getAttribute("class");
	}

	@Override
	public String getAttribute(String name) {
		return getAdaptedElement().getAttribute(name);
	}

	@Override
	public String getCssValue(String propertyName) {
		return getAdaptedElement().getCssValue(propertyName);
	}

	@Override
	public Point getLocation() {
		return getAdaptedElement().getLocation();
	}

	@Override
	public Dimension getSize() {
		return getAdaptedElement().getSize();
	}

	@Override
	public String getTagName() {
		return getAdaptedElement().getTagName();
	}

	@Override
	public String getText() {
		scrollIntoView();
		return getAdaptedElement().getText();
	}

	public long getVersion() {
		return adaptedElementFinder.getVersion();
	}

	@Override
	public boolean isDisplayed() {
		return getAdaptedElement().isDisplayed();
	}

	@Override
	public boolean isEnabled() {
		return getAdaptedElement().isEnabled();
	}

	@Override
	public boolean isSelected() {
		return getAdaptedElement().isSelected();
	}

	/**
	 * Use sendKeysWithoutOnchangeEvent instead
	 */
	@Override
	public void sendKeys(CharSequence... keysToSend) {
		sendKeysWithoutOnchangeEvent(keysToSend);

	}

	public void sendKeysWithoutOnchangeEvent(CharSequence... keysToSend) {
		getAdaptedElement().sendKeys(keysToSend);
	}

	@Override
	public void submit() {
		getAdaptedElement().submit();
	}

	@Override
	public String toString() {
		return "anElement";
	}

	public void waitForRemoval() {
		waitForRemoval(10000);
	}

	public void waitForRemoval(long timeout) {
		new ConditionWithTimeout() {

			@Override
			protected boolean evaluate() {
				try {
					getLocation();
					return false;
				} catch (Exception e) {
					return true;
				}
			}
		}.waitForTrue(timeout);
	}

	public void waitUntilElementExist(By by) {
		waitUntilElementExist(by, 10000);
	}

	public void waitUntilElementExist(final By by, long timeout) {
		new ConditionWithTimeout() {

			@Override
			protected boolean evaluate() {
				return findElement(by) != null;
			}
		}.waitForTrue(timeout);
	}

	public void waitUntilElementRemoved(By by) {
		waitUntilElementRemoved(by, 10000);
	}

	public void waitUntilElementRemoved(final By by, long timeout) {
		new ConditionWithTimeout() {

			@Override
			protected boolean evaluate() {
				try {
					findElement(by).getLocation();
					return false;
				} catch (Exception e) {
					return true;
				}
			}
		}.waitForTrue(timeout);
	}

	public WD getWebDriver() {
		return webDriver;
	}

	public WebElementFinder<WebElement> getAdaptedElementFinder() {
		return adaptedElementFinder;
	}

	public void scrollIntoView() {

		//Actions actions = new Actions(webDriver.getAdaptedDriver());
		//actions.moveToElement(this.getAdaptedSeleniumElement());
		// actions.click();
		//actions.perform();

		webDriver.scrollIntoView(this);
	}

}
