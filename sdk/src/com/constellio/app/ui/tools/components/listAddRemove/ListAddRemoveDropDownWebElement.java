package com.constellio.app.ui.tools.components.listAddRemove;

import com.constellio.app.ui.tools.DropDownWebElement;
import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebElement;

public class ListAddRemoveDropDownWebElement extends ListAddRemoveFieldWebElement<DropDownWebElement> {

	public ListAddRemoveDropDownWebElement(
			ConstellioWebElement nestedElement) {
		super(nestedElement);
	}

	@Override
	protected DropDownWebElement wrapInputElement(ConstellioWebElement element) {
		return new DropDownWebElement(element);
	}

	public ListAddRemoveDropDownWebElement add(String choiceCaption) {
		getInputComponent().select(choiceCaption);
		super.clickAdd();
		return this;
	}

	public ListAddRemoveDropDownWebElement modify(int index, String choiceCaption) {
		super.clickModify(index);
		add(choiceCaption);
		return this;
	}

	public ListAddRemoveDropDownWebElement clickAdd() {
		super.clickAdd();
		return this;
	}

	public ListAddRemoveDropDownWebElement clickModify(int index) {
		super.clickModify(index);
		return this;
	}

	public ListAddRemoveDropDownWebElement remove(int index) {
		super.remove(index);
		return this;
	}

}
