package com.constellio.app.ui.pages.globalGroup;

import com.constellio.app.ui.tools.OptionGroupWebElement;
import com.constellio.app.ui.tools.PageHelper;
import com.constellio.app.ui.tools.components.basic.TextFieldWebElement;
import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebDriver;

public class AddEditGlobalGroupPage extends PageHelper {

	public AddEditGlobalGroupPage(ConstellioWebDriver driver) {
		super(driver);
	}

	public TextFieldWebElement getCodeElement() {
		return getTextFieldWebElementById("code");
	}

	public TextFieldWebElement getNameElement() {
		return getTextFieldWebElementById("name");
	}

	public OptionGroupWebElement getCollectionsElement() {
		return getOptionGroupWebElementById("collections");
	}

	public OptionGroupWebElement getStatusElement() {
		return getOptionGroupWebElementById("status");
	}
}
