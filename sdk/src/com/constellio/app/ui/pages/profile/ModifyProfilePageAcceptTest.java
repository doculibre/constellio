package com.constellio.app.ui.pages.profile;

import static com.constellio.app.ui.i18n.i18n.$;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;

import com.constellio.app.modules.rm.navigation.RMNavigationConfiguration;
import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.ui.application.NavigatorConfigurationService;
import com.constellio.model.services.security.authentification.AuthenticationService;
import com.constellio.model.services.users.UserServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.annotations.UiTest;
import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebDriver;

@UiTest
public class ModifyProfilePageAcceptTest extends ConstellioTest {
	ConstellioWebDriver driver;
	RMSchemasRecordsServices schemas;
	UserServices userServices;
	AuthenticationService authenticationService;
	RMTestRecords records = new RMTestRecords(zeCollection);

	ModifyProfilePage modifyProfilePage;

	@Before
	public void setUp()
			throws Exception {

		prepareSystem(
				withZeCollection().withConstellioRMModule().withAllTestUsers().withRMTest(records)
						.withFoldersAndContainersOfEveryStatus()
		);

		schemas = new RMSchemasRecordsServices(zeCollection, getModelLayerFactory());
		userServices = getModelLayerFactory().newUserServices();
		authenticationService = getModelLayerFactory().newAuthenticationService();

		driver = newWebDriver(loggedAsUserInCollection("admin", zeCollection));

		modifyProfilePage = new ModifyProfilePage(driver);

	}

	@Test
	public void testPage()
			throws Exception {

		whenModifyProfileThenNavigateToModifyProfileWithCorrectInfos();
		givenUserInZePageWihtParamsWhenClickInModifyProfileAndClickCancelThenNavigateToZePageWithParams();
		givenUserInZePageWihtParamsWhenClickInModifyProfileAndClickSaveThenNavigateToZePageWithParams();
		whenModifyInfosAndCancelThenInfosAreNotUpdated();
		whenModifyInfosAndSaveThenInfosAreUpdated();
		whenModifyInfosAndSaveWithMissingRequiredInfoThenStayInPage();
		givenWrongOldPasswordWhenModifyPasswordThenItIsNotUpdated();
		givenEmptyOldPasswordWhenModifyPasswordThenItIsNotUpdated();
		givenWrongConfirmPasswordWhenModifyPasswordThenItIsNotUpdated();
		givenEmptyConfirmPasswordWhenModifyPasswordThenItIsNotUpdated();
		whenModifyPasswordThenItIsUpdated();

	}

	private void whenModifyProfileThenNavigateToModifyProfileWithCorrectInfos()
			throws Exception {

		modifyProfilePage.navigateToPage();

		assertThat(modifyProfilePage.getFirstNameElement().getValue()).isEqualTo("System");
		assertThat(modifyProfilePage.getLastNameElement().getValue()).isEqualTo("Admin");
		assertThat(modifyProfilePage.getEmailElement().getValue()).isEqualTo("admin@organization.com");
		assertThat(modifyProfilePage.getPhoneElement().getValue()).isEqualTo("");
		assertThat(modifyProfilePage.getPasswordElement().getValue()).isEqualTo("");
		assertThat(modifyProfilePage.getConfirmPasswordElement().getValue()).isEqualTo("");
		assertThat(modifyProfilePage.getOldPasswordElement().getValue()).isEqualTo("");
		assertThat(modifyProfilePage.getStartTabElement().getCheckedValues().get(0))
				.isEqualTo($("HomeView.tab." + RMNavigationConfiguration.LAST_VIEWED_FOLDERS));
		assertThat(modifyProfilePage.getDefaultTaxonomyElement().getCheckedValues()).isEmpty();
	}

	private void givenUserInZePageWihtParamsWhenClickInModifyProfileAndClickCancelThenNavigateToZePageWithParams()
			throws Exception {

		modifyProfilePage.navigateToEditHeroesGroupPage();
		modifyProfilePage.navigateToPage();

		modifyProfilePage.getCancelButton().click();
		modifyProfilePage.waitForPageReload();

		assertThat(driver.getCurrentPage()).isEqualTo(
				NavigatorConfigurationService.GROUP_ADD_EDIT + "/globalGroupCode=heroes;locale=fr");
	}

