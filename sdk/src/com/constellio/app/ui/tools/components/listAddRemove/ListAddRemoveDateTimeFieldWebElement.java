package com.constellio.app.ui.tools.components.listAddRemove;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;

import com.constellio.app.ui.tools.components.basic.DateFieldWebElement;
import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebElement;

public class ListAddRemoveDateTimeFieldWebElement extends ListAddRemoveFieldWebElement<DateFieldWebElement> {

	public ListAddRemoveDateTimeFieldWebElement(ConstellioWebElement nestedElement) {
		super(nestedElement);
	}

	@Override
	protected DateFieldWebElement wrapInputElement(ConstellioWebElement element) {
		return new DateFieldWebElement(element, "yyyy-MM-dd hh:mm:ss");
	}
	
	public ListAddRemoveDateTimeFieldWebElement add(LocalDateTime value) {
		getInputComponent().setValueWithTime(value);;
		super.clickAdd();
		return this;
	}

	public ListAddRemoveDateTimeFieldWebElement modify(int index, LocalDateTime newValue) {
		super.clickModify(index);
		add(newValue);
		return this;
	}

	public ListAddRemoveDateTimeFieldWebElement remove(int index) {
		super.remove(index);
		return this;
	}	
}

