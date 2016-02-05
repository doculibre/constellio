package com.constellio.app.ui.tools.vaadin;

import org.openqa.selenium.By;

import com.constellio.app.ui.framework.components.BaseForm;
import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebDriver;
import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebElement;

public class BaseFormWebElement {
	ConstellioWebElement nestedElement;
	ConstellioWebDriver driver;
	ConstellioWebElement saveButton;
	ConstellioWebElement cancelButton;

	public BaseFormWebElement(ConstellioWebElement nestedElement) {
		this.nestedElement = nestedElement;
		this.driver = this.nestedElement.getWebDriver();
		nestedElement.click();
		this.saveButton = nestedElement.findElement(By.className(BaseForm.SAVE_BUTTON));
		this.cancelButton = this.driver.findElement(By.className(BaseForm.CANCEL_BUTTON));
	}

	public ConstellioWebElement getSaveButton() {
		return saveButton;
	}

	public ConstellioWebElement getCancelButton() {
		return cancelButton;
	}

	public void cancel() {
		click(cancelButton);
	}

	public void ok() {
		click(saveButton);
	}

	private void click(ConstellioWebElement saveButton) {
		//int timeoutInSeconds = 100;
		//String lastPageDateString = driver.getPageLoadTimeAsString(100);
		saveButton.click();
		//driver.waitForPageReload(timeoutInSeconds, lastPageDateString);
	}
}
