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
package com.constellio.app.ui.acceptation.management.schemas.display.group;

import java.util.HashMap;
import java.util.Map;

import org.openqa.selenium.By;

import com.constellio.app.ui.application.NavigatorConfigurationService;
import com.constellio.app.ui.pages.management.schemas.display.group.ListMetadataGroupSchemaTypeViewImpl;
import com.constellio.app.ui.params.ParamUtils;
import com.constellio.app.ui.tools.ButtonWebElement;
import com.constellio.app.ui.tools.PageHelper;
import com.constellio.app.ui.tools.RecordContainerWebElement;
import com.constellio.app.ui.tools.RecordContainerWebElement.RecordContainerWebElementRow;
import com.constellio.app.ui.tools.components.basic.TextFieldWebElement;
import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebDriver;
import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebElement;

public class MetadataGroupSchemaPage extends PageHelper {
	public static final int TITLE_COLUMN = 0;

	public MetadataGroupSchemaPage(ConstellioWebDriver driver) {
		super(driver);
	}

	public MetadataGroupSchemaPage navigateToPage(String schemaTypeCode) {
		Map<String, String> paramsMap = new HashMap<>();
		paramsMap.put("schemaTypeCode", schemaTypeCode);
		String params = ParamUtils.addParams(NavigatorConfigurationService.LIST_ONGLET, paramsMap);
		driver.navigateTo().url(params);
		return this;
	}

	public RecordContainerWebElement getGroupTable() {
		ConstellioWebElement element = driver.findRequiredElement(By.className(ListMetadataGroupSchemaTypeViewImpl.GROUP_TABLE));
		return new RecordContainerWebElement(element);
	}

	public RecordContainerWebElementRow getGroupWithName(String title) {
		return getGroupTable().getFirstRowWithValueInColumn(title, TITLE_COLUMN);
	}

	public void addGroupWithName(String name) {
		ConstellioWebElement element = driver.findRequiredElement(By.className(ListMetadataGroupSchemaTypeViewImpl.GROUP_NAME));
		TextFieldWebElement nameField = new TextFieldWebElement(element);
		nameField.setValue(name);

		element = driver.findRequiredElement(By.className(ListMetadataGroupSchemaTypeViewImpl.GROUP_BUTTON));
		ButtonWebElement button = new ButtonWebElement(element);
		button.click();
	}

	public void removeGroupWithName(String name) {
		RecordContainerWebElementRow record = getGroupTable().getFirstRowWithValueInColumn(name, TITLE_COLUMN);
		record.clickButtonAndConfirm(ListMetadataGroupSchemaTypeViewImpl.GROUP_DELETE_BUTTON);
	}
}
