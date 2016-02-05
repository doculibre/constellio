package com.constellio.app.ui.pages.events;

import com.constellio.app.ui.framework.components.EventByIdSearchPanel;
import com.constellio.app.ui.tools.BaseComboBoxWebElement;
import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebDriver;

public class ByIdEventCategoryFacade extends BaseEventCategoryFacade {
	private BaseComboBoxWebElement byIdElement;

	public ByIdEventCategoryFacade(ConstellioWebDriver driver) {
		super(driver);
		byIdElement = new BaseComboBoxWebElement(driver.find(EventByIdSearchPanel.LOOKUP_STYLE_CODE));
	}

	public void selectElement(String byIdFieldValue) {
		byIdElement.typeAndSelectFirst(byIdFieldValue);
		//TODO remove when problem with lookup fixed
		byIdElement.selectFromListValues(1);
	}
}
