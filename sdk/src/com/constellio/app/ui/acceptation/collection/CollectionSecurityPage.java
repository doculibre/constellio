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
import com.constellio.app.ui.pages.collection.ListCollectionUserViewImpl;
import com.constellio.app.ui.tools.ButtonWebElement;
import com.constellio.app.ui.tools.DropDownWebElement;
import com.constellio.app.ui.tools.LookupWebElement;
import com.constellio.app.ui.tools.PageHelper;
import com.constellio.app.ui.tools.RecordContainerWebElement;
import com.constellio.app.ui.tools.RecordContainerWebElement.RecordContainerWebElementRow;
import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebDriver;
import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebElement;

public class CollectionSecurityPage extends PageHelper {
	public static final int CODE_COLUMN = 0;
	public static final int NAME_COLUMN = 0;

	public CollectionSecurityPage(ConstellioWebDriver driver) {
		super(driver);
	}

	public CollectionSecurityPage navigateToPage() {
		driver.navigateTo().url(NavigatorConfigurationService.COLLECTION_USER_LIST);
		return this;
	}

	public LookupWebElement getUserLookup() {
		ConstellioWebElement element = driver.findRequiredElement(By.className(ListCollectionUserViewImpl.USER_LOOKUP));
		return new LookupWebElement(element);
	}

	public DropDownWebElement getUserRolesListSelect() {
		ConstellioWebElement element = driver.findElement(By.className(ListCollectionUserViewImpl.ROLES_USERS_COMBO));
		return new DropDownWebElement(element);
	}

	public DropDownWebElement getGroupRolesListSelect() {
		ConstellioWebElement element = driver.findElement(By.className(ListCollectionUserViewImpl.ROLES_GROUPS_COMBO));
		return new DropDownWebElement(element);
	}

	public ButtonWebElement getUserAddButton() {
		return new ButtonWebElement(driver.findRequiredElement(By.className(ListCollectionUserViewImpl.USER_ADD)));
	}

	public RecordContainerWebElement getUserTable() {
		driver.waitUntilElementExist(By.className(ListCollectionUserViewImpl.USER_TABLE));
		ConstellioWebElement element = driver.findRequiredElement(By.className(ListCollectionUserViewImpl.USER_TABLE));
		return new RecordContainerWebElement(element);
	}

	public RecordContainerWebElementRow getUserWithName(String name) {
		return getUserTable().getFirstRowWithValueInColumn(name, NAME_COLUMN);
	}

	public CollectionSecurityPage addUserAndRole(String lookupText, String role) {
		getUserLookup().typeAndSelectFirst(lookupText);
		getUserRolesListSelect().select(role);
		getUserAddButton().click();
		return this;
	}

	public LookupWebElement getGroupLookup() {
		ConstellioWebElement element = driver.findRequiredElement(By.className(ListCollectionUserViewImpl.GROUP_LOOKUP));
		return new LookupWebElement(element);
	}

	public ButtonWebElement getGroupAddButton() {
		return new ButtonWebElement(driver.findRequiredElement(By.className(ListCollectionUserViewImpl.GROUP_ADD)));
	}

	public RecordContainerWebElement getGroupTable() {

		ConstellioWebElement element = driver.findRequiredElement(By.className(ListCollectionUserViewImpl.GROUP_TABLE));
		return new RecordContainerWebElement(element);
	}

	public CollectionSecurityPage addGroupAndRole(String lookupText, String role) {
		ConstellioWebElement groupTable = driver.findRequiredElement(By.className(ListCollectionUserViewImpl.GROUP_TABLE));
		getGroupLookup().typeAndSelectFirst(lookupText);
		getGroupRolesListSelect().select(role);
		getGroupAddButton().clickAndWaitForElementRefresh(groupTable);
		return this;
	}

	public RecordContainerWebElementRow getGroupWithCode(String code) {
		return getGroupTable().getFirstRowWithValueInColumn(code, CODE_COLUMN);
	}
}