	private void givenUserInZePageWihtParamsWhenClickInModifyProfileAndClickSaveThenNavigateToZePageWithParams()
			throws Exception {

		modifyProfilePage.navigateToEditHeroesGroupPage();
		modifyProfilePage.navigateToPage();

		modifyProfilePage.getSaveButton().click();
		modifyProfilePage.waitForPageReload();

		assertThat(driver.getCurrentPage()).isEqualTo(
				NavigatorConfigurationService.GROUP_ADD_EDIT + "/globalGroupCode=heroes;locale=fr");

	}

	private void whenModifyInfosAndCancelThenInfosAreNotUpdated()
			throws Exception {
		modifyProfilePage.navigateToPage();
		modifyProfilePage.getFirstNameElement().setValue("System1");
		modifyProfilePage.getLastNameElement().setValue("Administrator");
		modifyProfilePage.getEmailElement().setValue("administrator@constellio.com");
		modifyProfilePage.getPhoneElement().setValue("33333333");
		modifyProfilePage.getStartTabElement().toggle($("HomeView.tab." + RMNavigationConfiguration.LAST_VIEWED_DOCUMENTS));
		modifyProfilePage.getDefaultTaxonomyElement().toggle("plan");
		modifyProfilePage.getCancelButton().clickAndWaitForPageReload();

		modifyProfilePage.navigateToPage();
		assertThat(modifyProfilePage.getFirstNameElement().getValue()).isEqualTo("System");
		assertThat(modifyProfilePage.getLastNameElement().getValue()).isEqualTo("Admin");
		assertThat(modifyProfilePage.getEmailElement().getValue()).isEqualTo("admin@organization.com");
		assertThat(modifyProfilePage.getPhoneElement().getValue()).isEqualTo("");
		assertThat(modifyProfilePage.getStartTabElement().getCheckedValues().get(0))
				.isEqualTo($("HomeView.tab." + RMNavigationConfiguration.LAST_VIEWED_FOLDERS));
		assertThat(modifyProfilePage.getDefaultTaxonomyElement().getCheckedValues()).isEmpty();
	}

	private void whenModifyInfosAndSaveThenInfosAreUpdated()
			throws Exception {

		modifyProfilePage.navigateToPage();
		modifyProfilePage.getFirstNameElement().setValue("System1");
		modifyProfilePage.getLastNameElement().setValue("Administrator");
		modifyProfilePage.getPhoneElement().setValue("33333333");
		modifyProfilePage.getEmailElement().setValue("administrator@constellio.com");
		modifyProfilePage.getStartTabElement().toggle($("HomeView.tab." + RMNavigationConfiguration.LAST_VIEWED_DOCUMENTS));
		modifyProfilePage.getDefaultTaxonomyElement().toggle("Plan de classification");
		modifyProfilePage.getSaveButton().click();
		modifyProfilePage.waitForPageReload();

		modifyProfilePage.navigateToPage();
		assertThat(modifyProfilePage.getFirstNameElement().getValue()).isEqualTo("System1");
		assertThat(modifyProfilePage.getLastNameElement().getValue()).isEqualTo("Administrator");
		assertThat(modifyProfilePage.getEmailElement().getValue()).isEqualTo("administrator@constellio.com");
		assertThat(modifyProfilePage.getPhoneElement().getValue()).isEqualTo("33333333");
		assertThat(modifyProfilePage.getStartTabElement().getCheckedValues().get(0))
				.isEqualTo($("HomeView.tab." + RMNavigationConfiguration.LAST_VIEWED_DOCUMENTS));
		assertThat(modifyProfilePage.getDefaultTaxonomyElement().getCheckedValues().get(0)).isEqualTo("Plan de classification");

	}

	private void whenModifyInfosAndSaveWithMissingRequiredInfoThenStayInPage()
			throws Exception {
		modifyProfilePage.navigateToPage();

		modifyProfilePage.getFirstNameElement().setValue("System1");
		modifyProfilePage.getLastNameElement().setValue("");
		modifyProfilePage.getEmailElement().setValue("administrator@constellio.com");
		modifyProfilePage.getPhoneElement().setValue("33333333");
		modifyProfilePage.getStartTabElement().toggle($("HomeView.tab." + RMNavigationConfiguration.LAST_VIEWED_DOCUMENTS));
		modifyProfilePage.getDefaultTaxonomyElement().toggle("Plan de classification");
		modifyProfilePage.getSaveButton().click();

		assertThat(driver.getCurrentPage()).isEqualTo(
				NavigatorConfigurationService.MODIFY_PROFILE + "/groupAddEdit/globalGroupCode=heroes;locale=fr");

	}

