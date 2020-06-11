package com.constellio.sdk;

import ca.grics.GricsPlugin;
import com.constellio.AgentModule;
import com.constellio.EmailToPDF.EmailToPDFModule;
import com.constellio.app.modules.ai.ConstellioAIModule;
import com.constellio.app.modules.batchImport.BatchImportPlugin;
import com.constellio.app.modules.es.services.ESSchemasRecordsServices;
import com.constellio.app.modules.legalhold.LegalHoldModule;
import com.constellio.app.modules.rm.DemoTestRecords;
import com.constellio.app.modules.rm.RMConfigs;
import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.reports.ReportsPlugin;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.services.appManagement.AppManagementService;
import com.constellio.app.services.appManagement.AppManagementService.LicenseInfo;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.dev.ConstellioDevPlugin;
import com.constellio.migration.server.MigrationServicesPlugin;
import com.constellio.model.conf.ldap.LDAPDirectoryType;
import com.constellio.model.conf.ldap.RegexFilter;
import com.constellio.model.conf.ldap.config.LDAPServerConfiguration;
import com.constellio.model.conf.ldap.config.LDAPUserSyncConfiguration;
import com.constellio.model.entities.batchprocess.AsyncTask;
import com.constellio.model.entities.batchprocess.AsyncTaskExecutionParams;
import com.constellio.model.entities.enums.ParsingBehavior;
import com.constellio.model.entities.enums.SearchSortType;
import com.constellio.model.services.batch.manager.BatchProcessesManagerWithAsyncTasksAcceptanceTest;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.users.sync.LDAPUserSyncManager;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.annotations.MainTest;
import com.constellio.sdk.tests.annotations.MainTestDefaultStart;
import com.constellio.sdk.tests.annotations.UiTest;
import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebDriver;
import com.constellio.workflows.WorkflowsModule;
import org.joda.time.Duration;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import static java.util.Arrays.asList;

@UiTest
@MainTest
public class StartDemoRMConstellioAcceptTest extends ConstellioTest {

	RecordServices recordServices;
	ConstellioWebDriver driver;
	RMTestRecords records = new RMTestRecords(zeCollection);
	DemoTestRecords records2 = new DemoTestRecords("enterpriseCollection");
	RMSchemasRecordsServices schemas;

	@Before
	public void setUp()
			throws Exception {

		givenBackgroundThreadsEnabled();
		givenSystemLanguageIs("fr");
		givenTransactionLogIsEnabled();

		prepareSystem(
				withZeCollection().withConstellioRMModule().withAllTestUsers().withRMTest(records)
						.withFoldersAndContainersOfEveryStatus()
						.withDocumentsHavingContent().withDocumentsDecommissioningList()
		);
		//inCollection("LaCollectionDeRida").setCollectionTitleTo("Collection d'entreprise");
		inCollection(zeCollection).setCollectionTitleTo("Collection de test");

		//				givenInstalledModule(IvanhoeCambridgePlugin.class);
						givenInstalledModule(BatchImportPlugin.class);
						givenInstalledModule(MigrationServicesPlugin.class);
						givenInstalledModule(GricsPlugin.class);
		//givenInstalledModule(ConstellioAIModule.class).enabledInEveryCollections();
		//givenInstalledModule(AgentModule.class).enabledInEveryCollections();
		//givenInstalledModule(ConstellioSharepointModule.class).enabledInEveryCollections();
		//givenInstalledModule(ConstellioSSOModule.class).enabledInEveryCollections();
		//givenInstalledModule(MccPlugin.class).enabledInEveryCollections();
		givenInstalledModule(AgentModule.class);
		givenInstalledModule(ReportsPlugin.class);
		givenInstalledModule(EmailToPDFModule.class);
		givenInstalledModule(ConstellioDevPlugin.class).enabledInEveryCollections();
		givenInstalledModule(BatchImportPlugin.class).enabledInEveryCollections();
		givenInstalledModule(MigrationServicesPlugin.class).enabledInEveryCollections();
		givenInstalledModule(ConstellioDevPlugin.class).enabledInEveryCollections();
		//givenInstalledModule(GricsPlugin.class).enabledInEveryCollections();
		//		givenInstalledModule(ConstellioESSModule.class);//
		givenInstalledModule(
				WorkflowsModule.class);// 		givenInstalledModule(BatchImportPlugin.class).enabledInEveryCollections();
		//		givenInstalledModule(MigrationServicesPlugin.class).enabledInEveryCollections();
		//		givenInstalledModule(GricsPlugin.class).enabledInEveryCollections();
		//		givenInstalledModule(RestApiPlugin.class).enabledInEveryCollections();
		givenInstalledModule(LegalHoldModule.class).enabledInEveryCollections();

		recordServices = getModelLayerFactory().newRecordServices();

	}

