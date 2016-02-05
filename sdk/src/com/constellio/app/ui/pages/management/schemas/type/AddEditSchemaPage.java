package com.constellio.app.ui.pages.management.schemas.type;

import com.constellio.app.ui.tools.PageHelper;
import com.constellio.app.ui.tools.components.basic.TextFieldWebElement;
import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebDriver;

public class AddEditSchemaPage extends PageHelper {

	public AddEditSchemaPage(ConstellioWebDriver driver) {
		super(driver);
	}

	public TextFieldWebElement getCodeElement() {
		return getTextFieldWebElementById("localCode");
	}

	public TextFieldWebElement getTitleElement() {
		return getTextFieldWebElementById("label");
	}

}
