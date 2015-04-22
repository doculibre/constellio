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
package com.constellio.app.ui.pages.management.collections;

import java.awt.*;
import java.awt.event.KeyEvent;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.interactions.Actions;

import com.constellio.app.ui.tools.vaadin.BaseFormWebElement;
import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebDriver;
import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebElement;
import com.vaadin.ui.TextField;

public class AddEditCollectionFacade {
	private final BaseFormWebElement baseFormWebElement;
	private final ConstellioWebElement code;
	private final ConstellioWebElement name;
	ConstellioWebDriver driver;


	public AddEditCollectionFacade(ConstellioWebDriver driver) {
		this.driver = driver;
		this.baseFormWebElement = new BaseFormWebElement(this.driver.find(AddEditCollectionViewImpl.BASE_FORM_STYLE));
		this.code = this.driver.find(AddEditCollectionViewImpl.CODE_FIELD_STYLE);
		this.name = this.driver.find(AddEditCollectionViewImpl.NAME_FIELD_STYLE);

	}

	public void setName(String name) {
		this.name.changeValueTo(name);
	}

	public void cancel() {
		this.baseFormWebElement.cancel();
	}

	public void save() {
		this.baseFormWebElement.ok();
	}

	public void setCode(String code) {
		this.code.changeValueTo(code);
	}

	public String getErrorMessage() {
		ConstellioWebElement  error = this.driver.find("v-Notification-error");
		return error.getText();
	}

	public boolean isCodeFieldEnabled() {
		return this.code.isEnabled();
	}

}
