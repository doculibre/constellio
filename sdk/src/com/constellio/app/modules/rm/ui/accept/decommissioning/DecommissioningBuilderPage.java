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
package com.constellio.app.modules.rm.ui.accept.decommissioning;

import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.By;

import com.constellio.app.modules.rm.services.decommissioning.SearchType;
import com.constellio.app.modules.rm.ui.pages.decommissioning.DecommissioningBuilderViewImpl;
import com.constellio.app.ui.application.NavigatorConfigurationService;
import com.constellio.app.ui.framework.components.BaseForm;
import com.constellio.app.ui.tools.AutocompleteWebElement;
import com.constellio.app.ui.tools.ButtonWebElement;
import com.constellio.app.ui.tools.CheckboxWebElement;
import com.constellio.app.ui.tools.PageHelper;
import com.constellio.app.ui.tools.RecordFormWebElement;
import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebDriver;
import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebElement;

public class DecommissioningBuilderPage extends PageHelper {
	protected DecommissioningBuilderPage(ConstellioWebDriver driver) {
		super(driver);
	}

	public DecommissioningBuilderPage navigateToPage(SearchType type) {
		driver.navigateTo().url(NavigatorConfigurationService.DECOMMISSIONING_LIST_BUILDER + "/" + type);
		return this;
	}

	public DecommissioningBuilderPage searchAndWaitForResults() {
		getSearchButton().click();
		driver.waitUntilElementExist(By.className(DecommissioningBuilderViewImpl.CREATE_LIST));
		return this;
	}

	public AutocompleteWebElement getAdministrativeUnit() {
		ConstellioWebElement element = driver.findRequiredElement(By.className(DecommissioningBuilderViewImpl.ADMIN_UNIT));
		return new AutocompleteWebElement(element);
	}

	public ButtonWebElement getSearchButton() {
		ConstellioWebElement element = driver.findRequiredElement(By.className(DecommissioningBuilderViewImpl.SEARCH));
		return new ButtonWebElement(element);
	}

	public ButtonWebElement getCreateButton() {
		ConstellioWebElement element = driver.findRequiredElement(By.className(DecommissioningBuilderViewImpl.CREATE_LIST));
		return new ButtonWebElement(element);
	}

	public List<CheckboxWebElement> getAllResultCheckBoxes() {
		List<CheckboxWebElement> result = new ArrayList<>();
		for (ConstellioWebElement element : driver.findRequiredElements(By.xpath("//input[@type='checkbox']/.."))) {
			result.add(new CheckboxWebElement(element));
		}
		return result;
	}

	public RecordFormWebElement openCreateForm() {
		getCreateButton().click();
		driver.waitUntilElementExist(By.className(BaseForm.BASE_FORM));
		ConstellioWebElement element = driver.findRequiredElement(By.className(BaseForm.BASE_FORM));
		return new RecordFormWebElement(element);
	}
}
