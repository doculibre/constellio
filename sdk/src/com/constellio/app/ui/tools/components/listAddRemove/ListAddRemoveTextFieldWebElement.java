package com.constellio.app.ui.tools.components.listAddRemove;

import com.constellio.app.ui.tools.components.basic.TextFieldWebElement;
import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebElement;

public class ListAddRemoveTextFieldWebElement extends ListAddRemoveFieldWebElement<TextFieldWebElement> {

	public ListAddRemoveTextFieldWebElement(
			ConstellioWebElement nestedElement) {
		super(nestedElement);
	}

	@Override
	protected TextFieldWebElement wrapInputElement(ConstellioWebElement element) {
		return new TextFieldWebElement(element);
	}

	public ListAddRemoveTextFieldWebElement add(String value) {
		getInputComponent().setValue(value);
		clickAdd();
		return this;
	}
	
	public ListAddRemoveTextFieldWebElement addDate(String date) {
		getInputDateComponent().setValue(date);
		clickAdd();
		return this;
	}

	public ListAddRemoveTextFieldWebElement modifyTo(int index, String value) {
		clickModify(index);
		getInputComponent().setValue(value);
		clickAdd();
		return this;
	}

	public ListAddRemoveTextFieldWebElement setValue(String value) {
		getInputComponent().setValue(value);
		return this;

	}
	
	
}