	private void deactivateApprovalRequirements() {
		givenConfig(RMConfigs.REQUIRE_APPROVAL_FOR_DESTRUCTION_OF_ACTIVE, false);
		givenConfig(RMConfigs.REQUIRE_APPROVAL_FOR_DEPOSIT_OF_ACTIVE, false);
		givenConfig(RMConfigs.REQUIRE_APPROVAL_FOR_CLOSING, false);
		givenConfig(RMConfigs.REQUIRE_APPROVAL_FOR_TRANSFER, false);
		givenConfig(RMConfigs.REQUIRE_APPROVAL_FOR_DESTRUCTION_OF_SEMIACTIVE, false);
		givenConfig(RMConfigs.REQUIRE_APPROVAL_FOR_DEPOSIT_OF_SEMIACTIVE, false);
	}

	public Folder buildDefaultFolder(String id) {
		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
		return rm.newFolderWithId(id).setTitle(id).setAdministrativeUnitEntered(records.unitId_10)
				.setCategoryEntered(records.categoryId_X13).setRetentionRuleEntered(records.ruleId_1)
				.setMediumTypes(rm.getMediumTypeByCode("PA").getId())
				.setOpenDate(new LocalDate().minusYears(100)).setCloseDateEntered(new LocalDate().minusYears(100));
	}

	public Folder buildDefaultFolder1(String id) {
		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
		return rm.newFolderWithId(id).setTitle(id).setAdministrativeUnitEntered(records.unitId_10)
				.setCategoryEntered(records.categoryId_X13).setRetentionRuleEntered(records.ruleId_1)
				.setMediumTypes(rm.getMediumTypeByCode("PA").getId())
				.setOpenDate(new LocalDate().minusYears(100)).setCloseDateEntered(new LocalDate().minusYears(100));
	}

	protected boolean isValidLicense(AppLayerFactory appLayerFactory) {
		AppManagementService appManagementService = appLayerFactory.newApplicationService();
		LicenseInfo licenseInfo = appManagementService.getLicenseInfo();
		return licenseInfo != null && licenseInfo.getExpirationDate().isAfter(new LocalDate());
	}

	public void configLDAP() {
		LDAPServerConfiguration ldapServerConfiguration = new LDAPServerConfiguration(asList("ldap://192.168.1.186:389"),
				asList("test.doculibre.ca"),
				LDAPDirectoryType.ACTIVE_DIRECTORY, true, false);
		LDAPUserSyncConfiguration ldapUserSyncConfiguration = new LDAPUserSyncConfiguration(
				"administrator",
				"t3stdocul!bre3",
				new RegexFilter(".*", ""),
				new RegexFilter(".*", ""),
				Duration.standardDays(1),
				new ArrayList<String>(),
				Arrays.asList("OU=Groupes,DC=test,DC=doculibre,DC=ca"),
				Arrays.asList("CN=Users,DC=test,DC=doculibre,DC=ca"),
				new ArrayList<String>(),
				true,
				asList(zeCollection));
		getModelLayerFactory().getLdapConfigurationManager()
				.saveLDAPConfiguration(ldapServerConfiguration, ldapUserSyncConfiguration);

		LDAPUserSyncManager.LDAPSynchProgressionInfo info = new LDAPUserSyncManager.LDAPSynchProgressionInfo();
		getModelLayerFactory().getLdapUserSyncManager().synchronizeIfPossible(info);
	}

