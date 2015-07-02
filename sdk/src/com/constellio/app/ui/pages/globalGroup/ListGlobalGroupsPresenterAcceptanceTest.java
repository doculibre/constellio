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
package com.constellio.app.ui.pages.globalGroup;

import java.util.ArrayList;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.constellio.app.ui.application.NavigatorConfigurationService;
import com.constellio.model.entities.security.global.GlobalGroup;
import com.constellio.model.entities.security.global.GlobalGroupStatus;
import com.constellio.model.entities.security.global.UserCredential;
import com.constellio.model.entities.security.global.UserCredentialStatus;
import com.constellio.model.services.users.GlobalGroupsManager;
import com.constellio.model.services.users.UserServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.annotations.InDevelopmentTest;
import com.constellio.sdk.tests.annotations.UiTest;
import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebDriver;

@UiTest
@InDevelopmentTest
public class ListGlobalGroupsPresenterAcceptanceTest extends ConstellioTest {

	ConstellioWebDriver driver;
	UserServices userServices;
	GlobalGroupsManager globalGroupsManager;

	@Before
	public void setUp()
			throws Exception {

		prepareSystem(
				withZeCollection().withConstellioRMModule().withAllTestUsers()
		);

		userServices = getModelLayerFactory().newUserServices();
		globalGroupsManager = getModelLayerFactory().getGlobalGroupsManager();

		createGroup("Legends", "Legends group");
		createGroup("Heroes", "Heroes group");
		createGroup("BGroup", "B group");
		createGroup("AGroup", "A group");
		createGroup("DGroup", "D group");
		createGroup("CGroup", "C group");
		createGroup("FGroup", "F group");
		createGroup("EGroup", "E group");

		createUser();

		driver = newWebDriver(loggedAsUserInCollection("admin", zeCollection));
	}

	@Test
	@Ignore
	public void whenNavigateToUserCredentialsListThenOk()
			throws Exception {

		driver.navigateTo().url(NavigatorConfigurationService.USER_LIST);
		waitUntilICloseTheBrowsers();
	}

	//

	private void createUser() {
		UserCredential userCredential = new UserCredential("dakota", "Dakota", "Indien", "dakota@gmail.com",
				Arrays.asList("AGroup", "BGroup"),
				Arrays.asList(zeCollection), UserCredentialStatus.ACTIVE, null);
		userServices.addUpdateUserCredential(userCredential);
	}

	private void createGroup(String code, String name) {
		GlobalGroup globalGroup = new GlobalGroup(code, name, new ArrayList<String>(), null, GlobalGroupStatus.ACTIVE);
		userServices.addUpdateGlobalGroup(globalGroup);
	}
}