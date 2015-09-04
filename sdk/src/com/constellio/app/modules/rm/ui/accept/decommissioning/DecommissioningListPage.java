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

import com.constellio.app.modules.rm.ui.pages.decommissioning.DecommissioningListViewImpl;
import com.constellio.app.ui.application.NavigatorConfigurationService;
import com.constellio.app.ui.framework.buttons.DeleteButton;
import com.constellio.app.ui.framework.buttons.EditButton;
import com.constellio.app.ui.tools.ButtonWebElement;
import com.constellio.app.ui.tools.PageHelper;
import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebDriver;
import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebElement;

public class DecommissioningListPage extends PageHelper {
	private final String recordId;

	protected DecommissioningListPage(ConstellioWebDriver driver, String recordId) {
		super(driver);
		this.recordId = recordId;
	}

	public DecommissioningListPage navigateToPage() {
		driver.navigateTo().url(NavigatorConfigurationService.DECOMMISSIONING_LIST_DISPLAY + "/" + recordId);
		return this;
	}

	public ButtonWebElement getEditButton() {
		ConstellioWebElement element = driver.findRequiredElement(By.className(EditButton.BUTTON_STYLE));
		return new ButtonWebElement(element);
	}

	public ButtonWebElement getDeleteButton() {
		ConstellioWebElement element = driver.findRequiredElement(By.className(DeleteButton.BUTTON_STYLE));
		return new ButtonWebElement(element);
	}

	public ButtonWebElement getProcessButton() {
		ConstellioWebElement element = driver.findRequiredElement(By.className(DecommissioningListViewImpl.PROCESS));
		return new ButtonWebElement(element);
	}

	public ButtonWebElement getApprovalRequestButton() {
		ConstellioWebElement element = driver.findRequiredElement(By.className(DecommissioningListViewImpl.APPROVAL_REQUEST_BUTTON));
		return new ButtonWebElement(element);
	}

	public ButtonWebElement getApprovalButton() {
		ConstellioWebElement element = driver.findRequiredElement(By.className(DecommissioningListViewImpl.APPROVAL_BUTTON));
		return new ButtonWebElement(element);
	}

	public ButtonWebElement getValidationButton() {
		ConstellioWebElement element = driver.findRequiredElement(By.className(DecommissioningListViewImpl.VALIDATION_BUTTON));
		return new ButtonWebElement(element);
	}

	public ButtonWebElement getValidationRequestButton() {
		ConstellioWebElement element = driver.findRequiredElement(By.className(DecommissioningListViewImpl.VALIDATION_REQUEST_BUTTON));
		return new ButtonWebElement(element);
	}
}
