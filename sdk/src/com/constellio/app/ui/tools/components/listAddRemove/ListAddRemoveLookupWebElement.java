package com.constellio.app.ui.tools.components.listAddRemove;

import com.constellio.app.ui.tools.LookupWebElement;
import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebElement;

public class ListAddRemoveLookupWebElement extends ListAddRemoveFieldWebElement<LookupWebElement> {

	public ListAddRemoveLookupWebElement(
			ConstellioWebElement nestedElement) {
		super(nestedElement);
	}

	@Override
	protected LookupWebElement wrapInputElement(ConstellioWebElement element) {
		return new LookupWebElement(element);
	}

	public ListAddRemoveLookupWebElement addElementByChoosingFirstChoice(String text) {
		getInputComponent().typeAndSelectFirst(text);
		super.clickAdd();
		return this;
	}

	public ListAddRemoveLookupWebElement modifyElementByChoosingFirstChoice(int index, String text) {
		super.clickModify(index);
		getInputComponent().clear();
		addElementByChoosingFirstChoice(text);
		return this;
	}

	public ListAddRemoveLookupWebElement clickAdd() {
		super.clickAdd();
		return this;
	}

	public ListAddRemoveLookupWebElement clickModify(int index) {
		super.clickModify(index);
		return this;
	}

	public ListAddRemoveLookupWebElement remove(int index) {
		super.remove(index);
		return this;
	}

}
