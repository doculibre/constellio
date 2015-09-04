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
package com.constellio.app.ui.acceptation.management.authorizations;

import org.openqa.selenium.By;

import com.constellio.app.ui.application.NavigatorConfigurationService;
import com.constellio.app.ui.pages.management.authorizations.ListAuthorizationsViewImpl;
import com.constellio.app.ui.tools.PageHelper;
import com.constellio.app.ui.tools.RecordContainerWebElement;
import com.constellio.model.entities.records.wrappers.RecordWrapper;
import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebDriver;
import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebElement;

public class ListAuthorizationsPage extends PageHelper {
	protected ListAuthorizationsPage(ConstellioWebDriver driver) {
		super(driver);
	}

	public ListAuthorizationsPage navigateToPrincipalPage(RecordWrapper principal) {
		driver.navigateTo().url(NavigatorConfigurationService.LIST_PRINCIPAL_ACCESS_AUTHORIZATIONS + "/" + principal.getId());
		return this;
	}

	public ListAuthorizationsPage navigateToObjectPage(RecordWrapper object) {
		driver.navigateTo().url(NavigatorConfigurationService.LIST_OBJECT_ACCESS_AUTHORIZATIONS + "/" + object.getId());
		return this;
	}

	public RecordContainerWebElement getOwnAuthorizationsTable() {
		ConstellioWebElement element = driver.findRequiredElement(By.className(ListAuthorizationsViewImpl.AUTHORIZATIONS));
		return new RecordContainerWebElement(element);
	}

	public RecordContainerWebElement getInheritedAuthorizationsTable() {
		ConstellioWebElement element = driver.findRequiredElement(
				By.className(ListAuthorizationsViewImpl.INHERITED_AUTHORIZATIONS));
		return new RecordContainerWebElement(element);
	}
}
