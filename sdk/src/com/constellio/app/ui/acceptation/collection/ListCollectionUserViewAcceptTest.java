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
package com.constellio.app.ui.acceptation.collection;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.ui.framework.buttons.AuthorizationsButton;
import com.constellio.app.ui.framework.buttons.DeleteButton;
import com.constellio.app.ui.framework.buttons.DisplayButton;
import com.constellio.app.ui.framework.buttons.RolesButton;
import com.constellio.app.ui.tools.RecordContainerWebElement;
import com.constellio.model.entities.records.wrappers.Group;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.security.global.GlobalGroup;
import com.constellio.model.entities.security.global.GlobalGroupStatus;
import com.constellio.model.entities.security.global.UserCredential;
import com.constellio.model.services.security.roles.RolesManager;
import com.constellio.model.services.users.UserServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.annotations.UiTest;
import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebDriver;

@UiTest
public class ListCollectionUserViewAcceptTest extends ConstellioTest {

	public static final int USERNAME_COLUMN = 1;
	public static final int GROUPCODE_COLUMN = 0;

	public static final String GROUP_CODE = "test";
	public static final String GROUP_NAME = "Test group";
	public static final String CHUCK = "chuck";
	public static final String CHUCK_NORRIS = "Chuck Norris";
	public static final String SYSTEM_ADMIN = "System Admin";

	UserServices userServices;
	RolesManager rolesManager;
	RMTestRecords records = new RMTestRecords(zeCollection);
	ConstellioWebDriver driver;
	CollectionSecurityPage page;

	@Before
	public void setUp()
			throws Exception {
		prepareSystem(
				withZeCollection().withConstellioRMModule().withAllTestUsers().withRMTest(records)
		);

		userServices = getModelLayerFactory().newUserServices();
		userServices.addUpdateGlobalGroup(
				new GlobalGroup(GROUP_CODE, GROUP_NAME, Arrays.asList(zeCollection), null, GlobalGroupStatus.ACTIVE));
		rolesManager = getModelLayerFactory().getRolesManager();

		driver = newWebDriver(loggedAsUserInCollection(admin, zeCollection));
		page = new CollectionSecurityPage(driver).navigateToPage();
	}

	@Test
	public void givenBaseConfigurationThenDisplayCorrectInformation()
			throws Exception {
		RecordContainerWebElement groups = page.getGroupTable();
		assertThat(groups.countRows()).isEqualTo(1);
		assertThat(groups.hasRowWithValueInColumn(GROUP_CODE, GROUPCODE_COLUMN)).isTrue();

		List<UserCredential> test_users = records.getUsers().getAllUsers();
		RecordContainerWebElement users = page.getUserTable();
		assertThat(users.countRows()).isEqualTo(test_users.size());
		for (UserCredential user : test_users) {
			String fullName = user.getFirstName() + " " + user.getLastName();
			assertThat(users.hasRowWithValueInColumn(fullName, USERNAME_COLUMN)).isTrue();
		}
	}

	@Test
	public void givenNavigationButtonsClickedThenNavigateToTheCorrectPage() {
		page.getGroupWithCode(GROUP_CODE).clickButton(DisplayButton.BUTTON_STYLE);
		assertThat(driver.findRequiredElement(By.id("display-value-group_default_code")).getText()).isEqualTo(GROUP_CODE);

		page.navigateToPage().getGroupWithCode(GROUP_CODE).clickButton(AuthorizationsButton.BUTTON_STYLE);
		assertThat(page.getTitle()).contains("Autorisations").contains(GROUP_NAME);

		page.navigateToPage().getGroupWithCode(GROUP_CODE).clickButton(RolesButton.BUTTON_STYLE);
		assertThat(page.getTitle()).contains("Rôles").contains(GROUP_NAME);

		page.navigateToPage().getUserWithName(CHUCK_NORRIS).clickButton(DisplayButton.BUTTON_STYLE);
		assertThat(driver.findRequiredElement(By.id("display-value-user_default_username")).getText()).isEqualTo(CHUCK);

		page.navigateToPage().getUserWithName(CHUCK_NORRIS).clickButton(AuthorizationsButton.BUTTON_STYLE);
		assertThat(page.getTitle()).contains("Autorisations").contains(CHUCK_NORRIS);

		page.navigateToPage().getUserWithName(CHUCK_NORRIS).clickButton(RolesButton.BUTTON_STYLE);
		assertThat(page.getTitle()).contains("Rôles").contains(CHUCK_NORRIS);
	}

	@Test
	public void givenUserTriesToRemoveHimselfThenDontRemoveTheUser() {
		String message = page.getUserWithName(SYSTEM_ADMIN)
				.clickButtonAndConfirmAndWaitForWarningMessage(DeleteButton.BUTTON_STYLE);

		assertThat(message).isNotNull();
		assertThat(page.getUserTable().hasRowWithValueInColumn(SYSTEM_ADMIN, USERNAME_COLUMN)).isTrue();
	}

