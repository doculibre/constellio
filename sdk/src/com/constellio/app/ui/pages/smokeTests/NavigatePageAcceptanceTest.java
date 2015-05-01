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

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;

import com.constellio.app.modules.rm.DemoTestRecords;
import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.ui.application.NavigatorConfigurationService;
import com.constellio.model.services.records.RecordServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.annotations.UiTest;
import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebDriver;

@UiTest
public class NavigatePageAcceptanceTest extends ConstellioTest {

	NavigatePage page;
	RecordServices recordServices;
	ConstellioWebDriver driver;
	RMTestRecords records;
	String folderId;

	@Before
	public void setUp()
			throws Exception {

		givenCollectionWithTitle(zeCollection, "Collection de test").withConstellioRMModule().withAllTestUsers();
		givenCollectionWithTitle("LaCollectionDeRida", "Collection d'entreprise").withConstellioRMModule()
				.withAllTestUsers();

		recordServices = getModelLayerFactory().newRecordServices();

		records = new RMTestRecords(zeCollection).setup(getModelLayerFactory()).withFoldersAndContainersOfEveryStatus()
				.withEvents();
		new DemoTestRecords("LaCollectionDeRida").setup(getModelLayerFactory()).withFoldersAndContainersOfEveryStatus();

	}

	@Test
	public void allTestsInZeCollection()
			throws Exception {
		driver = newWebDriver(loggedAsUserInCollection(admin, zeCollection));
		page = new NavigatePage(driver);
		folderId = recordIdWithTitleInCollection("Chou-fleur", zeCollection);

		allTests();
	}

	@Test
	public void allTestsInCollectionDeRida()
			throws Exception {
		driver = newWebDriver(loggedAsUserInCollection(admin, "LaCollectionDeRida"));
		page = new NavigatePage(driver);
		folderId = recordIdWithTitleInCollection("Guides d'évaluations", "LaCollectionDeRida");

		allTests();
	}

	private void allTests()
			throws Exception {
		page.navigateToRecordsManagement();
		givenUserInRecordsManagementWhenClickOnAddDocumentAndClickCancelThenUserInLastViewedFolders();
		givenUserInRecordsManagementWhenClickOnAddFolderAndClickCancelThenUserInLastViewedFolder();
		givenUserInRecordsManagementWhenClickOnRecordsManagementMenuThenUserSeeTheRightView();
		givenUserInRecordsManagementWhenClickOnConstellioMenuThenUserSeeTheRightView();

		page.navigateToAFolder(folderId);
		givenUserInAFolderWhenClickOnFolderMenuAndCancelThenUserInAFolderView();

		page.navigateToArchivesManagementContainers();
		givenUserInArchivesManagementContainersWhenClickOnContainersMenuThenUserSeeTheRightView();

		page.navigateToArchivesManagement();
		givenUserInArchivesManagementWhenClickOnIconsMenuAndGoBackThenUserInArchivesManagement();

		page.navigateToEventsList();
		givenUserInEventsListWhenClickOnLogsIconsAndGoBackThenUserInEventsList();

		page.navigateToAdminModule();
		givenUserInAdminModuleWhenClickAdminModuleIconsAndGoBackThenUserInAdminModule();

		page.navigateToPermissionManagement();
		givenUserInPermissionManagementWhenClickOnCreateRoleAndCancelThenUserInPermissionManagement();

		//Test not working
		//		givenUserInArchivesManagementReportsWhenClickOnReportsIconAndCloseBoxThenUserInReports();

		page.navigateToRecordsManagement();
		givenUserInRecordsManagementWhenClickOnAFolderAndClickCancelThenUserInLastViewedFolder();
	}

	public void givenUserInRecordsManagementWhenClickOnAddDocumentAndClickCancelThenUserInLastViewedFolders()
			throws Exception {
		page.getAddDocumentButton().click();
		page.waitForPageReload();
		assertThat(driver.getCurrentPage())
				.isEqualTo(NavigatorConfigurationService.ADD_DOCUMENT);

		clickCancelAndGoBackToRecordsManagement();
	}

	public void whenNavigateToDetailsViewThenLabelsExists()
			throws Exception {
		assertThat(driver.findElement(By.className("v-label"))).isNotNull();
	}

	public void givenUserInRecordsManagementWhenClickOnAddFolderAndClickCancelThenUserInLastViewedFolder()
			throws Exception {

		page.getAddFolderButton().click();
		page.waitForPageReload();
		assertThat(driver.getCurrentPage())
				.isEqualTo(NavigatorConfigurationService.ADD_FOLDER);

		clickCancelAndGoBackToRecordsManagement();
	}

