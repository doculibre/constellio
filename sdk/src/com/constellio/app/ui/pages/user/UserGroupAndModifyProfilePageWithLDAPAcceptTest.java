package com.constellio.app.ui.pages.user;

import com.constellio.app.modules.rm.DemoTestRecords;
import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.navigation.RMNavigationConfiguration;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.pages.base.SchemaPresenterUtils;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.app.ui.pages.globalGroup.AddEditGlobalGroupPage;
import com.constellio.app.ui.pages.globalGroup.DisplayGlobalGroupPage;
import com.constellio.app.ui.pages.globalGroup.ListGlobalGroupPage;
import com.constellio.app.ui.pages.profile.ModifyProfilePage;
import com.constellio.model.conf.LDAPTestConfig;
import com.constellio.model.conf.ldap.LDAPConfigurationManager;
import com.constellio.model.conf.ldap.config.LDAPServerConfiguration;
import com.constellio.model.conf.ldap.config.LDAPUserSyncConfiguration;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.security.global.GlobalGroupStatus;
import com.constellio.model.entities.security.global.UserCredential;
import com.constellio.model.entities.security.global.UserCredentialStatus;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.security.authentification.AuthenticationService;
import com.constellio.model.services.users.UserServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.annotations.InDevelopmentTest;
import com.constellio.sdk.tests.annotations.UiTest;
import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebDriver;
import org.joda.time.Duration;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;
import static org.assertj.core.api.Assertions.assertThat;

@UiTest
public class UserGroupAndModifyProfilePageWithLDAPAcceptTest extends ConstellioTest {
	public static final String ADMINISTRATOR = "Administrator";
	public static final String RGD = "RGD";

	AddEditUserCredentialPage addEditUserCredentialPage;
	ListUserCredentialPage listUserCredentialPage;
	DisplayUserCredentialPage displayUserCredentialPage;
	ModifyProfilePage modifyProfilePage;
	AddEditGlobalGroupPage addEditGlobalGroupPage;
	ListGlobalGroupPage listGlobalGroupPage;
	DisplayGlobalGroupPage displayGlobalGroupPage;

	AuthenticationService authenticationService;
	RecordServices recordServices;
	ConstellioWebDriver driver;
	RMTestRecords records = new RMTestRecords(zeCollection);
	DemoTestRecords records2 = new DemoTestRecords("LaCollectionDeRida");
	SessionContext sessionContext;
	User administratorInZeCollection;
	LDAPUserSyncConfiguration userSync;
	LDAPServerConfiguration serverConf;
	LDAPConfigurationManager ldapConfigurationManager;
	UserServices userServices;

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
		userServices = getModelLayerFactory().newUserServices();
		ldapConfigurationManager = getModelLayerFactory().getLdapConfigurationManager();
		authenticationService = getModelLayerFactory().newAuthenticationService();

		givenAdmin();

