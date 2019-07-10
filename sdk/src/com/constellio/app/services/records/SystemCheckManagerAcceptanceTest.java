package com.constellio.app.services.records;

import com.constellio.app.modules.es.model.connectors.smb.ConnectorSmbFolder;
import com.constellio.app.modules.es.services.ESSchemasRecordsServices;
import com.constellio.app.modules.rm.RMConfigs;
import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.extensions.RMSystemCheckExtension;
import com.constellio.app.modules.rm.model.enums.AllowModificationOfArchivisticStatusAndExpectedDatesChoice;
import com.constellio.app.modules.rm.model.enums.CopyType;
import com.constellio.app.modules.rm.model.enums.FolderStatus;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Category;
import com.constellio.app.modules.rm.wrappers.DecommissioningList;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Email;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.structures.DecomListFolderDetail;
import com.constellio.app.modules.rm.wrappers.structures.FolderDetailStatus;
import com.constellio.app.modules.rm.wrappers.type.DocumentType;
import com.constellio.app.modules.rm.wrappers.type.FolderType;
import com.constellio.app.modules.robots.model.wrappers.Robot;
import com.constellio.app.modules.robots.services.RobotSchemaRecordServices;
import com.constellio.app.ui.pages.search.criteria.CriterionBuilder;
import com.constellio.data.dao.dto.records.RecordDTO;
import com.constellio.data.dao.dto.records.RecordDeltaDTO;
import com.constellio.data.dao.dto.records.RecordsFlushing;
import com.constellio.data.dao.dto.records.TransactionDTO;
import com.constellio.data.dao.services.bigVault.RecordDaoException;
import com.constellio.data.dao.services.records.RecordDao;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.RecordWrapper;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.contents.ContentManager;
import com.constellio.model.services.contents.ContentVersionDataSummary;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.schemas.MetadataSchemaTypesAlteration;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.TestRecord;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup.AnotherSchemaMetadatas;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup.ZeSchemaMetadatas;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.common.SolrInputDocument;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.constellio.app.services.records.SystemCheckManager.BROKEN_REFERENCES_METRIC;
import static com.constellio.app.services.records.SystemCheckManager.CHECKED_REFERENCES_METRIC;
import static com.constellio.app.services.records.SystemCheckManagerAcceptanceTestResources.expectedMessage1;
import static com.constellio.app.services.records.SystemCheckManagerAcceptanceTestResources.expectedMessage2;
import static com.constellio.app.services.records.SystemCheckManagerAcceptanceTestResources.expectedMessage3;
import static com.constellio.app.services.records.SystemCheckManagerAcceptanceTestResources.expectedMessage4;
import static com.constellio.app.services.records.SystemCheckManagerAcceptanceTestResources.expectedMessage5;
import static com.constellio.app.services.records.SystemCheckManagerAcceptanceTestResources.expectedMessage6;
import static com.constellio.model.entities.schemas.Schemas.TITLE;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.ALL;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static com.constellio.sdk.tests.TestUtils.asMap;
import static com.constellio.sdk.tests.TestUtils.extractingSimpleCodeAndParameters;
import static com.constellio.sdk.tests.TestUtils.frenchMessages;
import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichIsMultivalue;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.joda.time.LocalDate.now;

public class SystemCheckManagerAcceptanceTest extends ConstellioTest {

	TestsSchemasSetup setup = new TestsSchemasSetup(zeCollection);
	ZeSchemaMetadatas zeSchema = setup.new ZeSchemaMetadatas();
	AnotherSchemaMetadatas anotherSchema = setup.new AnotherSchemaMetadatas();

	RMTestRecords records = new RMTestRecords(zeCollection);
	RMSchemasRecordsServices rm;

	@Before
	public void setUp()
			throws Exception {
		givenTimeIs(new LocalDate(2014, 12, 12));
		cacheIntegrityCheckedAfterTest = false;
	}

