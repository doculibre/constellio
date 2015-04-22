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
