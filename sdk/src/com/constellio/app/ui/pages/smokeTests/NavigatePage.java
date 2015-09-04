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
package com.constellio.app.ui.pages.smokeTests;

import java.util.List;

import org.openqa.selenium.By;

import com.constellio.app.ui.application.NavigatorConfigurationService;
import com.constellio.app.ui.tools.PageHelper;
import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebDriver;
import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebElement;

public class NavigatePage extends PageHelper {

	public NavigatePage(ConstellioWebDriver driver) {
		super(driver);
	}

	public void navigateToRecordsManagement() {
		driver.navigateTo().url(NavigatorConfigurationService.RECORDS_MANAGEMENT);
	}

	public void navigateToArchivesManagement() {
		driver.navigateTo().url(NavigatorConfigurationService.ARCHIVES_MANAGEMENT);
	}

	public void navigateToArchivesManagementContainers() {
		driver.navigateTo().url(NavigatorConfigurationService.CONTAINERS_BY_ADMIN_UNITS);
	}

	public void navigateToArchivesManagementDecommissioning() {
		driver.navigateTo().url(NavigatorConfigurationService.REPORTS);
	}

	public void navigateToAFolder(String folderName) {
		driver.navigateTo().url(NavigatorConfigurationService.DISPLAY_FOLDER + "/" + folderName);
	}

	public void navigateToEventsList() {
		driver.navigateTo().url(NavigatorConfigurationService.EVENTS_LIST);
	}

	public void navigateToAdminModule() {
		driver.navigateTo().url(NavigatorConfigurationService.ADMIN_MODULE);
	}

	public void navigateToCollectionManagement() {
		driver.navigateTo().url(NavigatorConfigurationService.COLLECTION_ADD_EDIT);
	}

	public void navigateToPermissionManagement() {
		driver.navigateTo().url(NavigatorConfigurationService.PERMISSION_MANAGEMENT);
	}

	public ConstellioWebElement getCloseBoxButton() {
		return driver.findElement(By.className("v-window-closebox"));
	}

	public ConstellioWebElement getAddDocumentButton() {
		return driver.findElement(By.className("v-button-addDocument"));
	}

	public ConstellioWebElement getAddFolderButton() {
		return driver.findElement(By.className("v-button-addFolder"));
	}

	public ConstellioWebElement getArchivesManagementButton() {
		return driver.findElement(By.className("archivesManagement"));
	}

	public ConstellioWebElement getRecordsManagementButton() {
		return driver.findElement(By.className("recordsManagement"));
	}

	public ConstellioWebElement getLogsButton() {
		return driver.findElement(By.className("logs"));
	}

	public ConstellioWebElement getAdminModuleButton() {
		return driver.findElement(By.className("adminModule"));
	}

	public ConstellioWebElement getUserDocumentsButton() {
		return driver.findElement(By.className("userDocuments"));
	}

	public ConstellioWebElement getIntoFolder(int index) {
		ConstellioWebElement recordTableElement = driver.findElement(By.className("record-table"));
		ConstellioWebElement tableElement = recordTableElement.findElement(By.className("v-table-body"));
		List<ConstellioWebElement> listRows = tableElement.findAdaptElements(By.tagName("tr"));
		return listRows.get(index).findElement(By.className("v-table-cell-wrapper"));
	}

	public ConstellioWebElement getDeleteFolderButton() {
		return driver.findElement(By.className("deleteLogically-button"));
	}

	public ConstellioWebElement getDeletePopupCancelButton() {
		return driver.findElement(By.id("confirmdialog-cancel-button"));
	}

	public ConstellioWebElement getCreateRoleButton() {
		return driver.findElement(By.className("create-role"));
	}

	public ConstellioWebElement getCurrentPageTabMenu(int tabNumber) {
		List<ConstellioWebElement> listTabMenu = driver.findAdaptElements(By.className("v-caption"));
		return listTabMenu.get(tabNumber);
	}

	public ConstellioWebElement getCurrentPageIcons(int iconNumber) {
		List<ConstellioWebElement> listImages = driver.findAdaptElements(By.tagName("img"));
		return listImages.get(iconNumber);
	}

	public ConstellioWebElement getFolderMenuButton(int buttonNumber) {
		List<ConstellioWebElement> listButtonActionMenu = driver.findAdaptElements(By.className("action-menu-button"));
		return listButtonActionMenu.get(buttonNumber);
	}
}
