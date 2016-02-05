package com.constellio.app.ui.acceptation.permissions;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.constellio.app.ui.pages.management.permissions.PermissionsManagementViewImpl.CreateRoleButton;
import com.constellio.model.entities.security.Role;
import com.constellio.model.services.extensions.ConstellioModulesManager;
import com.constellio.model.services.security.roles.RolesManager;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.annotations.UiTest;
import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebDriver;

@UiTest
public class PermissionsManagementViewAcceptTest extends ConstellioTest {
	ConstellioWebDriver driver;
	PermissionsManagementPage page;

	List<String> permissions;
	Role loser;
	Role god;

	@Before
	public void setUp()
			throws Exception {
		prepareSystem(
				withZeCollection().withConstellioRMModule().withAllTestUsers()
		);
		inCollection(zeCollection).giveWriteAndDeleteAccessTo(admin);

		permissions = new ArrayList<>();
		ConstellioModulesManager modulesManager = getAppLayerFactory().getModulesManager();
		for (String permissionGroup : modulesManager.getPermissionGroups(zeCollection)) {
			permissions.addAll(modulesManager.getPermissionsInGroup(zeCollection, permissionGroup));
		}

		loser = new Role(zeCollection, "L", "Loser", new ArrayList<String>());
		god = new Role(zeCollection, "G", "God", permissions);

		RolesManager roleManager = getModelLayerFactory().getRolesManager();
		roleManager.addRole(loser);
		roleManager.addRole(god);

		driver = newWebDriver(loggedAsUserInCollection(admin, zeCollection));
		page = new PermissionsManagementPage(driver).navigateToPage();
	}

	@Test
	public void givenBaseConfigurationThenDisplayCorrectPermissions() {
		verifyCleanPermissionState();
	}

	@Test
	public void givenPermissionsModifiedThenUpdateTheActionButtons() {
		assertThat(page.getCreateRoleButton().isEnabled()).isTrue();
		assertThat(page.getSaveButton().isEnabled()).isFalse();
		assertThat(page.getRevertButton().isEnabled()).isFalse();

		page.getPermission("L", permissions.get(0)).toggle();
		assertThat(page.getPermission("L", permissions.get(0)).isChecked()).isTrue();
		assertThat(page.getCreateRoleButton().isEnabled()).isTrue();
		assertThat(page.getSaveButton().isEnabled()).isTrue();
		assertThat(page.getRevertButton().isEnabled()).isTrue();

		page.getPermission("L", permissions.get(0)).toggle();
		assertThat(page.getPermission("L", permissions.get(0)).isChecked()).isFalse();
		assertThat(page.getCreateRoleButton().isEnabled()).isTrue();
		assertThat(page.getSaveButton().isEnabled()).isFalse();
		assertThat(page.getRevertButton().isEnabled()).isFalse();
	}

	@Test
	public void givenPermissionsModifiedAndRevertedThenDisplayCorrectPermissions() {
		for (String permission : permissions) {
			page.getPermission("L", permission).toggle();
			page.getPermission("G", permission).toggle();
		}
		page.getRevertButton().click();
		verifyCleanPermissionState();
	}

	@Test
	public void givenPermissionsModifiedAndSavedThenDisplayCorrectPermissions() {
		for (String permission : permissions) {
			page.getPermission("L", permission).toggle();
			page.getPermission("G", permission).toggle();
		}
		page.getSaveButton().click();
		verifyInvertedPermissionState();
	}

	@Test
	public void givenRoleAddedThenDisplayAddedRole() {
		page.openCreateRoleForm()
				.setValue(CreateRoleButton.ROLE_CODE, "N")
				.setValue(CreateRoleButton.ROLE_TITLE, "New")
				.clickSaveButtonAndWaitForPageReload();
		verifyCleanPermissionState();
		for (String permission : permissions) {
			assertThat(page.getPermission("N", permission).isChecked()).isFalse();
		}
	}

	private void verifyCleanPermissionState() {
		for (String permission : permissions) {
			assertThat(page.getPermission("L", permission).isChecked()).isFalse();
			assertThat(page.getPermission("G", permission).isChecked()).isTrue();
		}
	}

	private void verifyInvertedPermissionState() {
		for (String permission : permissions) {
			assertThat(page.getPermission("L", permission).isChecked()).isTrue();
			assertThat(page.getPermission("G", permission).isChecked()).isFalse();
		}
	}
}
