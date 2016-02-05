package com.constellio.app.ui.tools;

import org.openqa.selenium.By;

import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebElement;

public class DropDownWebElement extends AutocompleteWebElement{

	public DropDownWebElement(ConstellioWebElement element) {
		super(element);
	}

	public DropDownWebElement select(String choiceCaption) {
		this.selectItemContainingText(choiceCaption);

		return this;
	}
	
}
