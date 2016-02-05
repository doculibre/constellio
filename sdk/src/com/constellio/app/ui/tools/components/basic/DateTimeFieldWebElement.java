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
