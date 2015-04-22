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
package com.constellio.app.ui.acceptation.collection;

import org.openqa.selenium.By;

import com.constellio.app.ui.application.NavigatorConfigurationService;
import com.constellio.app.ui.pages.collection.CollectionUserRolesViewImpl;
import com.constellio.app.ui.tools.AutocompleteWebElement;
import com.constellio.app.ui.tools.ButtonWebElement;
import com.constellio.app.ui.tools.PageHelper;
import com.constellio.app.ui.tools.RecordContainerWebElement;
import com.constellio.model.entities.records.wrappers.Group;
import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebDriver;
import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebElement;

public class CollectionGroupRolesPage extends PageHelper {
	protected CollectionGroupRolesPage(ConstellioWebDriver driver) {
		super(driver);
	}

	public CollectionGroupRolesPage navigateToPage(Group group) {
		driver.navigateTo().url(NavigatorConfigurationService.COLLECTION_GROUP_ROLES + "/" + group.getId());
		return this;
	}

	public AutocompleteWebElement getRoleLookup() {
		ConstellioWebElement element = driver.findRequiredElement(By.className(CollectionUserRolesViewImpl.ROLE_SELECTOR));
		return new AutocompleteWebElement(element);
	}

	public ButtonWebElement getAddRoleButton() {
		ConstellioWebElement element = driver.findRequiredElement(By.className(CollectionUserRolesViewImpl.ADD_ROLE));
		return new ButtonWebElement(element);
	}

	public CollectionGroupRolesPage addRole(String role) {
		getRoleLookup().typeAndSelectFirst(role);
		getAddRoleButton().click();
		return this;
	}

	public RecordContainerWebElement getRoleTable() {
		ConstellioWebElement element = driver.findRequiredElement(By.className(CollectionUserRolesViewImpl.ROLES));
		return new RecordContainerWebElement(element);
	}
}
