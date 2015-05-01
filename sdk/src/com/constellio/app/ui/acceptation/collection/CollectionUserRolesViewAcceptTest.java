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

import org.junit.Before;
import org.junit.Test;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.ui.framework.buttons.DeleteButton;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.annotations.UiTest;
import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebDriver;

@UiTest
public class CollectionUserRolesViewAcceptTest extends ConstellioTest {
	public static final String USER = "Utilisateur";
	public static final String RGD = "Responsable de la gestion documentaire";
	public static final String MANAGER = "Gestionnaire";

	RMTestRecords records;
	ConstellioWebDriver driver;
	CollectionUserRolesPage page;
	User user;

	@Before
	public void setUp()
			throws Exception {
		givenCollection(zeCollection).withConstellioRMModule().withAllTestUsers();
		records = new RMTestRecords(zeCollection).setup(getModelLayerFactory());

		driver = newWebDriver(loggedAsUserInCollection(admin, zeCollection));
		user = records.getAlice();
		page = new CollectionUserRolesPage(driver).navigateToPage(user);
	}

	@Test
	public void givenRolesAddedAndRemovedThenAddAndRemoveTheRoles() {
		assertThat(page.getTitle()).contains(user.getTitle());
		assertThat(page.getAddRoleButton().isEnabled()).isFalse();
		assertThat(page.getRoleTable().countRows()).isEqualTo(1);

		assertThat(page.getRoleTable().hasRowWithValueInColumn(USER, 0)).isTrue();

		page.addRole(RGD);
		assertThat(page.getRoleTable().hasRowWithValueInColumn(USER, 0)).isTrue();
		assertThat(page.getRoleTable().hasRowWithValueInColumn(RGD, 0)).isTrue();

		page.addRole(MANAGER);
		assertThat(page.getRoleTable().hasRowWithValueInColumn(USER, 0)).isTrue();
		assertThat(page.getRoleTable().hasRowWithValueInColumn(RGD, 0)).isTrue();
		assertThat(page.getRoleTable().hasRowWithValueInColumn(MANAGER, 0)).isTrue();

		page.getRoleTable().getFirstRowWithValueInColumn(USER, 0).clickButtonAndConfirm(DeleteButton.BUTTON_STYLE);
		assertThat(page.getRoleTable().hasRowWithValueInColumn(USER, 0)).isFalse();

	}
}
