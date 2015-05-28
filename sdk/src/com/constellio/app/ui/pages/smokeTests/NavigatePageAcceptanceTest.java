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

import static com.constellio.app.modules.rm.ui.pages.management.AdminRMModuleViewImpl.CONFIG_BUTTON;
import static com.constellio.app.modules.rm.ui.pages.management.AdminRMModuleViewImpl.FILING_SPACES_BUTTON;
import static com.constellio.app.modules.rm.ui.pages.management.AdminRMModuleViewImpl.IMPORT_BUTTON;
import static com.constellio.app.modules.rm.ui.pages.management.AdminRMModuleViewImpl.LDAP_BUTTON;
import static com.constellio.app.modules.rm.ui.pages.management.AdminRMModuleViewImpl.MANAGE_COLLECTIONS_BUTTON;
import static com.constellio.app.modules.rm.ui.pages.management.AdminRMModuleViewImpl.MANAGE_GROUPS_BUTTON;
import static com.constellio.app.modules.rm.ui.pages.management.AdminRMModuleViewImpl.MANAGE_ROLES_BUTTON;
import static com.constellio.app.modules.rm.ui.pages.management.AdminRMModuleViewImpl.MANAGE_USERS_BUTTON;
import static com.constellio.app.modules.rm.ui.pages.management.AdminRMModuleViewImpl.MANAGE_USER_CREDENTIAL_BUTTON;
import static com.constellio.app.modules.rm.ui.pages.management.AdminRMModuleViewImpl.METADATA_SCHEMAS_BUTTON;
import static com.constellio.app.modules.rm.ui.pages.management.AdminRMModuleViewImpl.RETENTION_CALENDAR_BUTTON;
import static com.constellio.app.modules.rm.ui.pages.management.AdminRMModuleViewImpl.TAXONOMIES_BUTTON;
import static com.constellio.app.modules.rm.ui.pages.management.AdminRMModuleViewImpl.UNIFORM_SUBDIVISIONS_BUTTON;
import static com.constellio.app.modules.rm.ui.pages.management.AdminRMModuleViewImpl.VALUE_DOMAIN_BUTTON;
import static com.constellio.app.ui.pages.events.EventCategoriesViewImpl.BORROWED_DOCUMENTS_LINK_BUTTON;
import static com.constellio.app.ui.pages.events.EventCategoriesViewImpl.BY_FOLDER_EVENTS_LINK_BUTTON;
import static com.constellio.app.ui.pages.events.EventCategoriesViewImpl.BY_USER_EVENTS_LINK_BUTTON;
import static com.constellio.app.ui.pages.events.EventCategoriesViewImpl.CURRENTLY_BORROWED_DOCUMENTS_LINK_BUTTON;
import static com.constellio.app.ui.pages.events.EventCategoriesViewImpl.DECOMMISSIONING_EVENTS_LINK_BUTTON;
import static com.constellio.app.ui.pages.events.EventCategoriesViewImpl.FILING_SPACE_EVENTS_LINK_BUTTON;
import static com.constellio.app.ui.pages.events.EventCategoriesViewImpl.RECORDS_CREATION_LINK_BUTTON;
import static com.constellio.app.ui.pages.events.EventCategoriesViewImpl.RECORDS_DELETION_LINK_BUTTON;
import static com.constellio.app.ui.pages.events.EventCategoriesViewImpl.RECORDS_MODIFICATION_LINK_BUTTON;
import static com.constellio.app.ui.pages.events.EventCategoriesViewImpl.SYSTEM_USAGE_LINK_BUTTON;
import static com.constellio.app.ui.pages.events.EventCategoriesViewImpl.USERS_AND_GROUPS_LINK_BUTTON;
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

		page.navigateToArchivesManagementDecommissioning();
		givenUserInArchivesManagementReportsWhenClickOnReportsIconAndCloseBoxThenUserInReports();

		page.navigateToRecordsManagement();
		givenUserInRecordsManagementWhenClickOnAFolderAndClickCancelThenUserInLastViewedFolder();

		page.navigateToCollectionManagement();
		givenUserWhenCreateCollectionThenUserHaveAllAccessOnCollection();
	}

	public void givenUserInRecordsManagementWhenClickOnAddDocumentAndClickCancelThenUserInLastViewedFolders()
			throws Exception {
		page.getAddDocumentButton().clickAndWaitForPageReload();
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

		page.getAddFolderButton().clickAndWaitForPageReload();
		assertThat(driver.getCurrentPage())
				.isEqualTo(NavigatorConfigurationService.ADD_FOLDER);

		clickCancelAndGoBackToRecordsManagement();
	}

	public void givenUserInRecordsManagementWhenClickOnAFolderAndClickCancelThenUserInLastViewedFolder()
			throws Exception {
		page.navigateToRecordsManagement();

		page.getIntoFolder(0).clickAndWaitForPageReload();

		page.getBackButton().clickAndWaitForPageReload();
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

		page.getBackButton().clickAndWaitForPageReload();
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

		clickOnCurrentPageIconAndWaitForReload(SYSTEM_USAGE_LINK_BUTTON);
		clickOnBackButtonAndGoBackToEventsList();

		clickOnCurrentPageIconAndWaitForReload(USERS_AND_GROUPS_LINK_BUTTON);
		clickOnBackButtonAndGoBackToEventsList();

		clickOnCurrentPageIconAndWaitForReload(RECORDS_CREATION_LINK_BUTTON);
		clickOnBackButtonAndGoBackToEventsList();

		clickOnCurrentPageIconAndWaitForReload(RECORDS_MODIFICATION_LINK_BUTTON);
		clickOnBackButtonAndGoBackToEventsList();

		clickOnCurrentPageIconAndWaitForReload(RECORDS_DELETION_LINK_BUTTON);
		clickOnBackButtonAndGoBackToEventsList();

		clickOnCurrentPageIconAndWaitForReload(CURRENTLY_BORROWED_DOCUMENTS_LINK_BUTTON);
		clickOnBackButtonAndGoBackToEventsList();

		clickOnCurrentPageIconAndWaitForReload(BORROWED_DOCUMENTS_LINK_BUTTON);
		clickOnBackButtonAndGoBackToEventsList();

		clickOnCurrentPageIconAndWaitForReload(FILING_SPACE_EVENTS_LINK_BUTTON);
		clickOnBackButtonAndGoBackToEventsList();

		clickOnCurrentPageIconAndWaitForReload(BY_FOLDER_EVENTS_LINK_BUTTON);
		clickOnBackButtonAndGoBackToEventsList();

		clickOnCurrentPageIconAndWaitForReload(BY_USER_EVENTS_LINK_BUTTON);
		clickOnBackButtonAndGoBackToEventsList();

		clickOnCurrentPageIconAndWaitForReload(DECOMMISSIONING_EVENTS_LINK_BUTTON);
		clickOnBackButtonAndGoBackToEventsList();
	}

	public void givenUserInAdminModuleWhenClickAdminModuleIconsAndGoBackThenUserInAdminModule()
			throws Exception {

		clickOnCurrentPageIconAndWaitForReload(TAXONOMIES_BUTTON);
		clickOnBackButtonAndGoBackToAdminModule();

		clickOnCurrentPageIconAndWaitForReload(UNIFORM_SUBDIVISIONS_BUTTON);
		clickOnBackButtonAndGoBackToAdminModule();

		clickOnCurrentPageIconAndWaitForReload(RETENTION_CALENDAR_BUTTON);
		clickOnBackButtonAndGoBackToAdminModule();

		clickOnCurrentPageIconAndWaitForReload(VALUE_DOMAIN_BUTTON);
		clickOnBackButtonAndGoBackToAdminModule();

		clickOnCurrentPageIconAndWaitForReload(METADATA_SCHEMAS_BUTTON);
		clickOnBackButtonAndGoBackToAdminModule();

		clickOnCurrentPageIconAndWaitForReload(FILING_SPACES_BUTTON);
		clickOnBackButtonAndGoBackToAdminModule();

		clickOnCurrentPageIconAndWaitForReload(MANAGE_USERS_BUTTON);
		clickOnBackButtonAndGoBackToAdminModule();

		clickOnCurrentPageIconAndWaitForReload(MANAGE_ROLES_BUTTON);
		clickOnBackButtonAndGoBackToAdminModule();

		/* Not implemented yet
		clickOnCurrentPageIconAndWaitForReload(DATA_EXTRACTOR_BUTTON);
		clickOnBackButtonAndGoBackToAdminModule();

		clickOnCurrentPageIconAndWaitForReload(CONNECTORS_BUTTON);
		clickOnBackButtonAndGoBackToAdminModule();

		clickOnCurrentPageIconAndWaitForReload(SEARCH_ENGINE_BUTTON);
		clickOnBackButtonAndGoBackToAdminModule();

		clickOnCurrentPageIconAndWaitForReload(TRASH_BIN_BUTTON);
		clickOnBackButtonAndGoBackToAdminModule();*/

		clickOnCurrentPageIconAndWaitForReload(CONFIG_BUTTON);
		clickOnBackButtonAndGoBackToAdminModule();

		clickOnCurrentPageIconAndWaitForReload(LDAP_BUTTON);
		clickOnBackButtonAndGoBackToAdminModule();

		clickOnCurrentPageIconAndWaitForReload(MANAGE_GROUPS_BUTTON);
		clickOnBackButtonAndGoBackToAdminModule();

		clickOnCurrentPageIconAndWaitForReload(MANAGE_USER_CREDENTIAL_BUTTON);
		clickOnBackButtonAndGoBackToAdminModule();

		clickOnCurrentPageIconAndWaitForReload(MANAGE_COLLECTIONS_BUTTON);
		clickOnBackButtonAndGoBackToAdminModule();

		/* Not implemented yet
		clickOnCurrentPageIconAndWaitForReload(MODULES_BUTTON);
		clickOnBackButtonAndGoBackToAdminModule();*/

		clickOnCurrentPageIconAndWaitForReload(IMPORT_BUTTON);
		clickOnBackButtonAndGoBackToAdminModule();

		/* Not implemented yet
		clickOnCurrentPageIconAndWaitForReload(BIG_DATA_BUTTON);
		clickOnBackButtonAndGoBackToAdminModule();

		// Back Button not implemented
		clickOnCurrentPageIconAndWaitForReload(UPDATE_CENTER_BUTTON);
		clickOnBackButtonAndGoBackToAdminModule();*/
	}

	public void givenUserInPermissionManagementWhenClickOnCreateRoleAndCancelThenUserInPermissionManagement()
			throws Exception {

		page.getCreateRoleButton().clickAndWaitForPageReload();

		page.getCancelButton().clickAndWaitForPageReload();
		assertThat(driver.getCurrentPage())
				.isEqualTo(NavigatorConfigurationService.PERMISSION_MANAGEMENT);
	}

	public void givenUserInRecordsManagementWhenClickOnConstellioMenuThenUserSeeTheRightView()
			throws Exception {

		page.getArchivesManagementButton().clickAndWaitForPageReload();
		assertThat(driver.getCurrentPage())
				.isEqualTo(NavigatorConfigurationService.ARCHIVES_MANAGEMENT);

		page.getLogsButton().clickAndWaitForPageReload();
		assertThat(driver.getCurrentPage())
				.isEqualTo(NavigatorConfigurationService.EVENTS_LIST);

		page.getAdminModuleButton().clickAndWaitForPageReload();
		assertThat(driver.getCurrentPage())
				.isEqualTo(NavigatorConfigurationService.ADMIN_MODULE);

		page.getUserDocumentsButton().clickAndWaitForPageReload();
		assertThat(driver.getCurrentPage())
				.isEqualTo(NavigatorConfigurationService.LIST_USER_DOCUMENTS);

		page.getRecordsManagementButton().clickAndWaitForPageReload();
		assertThat(driver.getCurrentPage())
				.isEqualTo("/lastViewedFolders");
	}

	public void givenUserWhenCreateCollectionThenUserHaveAllAccessOnCollection()
			throws Exception {
		page.waitForPageReload();
		assertThat(driver.getCurrentPage()).isEqualTo(NavigatorConfigurationService.COLLECTION_ADD_EDIT);

		page.getTextFieldWebElementById("code").setValue("test");
		page.getTextFieldWebElementById("name").setValue("Collection de test");

		page.getButtonByClassName("base-form-save").clickAndWaitForPageReload();

		page.logout();
		page.waitForPageReload();

		assertThat(driver.getCurrentPage()).isEqualTo("listCollections");

		driver = newWebDriver(loggedAsUserInCollection("admin", "test"));

		assertThat(driver.getCurrentPage()).isEqualTo("/lastViewedFolders");
	}

	// 	TODO : find a way to click on the popup closeBox. Currently, a new window open with the pdf instead of closing the popup.
	public void givenUserInArchivesManagementReportsWhenClickOnReportsIconAndCloseBoxThenUserInReports()
			throws Exception {
		clickOnCurrentPageIconAndWaitForReload(3);

		page.getCloseBoxButton().clickAndWaitForPageReload();
		assertThat(driver.getCurrentPage())
				.isEqualTo(NavigatorConfigurationService.REPORTS);

		clickOnCurrentPageIconAndWaitForReload(4);

		page.getCloseBoxButton().clickAndWaitForPageReload();
		assertThat(driver.getCurrentPage())
				.isEqualTo(NavigatorConfigurationService.REPORTS);

		clickOnCurrentPageIconAndWaitForReload(5);

		page.getCloseBoxButton().clickAndWaitForPageReload();
		assertThat(driver.getCurrentPage())
				.isEqualTo(NavigatorConfigurationService.REPORTS);

		clickOnCurrentPageIconAndWaitForReload(6);

		page.getCloseBoxButton().clickAndWaitForPageReload();
		assertThat(driver.getCurrentPage())
				.isEqualTo(NavigatorConfigurationService.REPORTS);

		clickOnCurrentPageIconAndWaitForReload(7);

		page.getCloseBoxButton().clickAndWaitForPageReload();
		assertThat(driver.getCurrentPage())
				.isEqualTo(NavigatorConfigurationService.REPORTS);

		clickOnCurrentPageIconAndWaitForReload(8);

		page.getCloseBoxButton().clickAndWaitForPageReload();
		assertThat(driver.getCurrentPage())
				.isEqualTo(NavigatorConfigurationService.REPORTS);
	}

	private void clickCancelAndGoBackToRecordsManagement() {
		page.getCancelButton().clickAndWaitForPageReload();
		assertThat(driver.getCurrentPage())
				.isEqualTo(NavigatorConfigurationService.RECORDS_MANAGEMENT + "/lastViewedFolders");
	}

	private void clickCancelAndGoBackToFolderDetails() {
		page.getCancelButton().clickAndWaitForPageReload();
		assertThat(driver.getCurrentPage())
				.isEqualTo(NavigatorConfigurationService.DISPLAY_FOLDER + "/" + folderId);
	}

	private void clickOnBackButtonAndGoBackToArchivesManagement() {
		page.getBackButton().clickAndWaitForPageReload();
		assertThat(driver.getCurrentPage())
				.isEqualTo(NavigatorConfigurationService.ARCHIVES_MANAGEMENT);
	}

	private void clickOnBackButtonAndGoBackToEventsList() {
		page.getBackButton().clickAndWaitForPageReload();
		assertThat(driver.getCurrentPage())
				.isEqualTo(NavigatorConfigurationService.EVENTS_LIST);
	}

	private void clickOnBackButtonAndGoBackToAdminModule() {
		page.getBackButton().clickAndWaitForPageReload();
		assertThat(driver.getCurrentPage())
				.isEqualTo(NavigatorConfigurationService.ADMIN_MODULE);
	}

	private void clickOnCurrentPageIconAndWaitForReload(String classIcon) {
		page.getFirstButtonByClassName(classIcon).clickAndWaitForPageReload();
	}

	private void clickOnCurrentPageIconAndWaitForReload(int indexIcon) {
		page.getCurrentPageIcons(indexIcon).clickAndWaitForPageReload();
	}

	private void clickOnTabMenuAndWaitForReload(int indexTabMenu) {
		page.getCurrentPageTabMenu(indexTabMenu).clickAndWaitForPageReload();
	}

	private void clickOnFolderMenuAndWaitForReload(int indexMenu) {
		page.getFolderMenuButton(indexMenu).clickAndWaitForPageReload();
	}

}
