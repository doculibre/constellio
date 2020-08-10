package com.constellio.app.ui.pages.trash;

import com.constellio.app.modules.rm.DemoTestRecords;
import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.data.utils.TimeProvider;
import com.constellio.model.entities.records.wrappers.RecordWrapper;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.users.UserServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.annotations.InDevelopmentTest;
import com.constellio.sdk.tests.annotations.MainTestDefaultStart;
import com.constellio.sdk.tests.annotations.UiTest;
import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebDriver;
import com.constellio.sdk.tests.setups.Users;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;

@UiTest
@InDevelopmentTest
public class TrashViewImplAcceptanceTest extends ConstellioTest {
	Users users = new Users();

	RecordServices recordServices;
	ConstellioWebDriver driver;
	RMTestRecords records = new RMTestRecords(zeCollection);
	DemoTestRecords records2 = new DemoTestRecords("LaCollectionDeRida");
	RMSchemasRecordsServices schemas;

	@Before
	public void setUp()
			throws Exception {

		//givenBackgroundThreadsEnabled();

		givenTransactionLogIsEnabled();
		prepareSystem(
				withZeCollection().withConstellioRMModule().withAllTestUsers()
						.withRMTest(records).withFoldersAndContainersOfEveryStatus().withDocumentsDecommissioningList().withDocumentsHavingContent()
		);
		inCollection(zeCollection).setCollectionTitleTo("Collection de test");
		users.setUp(getModelLayerFactory().newUserServices(), zeCollection);


		recordServices = getModelLayerFactory().newRecordServices();

		UserServices userServices = getModelLayerFactory().newUserServices();
		String token = userServices.generateToken("admin");
		String serviceKey = userServices.getUser("admin").getServiceKey();
		System.out.println("Admin token : \"" + token + "\", Admin service key \"" + serviceKey + "\"");
		System.out.println("http://localhost:7070/constellio/select?token=" + token + "&serviceKey=" + serviceKey
						   + "&fq=-type_s:index" + "&q=*:*");

		//givenConfig(RMConfigs.DOCUMENT_RETENTION_RULES, true);


	}

	@Test
	@MainTestDefaultStart
	public void startOnHomePageAsAdmin()
			throws Exception {
		//getAppLayerFactory().getSystemGlobalConfigsManager().setReindexingRequired(true);
		//getDataLayerFactory().getDataLayerLogger().setPrintAllQueriesLongerThanMS(0);
		deleteLogicallySomeRecords();

		driver = newWebDriver(loggedAsUserInCollection(admin, zeCollection));
		waitUntilICloseTheBrowsers();
	}

	private void deleteLogicallySomeRecords()
			throws RecordServicesException {
		LocalDateTime now;
		givenTimeIs(now = TimeProvider.getLocalDateTime());
		User adminUser = users.adminIn(zeCollection);
		Folder folderA1 = records.getFolder_A01();
		recordServices.logicallyDelete(folderA1.getWrappedRecord(), adminUser);
		recordServices.logicallyDelete(records.getCategory_X13().getWrappedRecord(), adminUser);
		givenTimeIs(now.plusDays(1));
		recordServices.logicallyDelete(records.getDocumentWithContent_A49().getWrappedRecord(), adminUser);
		recordServices.add((RecordWrapper) folderA1.set(Schemas.ERROR_ON_PHYSICAL_DELETION.getLocalCode(), true));
	}
}