	public void givenUserInRecordsManagementWhenClickOnAFolderAndClickCancelThenUserInLastViewedFolder()
			throws Exception {
		page.navigateToRecordsManagement();

		page.getIntoFolder(0).click();
		page.waitForPageReload();

		page.getBackButton().click();
		page.waitForPageReload();
		assertThat(driver.getCurrentPage())
				.isEqualTo(NavigatorConfigurationService.RECORDS_MANAGEMENT + "/lastViewedFolders");
	}

	public void givenUserInAFolderWhenClickOnFolderMenuAndCancelThenUserInAFolderView()
			throws Exception {

		clickOnFolderMenuAndWaitForReload(0);
		whenNavigateToDetailsViewThenLabelsExists();
		assertThat(driver.getCurrentPage())
				.isEqualTo(NavigatorConfigurationService.ADD_DOCUMENT + "/parentId%253D" + folderId);
		clickCancelAndGoBackToFolderDetails();

		whenNavigateToDetailsViewThenLabelsExists();

		clickOnFolderMenuAndWaitForReload(1);
		assertThat(driver.getCurrentPage())
				.isEqualTo(NavigatorConfigurationService.ADD_FOLDER + "/parentId%253D" + folderId);
		clickCancelAndGoBackToFolderDetails();

		clickOnFolderMenuAndWaitForReload(2);
		assertThat(driver.getCurrentPage())
				.isEqualTo(NavigatorConfigurationService.EDIT_FOLDER + "/id%253D" + folderId);
		clickCancelAndGoBackToFolderDetails();

		clickOnFolderMenuAndWaitForReload(3);
		driver.findElement(By.className("cancel-deletion")).clickAndWaitForRemoval();

		assertThat(driver.getCurrentPage())
				.isEqualTo(NavigatorConfigurationService.DISPLAY_FOLDER + "/" + folderId);

		//TODO
		//clickOnFolderMenuAndWaitForReload(4);
		//page.getCloseBoxButton().click();
		//page.waitForPageReload();
		//assertThat(driver.getCurrentPage())
		//	.isEqualTo(NavigatorConfigurationService.DISPLAY_FOLDER + "/" + folderId);

		clickOnFolderMenuAndWaitForReload(5);
		assertThat(driver.getCurrentPage())
				.isEqualTo(NavigatorConfigurationService.LIST_OBJECT_AUTHORIZATIONS + "/" + folderId);//LIST_OBJECT_AUTHORIZATIONS

		page.getBackButton().click();
		page.waitForPageReload();
		assertThat(driver.getCurrentPage())
				.isEqualTo(NavigatorConfigurationService.DISPLAY_FOLDER + "/" + folderId);

		clickOnFolderMenuAndWaitForReload(6);
		clickCancelAndGoBackToFolderDetails();

		whenNavigateToDetailsViewThenLabelsExists();
	}

	public void givenUserInRecordsManagementWhenClickOnRecordsManagementMenuThenUserSeeTheRightView()
			throws Exception {

		clickOnTabMenuAndWaitForReload(2);
		assertThat(driver.getCurrentPage())
				.isEqualTo(NavigatorConfigurationService.RECORDS_MANAGEMENT + "/lastViewedDocuments");

		clickOnTabMenuAndWaitForReload(3);
		assertThat(driver.getCurrentPage())
				.isEqualTo(NavigatorConfigurationService.RECORDS_MANAGEMENT + "/taxonomies");

		clickOnTabMenuAndWaitForReload(1);
		assertThat(driver.getCurrentPage())
				.isEqualTo(NavigatorConfigurationService.RECORDS_MANAGEMENT + "/lastViewedFolders");
	}

	public void givenUserInArchivesManagementWhenClickOnIconsMenuAndGoBackThenUserInArchivesManagement()
			throws Exception {

		clickOnCurrentPageIconAndWaitForReload(2);
		assertThat(driver.getCurrentPage())
				.isEqualTo(NavigatorConfigurationService.DECOMMISSIONING);
		clickOnBackButtonAndGoBackToArchivesManagement();

		clickOnCurrentPageIconAndWaitForReload(3);
		assertThat(driver.getCurrentPage())
				.isEqualTo(NavigatorConfigurationService.CONTAINERS_BY_ADMIN_UNITS + "/transferNoStorageSpace");
		clickOnBackButtonAndGoBackToArchivesManagement();

		clickOnCurrentPageIconAndWaitForReload(4);
		assertThat(driver.getCurrentPage())
				.isEqualTo(NavigatorConfigurationService.REPORTS);
		clickOnBackButtonAndGoBackToArchivesManagement();
	}

