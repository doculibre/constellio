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
package com.constellio.app.ui.pages.globalGroup;

import java.util.List;

import org.openqa.selenium.By;

import com.constellio.app.ui.framework.buttons.AddButton;
import com.constellio.app.ui.framework.buttons.BackButton;
import com.constellio.app.ui.framework.buttons.DeleteButton;
import com.constellio.app.ui.framework.buttons.EditButton;
import com.constellio.app.ui.framework.buttons.SearchButton;
import com.constellio.app.ui.tools.ButtonWebElement;
import com.constellio.app.ui.tools.PageHelper;
import com.constellio.app.ui.tools.components.basic.TextFieldWebElement;
import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebDriver;
import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebElement;

public class DisplayGlobalGroupPage extends PageHelper {

	public DisplayGlobalGroupPage(ConstellioWebDriver driver) {
		super(driver);
	}

	public TextFieldWebElement getSearchInputSubGroups() {
		ConstellioWebElement element = driver.findAdaptElements(By.tagName("input")).get(1);
		return new TextFieldWebElement(element);
	}

	public TextFieldWebElement getSearchInputUsersInGroup() {
		ConstellioWebElement element = driver.findAdaptElements(By.tagName("input")).get(2);
		return new TextFieldWebElement(element);
	}

	public TextFieldWebElement getSearchInputUsers() {
		ConstellioWebElement element = driver.findAdaptElements(By.tagName("input")).get(3);
		return new TextFieldWebElement(element);
	}

	public ButtonWebElement getAddButton() {
		return getButtonByClassName(AddButton.BUTTON_STYLE, 0);
	}

	public ButtonWebElement getBackButton() {
		return getButtonByClassName(BackButton.BUTTON_STYLE, 0);
	}

	public ButtonWebElement getSearchButtonSubGroups() {
		return getButtonByClassName(SearchButton.STYLE_NAME, 1);
	}

	public ButtonWebElement getSearchButtonUsersInGroup() {
		return getButtonByClassName(SearchButton.STYLE_NAME, 2);
	}

	public ButtonWebElement getSearchButtonUsers() {
		return getButtonByClassName(SearchButton.STYLE_NAME, 3);
	}

	public ButtonWebElement getEditUserCredentialButtonOnIndex(int index) {
		return getButtonByClassName(EditButton.BUTTON_STYLE, index);
	}

	public ButtonWebElement getAddSubGlobalGroupButtonMenuAction() {
		return getButtonByClassName("DisplayGlobalGroupView.addSubGroup", 0);
	}

	public ButtonWebElement getEditSubGlobalGroupButtonOnIndex(int index) {
		return getButtonByClassName("DisplayGlobalGroupView.editSubGroup", index);
	}

	public ButtonWebElement getDeleteSubGlobalGroupButtonOnIndex(int index) {
		return getButtonByClassName("DisplayGlobalGroupView.deleteSubGroup", index);
	}

	public ButtonWebElement getEditGlobalGroupButtonMenuAction() {
		return getButtonByCssSelector("." + EditButton.BUTTON_STYLE + ".action-menu-button");
	}

	private ButtonWebElement getButtonByCssSelector(String cssSelectors) {
		ConstellioWebElement element = driver
				.findElement(By.cssSelector(cssSelectors));
		return new ButtonWebElement(element);
	}

	public ButtonWebElement getDeleteGlobalGroupButtonMenuAction() {
		return getButtonByCssSelector("." + DeleteButton.BUTTON_STYLE + ".action-menu-button");
	}

	public ButtonWebElement getDeleteButtonOnIndex(int index) {
		return getButtonByClassName(DeleteButton.BUTTON_STYLE, index);
	}

	public List<ConstellioWebElement> getTableRowsSubGroups() {
		return getTableRowsByClassName("DisplayGlobalGroup.listSubGroups");
	}

	public List<ConstellioWebElement> getTableRowsUsersInGroup() {
		return getTableRowsByClassName("DisplayGlobalGroup.listGroupsUserCredentials");
	}

	public List<ConstellioWebElement> getTableRowsUsers() {
		return getTableRowsByClassName("DisplayGlobalGroup.listUserCredentials");
	}

	private List<ConstellioWebElement> getTableRowsByClassName(String className) {
		ConstellioWebElement tableElement = driver.findAdaptElements(By.className(className))
				.get(0);
		tableElement = tableElement.findAdaptElements(By.tagName("table")).get(1);
		List<ConstellioWebElement> rows = tableElement.findAdaptElements(By.tagName("tr"));
		return rows;
	}
}
