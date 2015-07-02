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
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.openqa.selenium.By;

import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebElement;

public class DateTimeFieldWebElement {

	ConstellioWebElement element;
	TextFieldWebElement textField;

	private DateTimeFormatter format;

	public DateTimeFieldWebElement(ConstellioWebElement element) {
		this(element, "yyyy-MM-dd HH:mm:ss");
	}

	public DateTimeFieldWebElement(ConstellioWebElement element, String pattern) {
		this.element = element;
		this.format = DateTimeFormat.forPattern(pattern);
		element.printHierarchy();
		this.textField = new TextFieldWebElement(element.findElement(By.className("v-textfield")));
	}

	public LocalDateTime getValue() {
		String value = textField.getValue();
		if (StringUtils.isBlank(value)) {
			return null;
		} else {
			return LocalDateTime.parse(value, format);
		}
	}

	public void setValue(LocalDateTime value) {
		if (value == null) {
			textField.clear();
		} else {
			textField.setValue(value.toString(format));
		}
	}
}
