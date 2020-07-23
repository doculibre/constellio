package com.constellio.app.ui;

import com.constellio.app.modules.rm.DemoTestRecords;
import com.constellio.app.modules.rm.RMConfigs;
import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.model.enums.DocumentsTypeChoice;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.tasks.services.TasksSchemasRecordsServices;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.data.dao.services.factories.DataLayerFactory;
import com.constellio.data.utils.dev.Toggle;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.schemas.MetadataNetwork;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.schemas.MetadataSchemaTypesAlteration;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.search.SearchServices.RecordIdVersion;
import com.constellio.model.services.users.UserServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.annotations.MainTest;
import com.constellio.sdk.tests.annotations.MainTestDefaultStart;
import com.constellio.sdk.tests.annotations.UiTest;
import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebDriver;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.Iterator;

import static com.constellio.model.entities.schemas.MetadataValueType.DATE;
import static com.constellio.model.entities.schemas.MetadataValueType.DATE_TIME;
import static com.constellio.model.entities.schemas.MetadataValueType.STRING;

@UiTest
@MainTest
public class StartDemoRMConstellioWithoutPluginsAcceptTest extends ConstellioTest {

	RecordServices recordServices;
	ConstellioWebDriver driver;
	RMTestRecords records = new RMTestRecords(zeCollection);
	DemoTestRecords records2 = new DemoTestRecords("LaCollectionDeRida");
	RMSchemasRecordsServices schemas;

