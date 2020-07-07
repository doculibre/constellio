package com.constellio.app.ui.pages.globalGroup;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.ui.application.NavigatorConfigurationService;
import com.constellio.model.entities.security.global.GlobalGroupStatus;
import com.constellio.model.services.users.UserServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.annotations.UiTest;
import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebDriver;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

@UiTest
public class GlobalGroupPageAcceptTest extends ConstellioTest {

	ConstellioWebDriver driver;
	RMSchemasRecordsServices rm;
	RMTestRecords records = new RMTestRecords(zeCollection);

	AddEditGlobalGroupPage addEditGlobalGroupPage;
	ListGlobalGroupPage listGlobalGroupPage;
	DisplayGlobalGroupPage displayGlobalGroupPage;
	UserServices userServices;

	@Before
	public void setUp()
			throws Exception {

		prepareSystem(
				withZeCollection().withConstellioRMModule().withAllTestUsers().withRMTest(records)
						.withFoldersAndContainersOfEveryStatus()
		);

		rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());

		givenCollection("otherCollection");


		driver = newWebDriver(loggedAsUserInCollection("admin", zeCollection));
		userServices = getModelLayerFactory().newUserServices();
		addEditGlobalGroupPage = new AddEditGlobalGroupPage(driver);
		listGlobalGroupPage = new ListGlobalGroupPage(driver);
		displayGlobalGroupPage = new DisplayGlobalGroupPage(driver);

	}

	//	@InDevelopmentTest
	@Test
	public void testPage()
			throws Exception {

		givenListGroupsPageWhenSearchThenOk();
		givenListGroupsPageWhenBackButtonClickedThenNavigateToAdminModule();

		givenAddPageWhenAddGlobalGroupAndCancelButtonClickedThenNavigateToListGroups();
		givenAddPageWhenAddGlobalGroupThenOk();

		givenEditPageWhenEditGlobalGroupThenOk();

		givenDisplayPageWhenSearchAndAddUserInGroupThenCanSearchHimInUserGroupTable();
		givenDisplayPageWhenClickInModifyThenNavigateToAddEditGlobalGroup();
		givenDisplayPageWhenClickInEditUserThenNavigateToAddEditUserCredential();
		givenDisplayPageWhenClickInDeleteUserFromGroupThenCanSearchHimInUserTable();
		givenDisplayPageWhenClickInBackButtonThenNavigateToListGroup();
		givenDisplayPageWhenAddSubGroupThenCanSearchIt();
		givenDisplayPageWhenEditSubGroupThenCanSearchIt();
		givenDisplayPageWhenDeleteSubGroupThenOk();
		givenDisplayPageWhenClickInDeleteGroupThenDeleteItAndNavigateToListGroup();

		givenListGroupsWhenDeleteHeroesGroupAndNoConfirmButtonClickedThenNoDeleteIt();
		givenListGroupsWhenDeleteHeroesGroupAndConfirmButtonClickedThenDeleteIt();
	}

	private void givenListGroupsPageWhenSearchThenOk()
			throws Exception {

		listGlobalGroupPage.navigateToListGlobalGroupsPage();

		assertThat(listGlobalGroupPage.getTableRows()).hasSize(2);

		listGlobalGroupPage.getSearchInput().setValue("Heroes");
		listGlobalGroupPage.getSearchButton().click();
		listGlobalGroupPage.waitForPageReload();

		assertThat(listGlobalGroupPage.getTableRows()).hasSize(1);
		assertThat(listGlobalGroupPage.getTableRows().get(0).getText()).contains("heroes");

	}

	private void givenListGroupsPageWhenBackButtonClickedThenNavigateToAdminModule()
			throws Exception {

		listGlobalGroupPage.getBackButton().click();
		listGlobalGroupPage.waitForPageReload();

		assertThat(driver.getCurrentPage()).isEqualTo(
				NavigatorConfigurationService.ADMIN_MODULE);
	}

	private void givenAddPageWhenAddGlobalGroupAndCancelButtonClickedThenNavigateToListGroups()
			throws Exception {

		listGlobalGroupPage.navigateToListGlobalGroupsPage();
		listGlobalGroupPage.waitForPageReload();
		givenAddPage();

		assertThat(addEditGlobalGroupPage.getStatusElement().isEnabled()).isFalse();

		addEditGlobalGroupPage.getCodeElement().setValue("zeGroup1");
		addEditGlobalGroupPage.getNameElement().setValue("The zeGroup 1");
		addEditGlobalGroupPage.getCollectionsElement().toggle(zeCollection);
		addEditGlobalGroupPage.getCollectionsElement().toggle("otherCollection");
		addEditGlobalGroupPage.getCancelButton().click();
		addEditGlobalGroupPage.waitForPageReload();

		assertThat(listGlobalGroupPage.getTableRows()).hasSize(2);
	}

	private void givenAddPageWhenAddGlobalGroupThenOk()
			throws Exception {

		givenAddPage();

		addEditGlobalGroupPage.getCodeElement().setValue("zeGroup1");
		addEditGlobalGroupPage.getNameElement().setValue("The zeGroup 1");
		addEditGlobalGroupPage.getCollectionsElement().toggle(zeCollection);
		addEditGlobalGroupPage.getCollectionsElement().toggle("otherCollection");
		addEditGlobalGroupPage.getSaveButton().click();
		addEditGlobalGroupPage.waitForPageReload();

		assertThat(listGlobalGroupPage.getTableRows()).hasSize(3);
		assertThat(listGlobalGroupPage.getTableRows().get(2).getText()).contains("zeGroup1");
	}

	private void givenEditPageWhenEditGlobalGroupThenOk()
			throws Exception {

		givenEditPageForIndex(0);

		addEditGlobalGroupPage.getNameElement().setValue("The heroes 1");
		addEditGlobalGroupPage.getCollectionsElement().toggle(zeCollection);
		addEditGlobalGroupPage.getCollectionsElement().toggle("otherCollection");
		addEditGlobalGroupPage.getStatusElement().toggle(GlobalGroupStatus.ACTIVE.name());
		addEditGlobalGroupPage.getSaveButton().click();
		addEditGlobalGroupPage.waitForPageReload();

		assertThat(listGlobalGroupPage.getTableRows()).hasSize(3);
		assertThat(listGlobalGroupPage.getTableRows().get(0).getText()).contains("heroes 1");
	}

	private void givenDisplayPageWhenSearchAndAddUserInGroupThenCanSearchHimInUserGroupTable()
			throws Exception {

		givenDisplayPageForIndex(0);

		displayGlobalGroupPage.getSearchInputUsers().setValue("admin");
		displayGlobalGroupPage.getSearchButtonUsers().click();
		displayGlobalGroupPage.waitForPageReload();

		assertThat(displayGlobalGroupPage.getTableRowsUsers()).hasSize(1);
		assertThat(displayGlobalGroupPage.getTableRowsUsers().get(0).getText()).contains("admin");
		assertThat(displayGlobalGroupPage.getTableRowsUsersInGroup()).hasSize(3);

		displayGlobalGroupPage.getAddButton().click();
		displayGlobalGroupPage.waitForPageReload();

		assertThat(displayGlobalGroupPage.getTableRowsUsersInGroup()).hasSize(4);

		displayGlobalGroupPage.getSearchInputUsersInGroup().setValue("admin");
		displayGlobalGroupPage.getSearchButtonUsersInGroup().click();

		assertThat(displayGlobalGroupPage.getTableRowsUsersInGroup()).hasSize(1);
		assertThat(displayGlobalGroupPage.getTableRowsUsersInGroup().get(0).getText()).contains("admin");
	}

	private void givenDisplayPageWhenClickInModifyThenNavigateToAddEditGlobalGroup() {

		givenDisplayPageForIndex(0);

		displayGlobalGroupPage.getEditGlobalGroupButtonMenuAction().click();
		displayGlobalGroupPage.waitForPageReload();

		assertThat(driver.getCurrentPage())
				.isEqualTo(NavigatorConfigurationService.GROUP_ADD_EDIT + "/groupList/groupDisplay/globalGroupCode%253Dheroes");
	}

	private void givenDisplayPageWhenClickInEditUserThenNavigateToAddEditUserCredential() {

		givenDisplayPageForIndex(0);

		displayGlobalGroupPage.getEditUserCredentialButtonOnIndex(1).click();
		displayGlobalGroupPage.waitForPageReload();

		assertThat(driver.getCurrentPage())
				.isEqualTo(NavigatorConfigurationService.USER_ADD_EDIT
						   + "/groupList/groupDisplay/globalGroupCode%253Dheroes%253Busername%253Dadmin");
	}

	public void givenDisplayPageWhenClickInDeleteUserFromGroupThenCanSearchHimInUserTable()
			throws Exception {

		givenDisplayPageForIndex(0);

		displayGlobalGroupPage.getSearchInputUsersInGroup().setValue("charles");
		displayGlobalGroupPage.getSearchButtonUsersInGroup().click();
		displayGlobalGroupPage.waitForPageReload();

		assertThat(displayGlobalGroupPage.getTableRowsUsersInGroup()).hasSize(1);
		assertThat(displayGlobalGroupPage.getTableRowsUsersInGroup().get(0).getText()).contains("charles");
		assertThat(displayGlobalGroupPage.getTableRowsUsers()).hasSize(6);

		displayGlobalGroupPage.getDeleteButtonOnIndex(1).click();
		displayGlobalGroupPage.waitForPageReload();
		displayGlobalGroupPage.getCancelConfirmationDialogButton().click();
		displayGlobalGroupPage.waitForPageReload();

		assertThat(displayGlobalGroupPage.getTableRowsUsers()).hasSize(6);

		displayGlobalGroupPage.getDeleteButtonOnIndex(1).click();
		displayGlobalGroupPage.getOkConfirmationDialogButton().click();
		displayGlobalGroupPage.waitForPageReload();
		displayGlobalGroupPage.getSearchInputUsers().setValue("charles");
		displayGlobalGroupPage.getSearchButtonUsers().click();

		assertThat(displayGlobalGroupPage.getTableRowsUsersInGroup()).hasSize(3);
		assertThat(displayGlobalGroupPage.getTableRowsUsers()).hasSize(1);
		assertThat(displayGlobalGroupPage.getTableRowsUsers().get(0).getText()).contains("charles");
	}

	private void givenDisplayPageWhenClickInBackButtonThenNavigateToListGroup() {

		givenDisplayPageForIndex(0);

		displayGlobalGroupPage.getBackButton().click();
		displayGlobalGroupPage.waitForPageReload();

		assertThat(driver.getCurrentPage())
				.isEqualTo(NavigatorConfigurationService.GROUP_LIST + "/globalGroupCode%253Dheroes");
	}

	private void givenDisplayPageWhenAddSubGroupThenCanSearchIt() {

		givenDisplayPageForIndex(0);

		displayGlobalGroupPage.getAddSubGlobalGroupButtonMenuAction().click();
		displayGlobalGroupPage.waitForPageReload();

		addEditGlobalGroupPage.getCodeElement().setValue("heroes1");
		addEditGlobalGroupPage.getNameElement().setValue("The heroes 1");
		addEditGlobalGroupPage.getCollectionsElement().toggle(zeCollection);
		addEditGlobalGroupPage.getCollectionsElement().toggle("otherCollection");
		addEditGlobalGroupPage.getSaveButton().click();
		addEditGlobalGroupPage.waitForPageReload();

		displayGlobalGroupPage.getSearchInputSubGroups().setValue("heroes1");
		displayGlobalGroupPage.getSearchButtonSubGroups().click();
		addEditGlobalGroupPage.waitForPageReload();

		assertThat(displayGlobalGroupPage.getTableRowsSubGroups()).hasSize(1);
		assertThat(displayGlobalGroupPage.getTableRowsSubGroups().get(0).getText()).contains("heroes1");
	}

	private void givenDisplayPageWhenEditSubGroupThenCanSearchIt() {

		givenDisplayPageForIndex(0);

		displayGlobalGroupPage.getEditSubGlobalGroupButtonOnIndex(0).click();
		displayGlobalGroupPage.waitForPageReload();

		addEditGlobalGroupPage.getNameElement().setValue("The heroes 1 modified");
		addEditGlobalGroupPage.getCollectionsElement().toggle(zeCollection);
		addEditGlobalGroupPage.getCollectionsElement().toggle("otherCollection");
		addEditGlobalGroupPage.getSaveButton().click();
		addEditGlobalGroupPage.waitForPageReload();

		displayGlobalGroupPage.getSearchInputSubGroups().setValue("heroes1");
		displayGlobalGroupPage.getSearchButtonSubGroups().click();
		addEditGlobalGroupPage.waitForPageReload();

		assertThat(displayGlobalGroupPage.getTableRowsSubGroups()).hasSize(1);
		assertThat(displayGlobalGroupPage.getTableRowsSubGroups().get(0).getText()).contains("The heroes 1 modified");

	}

	private void givenDisplayPageWhenDeleteSubGroupThenOk() {

		givenDisplayPageForIndex(0);

		displayGlobalGroupPage.getDeleteSubGlobalGroupButtonOnIndex(0).click();
		displayGlobalGroupPage.getOkConfirmationDialogButton().click();
		displayGlobalGroupPage.waitForPageReload();

		assertThat(userServices.getGroup("heroes1").getStatus()).isSameAs(GlobalGroupStatus.INACTIVE);
		displayGlobalGroupPage.getSearchInputSubGroups().setValue("heroes1");
		displayGlobalGroupPage.getSearchButtonSubGroups().click();
		assertThat(displayGlobalGroupPage.getTableRowsSubGroups()).isEmpty();
	}

	private void givenDisplayPageWhenClickInDeleteGroupThenDeleteItAndNavigateToListGroup() {

		givenDisplayPageForIndex(2);
		String currentPage = driver.getCurrentPage();

		displayGlobalGroupPage.getDeleteGlobalGroupButtonMenuAction().click();
		displayGlobalGroupPage.waitForPageReload();
		displayGlobalGroupPage.getCancelConfirmationDialogButton().click();
		displayGlobalGroupPage.waitForPageReload();

		assertThat(driver.getCurrentPage()).isEqualTo(currentPage);

		displayGlobalGroupPage.getDeleteGlobalGroupButtonMenuAction().click();
		displayGlobalGroupPage.waitForPageReload();
		displayGlobalGroupPage.getOkConfirmationDialogButton().click();
		displayGlobalGroupPage.waitForPageReload();

		assertThat(driver.getCurrentPage())
				.isEqualTo(NavigatorConfigurationService.GROUP_LIST);
		assertThat(listGlobalGroupPage.getTableRows()).hasSize(2);
	}

	private void givenListGroupsWhenDeleteHeroesGroupAndNoConfirmButtonClickedThenNoDeleteIt()
			throws Exception {

		listGlobalGroupPage.getDeleteButtonOnIndex(0).click();
		listGlobalGroupPage.waitForPageReload();
		listGlobalGroupPage.getCancelConfirmationDialogButton().click();
		listGlobalGroupPage.waitForPageReload();

		assertThat(listGlobalGroupPage.getTableRows()).hasSize(2);
		assertThat(listGlobalGroupPage.getTableRows().get(0).getText()).contains("heroes");
	}

	private void givenListGroupsWhenDeleteHeroesGroupAndConfirmButtonClickedThenDeleteIt()
			throws Exception {

		listGlobalGroupPage.getDeleteButtonOnIndex(0).click();
		listGlobalGroupPage.waitForPageReload();
		listGlobalGroupPage.getOkConfirmationDialogButton().click();
		listGlobalGroupPage.waitForPageReload();

		assertThat(listGlobalGroupPage.getTableRows()).hasSize(1);
		assertThat(listGlobalGroupPage.getTableRows().get(0).getText()).doesNotContain("heroes");
	}

	//

	private void givenAddPage() {
		listGlobalGroupPage.getAddButton().clickAndWaitForPageReload();
	}

	private void givenEditPageForIndex(int index) {
		listGlobalGroupPage.getEditGlobalGroupButtonOnIndex(index).clickAndWaitForPageReload();
	}

	private void givenDisplayPageForIndex(int index) {
		listGlobalGroupPage.navigateToListGlobalGroupsPage();
		listGlobalGroupPage.getDisplayGlobalGroupButtonOnIndex(index).clickAndWaitForPageReload();
	}
}
