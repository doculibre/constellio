package com.constellio.app.ui.tools.components.basic;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.openqa.selenium.By;

import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebElement;

public class DateFieldWebElement {

	ConstellioWebElement element;
	TextFieldWebElement textField;

	private DateTimeFormatter format;

	public DateFieldWebElement(ConstellioWebElement element) {
		this(element, "yyyy-MM-dd");
	}

	public DateFieldWebElement(ConstellioWebElement element, String pattern) {
		this.element = element;
		this.format = DateTimeFormat.forPattern(pattern);
		this.textField = new TextFieldWebElement(element.findElement(By.className("v-textfield")));
	}

	public LocalDate getValue() {
		String value = textField.getValue();
		if (StringUtils.isBlank(value)) {
			return null;
		} else {
			return LocalDate.parse(value, format);
		}
	}

	public void setValue(LocalDate value) {
		if (value == null) {
			setEmpty();
		} else {
			textField.setValue(value.toString(format));
		}
	}

	public void setValueWithTime(LocalDateTime value) {
		if (value == null) {
			setEmpty();
		} else {
			textField.clear();
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
			//			new ConditionWithTimeout() {
			//
			//				@Override
			//				protected boolean evaluate() {
			//					return StringUtils.isBlank(textField.getValue());
			//				}
			//			}.waitForTrue(1000);
			textField.setValue(value.toString(format));
		}
	}

	public void setEmpty() {
		textField.clear();
	}
}