	public void configConnector()
			throws RecordServicesException {
		ESSchemasRecordsServices es = new ESSchemasRecordsServices(zeCollection, getAppLayerFactory());
		es.getConnectorManager().createConnector(es.newConnectorSmbInstance()
				.setCode("FirstVMConnector")
				.setEnabled(true)
				.setUsername("Constellio")
				.setPassword("ncix123$")
				.setSeeds(asList("smb://LAPTOP-JJ3S0SB3/myNewShare/"))
				.setInclusions(asList("smb://LAPTOP-JJ3S0SB3/myNewShare/"))
				.setConnectorType(es.getConnectorTypeWithCode("smb"))
				.setSkipShareAccessControl(true)
				.setTitle("First VM Connector"));
	}

	public static class WordAsyncTask implements AsyncTask {

		String wordsToAdd;

		public WordAsyncTask(String wordsToAdd) {
			this.wordsToAdd = wordsToAdd;
		}

		@Override
		public void execute(AsyncTaskExecutionParams params) {

			System.out.println("Adding words '" + wordsToAdd + "'");
			BatchProcessesManagerWithAsyncTasksAcceptanceTest.words =
					BatchProcessesManagerWithAsyncTasksAcceptanceTest.words + wordsToAdd;
			try {
				Thread.sleep(100000);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		public Object[] getInstanceParameters() {
			return new Object[]{wordsToAdd};
		}
	}

	@Test
	@MainTestDefaultStart
	public void startOnHomePageAsAdmin()
			throws Exception {

		//Toggle.LOG_REQUEST_CACHE.enable();

		givenConfig(ConstellioEIMConfigs.SEARCH_SORT_TYPE, SearchSortType.ID_ASC);
		givenConfig(ConstellioEIMConfigs.DEFAULT_PARSING_BEHAVIOR, ParsingBehavior.SYNC_PARSING_FOR_ALL_CONTENTS);

		givenInstalledModule(ConstellioAIModule.class);

		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());

		//		Transaction transaction = new Transaction();
		//		transaction.add(rm.setType(records.getFolder_A01(), records.folderTypeEmploye())).set("subType", "customSubType")
		//				.setTitle("zetest");
		//		transaction.add(rm.setType(records.getFolder_A02(), records.folderTypeEmploye())).setTitle("zetest");
		//		getModelLayerFactory().newRecordServices().execute(transaction);
		getAppLayerFactory().getSystemGlobalConfigsManager().setReindexingRequired(false);

		// getDataLayerFactory().getDataLayerLogger().setPrintAllQueriesLongerThanMS(0);
		driver = newWebDriver(loggedAsUserInCollection(admin, zeCollection));
		waitUntilICloseTheBrowsers();
	}