	public void givenUserInArchivesManagementContainersWhenClickOnContainersMenuThenUserSeeTheRightView()
			throws Exception {

		clickOnTabMenuAndWaitForReload(2);
		assertThat(driver.getCurrentPage())
				.isEqualTo(NavigatorConfigurationService.CONTAINERS_BY_ADMIN_UNITS + "/depositNoStorageSpace");

		clickOnTabMenuAndWaitForReload(1);
		assertThat(driver.getCurrentPage())
				.isEqualTo(NavigatorConfigurationService.CONTAINERS_BY_ADMIN_UNITS + "/transferNoStorageSpace");

		clickOnTabMenuAndWaitForReload(3);
		assertThat(driver.getCurrentPage())
				.isEqualTo(NavigatorConfigurationService.CONTAINERS_BY_ADMIN_UNITS + "/transferWithStorageSpace");

		clickOnTabMenuAndWaitForReload(4);
		assertThat(driver.getCurrentPage())
				.isEqualTo(NavigatorConfigurationService.CONTAINERS_BY_ADMIN_UNITS + "/depositWithStorageSpace");

	}

	public void givenUserInEventsListWhenClickOnLogsIconsAndGoBackThenUserInEventsList()
			throws Exception {

		clickOnCurrentPageIconAndWaitForReload(2);
		clickOnBackButtonAndGoBackToEventsList();

		clickOnCurrentPageIconAndWaitForReload(3);
		clickOnBackButtonAndGoBackToEventsList();

		clickOnCurrentPageIconAndWaitForReload(4);
		clickOnBackButtonAndGoBackToEventsList();

		clickOnCurrentPageIconAndWaitForReload(5);
		clickOnBackButtonAndGoBackToEventsList();

		clickOnCurrentPageIconAndWaitForReload(6);
		clickOnBackButtonAndGoBackToEventsList();

		clickOnCurrentPageIconAndWaitForReload(7);
		clickOnBackButtonAndGoBackToEventsList();

		clickOnCurrentPageIconAndWaitForReload(8);
		clickOnBackButtonAndGoBackToEventsList();

		clickOnCurrentPageIconAndWaitForReload(9);
		clickOnBackButtonAndGoBackToEventsList();

		clickOnCurrentPageIconAndWaitForReload(10);
		clickOnBackButtonAndGoBackToEventsList();

		clickOnCurrentPageIconAndWaitForReload(11);
		clickOnBackButtonAndGoBackToEventsList();

		clickOnCurrentPageIconAndWaitForReload(12);
		clickOnBackButtonAndGoBackToEventsList();
	}

	public void givenUserInAdminModuleWhenClickAdminModuleIconsAndGoBackThenUserInAdminModule()
			throws Exception {

		clickOnCurrentPageIconAndWaitForReload(2);
		clickOnBackButtonAndGoBackToAdminModule();

		clickOnCurrentPageIconAndWaitForReload(3);
		clickOnBackButtonAndGoBackToAdminModule();

		clickOnCurrentPageIconAndWaitForReload(4);
		clickOnBackButtonAndGoBackToAdminModule();

		clickOnCurrentPageIconAndWaitForReload(5);
		clickOnBackButtonAndGoBackToAdminModule();

		clickOnCurrentPageIconAndWaitForReload(6);
		clickOnBackButtonAndGoBackToAdminModule();

		clickOnCurrentPageIconAndWaitForReload(7);
		clickOnBackButtonAndGoBackToAdminModule();

		clickOnCurrentPageIconAndWaitForReload(8);
		clickOnBackButtonAndGoBackToAdminModule();

		clickOnCurrentPageIconAndWaitForReload(9);
		clickOnBackButtonAndGoBackToAdminModule();

		clickOnCurrentPageIconAndWaitForReload(14);
		clickOnBackButtonAndGoBackToAdminModule();

		clickOnCurrentPageIconAndWaitForReload(15);
		clickOnBackButtonAndGoBackToAdminModule();

		clickOnCurrentPageIconAndWaitForReload(16);
		clickOnBackButtonAndGoBackToAdminModule();

		clickOnCurrentPageIconAndWaitForReload(17);
		clickOnBackButtonAndGoBackToAdminModule();
	}

	public void givenUserInPermissionManagementWhenClickOnCreateRoleAndCancelThenUserInPermissionManagement()
			throws Exception {

		page.getCreateRoleButton().click();
		page.waitForPageReload();

		page.getCancelButton().click();
		page.waitForPageReload();
		assertThat(driver.getCurrentPage())
				.isEqualTo(NavigatorConfigurationService.PERMISSION_MANAGEMENT);
	}