	@Test
	public void givenGroupIsAddedAndRemovedThenAddAndRemoveTheGroup() {
		page.getGroupWithCode(GROUP_CODE).clickButtonAndConfirm(DeleteButton.BUTTON_STYLE);
		assertThat(page.getGroupTable().hasRowWithValueInColumn(GROUP_CODE, GROUPCODE_COLUMN)).isFalse();
		page.addGroupAndRole(GROUP_CODE, "Utilisateur");
		assertThat(page.getGroupTable().hasRowWithValueInColumn(GROUP_CODE, GROUPCODE_COLUMN)).isTrue();
	}

	@Test
	public void givenUserIsAddedAndRemovedThenAddAndRemoveTheUser() {
		page.getUserWithName(CHUCK_NORRIS).clickButtonAndConfirm(DeleteButton.BUTTON_STYLE);
		assertThat(page.getUserTable().hasRowWithValueInColumn(CHUCK_NORRIS, USERNAME_COLUMN)).isFalse();

		page.addUserAndRole(CHUCK, "Utilisateur");
		page.waitForPageReload();

		assertThat(page.getUserTable().hasRowWithValueInColumn(CHUCK_NORRIS, USERNAME_COLUMN)).isTrue();
	}

	@Test
	public void whenSelectGroupAndRoleThenVerifyIfAddButtonIsEnable()
			throws Exception {
		assertThat(page.getGroupAddButton().isEnabled()).isFalse();

		page.getGroupLookup().typeAndSelectFirst(GROUP_CODE);

		assertThat(page.getGroupAddButton().isEnabled()).isFalse();

		page.getGroupRolesListSelect().typeAndSelectFirst("Utilisateur");

		assertThat(page.getGroupAddButton().isEnabled()).isTrue();

		page.getGroupLookup().typeAndSelectFirst("" + Keys.BACK_SPACE);
		//		page.getGroupLookup().expandOptions().select(0);

		assertThat(page.getGroupAddButton().isEnabled()).isFalse();
	}

	@Test
	public void whenSelectUserAndRoleThenVerifyIfAddButtonIsEnable()
			throws Exception {
		assertThat(page.getUserAddButton().isEnabled()).isFalse();

		page.getUserLookup().typeAndSelectFirst(CHUCK);

		assertThat(page.getUserAddButton().isEnabled()).isFalse();

		page.getUserRolesListSelect().typeAndSelectFirst("Utilisateur");

		assertThat(page.getUserAddButton().isEnabled()).isTrue();

		page.getUserLookup().typeAndSelectFirst("" + Keys.BACK_SPACE);
		//		page.getUserLookup().expandOptions().select(0);

		assertThat(page.getUserAddButton().isEnabled()).isFalse();
	}

	@Test
	public void whenRemoveOrAddUserWithRolesThenOk()
			throws Exception {
		page.addUserAndRole(CHUCK, "Gestionnaire");
		page.waitForPageReload();
		assertThat(getUserRoles(CHUCK)).contains("M");
		page.getUserWithName(CHUCK_NORRIS).clickButtonAndConfirm(DeleteButton.BUTTON_STYLE);
		assertThat(page.getUserTable().hasRowWithValueInColumn(CHUCK_NORRIS, USERNAME_COLUMN)).isFalse();

		page.addUserAndRole(CHUCK, "Utilisateur");
		page.waitForPageReload();

		assertThat(page.getUserTable().hasRowWithValueInColumn(CHUCK_NORRIS, USERNAME_COLUMN)).isTrue();
		assertThat(getUserRoles(CHUCK)).contains("U");
	}

	@Test
	public void whenRemoveOrAddGroupWithRolesThenOk()
			throws Exception {
		page.addGroupAndRole(GROUP_CODE, "Gestionnaire");
		page.getGroupWithCode(GROUP_CODE).clickButtonAndConfirm(DeleteButton.BUTTON_STYLE);
		assertThat(getGroupRoles(GROUP_CODE)).contains("M");

		page.addGroupAndRole(GROUP_CODE, "Utilisateur");

		assertThat(page.getGroupTable().hasRowWithValueInColumn(GROUP_CODE, GROUPCODE_COLUMN)).isTrue();
		assertThat(getGroupRoles(GROUP_CODE)).contains("U");
	}

	public List<String> getUserRoles(String username) {
		List<String> result = new ArrayList<>();
		User user = userServices.getUserRecordInCollection(username, zeCollection);
		if (user != null) {
			for (String role : user.getUserRoles()) {
				result.add(role);
			}
		}
		return result;
	}

	public List<String> getGroupRoles(String groupCode) {
		List<String> result = new ArrayList<>();
		Group group = userServices.getGroupInCollection(groupCode, zeCollection);
		if (group != null) {
			for (String role : group.getRoles()) {
				result.add(role);
			}
		}
		return result;
	}
}
