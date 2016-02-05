package com.constellio.app.ui.pages.user;

import com.constellio.app.ui.framework.components.BaseForm;
import com.constellio.app.ui.tools.ButtonWebElement;
import com.constellio.app.ui.tools.OptionGroupWebElement;
import com.constellio.app.ui.tools.PageHelper;
import com.constellio.app.ui.tools.components.basic.TextFieldWebElement;
import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebDriver;

public class AddEditUserCredentialPage extends PageHelper {

	public AddEditUserCredentialPage(ConstellioWebDriver driver) {
		super(driver);
	}

	public TextFieldWebElement getUsernameElement() {
		return getTextFieldWebElementById("username");
	}

	public TextFieldWebElement getFirstNameElement() {
		return getTextFieldWebElementById("firstName");
	}

	public TextFieldWebElement getLastNameElement() {
		return getTextFieldWebElementById("lastName");
	}

	public TextFieldWebElement getEmailElement() {
		return getTextFieldWebElementById("email");
	}

	public TextFieldWebElement getPasswordElement() {
		return getTextFieldWebElementById("password");
	}

	public TextFieldWebElement getConfirmPasswordElement() {
		return getTextFieldWebElementById("confirmPassword");
	}

	public TextFieldWebElement getOldPassword() {
		return getTextFieldWebElementById("oldPassword");
	}

	public OptionGroupWebElement getCollectionsElement() {
		return getOptionGroupWebElementById("collections");
	}

	public ButtonWebElement getCancelButton() {
		return getButtonByClassName(BaseForm.CANCEL_BUTTON, 0);
	}

	public ButtonWebElement getSaveButton() {
		return getButtonByClassName(BaseForm.SAVE_BUTTON, 0);
	}
}
