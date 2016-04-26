package com.constellio.app.ui.pages.smokeTests;

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
import com.constellio.app.modules.rm.navigation.RMNavigationConfiguration;
import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.services.migrations.CoreNavigationConfiguration;
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
	RMTestRecords records = new RMTestRecords(zeCollection);
	String folderId;
	DemoTestRecords records2 = new DemoTestRecords("LaCollectionDeRida");

	@Before
	public void setUp()
			throws Exception {

		prepareSystem(
				withZeCollection().withConstellioRMModule().withAllTestUsers().withRMTest(
						records).withFoldersAndContainersOfEveryStatus().withEvents(),
				withCollection("LaCollectionDeRida").withConstellioRMModule().withAllTestUsers().withRMTest(records2)
						.withFoldersAndContainersOfEveryStatus()
		);
		inCollection("LaCollectionDeRida").setCollectionTitleTo("Collection d'entreprise");
		inCollection(zeCollection).setCollectionTitleTo("Collection de test");

		recordServices = getModelLayerFactory().newRecordServices();

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

		/* Index out of bound line 414
		page.navigateToCollectionManagement();
		givenUserWhenCreateCollectionThenUserHaveAllAccessOnCollection();*/
	}

	public void givenUserInRecordsManagementWhenClickOnAddDocumentAndClickCancelThenUserInLastViewedFolders()
			throws Exception {
		page.getAddDocumentButton().clickAndWaitForPageReload();
		assertThat(driver.getCurrentPage())
				.isEqualTo(RMNavigationConfiguration.ADD_DOCUMENT);

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
				.isEqualTo(RMNavigationConfiguration.ADD_FOLDER);

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
				.isEqualTo(RMNavigationConfiguration.ADD_DOCUMENT + "/parentId%253D" + folderId);
		clickCancelAndGoBackToFolderDetails();

		whenNavigateToDetailsViewThenLabelsExists();

		clickOnFolderMenuAndWaitForReload(1);
		assertThat(driver.getCurrentPage())
				.isEqualTo(RMNavigationConfiguration.ADD_FOLDER + "/parentId%253D" + folderId);
		clickCancelAndGoBackToFolderDetails();

		clickOnFolderMenuAndWaitForReload(2);
		assertThat(driver.getCurrentPage())
				.isEqualTo(RMNavigationConfiguration.EDIT_FOLDER + "/id%253D" + folderId);
		clickCancelAndGoBackToFolderDetails();

		clickOnFolderMenuAndWaitForReload(3);
		driver.findElement(By.className("cancel-deletion")).clickAndWaitForRemoval();

		assertThat(driver.getCurrentPage())
				.isEqualTo(RMNavigationConfiguration.DISPLAY_FOLDER + "/" + folderId);

		//TODO
		//clickOnFolderMenuAndWaitForReload(4);
		//page.getCloseBoxButton().click();
		//page.waitForPageReload();
		//assertThat(driver.getCurrentPage())
		//	.isEqualTo(NavigatorConfigurationService.DISPLAY_FOLDER + "/" + folderId);

		clickOnFolderMenuAndWaitForReload(5);
		assertThat(driver.getCurrentPage())
				.isEqualTo(NavigatorConfigurationService.LIST_OBJECT_ACCESS_AUTHORIZATIONS + "/"
						+ folderId);//LIST_OBJECT_ACCESS_AUTHORIZATIONS

		page.getBackButton().clickAndWaitForPageReload();
		assertThat(driver.getCurrentPage())
				.isEqualTo(RMNavigationConfiguration.DISPLAY_FOLDER + "/" + folderId);

		clickOnFolderMenuAndWaitForReload(6);
		clickCancelAndGoBackToFolderDetails();

		whenNavigateToDetailsViewThenLabelsExists();
	}

	public void givenUserInRecordsManagementWhenClickOnRecordsManagementMenuThenUserSeeTheRightView()
			throws Exception {

		clickOnTabMenuAndWaitForReload(2);
		assertThat(driver.getCurrentPage())
				.isEqualTo(NavigatorConfigurationService.RECORDS_MANAGEMENT + "/lastViewedDocuments");

		clickOnTabMenuAndWaitForReload(4);
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
				.isEqualTo(RMNavigationConfiguration.DECOMMISSIONING);
		clickOnBackButtonAndGoBackToArchivesManagement();

		clickOnCurrentPageIconAndWaitForReload(4);
		assertThat(driver.getCurrentPage())
				.isEqualTo(RMNavigationConfiguration.CONTAINERS_BY_ADMIN_UNITS + "/transferNoStorageSpace");
		clickOnBackButtonAndGoBackToArchivesManagement();

		clickOnCurrentPageIconAndWaitForReload(5);
		assertThat(driver.getCurrentPage())
				.isEqualTo(RMNavigationConfiguration.REPORTS);
		clickOnBackButtonAndGoBackToArchivesManagement();
	}

	public void givenUserInArchivesManagementContainersWhenClickOnContainersMenuThenUserSeeTheRightView()
			throws Exception {

		clickOnTabMenuAndWaitForReload(2);
		assertThat(driver.getCurrentPage())
				.isEqualTo(RMNavigationConfiguration.CONTAINERS_BY_ADMIN_UNITS + "/depositNoStorageSpace");

		clickOnTabMenuAndWaitForReload(1);
		assertThat(driver.getCurrentPage())
				.isEqualTo(RMNavigationConfiguration.CONTAINERS_BY_ADMIN_UNITS + "/transferNoStorageSpace");

		clickOnTabMenuAndWaitForReload(3);
		assertThat(driver.getCurrentPage())
				.isEqualTo(RMNavigationConfiguration.CONTAINERS_BY_ADMIN_UNITS + "/transferWithStorageSpace");

		clickOnTabMenuAndWaitForReload(4);
		assertThat(driver.getCurrentPage())
				.isEqualTo(RMNavigationConfiguration.CONTAINERS_BY_ADMIN_UNITS + "/depositWithStorageSpace");

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

		clickOnCurrentPageIconAndWaitForReload(CoreNavigationConfiguration.TAXONOMIES);
		clickOnBackButtonAndGoBackToAdminModule();

		clickOnCurrentPageIconAndWaitForReload(RMNavigationConfiguration.UNIFORM_SUBDIVISIONS);
		clickOnBackButtonAndGoBackToAdminModule();

		clickOnCurrentPageIconAndWaitForReload(RMNavigationConfiguration.RETENTION_CALENDAR);
		clickOnBackButtonAndGoBackToAdminModule();

		clickOnCurrentPageIconAndWaitForReload(CoreNavigationConfiguration.VALUE_DOMAINS);
		clickOnBackButtonAndGoBackToAdminModule();

		clickOnCurrentPageIconAndWaitForReload(CoreNavigationConfiguration.METADATA_SCHEMAS);
		clickOnBackButtonAndGoBackToAdminModule();

		clickOnCurrentPageIconAndWaitForReload(CoreNavigationConfiguration.SECURITY);
		clickOnBackButtonAndGoBackToAdminModule();

		clickOnCurrentPageIconAndWaitForReload(CoreNavigationConfiguration.ROLES);
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

		clickOnCurrentPageIconAndWaitForReload(CoreNavigationConfiguration.CONFIG);
		clickOnBackButtonAndGoBackToAdminModule();

		clickOnCurrentPageIconAndWaitForReload(CoreNavigationConfiguration.LDAP_CONFIG);
		clickOnBackButtonAndGoBackToAdminModule();

		clickOnCurrentPageIconAndWaitForReload(CoreNavigationConfiguration.GROUPS);
		clickOnBackButtonAndGoBackToAdminModule();

		clickOnCurrentPageIconAndWaitForReload(CoreNavigationConfiguration.USERS);
		clickOnBackButtonAndGoBackToAdminModule();

		clickOnCurrentPageIconAndWaitForReload(CoreNavigationConfiguration.COLLECTIONS);
		clickOnBackButtonAndGoBackToAdminModule();

		/* Not implemented yet
		clickOnCurrentPageIconAndWaitForReload(MODULES_BUTTON);
		clickOnBackButtonAndGoBackToAdminModule();*/

		clickOnCurrentPageIconAndWaitForReload(CoreNavigationConfiguration.IMPORT_RECORDS);
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
				.isEqualTo(RMNavigationConfiguration.ARCHIVES_MANAGEMENT);

		page.getLogsButton().clickAndWaitForPageReload();
		assertThat(driver.getCurrentPage())
				.isEqualTo(NavigatorConfigurationService.EVENTS_LIST);

		page.getAdminModuleButton().clickAndWaitForPageReload();
		assertThat(driver.getCurrentPage())
				.isEqualTo(NavigatorConfigurationService.ADMIN_MODULE);

		page.getUserDocumentsButton().clickAndWaitForPageReload();
		assertThat(driver.getCurrentPage())
				.isEqualTo(RMNavigationConfiguration.LIST_USER_DOCUMENTS);

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

	public void givenUserInArchivesManagementReportsWhenClickOnReportsIconAndCloseBoxThenUserInReports()
			throws Exception {
		clickOnCurrentPageIconAndWaitForReload(3);

		page.getCloseBoxButton().clickAndWaitForPageReload();
		assertThat(driver.getCurrentPage())
				.isEqualTo(RMNavigationConfiguration.REPORTS);

		clickOnCurrentPageIconAndWaitForReload(4);

		page.getCloseBoxButton().clickAndWaitForPageReload();
		assertThat(driver.getCurrentPage())
				.isEqualTo(RMNavigationConfiguration.REPORTS);

		clickOnCurrentPageIconAndWaitForReload(5);

		page.getCloseBoxButton().clickAndWaitForPageReload();
		assertThat(driver.getCurrentPage())
				.isEqualTo(RMNavigationConfiguration.REPORTS);

		clickOnCurrentPageIconAndWaitForReload(6);

		page.getCloseBoxButton().clickAndWaitForPageReload();
		assertThat(driver.getCurrentPage())
				.isEqualTo(RMNavigationConfiguration.REPORTS);

		clickOnCurrentPageIconAndWaitForReload(7);

		page.getCloseBoxButton().clickAndWaitForPageReload();
		assertThat(driver.getCurrentPage())
				.isEqualTo(RMNavigationConfiguration.REPORTS);

		clickOnCurrentPageIconAndWaitForReload(8);

		page.getCloseBoxButton().clickAndWaitForPageReload();
		assertThat(driver.getCurrentPage())
				.isEqualTo(RMNavigationConfiguration.REPORTS);
	}

	private void clickCancelAndGoBackToRecordsManagement() {
		page.getCancelButton().clickAndWaitForPageReload();
		assertThat(driver.getCurrentPage())
				.isEqualTo(NavigatorConfigurationService.RECORDS_MANAGEMENT + "/lastViewedFolders");
	}

	private void clickCancelAndGoBackToFolderDetails() {
		page.getCancelButton().clickAndWaitForPageReload();
		assertThat(driver.getCurrentPage())
				.isEqualTo(RMNavigationConfiguration.DISPLAY_FOLDER + "/" + folderId);
	}

	private void clickOnBackButtonAndGoBackToArchivesManagement() {
		page.getBackButton().clickAndWaitForPageReload();
		assertThat(driver.getCurrentPage())
				.isEqualTo(RMNavigationConfiguration.ARCHIVES_MANAGEMENT);
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
