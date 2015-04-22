/**
 * Constellio
 * Copyright (C) 2010 DocuLibre inc.
 *
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
package com.constellio.sdk.tests.selenium.adapters.base;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.constellio.sdk.tests.selenium.conditions.ActionWithTimeout;
import com.constellio.sdk.tests.selenium.conditions.ConditionWithTimeout;

@SuppressWarnings({ "unchecked" })
public abstract class WebDriverAdapter<WE extends WebElement> implements WebDriver, JavascriptExecutor {

	protected WebDriver adapted;

	public WebDriverAdapter(WebDriver driver) {
		super();
		this.adapted = driver;
	}

	protected abstract WE adapt(WebElementFinder<WebElement> factory);

	@Override
	public void close() {
		adapted.close();
	}

	protected abstract void ensureNoApplicationException();

	public List<WE> findAdaptElements(final By by) {
		List<WE> adapters = new ArrayList<WE>();
		int size = findElements(by).size();
		for (int i = 0; i < size; i++) {
			adapters.add(findElementAtIndex(by, i));
		}
		return Collections.unmodifiableList(adapters);
	}

	public WE find(String className) {
		return findElement(By.className(className));
	}

	private WebElement nestedFindElement(By by) {
		try {
			return adapted.findElement(by);
		} catch (Throwable t) {
			String errorMessage = "Cannot find element " + by.toString().replace("By.", "by ");
			throw new RuntimeException(errorMessage, t);
		}

	}

	@Override
	public WE findElement(final By by) {
		ensureNoApplicationException();
		WebElement element = nestedFindElement(by);

		if (element == null) {
			return null;
		} else {
			WebElementFinder<WebElement> factory = new WebElementFinder<WebElement>() {

				@Override
				public WebElement get() {
					ensureNoApplicationException();
					return WebDriverAdapter.this.getAdaptedDriver().findElement(by);
				}

				@Override
				public String getOperationDescription() {
					return "webDriver.find(" + by.toString() + ")";
				}
			};
			factory.getUsingCache();
			return adapt(factory);
		}
	}

	public WE findElementAtIndex(final By by, final int index) {
		WebElementFinder<WebElement> factory = new WebElementFinder<WebElement>() {

			@Override
			public WebElement get() {
				ensureNoApplicationException();
				return WebDriverAdapter.this.getAdaptedDriver().findElements(by).get(index);
			}

			@Override
			public String getOperationDescription() {
				return "webDriver.findElements(" + by.toString() + ")";
			}
		};
		factory.getUsingCache();
		return adapt(factory);
	}

	@Override
	public List<WebElement> findElements(By by) {
		return adapted.findElements(by);
	}

	public WE findRequiredElement(By by) {
		WE element = findElement(by);
		if (element == null) {
			throw new RequiredElementNotFound(by);
		}
		return element;
	}

	public List<WE> findRequiredElements(By by) {
		List<WE> adapters = findAdaptElements(by);
		if (adapters.isEmpty()) {
			throw new RequiredElementNotFound(by);
		} else {
			return adapters;
		}
	}

	@Override
	public void get(String url) {
		adapted.get(url);
	}

	public WebDriver getAdaptedDriver() {
		return adapted;
	}

	@Override
	public String getCurrentUrl() {
		return adapted.getCurrentUrl();
	}

	@Override
	public String getPageSource() {
		try {
			return new ActionWithTimeout<String>() {

				@Override
				protected String execute()
						throws Exception {
					return adapted.getPageSource();
				}

			}.tryExecute(5000);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public String getTitle() {
		return adapted.getTitle();
	}

	@Override
	public String getWindowHandle() {
		return adapted.getWindowHandle();
	}

	@Override
	public Set<String> getWindowHandles() {
		return adapted.getWindowHandles();
	}

	@Override
	public Options manage() {
		return adapted.manage();
	}

	@Override
	public Navigation navigate() {
		return adapted.navigate();
	}

	@Override
	public void quit() {
		adapted.quit();
	}

	@Override
	public TargetLocator switchTo() {
		return adapted.switchTo();
	}

	@Override
	public String toString() {
		return adapted.toString();
	}

	public final void waitForCondition(ConditionWithTimeout condition) {
		condition.waitForTrue(10000);
	}

	public final void waitForCondition(ConditionWithTimeout condition, long timeout) {
		condition.waitForTrue(timeout);
	}

	public WE waitUntilElementExist(By by) {
		return waitUntilElementExist(by, 10000);
	}

	public WE waitUntilElementExist(final By by, long timeout) {
		final Object[] elementHolder = new Object[1];
		new ConditionWithTimeout() {

			@Override
			protected boolean evaluate() {
				WE element = findElement(by);
				elementHolder[0] = element;
				element.getLocation();
				return element != null;
			}
		}.waitForTrue(timeout);

		return (WE) elementHolder[0];
	}

	@Override
	public Object executeScript(String script, Object... args) {
		JavascriptExecutor executor = (JavascriptExecutor) adapted;
		return executor.executeScript(script, convertScriptArguments(args));
	}

	@Override
	public Object executeAsyncScript(String script, Object... args) {
		JavascriptExecutor executor = (JavascriptExecutor) adapted;
		return executor.executeAsyncScript(script, convertScriptArguments(args));
	}

	@SuppressWarnings("rawtypes")
	private Object[] convertScriptArguments(Object[] arguments) {
		Object[] convertedArguments = new Object[arguments.length];
		for (int i = 0; i < arguments.length; i++) {
			if (arguments[i] instanceof WebElementAdapter) {
				convertedArguments[i] = ((WebElementAdapter) arguments[i]).getAdaptedElement();
			} else {
				convertedArguments[i] = arguments[i];
			}
		}
		return convertedArguments;
	}

	public List<WE> getChildren() {
		return findAdaptElements(By.xpath("*"));
	}

	public void scrollIntoView(WebElement webElement) {
		executeScript("arguments[0].scrollIntoView();", webElement);
	}

}