	@Before
	public void setUp()
			throws Exception {
		givenBackgroundThreadsEnabled();

		givenTransactionLogIsEnabled();
		prepareSystem(
				withZeCollection().withConstellioRMModule().withAllTestUsers()
						.withRMTest(records).withFoldersAndContainersOfEveryStatus()
						.withDocumentsHavingContent().withDocumentsDecommissioningList()
		);
		inCollection(zeCollection).setCollectionTitleTo("Collection de test");

		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
		Iterator<RecordIdVersion> idVersionIterator =
				getModelLayerFactory().newSearchServices().recordsIdVersionIteratorUsingSolr(rm.folder.schemaType());
		while (idVersionIterator.hasNext()) {
			System.out.println(idVersionIterator.next().getRecordId());
		}

		recordServices = getModelLayerFactory().newRecordServices();
		AppLayerFactory appLayerFactory = getAppLayerFactory();

		DataLayerFactory dataLayerFactory = appLayerFactory.getModelLayerFactory().getDataLayerFactory();

		UserServices userServices = getModelLayerFactory().newUserServices();
		String token = userServices.generateToken("admin");
		String serviceKey = userServices.getUserConfigs("admin").getServiceKey();
		System.out.println("Admin token : \"" + token + "\", Admin service key \"" + serviceKey + "\"");
		System.out.println("http://localhost:7070/constellio/select?token=" + token + "&serviceKey=" + serviceKey
						   + "&fq=-type_s:index" + "&q=*:*");

		//givenConfig(RMConfigs.DOCUMENT_RETENTION_RULES, true);

		getModelLayerFactory().getMetadataSchemasManager().modify(zeCollection, new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				types.getSchema(Document.DEFAULT_SCHEMA).create("dateMeta1").setType(DATE)
						.addLabel(Language.French, "Date metadata 1");
				types.getSchema(Document.DEFAULT_SCHEMA).create("dateMeta2").setType(DATE)
						.addLabel(Language.French, "Date metadata 2");
				types.getSchema(Document.DEFAULT_SCHEMA).create("dateTimeMeta").setType(DATE_TIME)
						.addLabel(Language.French, "Datetime metadata");
			}
		});


	}

	@Test
	@MainTestDefaultStart
	public void startOnHomePageAsAdmin()
			throws Exception {
		//getAppLayerFactory().getSystemGlobalConfigsManager().setReindexingRequired(true);
		//getDataLayerFactory().getDataLayerLogger().setPrintAllQueriesLongerThanMS(0);
		setup();
		Toggle.ENABLE_PDFTRON_TRIAL.enable();
		MetadataNetwork network = getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(zeCollection)
				.getMetadataNetwork();

		driver = newWebDriver();
		waitUntilICloseTheBrowsers();

		// facebook.com
	}

	private void setup() {
		givenConfig(RMConfigs.DOCUMENTS_TYPES_CHOICE, DocumentsTypeChoice.ALL_DOCUMENTS_TYPES);
		getModelLayerFactory().getMetadataSchemasManager().modify(zeCollection, new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				MetadataSchemaBuilder folderTestSchema = types.getSchemaType("folder").createCustomSchema("test");
				MetadataSchemaBuilder documentTestSchema = types.getSchemaType("document").createCustomSchema("test");
				MetadataSchemaBuilder taskTestSchema = types.getSchemaType("userTask").createCustomSchema("test");

				MetadataSchemaBuilder folderTest2Schema = types.getSchemaType("folder").createCustomSchema("test2");
				MetadataSchemaBuilder documentTest2Schema = types.getSchemaType("document").createCustomSchema("test2");
				MetadataSchemaBuilder taskTest2Schema = types.getSchemaType("userTask").createCustomSchema("test2");

				folderTestSchema.create("toto").setType(STRING).setDefaultValue("tata");
				documentTestSchema.create("toto").setType(STRING).setDefaultValue("tata");
				taskTestSchema.create("toto").setType(STRING).setDefaultValue("tata");

				folderTest2Schema.create("toto").setType(STRING).setDefaultValue("titi");
				documentTest2Schema.create("toto").setType(STRING).setDefaultValue("titi");
				taskTest2Schema.create("toto").setType(STRING).setDefaultValue("titi");

				folderTestSchema.create("metadataOnlyInTest").setType(STRING).setDefaultValue("tata");
				documentTestSchema.create("metadataOnlyInTest").setType(STRING).setDefaultValue("tata");
				taskTestSchema.create("metadataOnlyInTest").setType(STRING).setDefaultValue("tata");

			}
		});

		SchemasDisplayManager schemaDisplayManager = getAppLayerFactory().getMetadataSchemasDisplayManager();
		schemaDisplayManager.saveSchema(
				schemaDisplayManager.getSchema(zeCollection, "folder_test").withNewFormMetadata("toto").withNewFormMetadata(
						"metadataOnlyInTest"));
		schemaDisplayManager.saveSchema(schemaDisplayManager.getSchema(zeCollection, "folder_test2").withNewFormMetadata("toto"));

		schemaDisplayManager.saveSchema(schemaDisplayManager.getSchema(zeCollection, "document_test").withNewFormMetadata("toto")
				.withNewFormMetadata("metadataOnlyInTest"));
		schemaDisplayManager
				.saveSchema(schemaDisplayManager.getSchema(zeCollection, "document_test2").withNewFormMetadata("toto"));

		schemaDisplayManager.saveSchema(schemaDisplayManager.getSchema(zeCollection, "userTask_test").withNewFormMetadata("toto")
				.withNewFormMetadata("metadataOnlyInTest"));
		schemaDisplayManager
				.saveSchema(schemaDisplayManager.getSchema(zeCollection, "userTask_test2").withNewFormMetadata("toto"));

		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
		TasksSchemasRecordsServices tasks = new TasksSchemasRecordsServices(zeCollection, getAppLayerFactory());
		try {
			Transaction transaction = new Transaction();

			transaction.add(rm.newFolderType().setCode("aaaa").setTitle("aaaa").setLinkedSchema("folder_test"));
			transaction.add(rm.newFolderType().setCode("bbbb").setTitle("bbbb").setLinkedSchema("folder_test2"));
			transaction.add(rm.newDocumentType().setCode("aaaa").setTitle("aaaa").setLinkedSchema("document_test"));
			transaction.add(rm.newDocumentType().setCode("bbbb").setTitle("bbbb").setLinkedSchema("document_test2"));
			transaction.add(tasks.newTaskType().setCode("aaaa").setTitle("aaaa").setLinkedSchema("userTask_test"));
			transaction.add(tasks.newTaskType().setCode("bbbb").setTitle("bbbb").setLinkedSchema("userTask_test2"));
			getModelLayerFactory().newRecordServices().execute(transaction);
		} catch (RecordServicesException e) {
			throw new RuntimeException(e);
		}
	}

	@Test
	public void startApplicationWithSaveState()
			throws Exception {

		givenTransactionLogIsEnabled();
		getCurrentTestSession().getFactoriesTestFeatures().givenSystemInState(
				new File("/path/to/the/saveState.zip")).withPasswordsReset();

		newWebDriver(loggedAsUserInCollection("zeUser", "myCollection"));
		waitUntilICloseTheBrowsers();

	}

	@Test
	public void startOnHomePageAsChuckNorris()
			throws Exception {
		driver = newWebDriver(loggedAsUserInCollection(chuckNorris, zeCollection));
		waitUntilICloseTheBrowsers();
	}

	@Test
	public void startOnHomePageAsDakota()
			throws Exception {
		driver = newWebDriver(loggedAsUserInCollection(dakota, zeCollection));
		waitUntilICloseTheBrowsers();
	}

	@Test
	public void startOnHomePageAsRida()
			throws Exception {
		driver = newWebDriver(loggedAsUserInCollection(admin, "LaCollectionDeRida"));
		waitUntilICloseTheBrowsers();
	}

	@Test
	public void startOnHomePageAsGandalf()
			throws Exception {
		driver = newWebDriver(loggedAsUserInCollection(gandalf, zeCollection));
		waitUntilICloseTheBrowsers();
	}

	@Test
	public void startOnHomePageAsBob()
			throws Exception {
		driver = newWebDriver(loggedAsUserInCollection(bobGratton, zeCollection));
		waitUntilICloseTheBrowsers();
	}

	@Test
	public void startOnHomePageAsCharles()
			throws Exception {
		driver = newWebDriver(loggedAsUserInCollection(charlesFrancoisXavier, zeCollection));
		waitUntilICloseTheBrowsers();
	}

	@Test
	public void startOnHomePageAsEdouard()
			throws Exception {
		driver = newWebDriver(loggedAsUserInCollection(edouard, zeCollection));
		waitUntilICloseTheBrowsers();
	}
}