	//	private void setup() {
	//		givenConfig(RMConfigs.DOCUMENTS_TYPES_CHOICE, DocumentsTypeChoice.ALL_DOCUMENTS_TYPES);
	//		getModelLayerFactory().getMetadataSchemasManager().modify(zeCollection, new MetadataSchemaTypesAlteration() {
	//			@Override
	//			public void alter(MetadataSchemaTypesBuilder types) {
	//				MetadataSchemaBuilder folderTestSchema = types.getSchemaType("folder").createCustomSchema("test");
	//				MetadataSchemaBuilder documentTestSchema = types.getSchemaType("document").createCustomSchema("test");
	//				MetadataSchemaBuilder taskTestSchema = types.getSchemaType("userTask").createCustomSchema("test");
	//
	//				MetadataSchemaBuilder folderTest2Schema = types.getSchemaType("folder").createCustomSchema("test2");
	//				MetadataSchemaBuilder documentTest2Schema = types.getSchemaType("document").createCustomSchema("test2");
	//				MetadataSchemaBuilder taskTest2Schema = types.getSchemaType("userTask").createCustomSchema("test2");
	//
	//				folderTestSchema.create("toto").setType(STRING).setDefaultValue("tata");
	//				documentTestSchema.create("toto").setType(STRING).setDefaultValue("tata");
	//				taskTestSchema.create("toto").setType(STRING).setDefaultValue("tata");
	//
	//				folderTest2Schema.create("toto").setType(STRING).setDefaultValue("titi");
	//				documentTest2Schema.create("toto").setType(STRING).setDefaultValue("titi");
	//				taskTest2Schema.create("toto").setType(STRING).setDefaultValue("titi");
	//
	//				folderTestSchema.create("metadataOnlyInTest").setType(STRING).setDefaultValue("tata");
	//				documentTestSchema.create("metadataOnlyInTest").setType(STRING).setDefaultValue("tata");
	//				taskTestSchema.create("metadataOnlyInTest").setType(STRING).setDefaultValue("tata");
	//
	//			}
	//		});
	//
	//		SchemasDisplayManager schemaDisplayManager = getAppLayerFactory().getMetadataSchemasDisplayManager();
	//		schemaDisplayManager.saveSchema(
	//				schemaDisplayManager.getSchema(zeCollection, "folder_test").withNewFormMetadata("toto").withNewFormMetadata(
	//						"metadataOnlyInTest"));
	//		schemaDisplayManager.saveSchema(schemaDisplayManager.getSchema(zeCollection, "folder_test2").withNewFormMetadata("toto"));
	//
	//		schemaDisplayManager.saveSchema(schemaDisplayManager.getSchema(zeCollection, "document_test").withNewFormMetadata("toto")
	//				.withNewFormMetadata("metadataOnlyInTest"));
	//		schemaDisplayManager
	//				.saveSchema(schemaDisplayManager.getSchema(zeCollection, "document_test2").withNewFormMetadata("toto"));
	//
	//		schemaDisplayManager.saveSchema(schemaDisplayManager.getSchema(zeCollection, "userTask_test").withNewFormMetadata("toto")
	//				.withNewFormMetadata("metadataOnlyInTest"));
	//		schemaDisplayManager
	//				.saveSchema(schemaDisplayManager.getSchema(zeCollection, "userTask_test2").withNewFormMetadata("toto"));
	//
	//		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(zeCollection, getModelLayerFactory());
	//		TasksSchemasRecordsServices tasks = new TasksSchemasRecordsServices(zeCollection, getAppLayerFactory());
	//		try {
	//			Transaction transaction = new Transaction();
	//
	//			transaction.add(rm.newFolderType().setCode("aaaa").setTitle("aaaa").setLinkedSchema("folder_test"));
	//			transaction.add(rm.newFolderType().setCode("bbbb").setTitle("bbbb").setLinkedSchema("folder_test2"));
	//			transaction.add(rm.newDocumentType().setCode("aaaa").setTitle("aaaa").setLinkedSchema("document_test"));
	//			transaction.add(rm.newDocumentType().setCode("bbbb").setTitle("bbbb").setLinkedSchema("document_test2"));
	//			transaction.add(tasks.newTaskType().setCode("aaaa").setTitle("aaaa").setLinkedSchema("userTask_test"));
	//			transaction.add(tasks.newTaskType().setCode("bbbb").setTitle("bbbb").setLinkedSchema("userTask_test2"));
	//			getModelLayerFactory().newRecordServices().execute(transaction);
	//		} catch (RecordServicesException e) {
	//			throw new RuntimeException(e);
	//		}
	//	}

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
	public void startOnHomePageAsAlice()
			throws Exception {
		driver = newWebDriver(loggedAsUserInCollection(aliceWonderland, zeCollection));
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