		setupLDAPWithoutSynchronization(userServices);
	}

	@Test
	@InDevelopmentTest
	//Ne passe pas sur le serveur d'intégration
	public void testPage()
			throws Exception {
		testWithAdminAndLDAPActiveAndWithoutSynchronization();

		//TODO fix this test
		//		givenAdministrator();
		//		testWithAdministratorAndLDAPActiveAndWithoutSynchronization();

		synchronizeLDAP();
		testWithAdminAndLDAPSynchronization();
		givenAdministrator();
		testWithAdministratorAndLDAPSynchronization();
		throw new RuntimeException("victory");
	}

	private void testWithAdminAndLDAPActiveAndWithoutSynchronization()
			throws Exception {
		driver.snapshot("1");
		navigatoToListUserCredentialPage();
		driver.snapshot("2");
		assertThat(ldapConfigurationManager.idUsersSynchActivated()).isFalse();
		assertThat(ldapConfigurationManager.isLDAPAuthentication()).isTrue();
		givenListUserCredentialsPageThenCanAddUser();
		givenAddPageWhenAddUserThenAddUserWithoutPassword(1);
		givenEditPageWhenEditUserThenEditUserWithoutPassword(1);
		givenAdminInEditPageWhenEditUserCredentialThenOk();
		givenDisplayPageWhenAddAndRemoveGroupsThenOk(1);
		givenListGlobalGroupPageThenCanAddGroup();
		givenAddGlobalGroupPageWhenAddGroupThenOk(1);
		givenEditGlobalGroupPageWhenEditGroupThenOk(1);
		givenDisplayGlobalGroupPageWhenAddSubGroupThenOk(1);
		givenDisplayPageWhenDeleteSubGroupThenOk(1);
		givenDisplayGlobalGroupPageWhenAddAndRemoveUserThenOk(1);
		givenDisplayGlobalGroupPageWhenRemoveGroupThenOk(1);
		givenModifyProfilePageThenCanEdit();
	}

	private void testWithAdministratorAndLDAPActiveAndWithoutSynchronization()
			throws Exception {
		navigatoToListUserCredentialPage();

		assertThat(ldapConfigurationManager.idUsersSynchActivated()).isFalse();
		assertThat(ldapConfigurationManager.isLDAPAuthentication()).isTrue();
		givenListUserCredentialsPageThenCanAddUser();
		givenAddPageWhenAddUserThenAddUserWithoutPassword(2);
		givenEditPageWhenEditUserThenEditUserWithoutPassword(2);
		givenDisplayPageWhenAddAndRemoveGroupsThenOk(2);
		givenListGlobalGroupPageThenCanAddGroup();
		givenAddGlobalGroupPageWhenAddGroupThenOk(2);
		givenEditGlobalGroupPageWhenEditGroupThenOk(2);
		givenDisplayGlobalGroupPageWhenAddSubGroupThenOk(2);
		givenDisplayPageWhenDeleteSubGroupThenOk(2);
		givenDisplayGlobalGroupPageWhenAddAndRemoveUserThenOk(2);
		givenDisplayGlobalGroupPageWhenRemoveGroupThenOk(2);
		givenModifyProfilePageThenCanEdit();
	}

	private void testWithAdminAndLDAPSynchronization()
			throws Exception {

		navigatoToListUserCredentialPage();

		assertThat(ldapConfigurationManager.idUsersSynchActivated()).isTrue();
		assertThat(ldapConfigurationManager.isLDAPAuthentication()).isTrue();
		givenListUserCredentialsPageThenCannotAddUser();
		givenListUserCredentialsPageWhenSearchForAdministratorThenCannotEdit();
		givenAdminInListUserCredentialsPagewhenSearchForHimSelfThenCanEdit();
		givenAdminInEditPageWhenEditUserCredentialThenOk();
		givenDisplayPageThenAddAndDeleteButtonsAreInvisiblesEditIsEnabled();
		givenModifyProfilePageThenCanEdit();
		givenListGlobalGroupsPageThenCannotAddOrEdit();
		givenDisplayGlobalGroupsPageThenAllEditAddAndDeleteButtonsAreInvisibles();

	}

	private void testWithAdministratorAndLDAPSynchronization()
			throws Exception {

		navigatoToListUserCredentialPage();

		assertThat(ldapConfigurationManager.idUsersSynchActivated()).isTrue();
		assertThat(ldapConfigurationManager.isLDAPAuthentication()).isTrue();
		givenListUserCredentialsPageThenCannotAddUser();
		givenListUserCredentialsPageWhenSearchForAdministratorThenCannotEdit();
		givenDisplayPageThenAllAddEditAndDeleteButtonsAreInvisibles();
		givenAdministratorInModifyProfilePageThenCanEditOnlyStartTabAndDefaultTaxonomy();
		givenListGlobalGroupsPageThenCannotAddOrEdit();
		givenDisplayGlobalGroupsPageThenAllEditAddAndDeleteButtonsAreInvisibles();
	}

	private void givenListUserCredentialsPageThenCanAddUser() {

		assertThat(listUserCredentialPage.getAddButton().isEnabled()).isTrue();
	}

	private void givenAddPageWhenAddUserThenAddUserWithoutPassword(int id) {

		listUserCredentialPage.navigateToListUserCredentialsPage();
		givenUserAddPage();

		addEditUserCredentialPage.getUsernameElement().clearAndSetValue("zeEdouard" + id);
		addEditUserCredentialPage.getFirstNameElement().clearAndSetValue("zeEdouard" + id);
		addEditUserCredentialPage.getLastNameElement().clearAndSetValue("Lechat" + id);
		addEditUserCredentialPage.getEmailElement().clearAndSetValue("ze" + id + "Edouard@constellio.com");
		addEditUserCredentialPage.getCollectionsElement().toggle(zeCollection);
		addEditUserCredentialPage.getSaveButton().clickAndWaitForPageReload();

		assertThat(userServices.getUserCredential("zeEdouard" + id)).isNotNull();
		assertThat(userServices.getUserCredential("zeEdouard" + id).getFirstName()).isEqualTo("zeEdouard" + id);
		assertThat(userServices.getUserCredential("zeEdouard" + id).getLastName()).isEqualTo("Lechat" + id);
		assertThat(userServices.getUserCredential("zeEdouard" + id).getEmail()).isEqualTo("ze" + id + "Edouard@constellio.com");
		assertThat(userServices.getUserCredential("zeEdouard" + id).getStatus()).isEqualTo(UserCredentialStatus.ACTIVE);
	}

	private void givenEditPageWhenEditUserThenEditUserWithoutPassword(int id) {

		listUserCredentialPage.navigateToListUserCredentialsPage();
		givenEditPageForUser("zeEdouard" + id);

		assertThat(addEditUserCredentialPage.getUsernameElement().isEnabled()).isFalse();
		addEditUserCredentialPage.getFirstNameElement().clearAndSetValue("zeEdouard1" + id);
		addEditUserCredentialPage.getLastNameElement().clearAndSetValue("Lechat1" + id);
		addEditUserCredentialPage.getEmailElement().clearAndSetValue("ze" + id + "Edouard@constellio.com");
		addEditUserCredentialPage.getCollectionsElement().toggle(zeCollection);
		addEditUserCredentialPage.getSaveButton().clickAndWaitForPageReload();

		assertThat(userServices.getUserCredential("zeEdouard" + id)).isNotNull();
		assertThat(userServices.getUserCredential("zeEdouard" + id).getFirstName()).isEqualTo("zeEdouard1" + id);
		assertThat(userServices.getUserCredential("zeEdouard" + id).getLastName()).isEqualTo("Lechat1" + id);
		assertThat(userServices.getUserCredential("zeEdouard" + id).getEmail()).isEqualTo("ze" + id + "Edouard@constellio.com");
		assertThat(userServices.getUserCredential("zeEdouard" + id).getStatus()).isEqualTo(UserCredentialStatus.ACTIVE);
	}

	private void givenDisplayPageWhenAddAndRemoveGroupsThenOk(int id) {

		listUserCredentialPage.navigateToListUserCredentialsPage();
		givenDisplayPageForUser("zeEdouard" + id);

		displayUserCredentialPage.getSearchInputGroups().setValue("Administrators");
		displayUserCredentialPage.getSearchButtonGroups().clickAndWaitForPageReload();
		displayUserCredentialPage.getAddButtonOnIndex(0).clickAndWaitForPageReload();

		assertThat(userServices.getUserCredential("zeEdouard" + id).getGlobalGroups()).hasSize(1);
		assertThat(userServices.getUserCredential("zeEdouard" + id).getGlobalGroups().get(0)).contains("");

		assertThat(displayUserCredentialPage.getEditGlobalGroupButtonOnIndex(0).isEnabled()).isTrue();
		displayUserCredentialPage.getDeleteButtonOnIndex(0).click();
		displayUserCredentialPage.getOkConfirmationDialogButton().clickAndWaitForPageReload();

		assertThat(userServices.getUserCredential("zeEdouard" + id).getGlobalGroups()).isEmpty();
	}

	private void givenListGlobalGroupPageThenCanAddGroup() {

		assertThat(listGlobalGroupPage.getAddButton().isEnabled()).isTrue();
	}

	private void givenAddGlobalGroupPageWhenAddGroupThenOk(int id) {
		listGlobalGroupPage.navigateToListGlobalGroupsPage();
		listGlobalGroupPage.getAddButton().clickAndWaitForPageReload();

		assertThat(addEditGlobalGroupPage.getStatusElement().isEnabled()).isFalse();
		addEditGlobalGroupPage.getCodeElement().setValue("zeGroup1" + id);
		addEditGlobalGroupPage.getNameElement().setValue("The zeGroup 1" + id);
		addEditGlobalGroupPage.getCollectionsElement().toggle(zeCollection);
		addEditGlobalGroupPage.getSaveButton().clickAndWaitForPageReload();

		assertThat(userServices.getGroup("zeGroup1" + id)).isNotNull();
	}

	private void givenEditGlobalGroupPageWhenEditGroupThenOk(int id) {

		listGlobalGroupPage.navigateToListGlobalGroupsPage();
		givenEditPageForGroup("zeGroup1" + id);

		assertThat(addEditGlobalGroupPage.getCodeElement().isEnabled()).isFalse();
		addEditGlobalGroupPage.getNameElement().clearAndSetValue("The zeGroup 1 modified" + id);
		addEditGlobalGroupPage.getCollectionsElement().toggle(zeCollection);
		addEditGlobalGroupPage.getCollectionsElement().toggle(zeCollection);
		addEditGlobalGroupPage.getSaveButton().clickAndWaitForPageReload();

		assertThat(userServices.getGroup("zeGroup1" + id)).isNotNull();
		assertThat(userServices.getGroup("zeGroup1" + id).getName()).isEqualTo("The zeGroup 1 modified" + id);
	}

	private void givenDisplayGlobalGroupPageWhenAddSubGroupThenOk(int id) {

		listGlobalGroupPage.navigateToListGlobalGroupsPage();
		givenDisplayPageForGroup("zeGroup1" + id);

		displayGlobalGroupPage.getAddSubGlobalGroupButtonMenuAction().click();
		displayGlobalGroupPage.waitForPageReload();

		addEditGlobalGroupPage.getCodeElement().clearAndSetValue("zeSubGroup1" + id);
		addEditGlobalGroupPage.getNameElement().clearAndSetValue("The zeSubGroup 1" + id);
		addEditGlobalGroupPage.getCollectionsElement().toggle(zeCollection);
		addEditGlobalGroupPage.getSaveButton().clickAndWaitForPageReload();

		assertThat(userServices.getGroup("zeSubGroup1" + id)).isNotNull();
		assertThat(userServices.getGroup("zeSubGroup1" + id).getParent()).isEqualTo("zeGroup1" + id);
	}

	private void givenDisplayPageWhenDeleteSubGroupThenOk(int id) {

		listGlobalGroupPage.navigateToListGlobalGroupsPage();
		givenDisplayPageForGroup("zeGroup1" + id);

		displayGlobalGroupPage.getDeleteSubGlobalGroupButtonOnIndex(0).clickAndWaitForPageReload();
		displayGlobalGroupPage.getOkConfirmationDialogButton().clickAndWaitForPageReload();

		assertThat(userServices.getGroup("zeSubGroup1" + id)).isNotNull();
		assertThat(userServices.getGroup("zeSubGroup1" + id).getStatus()).isEqualTo(GlobalGroupStatus.INACTIVE);
	}

	private void givenDisplayGlobalGroupPageWhenAddAndRemoveUserThenOk(int id) {

		listGlobalGroupPage.navigateToListGlobalGroupsPage();
		givenDisplayPageForGroup("zeGroup1" + id);

		displayGlobalGroupPage.getSearchInputUsers().clearAndSetValue("admin@organization.com");
		displayGlobalGroupPage.getSearchButtonUsers().click();
		displayGlobalGroupPage.waitForPageReload();
		displayGlobalGroupPage.getAddButton().clickAndWaitForPageReload();

		assertThat(userServices.getUserCredential("admin").getGlobalGroups()).contains("zeGroup1" + id);

		displayGlobalGroupPage.getSearchInputUsersInGroup().clearAndSetValue("admin@organization.com");
		displayGlobalGroupPage.getSearchButtonUsersInGroup().click();
		displayGlobalGroupPage.getDeleteButtonOnIndex(0).clickAndWaitForPageReload();
		displayGlobalGroupPage.getOkConfirmationDialogButton().clickAndWaitForPageReload();

		assertThat(userServices.getUserCredential("admin").getGlobalGroups()).doesNotContain("zeGroup1" + id);

	}

	private void givenDisplayGlobalGroupPageWhenRemoveGroupThenOk(int id) {

		listGlobalGroupPage.navigateToListGlobalGroupsPage();
		givenDisplayPageForGroup("zeGroup1" + id);

		displayGlobalGroupPage.getDeleteGlobalGroupButtonMenuAction().clickAndWaitForPageReload();

		assertThat(userServices.getGroup("zeSubGroup1" + id)).isNotNull();
		assertThat(userServices.getGroup("zeSubGroup1" + id).getStatus()).isEqualTo(GlobalGroupStatus.INACTIVE);
	}

	//
	private void givenListUserCredentialsPageThenCannotAddUser()
			throws Exception {

		assertThat(listUserCredentialPage.getAddButton().isEnabled()).isFalse();
	}

	private void givenListUserCredentialsPageWhenSearchForAdministratorThenCannotEdit()
			throws Exception {
		listUserCredentialPage.getSearchInput().setValue(ADMINISTRATOR);
		listUserCredentialPage.getSearchButton().clickAndWaitForPageReload();

		assertThat(listUserCredentialPage.getTableRows()).hasSize(1);
		assertThat(listUserCredentialPage.getTableRows().get(0).getText()).contains(ADMINISTRATOR);
		assertThat(listUserCredentialPage.findEditButtonElements()).isEmpty();
	}

	private void givenAdminInListUserCredentialsPagewhenSearchForHimSelfThenCanEdit()
			throws Exception {
		listUserCredentialPage.getSearchInput().clearAndSetValue("admin@organization.com");
		listUserCredentialPage.getSearchButton().clickAndWaitForPageReload();

		assertThat(listUserCredentialPage.getTableRows()).hasSize(1);
		assertThat(listUserCredentialPage.getTableRows().get(0).getText()).contains("admin");
		assertThat(listUserCredentialPage.getEditUserCredentialButtonOnIndex(0).isEnabled()).isTrue();

	}

	private void givenAdminInEditPageWhenEditUserCredentialThenOk()
			throws Exception {

		listUserCredentialPage.getSearchInput().clearAndSetValue("admin@organization.com");
		listUserCredentialPage.getSearchButton().clickAndWaitForPageReload();
		givenEditUserPageForIndex(0);

		assertThat(addEditUserCredentialPage.getUsernameElement().isEnabled()).isFalse();
		addEditUserCredentialPage.getFirstNameElement().clearAndSetValue("Admin1");
		addEditUserCredentialPage.getLastNameElement().clearAndSetValue("System1");
		addEditUserCredentialPage.getEmailElement().clearAndSetValue("admin@organization.com");
		addEditUserCredentialPage.getPasswordElement().clearAndSetValue("2wsx1qaz");
		addEditUserCredentialPage.getConfirmPasswordElement().clearAndSetValue("2wsx1qaz");
		addEditUserCredentialPage.getCollectionsElement().toggle(zeCollection);
		addEditUserCredentialPage.getCollectionsElement().toggle(zeCollection);
		addEditUserCredentialPage.getSaveButton().clickAndWaitForPageReload();

		assertThat(userServices.getUserCredential("admin").getFirstName()).isEqualTo("Admin1");
		assertThat(userServices.getUserCredential("admin").getLastName()).isEqualTo("System1");
		assertThat(userServices.getUserCredential("admin").getEmail()).isEqualTo("admin@organization.com");
		assertThat(userServices.getUserCredential("admin").getCollections()).isEqualTo(
				Arrays.asList("LaCollectionDeRida", zeCollection));
		assertThat(authenticationService.authenticate("admin", "2wsx1qaz"));
	}

	private void givenDisplayPageThenAllAddEditAndDeleteButtonsAreInvisibles() {

		listUserCredentialPage.getSearchInput().clearAndSetValue("admin@organization.com");
		listUserCredentialPage.getSearchButton().clickAndWaitForPageReload();
		givenDisplayUserPageForIndex(0);

		assertThat(displayUserCredentialPage.getEditGlobalGroupButtonMenuAction().isEnabled()).isFalse();
		assertThat(displayUserCredentialPage.findEditButtonElements()).hasSize(1);
		assertThat(displayUserCredentialPage.getEditGlobalGroupButtonMenuAction().isEnabled()).isFalse();
		assertThat(displayUserCredentialPage.findAddButtonElements()).isEmpty();
		assertThat(displayUserCredentialPage.findDeleteButtonElements()).isEmpty();
	}

	private void givenDisplayPageThenAddAndDeleteButtonsAreInvisiblesEditIsEnabled() {

		listUserCredentialPage.getSearchInput().clearAndSetValue("admin@organization.com");
		listUserCredentialPage.getSearchButton().clickAndWaitForPageReload();
		givenDisplayUserPageForIndex(0);

		assertThat(displayUserCredentialPage.getEditGlobalGroupButtonMenuAction().isEnabled()).isTrue();
		assertThat(displayUserCredentialPage.findEditButtonElements()).hasSize(1);
		assertThat(displayUserCredentialPage.getEditGlobalGroupButtonMenuAction().isEnabled()).isTrue();
		assertThat(displayUserCredentialPage.findAddButtonElements()).isEmpty();
		assertThat(displayUserCredentialPage.findDeleteButtonElements()).isEmpty();
	}

	private void givenModifyProfilePageThenCanEdit() {
		modifyProfilePage.navigateToPage();
		modifyProfilePage.waitForPageReload();

		modifyProfilePage.getFirstNameElement().clearAndSetValue("System1");
		modifyProfilePage.getLastNameElement().clearAndSetValue("Admin1");
		modifyProfilePage.getPhoneElement().clearAndSetValue("33333333");
		modifyProfilePage.getEmailElement().clearAndSetValue("admin@organization.com");
		modifyProfilePage.getStartTabElement().toggle($("HomeView.tab." + RMNavigationConfiguration.LAST_VIEWED_DOCUMENTS));
		modifyProfilePage.getDefaultTaxonomyElement().toggle("Plan de classification");
		modifyProfilePage.getSaveButton().click();
		modifyProfilePage.waitForPageReload();

		modifyProfilePage.navigateToPage();
		assertThat(modifyProfilePage.getFirstNameElement().getValue()).isEqualTo("System1");
		assertThat(modifyProfilePage.getLastNameElement().getValue()).isEqualTo("Admin1");
		assertThat(modifyProfilePage.getEmailElement().getValue()).isEqualTo("admin@organization.com");
		assertThat(modifyProfilePage.getPhoneElement().getValue()).isEqualTo("33333333");
		assertThat(modifyProfilePage.getStartTabElement().getCheckedValues().get(0))
				.isEqualTo($("HomeView.tab." + RMNavigationConfiguration.LAST_VIEWED_DOCUMENTS));
		assertThat(modifyProfilePage.getDefaultTaxonomyElement().getCheckedValues().get(0)).isEqualTo("Plan de classification");
	}

	private void givenAdministratorInModifyProfilePageThenCanEditOnlyStartTabAndDefaultTaxonomy() {
		modifyProfilePage.navigateToPage();

		assertThat(modifyProfilePage.getFirstNameElement().isEnabled()).isFalse();
		assertThat(modifyProfilePage.getLastNameElement().isEnabled()).isFalse();
		assertThat(modifyProfilePage.getPhoneElement().isEnabled()).isFalse();
		assertThat(modifyProfilePage.getEmailElement().isEnabled()).isFalse();
		modifyProfilePage.getStartTabElement().toggle($("HomeView.tab." + RMNavigationConfiguration.LAST_VIEWED_DOCUMENTS));
		modifyProfilePage.getDefaultTaxonomyElement().toggle("Plan de classification");
		modifyProfilePage.getSaveButton().click();
		modifyProfilePage.waitForPageReload();

		modifyProfilePage.navigateToPage();
		assertThat(modifyProfilePage.getFirstNameElement().getValue()).isEqualTo("");
		assertThat(modifyProfilePage.getLastNameElement().getValue()).isEqualTo("");
		assertThat(modifyProfilePage.getEmailElement().getValue()).isEqualTo("");
		assertThat(modifyProfilePage.getPhoneElement().getValue()).isEqualTo("");
		assertThat(modifyProfilePage.getStartTabElement().getCheckedValues().get(0))
				.isEqualTo($("HomeView.tab." + RMNavigationConfiguration.LAST_VIEWED_DOCUMENTS));
		assertThat(modifyProfilePage.getDefaultTaxonomyElement().getCheckedValues().get(0)).isEqualTo("Plan de classification");
	}

	private void givenListGlobalGroupsPageThenCannotAddOrEdit() {
		listGlobalGroupPage.navigateToListGlobalGroupsPage();

		assertThat(listGlobalGroupPage.getAddButton().isEnabled()).isFalse();
		assertThat(listGlobalGroupPage.findEditButtonElements()).isEmpty();
		assertThat(displayGlobalGroupPage.findDeleteButtonElements()).isEmpty();
	}

	private void givenDisplayGlobalGroupsPageThenAllEditAddAndDeleteButtonsAreInvisibles() {
		listGlobalGroupPage.getDisplayGlobalGroupButtonOnIndex(0).click();
		listGlobalGroupPage.waitForPageReload();

		assertThat(displayGlobalGroupPage.findEditButtonElements()).hasSize(1);
		assertThat(displayGlobalGroupPage.getEditGlobalGroupButtonMenuAction().isEnabled()).isFalse();
		assertThat(displayGlobalGroupPage.findAddButtonElements()).isEmpty();
		assertThat(displayGlobalGroupPage.getAddSubGlobalGroupButtonMenuAction().isEnabled()).isFalse();
		assertThat(displayGlobalGroupPage.findDeleteButtonElements()).hasSize(1);
		assertThat(displayGlobalGroupPage.getDeleteGlobalGroupButtonMenuAction().isEnabled()).isFalse();
	}

	//
	private void navigatoToListUserCredentialPage()
			throws Exception {
		driver.navigateTo().appManagement();
		listUserCredentialPage.navigateToListUserCredentialsPage();
	}

	private void givenAdministrator() {
		addAdministratorInCollection(userServices);
		sessionContext = loggedAsUserInCollection(ADMINISTRATOR, zeCollection);
		driver = newWebDriver(sessionContext);
		addEditUserCredentialPage = new AddEditUserCredentialPage(driver);
		listUserCredentialPage = new ListUserCredentialPage(driver);
		displayUserCredentialPage = new DisplayUserCredentialPage(driver);
		listGlobalGroupPage = new ListGlobalGroupPage(driver);
		displayGlobalGroupPage = new DisplayGlobalGroupPage(driver);
		addEditGlobalGroupPage = new AddEditGlobalGroupPage(driver);
		modifyProfilePage = new ModifyProfilePage(driver);
	}

	private void givenAdmin() {
		sessionContext = loggedAsUserInCollection("admin", zeCollection);
		driver = newWebDriver(sessionContext);
		addEditUserCredentialPage = new AddEditUserCredentialPage(driver);
		listUserCredentialPage = new ListUserCredentialPage(driver);
		displayUserCredentialPage = new DisplayUserCredentialPage(driver);
		listGlobalGroupPage = new ListGlobalGroupPage(driver);
		displayGlobalGroupPage = new DisplayGlobalGroupPage(driver);
		addEditGlobalGroupPage = new AddEditGlobalGroupPage(driver);
		modifyProfilePage = new ModifyProfilePage(driver);
		driver.snapshot("0");
	}

	private void setupLDAPWithoutSynchronization(UserServices userServices) {
		userSync = LDAPTestConfig.getLDAPUserSyncConfiguration();
		userSync.setDurationBetweenExecution(null);
		serverConf = LDAPTestConfig.getLDAPServerConfiguration();
		ldapConfigurationManager.saveLDAPConfiguration(serverConf, userSync);
		getModelLayerFactory().getLdapUserSyncManager().synchronizeIfPossible();
	}

	private void addAdministratorInCollection(UserServices userServices) {
		UserCredential administrator = userServices.getUser(ADMINISTRATOR);
		userServices.execute(administrator.getUsername(), (req) -> req.addCollection(zeCollection));
		administratorInZeCollection = userServices.getUserInCollection(ADMINISTRATOR, zeCollection);
		List<String> roles = new ArrayList<>(administratorInZeCollection.getUserRoles());
		roles.add(RGD);
		administratorInZeCollection.setUserRoles(roles);
		SchemaPresenterUtils presenterUtils = new SchemaPresenterUtils(User.DEFAULT_SCHEMA, ConstellioFactories
				.getInstance(),
				sessionContext);
		presenterUtils.addOrUpdate(administratorInZeCollection.getWrappedRecord());
	}

	private void synchronizeLDAP() {
		userSync.setDurationBetweenExecution(Duration.standardDays(1));
		ldapConfigurationManager.saveLDAPConfiguration(serverConf, userSync);
		getModelLayerFactory().getLdapUserSyncManager().synchronizeIfPossible();
	}

	private void givenUserAddPage() {

		listUserCredentialPage.getAddButton().clickAndWaitForPageReload();
	}

	private void givenGroupAddPage() {
		listGlobalGroupPage.getAddButton().clickAndWaitForPageReload();
	}

	private void givenEditUserPageForIndex(int index) {
		listUserCredentialPage.getEditUserCredentialButtonOnIndex(index).clickAndWaitForPageReload();
	}

	private void givenEditPageForUser(String user) {
		listUserCredentialPage.getSearchInput().setValue(user);
		listUserCredentialPage.getSearchButton().clickAndWaitForPageReload();
		listUserCredentialPage.getEditUserCredentialButtonOnIndex(0).clickAndWaitForPageReload();
	}

	private void givenEditPageForGroup(String group) {
		listGlobalGroupPage.getSearchInput().setValue(group);
		listGlobalGroupPage.getSearchButton().clickAndWaitForPageReload();
		listGlobalGroupPage.getEditGlobalGroupButtonOnIndex(0).clickAndWaitForPageReload();
	}

	private void givenDisplayPageForUser(String user) {
		listUserCredentialPage.getSearchInput().setValue(user);
		listUserCredentialPage.getSearchButton().clickAndWaitForPageReload();
		listUserCredentialPage.getDisplayUserCredentialButtonOnIndex(0).clickAndWaitForPageReload();
	}

	private void givenDisplayPageForGroup(String group) {
		listGlobalGroupPage.getSearchInput().setValue(group);
		listGlobalGroupPage.getSearchButton().clickAndWaitForPageReload();
		listGlobalGroupPage.getDisplayGlobalGroupButtonOnIndex(0).clickAndWaitForPageReload();
	}

	//

	private void givenDisplayUserPageForIndex(int index) {
		listUserCredentialPage.navigateToListUserCredentialsPage();
		listUserCredentialPage.getDisplayUserCredentialButtonOnIndex(index).clickAndWaitForPageReload();
	}

}
