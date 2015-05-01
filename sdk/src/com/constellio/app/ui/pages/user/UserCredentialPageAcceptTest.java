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
package com.constellio.app.ui.pages.user;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.ui.application.NavigatorConfigurationService;
import com.constellio.model.services.users.UserServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.annotations.UiTest;
import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebDriver;

@UiTest
public class UserCredentialPageAcceptTest extends ConstellioTest {
	ConstellioWebDriver driver;
	RMSchemasRecordsServices schemas;
	RMTestRecords rm = new RMTestRecords(zeCollection);

	AddEditUserCredentialPage addEditUserCredentialPage;
	ListUserCredentialPage listUserCredentialPage;
	DisplayUserCredentialPage displayUserCredentialPage;
	UserServices userServices;

	@Before
	public void setUp()
			throws Exception {
		schemas = new RMSchemasRecordsServices(zeCollection, getModelLayerFactory());

		givenCollection(zeCollection).withConstellioRMModule().withAllTestUsers();
		givenCollection("otherCollection");

		rm.setup(getModelLayerFactory()).withFoldersAndContainersOfEveryStatus();
		userServices = getModelLayerFactory().newUserServices();

		driver = newWebDriver(loggedAsUserInCollection("admin", zeCollection));

		addEditUserCredentialPage = new AddEditUserCredentialPage(driver);
		listUserCredentialPage = new ListUserCredentialPage(driver);
		displayUserCredentialPage = new DisplayUserCredentialPage(driver);

		listUserCredentialPage.navigateToListUserCredentialsPage();
	}

	@Test
	public void testPage()
			throws Exception {

		givenListUserCredentialsPageWhenSearchThenOk();
		givenListUserCredentialsPageWhenBackButtonClickedThenNavigateToAdminModule();

		givenAddPageWhenAddUserCredentialAndCancelButtonClickedThenNavigateToListUserCredentials();
		givenAddPageWhenAddUserCredentialWithDifferentsPasswordAndConfirmPasswordAndClickSaveThenStayInAddAddPage();
		givenAddPageWhenAddUserCredentialThenOk();
		givenEditPageWhenEditUserCredentialThenOk();

		givenDisplayPageWhenSearchAndAddGroupThenCanSearchItInUsersGroupsTable();
		givenDisplayPageWhenClickInModifyThenNavigateToAddEditUserCredential();
		givenDisplayPageWhenClickInEditGroupThenNavigateToAddEditGlobalGroup();
		givenDisplayPageWhenClickInRemoveGroupFromUserThenCanSearchItInGroupsTable();
		givenDisplayPageWhenClickInBackButtonThenNavigateToListUserCredentials();

		//		givenEditPageWhenEditUserCredentialWithWrongOldPasswordThenNotUpdated();
	}

	private void givenListUserCredentialsPageWhenSearchThenOk()
			throws Exception {
		assertThat(listUserCredentialPage.getTableRows()).hasSize(10);

		listUserCredentialPage.getSearchInput().setValue("Chuck");
		listUserCredentialPage.getSearchButton().clickAndWaitForPageReload();

		assertThat(listUserCredentialPage.getTableRows()).hasSize(1);
		assertThat(listUserCredentialPage.getTableRows().get(0).getText()).contains("Chuck");

	}

	private void givenListUserCredentialsPageWhenBackButtonClickedThenNavigateToAdminModule()
			throws Exception {
		listUserCredentialPage.getBackButton().clickAndWaitForPageReload();
		assertThat(driver.getCurrentPage()).isEqualTo(
				NavigatorConfigurationService.ADMIN_MODULE);
	}

