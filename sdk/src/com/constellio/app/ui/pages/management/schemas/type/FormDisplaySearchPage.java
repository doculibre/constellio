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
package com.constellio.app.ui.pages.management.schemas.type;

import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.Select;

import com.constellio.app.ui.tools.ButtonWebElement;
import com.constellio.app.ui.tools.PageHelper;
import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebDriver;
import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebElement;

public class FormDisplaySearchPage extends PageHelper {

	public FormDisplaySearchPage(ConstellioWebDriver driver) {
		super(driver);
	}

	public Select getOptionsElement() {
		ConstellioWebElement element = driver.findElement(By.className("v-listbuilder-options"));
		return new Select(element);
	}

	public Select getSelectionsElement() {
		ConstellioWebElement element = driver.findElement(By.className("v-listbuilder-selections"));
		return new Select(element);
	}

	public ButtonWebElement getAddButtonElement() {
		ConstellioWebElement element = driver.findElement(By.className("v-listbuilder-button-add"));
		return new ButtonWebElement(element);
	}

	public ButtonWebElement getRemoveButtonElement() {
		ConstellioWebElement element = driver.findElement(By.className("v-listbuilder-button-remove"));
		return new ButtonWebElement(element);
	}

	public ButtonWebElement getUpButtonElement() {
		ConstellioWebElement element = driver.findElement(By.className("v-listbuilder-button-up"));
		return new ButtonWebElement(element);
	}

	public ButtonWebElement getDownButtonElement() {
		ConstellioWebElement element = driver.findElement(By.className("v-listbuilder-button-down"));
		return new ButtonWebElement(element);
	}
}
