package com.constellio.app.ui.tools;

import java.util.List;

import org.openqa.selenium.By;

import com.constellio.app.ui.framework.buttons.AddButton;
import com.constellio.app.ui.framework.buttons.DeleteButton;
import com.constellio.app.ui.framework.buttons.EditButton;
import com.constellio.app.ui.framework.components.BaseForm;
import com.constellio.app.ui.tools.components.basic.TextFieldWebElement;
import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebDriver;
import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebElement;

public abstract class PageHelper {
	protected final ConstellioWebDriver driver;

	protected PageHelper(ConstellioWebDriver driver) {
		this.driver = driver;
	}

	public String getTitle() {
		return driver.findRequiredElement(By.className("h1")).getText();
	}

	public String getErrorMessage() {
		return getErrorMessage(true);
	}

	public String getErrorMessage(boolean dismissMessage) {
		ConstellioWebElement message = driver.findRequiredElement(By.className("error"));
		String result = message.getText();
		if (dismissMessage) {
			message.click();
		}
		return result;
	}

	public String getWarningMessage() {
		return getWarningMessage(true);
	}

	public String getWarningMessage(boolean dismissMessage) {
		ConstellioWebElement message = driver.findRequiredElement(By.className("warning"));
		String result = message.getText();
		if (dismissMessage) {
			message.click();
		}
		return result;
	}

	public void waitForPageReload() {
		int timeoutInSeconds = 2;
		String lastPageDateString = driver.getPageLoadTimeAsString(100);
		driver.waitForPageReload(timeoutInSeconds, lastPageDateString);
	}

	public void logout() {
		getButtonByClassName("v-menubar-menuitem-user-settings").click();
		getButtonByClassName("v-menubar-menuitem-disconnect-item").clickAndWaitForPageReload();
	}

	public ButtonWebElement getButtonByClassName(String className) {
		return getButtonByClassName(className, 0);
	}

	public ButtonWebElement getButtonByClassName(String className, int index) {
		ConstellioWebElement element = driver.findAdaptElements(By.className(className)).get(index);
		element.scrollIntoView();
		return new ButtonWebElement(element);
	}

	public ButtonWebElement getFirstButtonByClassName(String className) {
		return getButtonByClassName(className, 0);
	}

	public ButtonWebElement getBackButton() {
		ConstellioWebElement element = driver.findRequiredElement(By.className("back-button"));
		element.scrollIntoView();
		return new ButtonWebElement(element);
	}

	public TextFieldWebElement getTextFieldWebElementById(String id) {
		ConstellioWebElement textFieldElement = driver.findElement(By.id(id));
		textFieldElement.scrollIntoView();
		return new TextFieldWebElement(textFieldElement);
	}

	public OptionGroupWebElement getOptionGroupWebElementById(String id) {
		ConstellioWebElement optionGroupElement = driver.findElement(By.id(id));
		optionGroupElement.scrollIntoView();
		return new OptionGroupWebElement(optionGroupElement);
	}

	public ButtonWebElement getOkConfirmationDialogButton() {
		String id = "confirmdialog-ok-button";
		driver.waitUntilElementExist(By.id(id));
		return getButtonById(id);
	}

	public ButtonWebElement getCancelConfirmationDialogButton() {
		return getButtonById("confirmdialog-cancel-button");
	}

	public ButtonWebElement getButtonById(String id) {
		ConstellioWebElement element = driver.findElement(By.id(id));
		return new ButtonWebElement(element);
	}

	public ButtonWebElement getCancelButton() {
		return getButtonByClassName(BaseForm.CANCEL_BUTTON, 0);
	}

	public ButtonWebElement getSaveButton() {
		return getButtonByClassName(BaseForm.SAVE_BUTTON, 0);
	}

	public List<ConstellioWebElement> findEditButtonElements() {
		return driver.findAdaptElements(By.className(EditButton.BUTTON_STYLE));
	}

	public List<ConstellioWebElement> findAddButtonElements() {
		return driver.findAdaptElements(By.className(AddButton.BUTTON_STYLE));
	}

	public List<ConstellioWebElement> findDeleteButtonElements() {
		return driver.findAdaptElements(By.className(DeleteButton.BUTTON_STYLE));
	}
}
