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