	private void givenWrongOldPasswordWhenModifyPasswordThenItIsNotUpdated() {

		modifyProfilePage.navigateToPage();
		modifyProfilePage.waitForPageReload();

		modifyProfilePage.getPasswordElement().setValue("newPassword");
		modifyProfilePage.getConfirmPasswordElement().setValue("newPassword");
		modifyProfilePage.getOldPasswordElement().setValue("wrongPassword");

		modifyProfilePage.getSaveButton().click();
		modifyProfilePage.waitForPageReload();

		assertThat(authenticationService.authenticate("admin", "newPassword")).isFalse();
		assertThat(driver.getCurrentPage()).isEqualTo(
				NavigatorConfigurationService.MODIFY_PROFILE + "/groupAddEdit/globalGroupCode=heroes;locale=fr");
	}

	private void givenEmptyOldPasswordWhenModifyPasswordThenItIsNotUpdated() {

		modifyProfilePage.navigateToPage();
		modifyProfilePage.waitForPageReload();

		modifyProfilePage.getPasswordElement().setValue("newPassword");
		modifyProfilePage.getConfirmPasswordElement().setValue("newPassword");
		modifyProfilePage.getOldPasswordElement().setValue("");

		modifyProfilePage.getSaveButton().click();
		modifyProfilePage.waitForPageReload();

		assertThat(authenticationService.authenticate("admin", "newPassword")).isFalse();
		assertThat(driver.getCurrentPage()).isEqualTo(
				NavigatorConfigurationService.MODIFY_PROFILE + "/groupAddEdit/globalGroupCode=heroes;locale=fr");
	}

	private void givenWrongConfirmPasswordWhenModifyPasswordThenItIsNotUpdated() {

		modifyProfilePage.navigateToPage();
		modifyProfilePage.waitForPageReload();

		modifyProfilePage.getPasswordElement().setValue("newPassword");
		modifyProfilePage.getConfirmPasswordElement().setValue("newPassword1");
		modifyProfilePage.getOldPasswordElement().setValue("password");

		modifyProfilePage.getSaveButton().click();
		modifyProfilePage.waitForPageReload();

		assertThat(authenticationService.authenticate("admin", "newPassword")).isFalse();
		assertThat(driver.getCurrentPage()).isEqualTo(
				NavigatorConfigurationService.MODIFY_PROFILE + "/groupAddEdit/globalGroupCode=heroes;locale=fr");
	}

	private void givenEmptyConfirmPasswordWhenModifyPasswordThenItIsNotUpdated() {

		modifyProfilePage.navigateToPage();
		modifyProfilePage.waitForPageReload();

		modifyProfilePage.getPasswordElement().setValue("newPassword");
		modifyProfilePage.getConfirmPasswordElement().setValue("");
		modifyProfilePage.getOldPasswordElement().setValue("password");

		modifyProfilePage.getSaveButton().click();
		modifyProfilePage.waitForPageReload();

		assertThat(authenticationService.authenticate("admin", "newPassword")).isFalse();
		assertThat(driver.getCurrentPage()).isEqualTo(
				NavigatorConfigurationService.MODIFY_PROFILE + "/groupAddEdit/globalGroupCode=heroes;locale=fr");
	}

	private void whenModifyPasswordThenItIsUpdated()
			throws Exception {

		modifyProfilePage.navigateToPage();
		modifyProfilePage.waitForPageReload();

		modifyProfilePage.getPasswordElement().setValue("newPassword");
		modifyProfilePage.getConfirmPasswordElement().setValue("");
		modifyProfilePage.getConfirmPasswordElement().setValue("newPassword");
		modifyProfilePage.getOldPasswordElement().setValue("password");

		modifyProfilePage.getSaveButton().click();
		modifyProfilePage.waitForPageReload();

		assertThat(authenticationService.authenticate("admin", "newPassword")).isTrue();
	}

}