	private void givenAddPageWhenAddUserCredentialAndCancelButtonClickedThenNavigateToListUserCredentials()
			throws Exception {

		listUserCredentialPage.navigateToListUserCredentialsPage();
		givenAddPage();

		addEditUserCredentialPage.getUsernameElement().setValue("zeEdouard");
		addEditUserCredentialPage.getFirstNameElement().setValue("zeEdouard");
		addEditUserCredentialPage.getLastNameElement().setValue("Lechat");
		addEditUserCredentialPage.getEmailElement().setValue("zeEdouard@constellio.com");
		addEditUserCredentialPage.getPasswordElement().setValue("1qaz2wsx");
		addEditUserCredentialPage.getConfirmPasswordElement().setValue("1qaz2wsx");
		addEditUserCredentialPage.getCollectionsElement().toggle(zeCollection);
		addEditUserCredentialPage.getCollectionsElement().toggle("otherCollection");
		addEditUserCredentialPage.getCancelButton().clickAndWaitForPageReload();

		assertThat(listUserCredentialPage.getTableRows()).hasSize(10);
	}

	private void givenAddPageWhenAddUserCredentialWithDifferentsPasswordAndConfirmPasswordAndClickSaveThenStayInAddAddPage()
			throws Exception {

		givenAddPage();

		addEditUserCredentialPage.getUsernameElement().setValue("zeEdouard");
		addEditUserCredentialPage.getFirstNameElement().setValue("zeEdouard");
		addEditUserCredentialPage.getLastNameElement().setValue("Lechat");
		addEditUserCredentialPage.getEmailElement().setValue("zeEdouard@constellio.com");
		addEditUserCredentialPage.getPasswordElement().setValue("1qaz2wsx");
		addEditUserCredentialPage.getConfirmPasswordElement().setValue("aaaaaa");
		addEditUserCredentialPage.getCollectionsElement().toggle(zeCollection);
		addEditUserCredentialPage.getCollectionsElement().toggle("otherCollection");
		addEditUserCredentialPage.getSaveButton().clickAndWaitForPageReload();
		//addEditUserCredentialPage.waitForPageReload();

		assertThat(driver.getCurrentPage())
				.isEqualTo(NavigatorConfigurationService.USER_ADD_EDIT + "/" + NavigatorConfigurationService.USER_LIST + "/");
	}

	private void givenAddPageWhenAddUserCredentialThenOk()
			throws Exception {

		listUserCredentialPage.navigateToListUserCredentialsPage();
		givenAddPage();

		addEditUserCredentialPage.getUsernameElement().setValue("zeEdouard");
		addEditUserCredentialPage.getFirstNameElement().setValue("zeEdouard");
		addEditUserCredentialPage.getLastNameElement().setValue("Lechat");
		addEditUserCredentialPage.getEmailElement().setValue("zeEdouard@constellio.com");
		addEditUserCredentialPage.getPasswordElement().setValue("1qaz2wsx");
		addEditUserCredentialPage.getConfirmPasswordElement().setValue("1qaz2wsx");
		addEditUserCredentialPage.getCollectionsElement().toggle(zeCollection);
		addEditUserCredentialPage.getCollectionsElement().toggle("otherCollection");
		addEditUserCredentialPage.getSaveButton().clickAndWaitForPageReload();
		//addEditUserCredentialPage.waitForPageReload();

		assertThat(listUserCredentialPage.getTableRows()).hasSize(11);
		assertThat(listUserCredentialPage.getTableRows().get(10).getText()).contains("zeEdouard");
		assertThat(userServices.getUserInCollection("zeEdouard", zeCollection).getUserRoles()).containsOnly("U");
	}

	private void givenEditPageWhenEditUserCredentialThenOk()
			throws Exception {

		givenEditPageForIndex(8);

		assertThat(addEditUserCredentialPage.getUsernameElement().isEnabled()).isFalse();
		addEditUserCredentialPage.getFirstNameElement().setValue("zeEdouard1");
		addEditUserCredentialPage.getLastNameElement().setValue("Lechat1");
		addEditUserCredentialPage.getEmailElement().setValue("zeEdouard@constellio.ca");
		addEditUserCredentialPage.getPasswordElement().setValue("2wsx1qaz");
		addEditUserCredentialPage.getConfirmPasswordElement().setValue("2wsx1qaz");
		//		addEditUserCredentialPage.getOldPassword().setValue("1qaz2wsx");
		addEditUserCredentialPage.getCollectionsElement().toggle(zeCollection);
		addEditUserCredentialPage.getCollectionsElement().toggle("otherCollection");
		addEditUserCredentialPage.getSaveButton().clickAndWaitForPageReload();

		assertThat(listUserCredentialPage.getTableRows()).hasSize(11);
		assertThat(listUserCredentialPage.getTableRows().get(10).getText()).contains("zeEdouard");
	}

