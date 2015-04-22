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
package com.constellio.app.ui.acceptation.management.schemas;

import org.openqa.selenium.By;

import com.constellio.app.ui.application.NavigatorConfigurationService;
import com.constellio.app.ui.pages.management.schemas.ListSchemaTypeViewImpl;
import com.constellio.app.ui.tools.PageHelper;
import com.constellio.app.ui.tools.RecordContainerWebElement;
import com.constellio.app.ui.tools.RecordContainerWebElement.RecordContainerWebElementRow;
import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebDriver;
import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebElement;

public class SchemaTypePage extends PageHelper {
	public static final int TITLE_COLUMN = 0;

	public SchemaTypePage(ConstellioWebDriver driver) {
		super(driver);
	}

	public SchemaTypePage navigateToPage() {
		driver.navigateTo().url(NavigatorConfigurationService.DISPLAY_SCHEMA_TYPE);
		return this;
	}

	public RecordContainerWebElement getTypeTable() {
		ConstellioWebElement element = driver.findRequiredElement(By.className(ListSchemaTypeViewImpl.TYPE_TABLE));
		return new RecordContainerWebElement(element);
	}

	public RecordContainerWebElementRow getTypeWithTitle(String title) {
		return getTypeTable().getFirstRowWithValueInColumn(title, TITLE_COLUMN);
	}
}
