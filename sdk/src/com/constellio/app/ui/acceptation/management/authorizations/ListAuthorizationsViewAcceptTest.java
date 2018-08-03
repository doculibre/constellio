package com.constellio.app.ui.acceptation.management.authorizations;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.model.entities.records.wrappers.RecordWrapper;
import com.constellio.model.entities.security.Authorization;
import com.constellio.model.entities.security.Role;
import com.constellio.model.services.security.AuthorizationsServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.annotations.InDevelopmentTest;
import com.constellio.sdk.tests.annotations.UiTest;
import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebDriver;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static com.constellio.model.entities.security.global.AuthorizationAddRequest.authorizationInCollection;
import static org.assertj.core.api.Assertions.assertThat;

@UiTest
public class ListAuthorizationsViewAcceptTest extends ConstellioTest {
	AuthorizationsServices authorizationsService;
	RMTestRecords records = new RMTestRecords(zeCollection);

	ConstellioWebDriver driver;
	ListAuthorizationsPage page;

	RecordWrapper record;

	@Before
	public void setUp()
			throws Exception {
		prepareSystem(
				withZeCollection().withConstellioRMModule().withAllTestUsers().withRMTest(records)
		);

		authorizationsService = getModelLayerFactory().newAuthorizationsServices();

		driver = newWebDriver(loggedAsUserInCollection(admin, zeCollection));
	}

	@Test
	@InDevelopmentTest
	public void openThePrincipalPage() {
		givenHeroesWithMultiplePermissions();
		waitUntilICloseTheBrowsers();
	}

	@Test
	@InDevelopmentTest
	public void openTheObjectPage() {
		givenUnit10WithMultiplePermissions();
		waitUntilICloseTheBrowsers();
	}

	@Test
	public void givenGroupWithTwoAuthorizationsThenDisplayTwoAuthorizations() {
		givenHeroesWithMultiplePermissions();
		assertThat(page.getTitle()).contains(record.getTitle());
		assertThat(page.getOwnAuthorizationsTable().countRows()).isEqualTo(2);
	}

	@Test
	public void givenUserWithOwnAndInheritedAuthorizationsThenDisplayAuthorizations() {
		givenAliceWithMultiplePermissions();
		assertThat(page.getTitle()).contains(record.getTitle());
		assertThat(page.getOwnAuthorizationsTable().countRows()).isEqualTo(1);
		assertThat(page.getInheritedAuthorizationsTable().countRows()).isEqualTo(1);
	}

	@Test
	public void givenAdministrativeUnitWithTwoAuthorizationsThenDisplayTwoAuthorizations() {
		givenUnit10WithMultiplePermissions();
		assertThat(page.getTitle()).contains(record.getTitle());
		assertThat(page.getOwnAuthorizationsTable().countRows()).isEqualTo(2);
	}

	private void givenHeroesWithMultiplePermissions() {
		record = records.getHeroes();
		addAuthorizationWithoutDetaching(readWriteDelete(), Arrays.asList(record.getId()), records.unitId_10);
		addAuthorizationWithoutDetaching(read(), Arrays.asList(records.getHeroes().getId(), records.getLegends().getId()),
				records.unitId_30);
		waitForBatchProcesses();
		page = new ListAuthorizationsPage(driver).navigateToPrincipalPage(record);
	}

	private void givenAliceWithMultiplePermissions() {
		record = records.getAlice();
		addAuthorizationWithoutDetaching(readWriteDelete(), Arrays.asList(record.getId()), records.unitId_10);
		addAuthorizationWithoutDetaching(read(), Arrays.asList(records.getHeroes().getId(), records.getLegends().getId()),
				records.unitId_30);
		waitForBatchProcesses();
		page = new ListAuthorizationsPage(driver).navigateToPrincipalPage(record);
	}

	private void givenUnit10WithMultiplePermissions() {
		record = records.getUnit10();
		page = new ListAuthorizationsPage(driver).navigateToObjectPage(record);
	}

	private Authorization addAuthorizationWithoutDetaching(List<String> roles, List<String> principals, String record) {
		String id = authorizationsService.add(
				authorizationInCollection(zeCollection).forPrincipalsIds(principals).on(record).giving(roles));
		return authorizationsService.getAuthorization(zeCollection, id);
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
