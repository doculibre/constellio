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
package com.constellio.app.modules.rm.ui.accept.retentionRules;

import org.openqa.selenium.By;

import com.constellio.app.ui.application.NavigatorConfigurationService;
import com.constellio.app.ui.framework.components.RecordForm;
import com.constellio.app.ui.tools.PageHelper;
import com.constellio.app.ui.tools.RecordFormWebElement;
import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebDriver;
import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebElement;

public class AddEditRetentionRulePage extends PageHelper {
	public AddEditRetentionRulePage(ConstellioWebDriver driver) {
		super(driver);
	}

	public AddEditRetentionRulePage navigateToPage() {
		driver.navigateTo().url(NavigatorConfigurationService.ADD_RETENTION_RULE);
		return this;
	}

	public RecordFormWebElement getForm() {
		ConstellioWebElement element = driver.findRequiredElement(By.className(RecordForm.BASE_FORM));
		return new RecordFormWebElement(element);
	}
}
