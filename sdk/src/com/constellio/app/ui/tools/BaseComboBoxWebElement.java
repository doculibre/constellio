package com.constellio.app.ui.tools;

import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebElement;
import org.openqa.selenium.By;

public class BaseComboBoxWebElement extends AutocompleteWebElement {

	private final ConstellioWebElement selectButton;

	public BaseComboBoxWebElement(ConstellioWebElement element) {
		super(element);
		selectButton = element.findElement(By.className("v-filterselect-button"));
	}

	public void selectFromListValues(int index) {
		selectButton.click();
		super.select(index);
	}
}
