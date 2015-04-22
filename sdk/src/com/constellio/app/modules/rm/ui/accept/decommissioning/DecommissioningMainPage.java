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

import org.openqa.selenium.By;

import com.constellio.app.modules.rm.services.decommissioning.SearchType;
import com.constellio.app.modules.rm.ui.pages.decommissioning.DecommissioningMainViewImpl;
import com.constellio.app.ui.application.NavigatorConfigurationService;
import com.constellio.app.ui.tools.ButtonWebElement;
import com.constellio.app.ui.tools.PageHelper;
import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebDriver;
import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebElement;

public class DecommissioningMainPage extends PageHelper {
	protected DecommissioningMainPage(ConstellioWebDriver driver) {
		super(driver);
	}

	public DecommissioningMainPage navigateToPage() {
		driver.navigateTo().url(NavigatorConfigurationService.DECOMMISSIONING);
		return this;
	}

	public ButtonWebElement getCreationLink(SearchType type) {
		ConstellioWebElement element = driver.findRequiredElement(By.className(DecommissioningMainViewImpl.CREATE + type));
		return new ButtonWebElement(element);
	}

	public DecommissioningBuilderPage goToBuilder(SearchType type) {
		getCreationLink(type).clickAndWaitForPageReload();
		return new DecommissioningBuilderPage(driver);
	}
}