	@Test
	public void givenDocumentWithSchemaAndWrongTypeThenRepair()
			throws RecordServicesException {

		prepareSystem(
				withZeCollection().withConstellioRMModule().withConstellioESModule().withAllTestUsers()
						.withRMTest(records).withFoldersAndContainersOfEveryStatus().withDocumentsDecommissioningList()
		);

		MetadataSchemasManager manager = getAppLayerFactory().getModelLayerFactory().getMetadataSchemasManager();
		rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
		manager.modify(zeCollection, new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				types.getSchemaType(Document.SCHEMA_TYPE).createCustomSchema("USRdocumentSchema1");
				types.getSchemaType(Document.SCHEMA_TYPE).createCustomSchema("USRdocumentSchema2");
			}
		});

		RecordServices recordServices = getModelLayerFactory().newRecordServices();
		DocumentType documentType = rm.newDocumentType();
		documentType.setLinkedSchema("document_USRdocumentSchema1");
		documentType.setCode("USRdocumentSchema");
		documentType.setTitle("USRdocumentSchema");

		recordServices.add(documentType);

		Document document = rm.newDocumentWithTypeAndId(documentType, "MyDocument").setTitle("Document")
				.setFolder(records.folder_A01);
		document.setCreatedOn(LocalDateTime.now());

		documentType.setLinkedSchema("document_USRdocumentSchema2");
		recordServices.update(documentType);
		recordServices.add(document);

		SystemCheckManager systemCheckManager = new SystemCheckManager(getAppLayerFactory());
		SystemCheckResults results = systemCheckManager.runSystemCheck(true);

		assertThat(results.getRepairedRecords().size()).isEqualTo(1);
		assertThat(results.getRepairedRecords().contains("MyDocument")).isTrue();
		assertThat(results.getMetric(RMSystemCheckExtension.METRIC_TYPE_DO_NOT_CORRESPOND_TO_TYPE_TYPE).intValue()).isEqualTo(1);

		Document documentResult = rm.getDocument("MyDocument");
		assertThat(documentResult.getSchema().getCode()).isEqualTo("document_USRdocumentSchema2");
	}

	@Test
	public void givenFolderWithSchemaAndWrongTypeThenRepair()
			throws RecordServicesException {

		prepareSystem(
				withZeCollection().withConstellioRMModule().withConstellioESModule().withAllTestUsers()
						.withRMTest(records).withFoldersAndContainersOfEveryStatus().withDocumentsDecommissioningList()
		);

		MetadataSchemasManager manager = getAppLayerFactory().getModelLayerFactory().getMetadataSchemasManager();
		rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
		manager.modify(zeCollection, new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				types.getSchemaType(Folder.SCHEMA_TYPE).createCustomSchema("USRfolderSchema1");
				types.getSchemaType(Folder.SCHEMA_TYPE).createCustomSchema("USRfolderSchema2");
			}
		});

		RecordServices recordServices = getModelLayerFactory().newRecordServices();
		FolderType folderType = rm.newFolderType();
		folderType.setLinkedSchema("folder_USRfolderSchema1");
		folderType.setCode("USRfolderSchema");
		folderType.setTitle("USRfolderSchema");
		recordServices.add(folderType);

		Folder folder = rm.newFolderWithTypeAndId(folderType.getId(), "MyFolder").setTitle("Folder")
				.setParentFolder(records.folder_A01);
		folder.setType(folderType);
		folder.setOpenDate(LocalDate.now());
		folder.setCreatedOn(LocalDateTime.now());

		folderType.setLinkedSchema("folder_USRfolderSchema2");
		recordServices.update(folderType);
		recordServices.add(folder);

		SystemCheckManager systemCheckManager = new SystemCheckManager(getAppLayerFactory());
		SystemCheckResults results = systemCheckManager.runSystemCheck(true);

		assertThat(results.getRepairedRecords().size()).isEqualTo(1);
		assertThat(results.getRepairedRecords().contains("MyFolder")).isTrue();
		assertThat(results.getMetric(RMSystemCheckExtension.METRIC_TYPE_DO_NOT_CORRESPOND_TO_TYPE_TYPE).intValue()).isEqualTo(1);

		Folder documentResult = rm.getFolder("MyFolder");
		assertThat(documentResult.getSchema().getCode()).isEqualTo("folder_USRfolderSchema2");
	}

	@Test
	public void givenFolderWithSchemaAndWrongTypeThenRepairFail()
			throws RecordServicesException {

		prepareSystem(
				withZeCollection().withConstellioRMModule().withConstellioESModule().withAllTestUsers()
						.withRMTest(records).withFoldersAndContainersOfEveryStatus().withDocumentsDecommissioningList()
		);

		MetadataSchemasManager manager = getAppLayerFactory().getModelLayerFactory().getMetadataSchemasManager();
		rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
		manager.modify(zeCollection, new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				types.getSchemaType(Folder.SCHEMA_TYPE).createCustomSchema("USRfolderSchema1");
				types.getSchemaType(Folder.SCHEMA_TYPE).createCustomSchema("USRfolderSchema2");
				types.getSchemaType(Folder.SCHEMA_TYPE).getSchema("USRfolderSchema1").create("metadataInSchema1")
						.setType(MetadataValueType.STRING);
			}

		});

		RecordServices recordServices = getModelLayerFactory().newRecordServices();
		FolderType folderType = rm.newFolderType();
		folderType.setLinkedSchema("folder_USRfolderSchema1");
		folderType.setCode("USRfolderSchema");
		folderType.setTitle("USRfolderSchema");
		recordServices.add(folderType);

		Folder folder = rm.newFolderWithTypeAndId(folderType.getId(), "MyFolder").setTitle("Folder")
				.setParentFolder(records.folder_A01);
		folder.setType(folderType);
		folder.setOpenDate(LocalDate.now());
		folder.setCreatedOn(LocalDateTime.now());
		folder.set("metadataInSchema1", "StringValue");

		folderType.setLinkedSchema("folder_USRfolderSchema2");
		recordServices.update(folderType);
		recordServices.add(folder);

		SystemCheckManager systemCheckManager = new SystemCheckManager(getAppLayerFactory());
		SystemCheckResults results = systemCheckManager.runSystemCheck(true);

		assertThat(frenchMessages(results.errors))
				.contains("Le schéma de l'enregistrement MyFolder n’a pas pu être modifié pour celui de son type");
		assertThat(results.getRepairedRecords().size()).isEqualTo(0);
		assertThat(results.getMetric(RMSystemCheckExtension.METRIC_TYPE_DO_NOT_CORRESPOND_TO_TYPE_TYPE).intValue()).isEqualTo(1);
	}

	@Test
	public void testdocumentAndFolderSchemaCodeThen5Errors() {
		prepareSystem(
				withZeCollection().withConstellioRMModule().withConstellioESModule().withAllTestUsers()
						.withRMTest(records).withFoldersAndContainersOfEveryStatus().withDocumentsDecommissioningList()
		);

		SystemCheckResults systemCheckResults = new SystemCheckManager(getAppLayerFactory()).runSystemCheck(false);
		assertThat(frenchMessages(systemCheckResults.errors))
				.containsOnly("Dans le type de schéma de métadonnées document, le schéma code form ne débute pas par USR",
						"Dans le type de schéma de métadonnées document, le schéma code report ne débute pas par USR",
						"Dans le type de schéma de métadonnées folder, le schéma code employe ne débute pas par USR",
						"Dans le type de schéma de métadonnées folder, le schéma code meetingFolder ne débute pas par USR");
	}

	@Test
	public void givenSystemWithSubfolderWithShouldBeNullMetadataThenRepair()
			throws RecordServicesException, InterruptedException, RecordDaoException.NoSuchRecordWithId, RecordDaoException.OptimisticLocking {
		final String ID = "MonDossier";
		final String TITLE = "TITLE";

		prepareSystem(
				withZeCollection().withConstellioRMModule().withConstellioESModule().withAllTestUsers()
						.withRMTest(records).withFoldersAndContainersOfEveryStatus().withDocumentsDecommissioningList()
		);

		SystemCheckManager systemCheckManager = new SystemCheckManager(getAppLayerFactory());
		RecordServices recordServices = getModelLayerFactory().newRecordServices();
		rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());

		Folder folder = rm.newFolderWithId(ID);
		folder.setTitle(TITLE);

		folder.setOpenDate(new LocalDate());
		folder.setMainCopyRuleEntered(records.principal42_5_CId);
		folder.setUniformSubdivisionEntered(records.getUniformSubdivision1());
		folder.setAdministrativeUnitEntered(records.getUnit10());
		folder.setCategoryEntered(records.getCategory_X());
		folder.setRetentionRuleEntered(records.getRule1());
		folder.setCopyStatusEntered(CopyType.PRINCIPAL);

		Transaction transaction = new Transaction();
		transaction.add(folder);

		recordServices.execute(transaction);

		RecordDao recordDao = getDataLayerFactory().newRecordDao();

		Map<String, Object> modifiedValues = new HashMap<>();
		modifiedValues.put("parentFolderPId_s", records.folder_A01);

		RecordDTO record = recordDao.get(ID);
		RecordDeltaDTO recordDeltaDTO = new RecordDeltaDTO(record, modifiedValues, record.getFields());
		recordDao.execute(new TransactionDTO(RecordsFlushing.NOW()).withModifiedRecords(asList(recordDeltaDTO)));

		waitForBatchProcess();

		SystemCheckResults results = systemCheckManager.runSystemCheck(true);

		Folder folder2 = rm.getFolder(ID);

		assertThat(folder2.getMainCopyRuleIdEntered()).isNull();
		assertThat(folder2.getUniformSubdivisionEntered()).isNull();
		assertThat(folder2.getAdministrativeUnitEntered()).isNull();
		assertThat(folder2.getCategoryEntered()).isNull();
		assertThat(folder2.getRetentionRuleEntered()).isNull();
		assertThat(folder2.getCopyStatusEntered()).isNull();
		assertThat(results.getMetric(RMSystemCheckExtension.METRIC_SUB_FOLDER_WITH_NULL_FIELD_NOT_NULL)).isEqualTo(1);

	}

	@Test
	public void givenSystemWithCheckoutedEmailThenCheckIntheseEmail()
			throws RecordServicesException {
		final String ID = "000001";
		final String TITLE = "TITLE";

		prepareSystem(
				withZeCollection().withConstellioRMModule().withConstellioESModule().withAllTestUsers()
						.withRMTest(records).withFoldersAndContainersOfEveryStatus().withDocumentsDecommissioningList()
		);

		SystemCheckManager systemCheckManager = new SystemCheckManager(getAppLayerFactory());
		RecordServices recordServices = getModelLayerFactory().newRecordServices();
		rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());

		Email email = rm.newEmailWithId(ID);
		email.setTitle(TITLE);
		email.setFolder(records.folder_A01);

		ContentManager contentManager = getModelLayerFactory().getContentManager();
		ContentVersionDataSummary newDocumentsVersions = contentManager.upload(getTestResourceInputStream("testMessage.msg"));
		Content documentContent = contentManager.createMajor(records.getAdmin(), "testMessage.msg", newDocumentsVersions);

		email.setContent(documentContent);
		email.getContent().checkOut(records.getAdmin());

		Transaction transaction = new Transaction();
		transaction.add(email);

		recordServices.execute(transaction);

		Email emailCheckouted = rm.getEmail(ID);

		// Still checkouted
		assertThat(emailCheckouted.getContent().getCurrentCheckedOutVersion() == null).isFalse();

		systemCheckManager.runSystemCheck(true);

		Email emailCheckinByRepairService = rm.getEmail(ID);

		// Not checkouted anymore.
		assertThat(emailCheckinByRepairService.getContent().getCurrentCheckedOutVersion() == null).isTrue();
		assertThat(new SystemCheckReportBuilder(systemCheckManager).build()).contains("000001");
	}

	@Test
	public void givenSystemWithBrokenSingleValueLinksWhenSystemCheckingThenFindThoseLinks()
			throws Exception {

		defineSchemasManager().using(setup.withAReferenceFromAnotherSchemaToZeSchema());
		RecordServices recordServices = getModelLayerFactory().newRecordServices();
		SystemCheckManager systemCheckManager = new SystemCheckManager(getAppLayerFactory());

		Transaction transaction = new Transaction();
		transaction.add(new TestRecord(zeSchema, "zeId").set(TITLE, "1"));
		transaction.add(new TestRecord(anotherSchema, "recordWithProblem1").set(TITLE, "2")
				.set(anotherSchema.referenceFromAnotherSchemaToZeSchema(), "zeId"));
		transaction.add(new TestRecord(anotherSchema, "recordWithoutProblem").set(TITLE, "3")
				.set(anotherSchema.referenceFromAnotherSchemaToZeSchema(), "zeId"));
		transaction.add(new TestRecord(anotherSchema, "recordWithProblem2").set(TITLE, "4")
				.set(anotherSchema.referenceFromAnotherSchemaToZeSchema(), "zeId"));
		recordServices.execute(transaction);

		SolrClient solrClient = getDataLayerFactory().newRecordDao().getBigVaultServer().getNestedSolrServer();
		SolrInputDocument modificationBypassingIntegrityValidations = new SolrInputDocument();
		modificationBypassingIntegrityValidations.setField("id", "recordWithProblem1");
		modificationBypassingIntegrityValidations.setField("_version_", "1");
		modificationBypassingIntegrityValidations.setField("referenceFromAnotherSchemaToZeSchemaId_s", asMap("set", "bad"));
		solrClient.add(modificationBypassingIntegrityValidations);

		modificationBypassingIntegrityValidations = new SolrInputDocument();
		modificationBypassingIntegrityValidations.setField("id", "recordWithProblem2");
		modificationBypassingIntegrityValidations.setField("_version_", "1");
		modificationBypassingIntegrityValidations
				.setField("referenceFromAnotherSchemaToZeSchemaId_s", asMap("set", "notGood"));
		solrClient.add(modificationBypassingIntegrityValidations);

		solrClient.commit();

		//rams.put("metadataCode", referenceMetadata.getCode());
		//params.put("record", recordId);
		//params.put("brokenLinkRecordId

		getModelLayerFactory().getRecordsCaches().getCache(zeCollection).invalidateVolatileReloadPermanent(
				asList(zeSchema.typeCode(), anotherSchema.typeCode()));

		SystemCheckResults systemCheckResults = systemCheckManager.runSystemCheck(false);
		assertThat(systemCheckResults.getMetric(BROKEN_REFERENCES_METRIC)).isEqualTo(2);
		assertThat(systemCheckResults.getMetric(CHECKED_REFERENCES_METRIC)).isEqualTo(3);
		assertThat(systemCheckResults.repairedRecords.size()).isEqualTo(0);
		assertThat(extractingSimpleCodeAndParameters(systemCheckResults.errors, "metadataCode", "record", "brokenLinkRecordId"))
				.containsOnly(
						tuple("SystemCheckResultsBuilder_brokenLink",
								"anotherSchemaType_default_referenceFromAnotherSchemaToZeSchema", "recordWithProblem1", "bad"),
						tuple("SystemCheckResultsBuilder_brokenLink",
								"anotherSchemaType_default_referenceFromAnotherSchemaToZeSchema", "recordWithProblem2", "notGood")
				);
		assertThat(frenchMessages(systemCheckResults.errors)).containsOnly(
				"La métadonnée anotherSchemaType_default_referenceFromAnotherSchemaToZeSchema de l'enregistrement recordWithProblem1 référence un enregistrement inexistant : bad",
				"La métadonnée anotherSchemaType_default_referenceFromAnotherSchemaToZeSchema de l'enregistrement recordWithProblem2 référence un enregistrement inexistant : notGood"
		);
		assertThat(recordServices.getDocumentById("recordWithProblem1").<String>get(
				anotherSchema.referenceFromAnotherSchemaToZeSchema()))
				.isEqualTo("bad");
		assertThat(recordServices.getDocumentById("recordWithProblem2").<String>get(
				anotherSchema.referenceFromAnotherSchemaToZeSchema()))
				.isEqualTo("notGood");
		assertThat(new SystemCheckReportBuilder(systemCheckManager).build()).isEqualTo(expectedMessage1);

		systemCheckResults = systemCheckManager.runSystemCheck(true);
		assertThat(systemCheckResults.getMetric(BROKEN_REFERENCES_METRIC)).isEqualTo(2);
		assertThat(systemCheckResults.getMetric(CHECKED_REFERENCES_METRIC)).isEqualTo(3);
		assertThat(systemCheckResults.repairedRecords.size()).isEqualTo(2);
		assertThat(extractingSimpleCodeAndParameters(systemCheckResults.errors, "metadataCode", "record", "brokenLinkRecordId"))
				.containsOnly(
						tuple("SystemCheckResultsBuilder_brokenLink",
								"anotherSchemaType_default_referenceFromAnotherSchemaToZeSchema", "recordWithProblem1", "bad"),
						tuple("SystemCheckResultsBuilder_brokenLink",
								"anotherSchemaType_default_referenceFromAnotherSchemaToZeSchema", "recordWithProblem2", "notGood")
				);

		assertThat(recordServices.getDocumentById("recordWithProblem1").<Object>get(
				anotherSchema.referenceFromAnotherSchemaToZeSchema()))
				.isNull();
		assertThat(recordServices.getDocumentById("recordWithProblem2").<Object>get(
				anotherSchema.referenceFromAnotherSchemaToZeSchema()))
				.isNull();
		assertThat(new SystemCheckReportBuilder(systemCheckManager).build()).isEqualTo(expectedMessage2);

		systemCheckResults = new SystemCheckManager(getAppLayerFactory()).runSystemCheck(false);
		assertThat(systemCheckResults.getMetric(BROKEN_REFERENCES_METRIC)).isEqualTo(0);
		assertThat(systemCheckResults.getMetric(CHECKED_REFERENCES_METRIC)).isEqualTo(1);
		assertThat(systemCheckResults.repairedRecords.size()).isEqualTo(0);
		assertThat(systemCheckResults.errors.getValidationErrors()).isEmpty();
		assertThat(systemCheckResults.errors.getValidationWarnings()).isEmpty();
	}

	@Test
	public void givenSystemWithBrokenMultiValueLinksWhenSystemCheckingThenFindThoseLinks()
			throws Exception {
		//TODO AFTER-TEST-VALIDATION-SEQ
		givenDisabledAfterTestValidations();
		defineSchemasManager().using(setup.withAReferenceFromAnotherSchemaToZeSchema(whichIsMultivalue));
		RecordServices recordServices = getModelLayerFactory().newRecordServices();

		Transaction transaction = new Transaction();
		transaction.add(new TestRecord(zeSchema, "recordA").set(TITLE, "A"));
		transaction.add(new TestRecord(zeSchema, "recordB").set(TITLE, "B"));
		transaction.add(new TestRecord(zeSchema, "recordC").set(TITLE, "C"));
		transaction.add(new TestRecord(anotherSchema, "recordWithProblem1").set(TITLE, "2")
				.set(anotherSchema.referenceFromAnotherSchemaToZeSchema(), asList("recordC", "recordA", "recordB")));
		transaction.add(new TestRecord(anotherSchema, "recordWithoutProblem").set(TITLE, "3")
				.set(anotherSchema.referenceFromAnotherSchemaToZeSchema(), asList("recordA", "recordB")));
		transaction.add(new TestRecord(anotherSchema, "recordWithProblem2").set(TITLE, "4")
				.set(anotherSchema.referenceFromAnotherSchemaToZeSchema(), asList("recordA", "recordC")));
		recordServices.execute(transaction);

		SolrClient solrClient = getDataLayerFactory().newRecordDao().getBigVaultServer().getNestedSolrServer();
		solrClient.deleteById("recordC");
		solrClient.commit();

		//rams.put("metadataCode", referenceMetadata.getCode());
		//params.put("record", recordId);
		//params.put("brokenLinkRecordId

		SystemCheckManager systemCheckManager = new SystemCheckManager(getAppLayerFactory());
		SystemCheckResults systemCheckResults = systemCheckManager.runSystemCheck(false);
		assertThat(systemCheckResults.getMetric(BROKEN_REFERENCES_METRIC)).isEqualTo(2);
		assertThat(systemCheckResults.getMetric(CHECKED_REFERENCES_METRIC)).isEqualTo(7);
		assertThat(systemCheckResults.repairedRecords.size()).isEqualTo(0);
		assertThat(extractingSimpleCodeAndParameters(systemCheckResults.errors, "metadataCode", "record", "brokenLinkRecordId"))
				.containsOnly(
						tuple("SystemCheckResultsBuilder_brokenLink",
								"anotherSchemaType_default_referenceFromAnotherSchemaToZeSchema", "recordWithProblem1",
								"recordC"),
						tuple("SystemCheckResultsBuilder_brokenLink",
								"anotherSchemaType_default_referenceFromAnotherSchemaToZeSchema", "recordWithProblem2", "recordC")
				);
		assertThat(frenchMessages(systemCheckResults.errors)).containsOnly(
				"La métadonnée anotherSchemaType_default_referenceFromAnotherSchemaToZeSchema de l'enregistrement recordWithProblem1 référence un enregistrement inexistant : recordC",
				"La métadonnée anotherSchemaType_default_referenceFromAnotherSchemaToZeSchema de l'enregistrement recordWithProblem2 référence un enregistrement inexistant : recordC"
		);
		assertThat(recordServices.getDocumentById("recordWithProblem1").<List<String>>get(
				anotherSchema.referenceFromAnotherSchemaToZeSchema()))
				.isEqualTo(asList("recordC", "recordA", "recordB"));
		assertThat(recordServices.getDocumentById("recordWithProblem2").<List<String>>get(
				anotherSchema.referenceFromAnotherSchemaToZeSchema()))
				.isEqualTo(asList("recordA", "recordC"));
		assertThat(new SystemCheckReportBuilder(systemCheckManager).build()).isEqualTo(expectedMessage3);

		systemCheckResults = systemCheckManager.runSystemCheck(true);
		assertThat(systemCheckResults.getMetric(BROKEN_REFERENCES_METRIC)).isEqualTo(2);
		assertThat(systemCheckResults.getMetric(CHECKED_REFERENCES_METRIC)).isEqualTo(7);
		assertThat(systemCheckResults.repairedRecords.size()).isEqualTo(2);
		assertThat(extractingSimpleCodeAndParameters(systemCheckResults.errors, "metadataCode", "record", "brokenLinkRecordId"))
				.containsOnly(
						tuple("SystemCheckResultsBuilder_brokenLink",
								"anotherSchemaType_default_referenceFromAnotherSchemaToZeSchema", "recordWithProblem1",
								"recordC"),
						tuple("SystemCheckResultsBuilder_brokenLink",
								"anotherSchemaType_default_referenceFromAnotherSchemaToZeSchema", "recordWithProblem2", "recordC")
				);

		assertThat(recordServices.getDocumentById("recordWithProblem1").<List<String>>get(
				anotherSchema.referenceFromAnotherSchemaToZeSchema()))
				.isEqualTo(asList("recordA", "recordB"));
		assertThat(recordServices.getDocumentById("recordWithProblem2").<List<String>>get(
				anotherSchema.referenceFromAnotherSchemaToZeSchema()))
				.isEqualTo(asList("recordA"));
		assertThat(new SystemCheckReportBuilder(systemCheckManager).build()).isEqualTo(expectedMessage4);

		systemCheckResults = new SystemCheckManager(getAppLayerFactory()).runSystemCheck(false);
		assertThat(systemCheckResults.getMetric(BROKEN_REFERENCES_METRIC)).isEqualTo(0);
		assertThat(systemCheckResults.getMetric(CHECKED_REFERENCES_METRIC)).isEqualTo(5);
		assertThat(systemCheckResults.repairedRecords.size()).isEqualTo(0);
		assertThat(systemCheckResults.errors.getValidationErrors()).isEmpty();
		assertThat(systemCheckResults.errors.getValidationWarnings()).isEmpty();

	}

	@Test
	public void givenSystemWithReferenceMetadatasReferencingALogicallyDeletedRecordThenRepairable()
			throws Exception {
		//TODO AFTER-TEST-VALIDATION-SEQ
		givenDisabledAfterTestValidations();
		defineSchemasManager().using(setup.withAReferenceFromAnotherSchemaToZeSchema(whichIsMultivalue));
		RecordServices recordServices = getModelLayerFactory().newRecordServices();

		Transaction transaction = new Transaction();
		transaction.add(new TestRecord(zeSchema, "recordA").set(TITLE, "A").set(Schemas.LOGICALLY_DELETED_STATUS, true));
		transaction.add(new TestRecord(zeSchema, "recordB").set(TITLE, "B"));
		transaction.add(new TestRecord(zeSchema, "recordC").set(TITLE, "C"));
		recordServices.execute(transaction);

		getModelLayerFactory().getMetadataSchemasManager().modify(zeCollection, new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				types.getMetadata(anotherSchema.referenceFromAnotherSchemaToZeSchema().getCode())
						.setDefaultValue(asList("recordA", "recordD", "recordC"));
			}
		});

		//params.put("metadataCode", referenceMetadata.getCode());
		//params.put("record", recordId);
		//params.put("brokenLinkRecordId

		SystemCheckManager systemCheckManager = new SystemCheckManager(getAppLayerFactory());
		SystemCheckResults systemCheckResults = systemCheckManager.runSystemCheck(false);
		assertThat(systemCheckResults.getMetric(BROKEN_REFERENCES_METRIC)).isEqualTo(2);
		assertThat(systemCheckResults.getMetric(CHECKED_REFERENCES_METRIC)).isEqualTo(3);
		assertThat(systemCheckResults.repairedRecords.size()).isEqualTo(0);
		assertThat(extractingSimpleCodeAndParameters(systemCheckResults.errors, "metadataCode", "brokenLinkRecordId"))
				.containsOnly(
						tuple("SystemCheckResultsBuilder_brokenLinkInDefaultValues",
								"anotherSchemaType_default_referenceFromAnotherSchemaToZeSchema", "recordA"),
						tuple("SystemCheckResultsBuilder_brokenLinkInDefaultValues",
								"anotherSchemaType_default_referenceFromAnotherSchemaToZeSchema", "recordD")
				);
		assertThat(frenchMessages(systemCheckResults.errors)).containsOnly(
				"La métadonnée anotherSchemaType_default_referenceFromAnotherSchemaToZeSchema référence un enregistrement inexistant dans sa valeur par défaut : recordA",
				"La métadonnée anotherSchemaType_default_referenceFromAnotherSchemaToZeSchema référence un enregistrement inexistant dans sa valeur par défaut : recordD"
		);

		assertThat(getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(zeCollection)
				.getMetadata("anotherSchemaType_default_referenceFromAnotherSchemaToZeSchema").getDefaultValue())
				.isEqualTo(asList("recordA", "recordD", "recordC"));

		systemCheckResults = systemCheckManager.runSystemCheck(true);
		assertThat(systemCheckResults.getMetric(BROKEN_REFERENCES_METRIC)).isEqualTo(2);
		assertThat(systemCheckResults.getMetric(CHECKED_REFERENCES_METRIC)).isEqualTo(3);
		assertThat(systemCheckResults.repairedRecords.size()).isEqualTo(0);

		assertThat(extractingSimpleCodeAndParameters(systemCheckResults.errors, "metadataCode", "brokenLinkRecordId"))
				.containsOnly(
						tuple("SystemCheckResultsBuilder_brokenLinkInDefaultValues",
								"anotherSchemaType_default_referenceFromAnotherSchemaToZeSchema", "recordA"),
						tuple("SystemCheckResultsBuilder_brokenLinkInDefaultValues",
								"anotherSchemaType_default_referenceFromAnotherSchemaToZeSchema", "recordD")
				);
		assertThat(frenchMessages(systemCheckResults.errors)).contains(
				"La métadonnée anotherSchemaType_default_referenceFromAnotherSchemaToZeSchema référence un enregistrement inexistant dans sa valeur par défaut : recordA",
				"La métadonnée anotherSchemaType_default_referenceFromAnotherSchemaToZeSchema référence un enregistrement inexistant dans sa valeur par défaut : recordD"
		);

		assertThat(getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(zeCollection)
				.getMetadata("anotherSchemaType_default_referenceFromAnotherSchemaToZeSchema").getDefaultValue())
				.isEqualTo(asList("recordC"));

	}

	@Test
	public void givenRecordWithInvalidMetadataThenValidationErrorMessageIsSent()
			throws Exception {
		prepareSystem(
				withZeCollection().withConstellioRMModule().withConstellioESModule().withAllTestUsers()
						.withRMTest(records).withFoldersAndContainersOfEveryStatus().withDocumentsDecommissioningList()
		);
		inCollection(zeCollection).setCollectionTitleTo("Collection de test");

		givenDisabledAfterTestValidations();
		getModelLayerFactory().getMetadataSchemasManager().modify(zeCollection, new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				types.getDefaultSchema(Folder.SCHEMA_TYPE).get("title").setInputMask("AAAA-AAAA");
			}
		});

		SystemCheckManager systemCheckManager = new SystemCheckManager(getAppLayerFactory());
		SystemCheckResults systemCheckResults = systemCheckManager.runSystemCheck(false);
		assertThat(frenchMessages(systemCheckResults.errors)).contains(
				"Dossier Framboise (B06) : La valeur «Framboise» de la métadonnée «Titre» ne respecte pas le format «AAAA-AAAA»"
		);
	}

	@Test
	public void givenLogicallyDeletedAdministrativeUnitsAndCategoriesThenRepairRestoreThem()
			throws Exception {
		RMTestRecords records = new RMTestRecords(zeCollection);
		prepareSystem(withZeCollection().withConstellioRMModule().withAllTestUsers().withRMTest(records));
		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());

		RecordServices recordServices = getModelLayerFactory().newRecordServices();
		recordServices.add(rm.newFolder().setTitle("My folder").setAdministrativeUnitEntered(records.unitId_20)
				.setOpenDate(now()).setCategoryEntered(records.categoryId_Z100).setRetentionRuleEntered(records.ruleId_1));
		for (Category category : rm.searchCategorys(ALL)) {
			recordServices.update((RecordWrapper) category.set(Schemas.LOGICALLY_DELETED_STATUS.getLocalCode(), true));
		}
		recordServices.logicallyDelete(records.getUnit12c().getWrappedRecord(), User.GOD);
		recordServices.logicallyDelete(records.getUnit20d().getWrappedRecord(), User.GOD);
		recordServices.logicallyDelete(records.getUnit20e().getWrappedRecord(), User.GOD);
		recordServices.update((RecordWrapper) records.getUnit20().set(Schemas.LOGICALLY_DELETED_STATUS.getLocalCode(), true));
		assertThat(records.getUnit20().isLogicallyDeletedStatus()).isTrue();
		assertThat(records.getUnit12c().isLogicallyDeletedStatus()).isTrue();
		assertThat(records.getCategory_X13().isLogicallyDeletedStatus()).isTrue();
		assertThat(records.getCategory_Z().isLogicallyDeletedStatus()).isTrue();
		assertThat(records.getCategory_Z100().isLogicallyDeletedStatus()).isTrue();
		assertThat(records.getCategory_Z110().isLogicallyDeletedStatus()).isTrue();
		assertThat(records.getCategory_Z112().isLogicallyDeletedStatus()).isTrue();

		SystemCheckManager systemCheckManager = new SystemCheckManager(getAppLayerFactory());
		SystemCheckResults systemCheckResults = systemCheckManager.runSystemCheck(false);

		assertThat(systemCheckResults.repairedRecords.size()).isEqualTo(0);
		assertThat(extractingSimpleCodeAndParameters(systemCheckResults.errors, "schemaType", "recordId")).contains(
				SystemCheckManagerAcceptanceTestResources.expectedErrorsWhenLogicallyDeletedCategoriesAndUnits);
		assertThat(frenchMessages(systemCheckResults.errors)).contains(
				SystemCheckManagerAcceptanceTestResources.expectedErrorsWhenLogicallyDeletedCategoriesAndUnitsErrorMessages
		);
		assertThat(statusOf(records.unitId_20)).isEqualTo(RecordStatus.LOGICALLY_DELETED);
		assertThat(statusOf(records.unitId_12c)).isEqualTo(RecordStatus.LOGICALLY_DELETED);
		assertThat(statusOf(records.categoryId_X13)).isEqualTo(RecordStatus.LOGICALLY_DELETED);
		assertThat(statusOf(records.categoryId_Z)).isEqualTo(RecordStatus.LOGICALLY_DELETED);
		assertThat(statusOf(records.categoryId_Z100)).isEqualTo(RecordStatus.LOGICALLY_DELETED);
		assertThat(statusOf(records.categoryId_Z110)).isEqualTo(RecordStatus.LOGICALLY_DELETED);
		assertThat(statusOf(records.categoryId_Z112)).isEqualTo(RecordStatus.LOGICALLY_DELETED);
		assertThat(new SystemCheckReportBuilder(systemCheckManager).build()).isEqualTo(expectedMessage5);

		systemCheckResults = systemCheckManager.runSystemCheck(true);
		assertThat(systemCheckResults.repairedRecords.size()).isEqualTo(18);
		assertThat(extractingSimpleCodeAndParameters(systemCheckResults.errors, "schemaType", "recordId")).contains(
				SystemCheckManagerAcceptanceTestResources.expectedErrorsWhenLogicallyDeletedCategoriesAndUnits);
		assertThat(statusOf(records.unitId_20)).isEqualTo(RecordStatus.ACTIVE);
		assertThat(statusOf(records.unitId_12c)).isEqualTo(RecordStatus.DELETED);
		assertThat(statusOf(records.categoryId_X13)).isEqualTo(RecordStatus.DELETED);
		assertThat(statusOf(records.categoryId_Z100)).isEqualTo(RecordStatus.ACTIVE);
		assertThat(statusOf(records.categoryId_Z)).isEqualTo(RecordStatus.ACTIVE);
		assertThat(statusOf(records.categoryId_Z110)).isEqualTo(RecordStatus.DELETED);
		assertThat(statusOf(records.categoryId_Z112)).isEqualTo(RecordStatus.DELETED);
		assertThat(new SystemCheckReportBuilder(systemCheckManager).build()).isEqualTo(expectedMessage6);

		systemCheckResults = new SystemCheckManager(getAppLayerFactory()).runSystemCheck(false);
		assertThat(systemCheckResults.repairedRecords.size()).isEqualTo(0);
		assertThat(extractingSimpleCodeAndParameters(systemCheckResults.errors, "schemaType", "recordId")).hasSize(4);
	}

	@Test
	public void givenDecommissioningListWithInvalidFolderWhenRepairThenFixed()
			throws Exception {
		prepareSystem(
				withZeCollection().withConstellioRMModule().withConstellioESModule().withAllTestUsers()
						.withRMTest(records).withFoldersAndContainersOfEveryStatus().withDocumentsDecommissioningList()
		);
		inCollection(zeCollection).setCollectionTitleTo("Collection de test");

		givenDisabledAfterTestValidations();

		DecommissioningList list = records.getList01();

		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
		Folder zeFolder = rm.newFolderWithId("zeFolder").setTitle("ze title").setOpenDate(date(2016, 1, 1))
				.setParentFolder(records.folder_A03);
		getModelLayerFactory().newRecordServices().add(zeFolder);
		List<DecomListFolderDetail> details = new ArrayList<>(list.getFolderDetails());
		details.add(new DecomListFolderDetail().setFolderId(zeFolder.getId()).setFolderDetailStatus(FolderDetailStatus.INCLUDED));
		list.setFolderDetails(details);
		getModelLayerFactory().newRecordServices().update(list);

		getModelLayerFactory().newRecordServices().logicallyDelete(zeFolder.getWrappedRecord(), User.GOD);
		getDataLayerFactory().newRecordDao().getBigVaultServer().getNestedSolrServer().deleteById(zeFolder.getId());
		getModelLayerFactory().newRecordServices().flush();

		SystemCheckManager systemCheckManager = new SystemCheckManager(getAppLayerFactory());
		SystemCheckResults systemCheckResults = systemCheckManager.runSystemCheck(false);
		assertThat(frenchMessages(systemCheckResults.errors)).contains(
				"La métadonnée decommissioningList_default_folders de l'enregistrement list01 référence un enregistrement inexistant : zeFolder"
		);

		systemCheckResults = systemCheckManager.runSystemCheck(true);
		assertThat(frenchMessages(systemCheckResults.errors)).contains(
				"La métadonnée decommissioningList_default_folders de l'enregistrement list01 référence un enregistrement inexistant : zeFolder"
		);

		systemCheckResults = systemCheckManager.runSystemCheck(false);
		assertThat(frenchMessages(systemCheckResults.errors)).hasSize(4);
	}

	@Test
	public void givenDestructionOrDepositDateIsAfterTransferDateThenProblemFoundAndFixed()
			throws Exception {
		prepareSystem(withZeCollection().withConstellioRMModule().withConstellioESModule().withAllTestUsers()
				.withRMTest(records).withFoldersAndContainersOfEveryStatus());

		givenConfig(RMConfigs.ALLOW_MODIFICATION_OF_ARCHIVISTIC_STATUS_AND_EXPECTED_DATES,
				AllowModificationOfArchivisticStatusAndExpectedDatesChoice.ENABLED);
		Transaction transaction = new Transaction();

		//Probleme
		transaction.add(records.getFolder_A01().setManualArchivisticStatus(FolderStatus.SEMI_ACTIVE)
				.setActualTransferDate(date(2015, 1, 1)).setManualExpectedDestructionDate(date(2014, 1, 1)));

		//Probleme
		transaction.add(records.getFolder_A02().setManualArchivisticStatus(FolderStatus.SEMI_ACTIVE)
				.setActualTransferDate(date(2015, 1, 1)).setManualExpectedDepositDate(date(2014, 1, 1)));

		//OK
		transaction.add(records.getFolder_A03().setManualArchivisticStatus(FolderStatus.SEMI_ACTIVE)
				.setActualTransferDate(date(2015, 1, 1)).setManualExpectedDepositDate(date(2015, 1, 1)));

		//Probleme
		transaction.add(records.getFolder_A04().setManualArchivisticStatus(FolderStatus.ACTIVE)
				.setManualExpectedTransferDate(date(2015, 1, 1)).setManualExpectedDestructionDate(date(2014, 1, 1)));

		//Probleme
		transaction.add(records.getFolder_A05().setManualArchivisticStatus(FolderStatus.ACTIVE)
				.setManualExpectedTransferDate(date(2015, 1, 1)).setManualExpectedDepositDate(date(2014, 1, 1)));

		//OK
		transaction.add(records.getFolder_A06().setManualArchivisticStatus(FolderStatus.ACTIVE)
				.setManualExpectedTransferDate(date(2015, 1, 1)).setManualExpectedDepositDate(date(2015, 1, 1)));

		getModelLayerFactory().newRecordServices().execute(transaction);

		SystemCheckManager systemCheckManager = new SystemCheckManager(getAppLayerFactory());
		SystemCheckResults systemCheckResults = systemCheckManager.runSystemCheck(false);
		assertThat(frenchMessages(systemCheckResults.errors)).contains(
				"Dossier A01-Abeille : Date de destruction avant la date de transfert",
				"Dossier A02-Aigle : Date de versement avant la date de transfert",
				"Dossier A04-Baleine : Date de destruction avant la date de transfert",
				"Dossier A05-Belette : Date de versement avant la date de transfert"
		);

		systemCheckResults = systemCheckManager.runSystemCheck(true);
		assertThat(frenchMessages(systemCheckResults.errors)).contains(
				"Dossier A01-Abeille : Date de destruction avant la date de transfert",
				"Dossier A02-Aigle : Date de versement avant la date de transfert",
				"Dossier A04-Baleine : Date de destruction avant la date de transfert",
				"Dossier A05-Belette : Date de versement avant la date de transfert"
		);

		systemCheckResults = systemCheckManager.runSystemCheck(false);
		assertThat(frenchMessages(systemCheckResults.errors)).hasSize(4);
	}

	@Test
	public void givenOldFloatingRobotActionsWhenDiagnoseAndRepairThenRemoved()
			throws Exception {

		prepareSystem(withZeCollection().withConstellioRMModule().withConstellioESModule().withRobotsModule()
				.withAllTestUsers().withRMTest(records).withFoldersAndContainersOfEveryStatus());

		RobotSchemaRecordServices robotsSchemas = new RobotSchemaRecordServices(zeCollection, getAppLayerFactory());
		ESSchemasRecordsServices es = new ESSchemasRecordsServices(zeCollection, getAppLayerFactory());

		Robot robot = robotsSchemas.newRobot().setCode("Term").setTitle("Inator");
		Transaction tx = new Transaction();
		tx.add(robot.setSchemaFilter(es.connectorSmbFolder.schemaType().getCode()));
		tx.add(robotsSchemas.newActionParameters());
		tx.add(robotsSchemas.newActionParameters());
		String parameter = tx.add(robotsSchemas.newActionParameters()).getId();
		robot.setActionParameters(parameter);
		robot.setSearchCriterion(new CriterionBuilder(ConnectorSmbFolder.SCHEMA_TYPE)
				.where(es.connectorSmbFolder.url()).isContainingText("sharepoint://"));

		getModelLayerFactory().newRecordServices().execute(tx);
		SearchServices searchServices = getModelLayerFactory().newSearchServices();
		LogicalSearchQuery query = new LogicalSearchQuery(from(robotsSchemas.actionParameters.schemaType()).returnAll());
		assertThat(searchServices.getResultsCount(query)).isEqualTo(3);

		SystemCheckManager systemCheckManager = new SystemCheckManager(getAppLayerFactory());
		SystemCheckResults systemCheckResults = systemCheckManager.runSystemCheck(false);
		assertThat(frenchMessages(systemCheckResults.errors)).hasSize(4);
		assertThat(systemCheckResults.getMetric("robots.unusedRobotActions")).isEqualTo(2);
		assertThat(searchServices.getResultsCount(query)).isEqualTo(3);

		systemCheckResults = systemCheckManager.runSystemCheck(true);
		assertThat(frenchMessages(systemCheckResults.errors)).hasSize(4);
		assertThat(systemCheckResults.getMetric("robots.unusedRobotActions")).isEqualTo(2);
		assertThat(searchServices.getResultsCount(query)).isEqualTo(1);

		systemCheckResults = systemCheckManager.runSystemCheck(false);
		assertThat(frenchMessages(systemCheckResults.errors)).hasSize(4);
		assertThat(systemCheckResults.getMetric("robots.unusedRobotActions")).isEqualTo(0);
		assertThat(searchServices.getResultsCount(query)).isEqualTo(1);

	}

	private enum RecordStatus {ACTIVE, LOGICALLY_DELETED, DELETED}

	private RecordStatus statusOf(String id) {
		try {
			Record record = getModelLayerFactory().newRecordServices().getDocumentById(id);
			return record.<Boolean>get(Schemas.LOGICALLY_DELETED_STATUS) ? RecordStatus.LOGICALLY_DELETED : RecordStatus.ACTIVE;
		} catch (Exception e) {
			return RecordStatus.DELETED;
		}

	}
}