	//	private void givenEditPageWhenEditUserCredentialWithWrongOldPasswordThenNotUpdated()
	//			throws Exception {
	//
	//		givenEditPageForIndex(8);
	//
	//		assertThat(addEditUserCredentialPage.getUsernameElement().isEnabled()).isFalse();
	//		addEditUserCredentialPage.getFirstNameElement().setValue("zeEdouard1");
	//		addEditUserCredentialPage.getLastNameElement().setValue("Lechat1");
	//		addEditUserCredentialPage.getEmailElement().setValue("zeEdouard@constellio.ca");
	//		addEditUserCredentialPage.getPasswordElement().setValue("2wsx1qaz");
	//		addEditUserCredentialPage.getConfirmPasswordElement().setValue("2wsx1qaz");
	//		addEditUserCredentialPage.getOldPassword().setValue("aaaaa");
	//		addEditUserCredentialPage.getCollectionsElement().toggle(zeCollection);
	//		addEditUserCredentialPage.getCollectionsElement().toggle("otherCollection");
	//		addEditUserCredentialPage.getSaveButton().clickAndWaitForPageReload();
	//		//addEditUserCredentialPage.waitForPageReload();
	//
	//		assertThat(addEditUserCredentialPage.getErrorMessage()).isEqualTo("Incorrect username and/or password");
	//		assertThat(driver.getCurrentPage())
	//				.isEqualTo(NavigatorConfigurationService.USER_ADD_EDIT + "/" + NavigatorConfigurationService.USER_LIST
	//						+ "/username%253DzeEdouard");
	//
	//	}

	private void givenDisplayPageWhenSearchAndAddGroupThenCanSearchItInUsersGroupsTable()
			throws Exception {

		listUserCredentialPage.navigateToListUserCredentialsPage();
		givenDisplayPageForIndex(0);

		displayUserCredentialPage.getSearchInputGroups().setValue("heroes");
		displayUserCredentialPage.getSearchButtonGroups().clickAndWaitForPageReload();
		//displayUserCredentialPage.waitForPageReload();

		assertThat(displayUserCredentialPage.getTableRowsGroups()).hasSize(1);
		assertThat(displayUserCredentialPage.getTableRowsGroups().get(0).getText()).contains("heroes");
		assertThat(displayUserCredentialPage.getTableRowsUsersGroups()).isEmpty();

		displayUserCredentialPage.getAddButtonOnIndex(0).clickAndWaitForPageReload();
		//		displayUserCredentialPage.waitForPageReload();

		assertThat(displayUserCredentialPage.getTableRowsUsersGroups()).hasSize(1);

		displayUserCredentialPage.getSearchInputUsersGroups().setValue("heroes");
		displayUserCredentialPage.getSearchButtonUsersGroups().click();

		assertThat(displayUserCredentialPage.getTableRowsUsersGroups()).hasSize(1);
		assertThat(displayUserCredentialPage.getTableRowsUsersGroups().get(0).getText()).contains("heroes");
	}

	private void givenDisplayPageWhenClickInModifyThenNavigateToAddEditUserCredential() {

		givenDisplayPageForIndex(0);

		displayUserCredentialPage.getEditGlobalGroupButtonMenuAction().clickAndWaitForPageReload();
		//displayUserCredentialPage.waitForPageReload();
		assertThat(driver.getCurrentPage())
				.isEqualTo(NavigatorConfigurationService.USER_ADD_EDIT + "/" + NavigatorConfigurationService.USER_LIST
						+ "/" + NavigatorConfigurationService.USER_DISPLAY + "/username%253Dadmin");
	}