	public void givenUserInRecordsManagementWhenClickOnConstellioMenuThenUserSeeTheRightView()
			throws Exception {

		page.getArchivesManagementButton().click();
		page.waitForPageReload();
		assertThat(driver.getCurrentPage())
				.isEqualTo(NavigatorConfigurationService.ARCHIVES_MANAGEMENT);

		page.getLogsButton().click();
		page.waitForPageReload();
		assertThat(driver.getCurrentPage())
				.isEqualTo(NavigatorConfigurationService.EVENTS_LIST);

		page.getAdminModuleButton().click();
		page.waitForPageReload();
		assertThat(driver.getCurrentPage())
				.isEqualTo(NavigatorConfigurationService.ADMIN_MODULE);

		page.getUserDocumentsButton().click();
		page.waitForPageReload();
		assertThat(driver.getCurrentPage())
				.isEqualTo(NavigatorConfigurationService.LIST_USER_DOCUMENTS);

		page.getRecordsManagementButton().click();
		page.waitForPageReload();
		assertThat(driver.getCurrentPage())
				.isEqualTo("/lastViewedFolders");
	}

	// 	TODO : find a way to click on the popup closeBox. Currently, a new window open with the pdf instead of closing the popup.
	//	@Test
	//	public void givenUserInArchivesManagementReportsWhenClickOnReportsIconAndCloseBoxThenUserInReports()
	//			throws Exception {
	//		page.navigateToArchivesManagementDecommissioning();
	//
	//		clickOnCurrentPageIconAndWaitForReload(3);
	//
	//		page.getCloseBoxButton().();
	//		page.waitForPageReload();
	//
	//		waitUntilICloseTheBrowsers();
	//
	//				assertThat(driver.getCurrentPage())
	//						.isEqualTo(NavigatorConfigurationService.REPORTS);
	//
	//				clickOnCurrentPageIconAndWaitForReload(4);
	//
	//				page.getCloseBoxButton().();
	//				page.waitForPageReload();
	//				assertThat(driver.getCurrentPage())
	//						.isEqualTo(NavigatorConfigurationService.REPORTS);
	//
	//				clickOnCurrentPageIconAndWaitForReload(5);
	//
	//				page.getCloseBoxButton().();
	//				page.waitForPageReload();
	//				assertThat(driver.getCurrentPage())
	//						.isEqualTo(NavigatorConfigurationService.REPORTS);
	//
	//				clickOnCurrentPageIconAndWaitForReload(6);
	//
	//				page.getCloseBoxButton().();
	//				page.waitForPageReload();
	//				assertThat(driver.getCurrentPage())
	//						.isEqualTo(NavigatorConfigurationService.REPORTS);
	//
	//				clickOnCurrentPageIconAndWaitForReload(7);
	//
	//				page.getCloseBoxButton().();
	//				page.waitForPageReload();
	//				assertThat(driver.getCurrentPage())
	//						.isEqualTo(NavigatorConfigurationService.REPORTS);
	//
	//				clickOnCurrentPageIconAndWaitForReload(2);
	//
	//				page.getCloseBoxButton().();
	//				page.waitForPageReload();
	//				assertThat(driver.getCurrentPage())
	//						.isEqualTo(NavigatorConfigurationService.REPORTS);
	//	}

	private void clickCancelAndGoBackToRecordsManagement() {
		page.getCancelButton().click();
		page.waitForPageReload();
		assertThat(driver.getCurrentPage())
				.isEqualTo(NavigatorConfigurationService.RECORDS_MANAGEMENT + "/lastViewedFolders");
	}

	private void clickCancelAndGoBackToFolderDetails() {
		page.getCancelButton().click();
		page.waitForPageReload();
		assertThat(driver.getCurrentPage())
				.isEqualTo(NavigatorConfigurationService.DISPLAY_FOLDER + "/" + folderId);
	}

	private void clickOnBackButtonAndGoBackToArchivesManagement() {
		page.getBackButton().click();
		page.waitForPageReload();
		assertThat(driver.getCurrentPage())
				.isEqualTo(NavigatorConfigurationService.ARCHIVES_MANAGEMENT);
	}

	private void clickOnBackButtonAndGoBackToEventsList() {
		page.getBackButton().click();
		page.waitForPageReload();
		assertThat(driver.getCurrentPage())
				.isEqualTo(NavigatorConfigurationService.EVENTS_LIST);
	}

	private void clickOnBackButtonAndGoBackToAdminModule() {
		page.getBackButton().click();
		page.waitForPageReload();
		assertThat(driver.getCurrentPage())
				.isEqualTo(NavigatorConfigurationService.ADMIN_MODULE);
	}

	private void clickOnCurrentPageIconAndWaitForReload(int indexIcon) {
		page.getCurrentPageIcons(indexIcon).click();
		page.waitForPageReload();
	}

	private void clickOnTabMenuAndWaitForReload(int indexTabMenu) {
		page.getCurrentPageTabMenu(indexTabMenu).click();
		page.waitForPageReload();
	}

	private void clickOnFolderMenuAndWaitForReload(int indexMenu) {
		page.getFolderMenuButton(indexMenu).click();
		page.waitForPageReload();
	}

}
