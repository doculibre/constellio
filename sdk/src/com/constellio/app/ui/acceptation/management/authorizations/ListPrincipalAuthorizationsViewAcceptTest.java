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
package com.constellio.app.ui.acceptation.management.authorizations;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.model.entities.records.wrappers.RecordWrapper;
import com.constellio.model.entities.security.Authorization;
import com.constellio.model.entities.security.AuthorizationDetails;
import com.constellio.model.entities.security.CustomizedAuthorizationsBehavior;
import com.constellio.model.entities.security.Role;
import com.constellio.model.services.security.AuthorizationsServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.annotations.InDevelopmentTest;
import com.constellio.sdk.tests.annotations.UiTest;
import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebDriver;

@UiTest
public class ListPrincipalAuthorizationsViewAcceptTest extends ConstellioTest {
	AuthorizationsServices authorizationsService;
	RMTestRecords records;

	ConstellioWebDriver driver;
	ListAuthorizationsPage page;

	RecordWrapper principal;

	@Before
	public void setUp()
			throws Exception {
		givenCollection(zeCollection).withConstellioRMModule().withAllTestUsers();
		records = new RMTestRecords(zeCollection).setup(getModelLayerFactory());

		authorizationsService = getModelLayerFactory().newAuthorizationsServices();

		driver = newWebDriver(loggedAsUserInCollection(admin, zeCollection));
	}

	@Test
	@InDevelopmentTest
	public void openThePage() {
		givenHeroesWithMultiplePermissions();
		waitUntilICloseTheBrowsers();
	}

	@Test
	public void givenGroupWithTwoAuthorizationsThenDisplayTwoAuthorizations() {
		givenHeroesWithMultiplePermissions();
		assertThat(page.getTitle()).contains(principal.getTitle());
		assertThat(page.getOwnAuthorizationsTable().countRows()).isEqualTo(2);
	}

	@Test
	public void givenUserWithOwnAndInheritedAuthorizationsThenDisplayAuthorizations() {
		givenAliceWithMultiplePermissions();
		assertThat(page.getTitle()).contains(principal.getTitle());
		assertThat(page.getOwnAuthorizationsTable().countRows()).isEqualTo(1);
		assertThat(page.getInheritedAuthorizationsTable().countRows()).isEqualTo(1);
	}

	private void givenHeroesWithMultiplePermissions() {
		principal = records.getHeroes();
		addAuthorizationWithoutDetaching(readWriteDelete(), Arrays.asList(principal.getId()), records.unitId_10);
		addAuthorizationWithoutDetaching(read(), Arrays.asList(records.getHeroes().getId(), records.getLegends().getId()),
				records.unitId_30);
		waitForBatchProcesses();
		page = new ListAuthorizationsPage(driver).navigateToPage(principal);
	}

	private void givenAliceWithMultiplePermissions() {
		principal = records.getAlice();
		addAuthorizationWithoutDetaching(readWriteDelete(), Arrays.asList(principal.getId()), records.unitId_10);
		addAuthorizationWithoutDetaching(read(), Arrays.asList(records.getHeroes().getId(), records.getLegends().getId()),
				records.unitId_30);
		waitForBatchProcesses();
		page = new ListAuthorizationsPage(driver).navigateToPage(principal);
	}

	private Authorization addAuthorizationWithoutDetaching(List<String> roles, List<String> principals, String record) {
		AuthorizationDetails details = AuthorizationDetails.create(aString(), roles, zeCollection);
		Authorization authorization = new Authorization(details, principals, Arrays.asList(record));
		authorizationsService.add(authorization, CustomizedAuthorizationsBehavior.KEEP_ATTACHED, null);
		return authorization;
	}

	private List<String> read() {
		return Arrays.asList(Role.READ);
	}

	private List<String> readWriteDelete() {
		return Arrays.asList(Role.READ, Role.WRITE, Role.DELETE);
	}

	private void waitForBatchProcesses() {
		getModelLayerFactory().getBatchProcessesManager().waitUntilAllFinished();
	}
}
