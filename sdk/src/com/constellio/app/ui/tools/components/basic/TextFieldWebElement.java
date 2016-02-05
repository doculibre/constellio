package com.constellio.app.ui.tools.components.basic;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.Keys;

import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebElement;
import com.constellio.sdk.tests.selenium.conditions.ConditionWithTimeout;

public class TextFieldWebElement {

	ConstellioWebElement element;

	public TextFieldWebElement(ConstellioWebElement element) {
		this.element = element;
		this.element.scrollIntoView();
	}

	public String getValue() {
		return element.getAttribute("value");
	}

	public TextFieldWebElement clear() {
		element.clear();

		new ConditionWithTimeout() {
			@Override
			protected boolean evaluate() {
				return StringUtils.isBlank(element.getAttribute("value"));
			}
		}.waitForTrue(1000);
		return this;
	}

	public TextFieldWebElement enterText(final String text) {

		if (text != null && !text.isEmpty()) {
			element.sendKeys(text);

			new ConditionWithTimeout() {
				@Override
				protected boolean evaluate() {
					return element.getAttribute("value").endsWith(text);
				}
			}.waitForTrue(1000);

		}
		return this;
	}

	public TextFieldWebElement clearAndSetValue(String text) {
		clear();
		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
		element.sendKeys(text);
		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
		element.sendKeys(Keys.TAB);
		if (!text.equals(element.getAttribute("value"))) {
			return clearAndSetValue(text);
		}

		return this;
	}

	public TextFieldWebElement setValue(String text) {
		element.sendKeys(Keys.chord(Keys.CONTROL, "a"), text);
		return this;
	}

	public boolean isEnabled() {
		return element.isEnabled();
	}

	public void sendKeys(CharSequence... keys) {
		element.sendKeys(keys);
	}
}
