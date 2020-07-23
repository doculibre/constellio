package com.constellio.app.services.schemas.bulkImport;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.services.schemas.bulkImport.data.ImportDataProvider;
import com.constellio.app.services.schemas.bulkImport.data.xml.XMLImportDataProvider;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.users.SystemWideUserInfos;
import com.constellio.model.services.users.UserServices;
import com.constellio.model.services.users.UserServicesRuntimeException;
import com.constellio.sdk.tests.ConstellioTest;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

public class UserImportServicesAcceptanceTest extends ConstellioTest {
	RMSchemasRecordsServices rm;
	RMTestRecords records = new RMTestRecords(zeCollection);
	LocalDateTime shishOClock = new LocalDateTime().minusHours(1);

	BulkImportProgressionListener progressionListener = new LoggerBulkImportProgressionListener();
	UserImportServices importServices;
	SearchServices searchServices;
	UserServices userServices;
	User admin;

	@Before
	public void setUp()
			throws Exception {

		prepareSystem(
				withZeCollection().withConstellioRMModule().withAllTestUsers().withRMTest(records)
		);

		progressionListener = new LoggerBulkImportProgressionListener();
		importServices = new UserImportServices(getModelLayerFactory(), 100);
		searchServices = getModelLayerFactory().newSearchServices();
		userServices = getModelLayerFactory().newUserServices();

		admin = getModelLayerFactory().newUserServices().getUserInCollection("admin", zeCollection);

		rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
	}

	@Test
	public void whenImportingUsersXMLFileThenImportedCorrectly()
			throws Exception {
		File usersFile = getTestResourceFile("user.xml");
		importAndValidate(XMLImportDataProvider.forSingleXMLFile(getModelLayerFactory(), usersFile));
	}

	private void importAndValidate(ImportDataProvider importDataProvider) {
		List<String> collections = new ArrayList<>();
		collections.add(zeCollection);
		BulkImportResults results = importServices.bulkImport(importDataProvider, null, null, collections);
		assertThat(results.getImportErrors().size()).isEqualTo(0);
		validateAlice();

		validateBob();
	}

	@Test
	public void whenImportingUserWithInvalidGroupThenImportOtherUsersAndGiveAdequateErrorMessage()
			throws Exception {
		File usersFile = getTestResourceFile("userAliceWithInvalidGroup.xml");
		importAndValidateWhenUserWithInvalidGroup(XMLImportDataProvider.forSingleXMLFile(getModelLayerFactory(), usersFile));
	}

	private void importAndValidateWhenUserWithInvalidGroup(ImportDataProvider importDataProvider) {
		List<String> collections = new ArrayList<>();
		collections.add(zeCollection);
		BulkImportResults results = importServices.bulkImport(importDataProvider, null, null, collections);
		assertThat(results.getImportErrors().size()).isEqualTo(1);
		ImportError mariImportError = results.getImportErrors().get(0);
		assertThat(mariImportError.getInvalidElementId()).isEqualTo("mari");
		assertThat(mariImportError.getCompleteErrorMessage())
				.contains("Invalid group");//$("UserServicesRuntimeException_InvalidGroup"));
		assertThat(mariImportError.getCompleteErrorMessage()).contains($("legends1"));

		try {
			userServices.getUserInfos("mari");
			fail("mari shouldn't exist");
		} catch (UserServicesRuntimeException.UserServicesRuntimeException_NoSuchUser e) {
		}

		validateBob();
	}

	private void validateBob() {
		SystemWideUserInfos bob = userServices.getUserInfos("bob");
		assertThat(bob.getFirstName()).isEqualTo("Bob 'Elvis'");
		assertThat(bob.getLastName()).isEqualTo("Gratton");
		assertThat(bob.getEmail()).isEqualTo("bob@doculibre.com");
		assertThat(bob.isSystemAdmin()).isEqualTo(true);
		assertThat(bob.getGlobalGroups().size()).isEqualTo(0);
		assertThat(bob.getCollections()).contains(zeCollection);
		assertThat(bob.getCollections().size()).isEqualTo(1);
	}

	private void validateAlice() {
		SystemWideUserInfos alice = userServices.getUserInfos("alice");
		assertThat(alice.getFirstName()).isEqualTo("Alice");
		assertThat(alice.getLastName()).isEqualTo("Wonderland");
		assertThat(alice.getEmail()).isEqualTo("alice@doculibre.com");
		assertThat(alice.isSystemAdmin()).isEqualTo(false);
		assertThat(alice.getGlobalGroups()).contains("legends", "heroes");
		assertThat(alice.getGlobalGroups().size()).isEqualTo(2);
		assertThat(alice.getCollections()).contains(zeCollection);
		assertThat(alice.getCollections().size()).isEqualTo(1);
	}
}