	private void givenDisplayPageWhenClickInEditGroupThenNavigateToAddEditGlobalGroup() {

		givenDisplayPageForIndex(0);

		displayUserCredentialPage.getEditGlobalGroupButtonOnIndex(0).clickAndWaitForPageReload();
		//displayUserCredentialPage.waitForPageReload();
		assertThat(driver.getCurrentPage())
				.isEqualTo(NavigatorConfigurationService.GROUP_ADD_EDIT
						+ "/" + NavigatorConfigurationService.USER_LIST + "/" + NavigatorConfigurationService.USER_DISPLAY
						+ "/username%253Dadmin%253BglobalGroupCode%253Dheroes");
	}

	private void givenDisplayPageWhenClickInRemoveGroupFromUserThenCanSearchItInGroupsTable()
			throws Exception {

		givenDisplayPageForIndex(0);

		displayUserCredentialPage.getSearchInputUsersGroups().setValue("heroes");
		displayUserCredentialPage.getSearchButtonUsersGroups().clickAndWaitForPageReload();
		//		displayUserCredentialPage.waitForPageReload();

		assertThat(displayUserCredentialPage.getTableRowsUsersGroups()).hasSize(1);
		assertThat(displayUserCredentialPage.getTableRowsUsersGroups().get(0).getText()).contains("heroes");
		assertThat(displayUserCredentialPage.getTableRowsGroups()).hasSize(3);

		displayUserCredentialPage.getDeleteButtonOnIndex(0).clickAndWaitForPageReload();
		//		displayUserCredentialPage.waitForPageReload();
		displayUserCredentialPage.getCancelConfirmationDialogButton().clickAndWaitForPageReload();
		//		displayUserCredentialPage.waitForPageReload();

		assertThat(displayUserCredentialPage.getTableRowsGroups()).hasSize(3);

		displayUserCredentialPage.getDeleteButtonOnIndex(0).click();
		displayUserCredentialPage.getOkConfirmationDialogButton().clickAndWaitForPageReload();
		//		displayUserCredentialPage.waitForPageReload();
		displayUserCredentialPage.getSearchInputGroups().setValue("heroes");
		displayUserCredentialPage.getSearchButtonGroups().click();

		assertThat(displayUserCredentialPage.getTableRowsUsersGroups()).isEmpty();
		assertThat(displayUserCredentialPage.getTableRowsGroups()).hasSize(1);
		assertThat(displayUserCredentialPage.getTableRowsGroups().get(0).getText()).contains("heroes");
	}

	private void givenDisplayPageWhenClickInBackButtonThenNavigateToListUserCredentials() {

		givenDisplayPageForIndex(0);

		displayUserCredentialPage.getBackButton().clickAndWaitForPageReload();
		//displayUserCredentialPage.waitForPageReload();

		assertThat(driver.getCurrentPage())
				.isEqualTo(NavigatorConfigurationService.USER_LIST + "/username%253Dadmin");
	}

	//

	private void givenAddPage() {
		//listUserCredentialPage.navigateToListUserCredentialsPage();
		listUserCredentialPage.getAddButton().clickAndWaitForPageReload();
		//listUserCredentialPage.waitForPageReload();
	}

	private void givenEditPageForIndex(int index) {
		//listUserCredentialPage.navigateToListUserCredentialsPage();
		listUserCredentialPage.getEditUserCredentialButtonOnIndex(index).clickAndWaitForPageReload();
		//listUserCredentialPage.waitForPageReload();
	}

	private void givenDisplayPageForIndex(int index) {
		listUserCredentialPage.navigateToListUserCredentialsPage();
		listUserCredentialPage.getDisplayUserCredentialButtonOnIndex(index).clickAndWaitForPageReload();
		//listUserCredentialPage.waitForPageReload();
	}

}
