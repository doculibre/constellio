/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package com.constellio.app.ui.tools;

import org.openqa.selenium.By;

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

	public void waitForPageReload() {
		int timeoutInSeconds = 2;
		String lastPageDateString = driver.getPageLoadTimeAsString(100);
		driver.waitForPageReload(timeoutInSeconds, lastPageDateString);
	}

	public ButtonWebElement getButtonByClassName(String className, int index) {
		ConstellioWebElement element = driver.findAdaptElements(By.className(className)).get(index);
		element.scrollIntoView();
		return new ButtonWebElement(element);
	}

	public ButtonWebElement getBackButton() {
		ConstellioWebElement element = driver.findRequiredElement(By.className("back-button"));
		element.scrollIntoView();
		return new ButtonWebElement(element);
	}

	public TextFieldWebElement getTextFieldWebElementById(String id) {
		ConstellioWebElement textFieldElement = driver.findElement(By.id(id));
		textFieldElement.scrollIntoView();
		TextFieldWebElement textFieldWebElement = new TextFieldWebElement(textFieldElement);
		return textFieldWebElement;
	}

	public OptionGroupWebElement getOptionGroupWebElementById(String id) {
		ConstellioWebElement optionGroupElement = driver.findElement(By.id(id));
		optionGroupElement.scrollIntoView();
		OptionGroupWebElement optionGroupWebElement = new OptionGroupWebElement(optionGroupElement);
		return optionGroupWebElement;
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
}
