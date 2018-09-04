package com.constellio.app.ui.tools.components.listAddRemove;

import com.constellio.app.ui.tools.components.basic.DateFieldWebElement;
import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebElement;
import org.joda.time.LocalDate;

public class ListAddRemoveDateFieldWebElement extends ListAddRemoveFieldWebElement<DateFieldWebElement> {

	public ListAddRemoveDateFieldWebElement(
			ConstellioWebElement nestedElement) {
		super(nestedElement);
	}

	@Override
	protected DateFieldWebElement wrapInputElement(ConstellioWebElement element) {
		return new DateFieldWebElement(element);
	}

	public ListAddRemoveDateFieldWebElement add(LocalDate value) {
		getInputComponent().setValue(value);
		;
		super.clickAdd();
		return this;
	}

	public ListAddRemoveDateFieldWebElement modify(int index, LocalDate newValue) {
		super.clickModify(index);
		add(newValue);
		return this;
	}

	public ListAddRemoveDateFieldWebElement remove(int index) {
		super.remove(index);
		return this;
	}
}
