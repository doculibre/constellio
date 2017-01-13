package com.constellio.app.services.records;

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
import static com.constellio.sdk.tests.TestUtils.asMap;
import static com.constellio.sdk.tests.TestUtils.extractingSimpleCodeAndParameters;
import static com.constellio.sdk.tests.TestUtils.frenchMessages;
import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichIsMultivalue;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.joda.time.LocalDate.now;

import java.util.ArrayList;
import java.util.List;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.common.SolrInputDocument;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Category;
import com.constellio.app.modules.rm.wrappers.DecommissioningList;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.structures.DecomListFolderDetail;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.schemas.MetadataSchemaTypesAlteration;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.TestRecord;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup.AnotherSchemaMetadatas;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup.ZeSchemaMetadatas;

public class SystemCheckManagerAcceptanceTest extends ConstellioTest {

	TestsSchemasSetup setup = new TestsSchemasSetup(zeCollection);
	ZeSchemaMetadatas zeSchema = setup.new ZeSchemaMetadatas();
	AnotherSchemaMetadatas anotherSchema = setup.new AnotherSchemaMetadatas();

	RMTestRecords records = new RMTestRecords(zeCollection);

	@Before
	public void setUp()
			throws Exception {
		givenTimeIs(new LocalDate(2014, 12, 12));

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
		assertThat(recordServices.getDocumentById("recordWithProblem1").get(anotherSchema.referenceFromAnotherSchemaToZeSchema()))
				.isEqualTo("bad");
		assertThat(recordServices.getDocumentById("recordWithProblem2").get(anotherSchema.referenceFromAnotherSchemaToZeSchema()))
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

		assertThat(recordServices.getDocumentById("recordWithProblem1").get(anotherSchema.referenceFromAnotherSchemaToZeSchema()))
				.isNull();
		assertThat(recordServices.getDocumentById("recordWithProblem2").get(anotherSchema.referenceFromAnotherSchemaToZeSchema()))
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
		assertThat(recordServices.getDocumentById("recordWithProblem1").get(anotherSchema.referenceFromAnotherSchemaToZeSchema()))
				.isEqualTo(asList("recordC", "recordA", "recordB"));
		assertThat(recordServices.getDocumentById("recordWithProblem2").get(anotherSchema.referenceFromAnotherSchemaToZeSchema()))
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

		assertThat(recordServices.getDocumentById("recordWithProblem1").get(anotherSchema.referenceFromAnotherSchemaToZeSchema()))
				.isEqualTo(asList("recordA", "recordB"));
		assertThat(recordServices.getDocumentById("recordWithProblem2").get(anotherSchema.referenceFromAnotherSchemaToZeSchema()))
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
	public void givenRecordWithInvalidMetadataThenValidationErrorMessageIsSent()
			throws Exception {
		//TODO AFTER-TEST-VALIDATION-SEQ
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
				"La valeur «Framboise» de la métadonnée «Titre» ne respecte pas le format «AAAA-AAAA»"
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
			recordServices.update(category.set(Schemas.LOGICALLY_DELETED_STATUS.getLocalCode(), true));
		}
		recordServices.logicallyDelete(records.getUnit12c().getWrappedRecord(), User.GOD);
		recordServices.logicallyDelete(records.getUnit20d().getWrappedRecord(), User.GOD);
		recordServices.logicallyDelete(records.getUnit20e().getWrappedRecord(), User.GOD);
		recordServices.update(records.getUnit20().set(Schemas.LOGICALLY_DELETED_STATUS.getLocalCode(), true));
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
		assertThat(extractingSimpleCodeAndParameters(systemCheckResults.errors, "schemaType", "recordId")).containsOnly(
				SystemCheckManagerAcceptanceTestResources.expectedErrorsWhenLogicallyDeletedCategoriesAndUnits);
		assertThat(frenchMessages(systemCheckResults.errors)).containsOnly(
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
		assertThat(extractingSimpleCodeAndParameters(systemCheckResults.errors, "schemaType", "recordId")).containsOnly(
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
		assertThat(extractingSimpleCodeAndParameters(systemCheckResults.errors, "schemaType", "recordId")).isEmpty();
	}

	@Test
	public void givenDecommissioningListWithInvalidFolderWhenRepairThenFixed()
			throws Exception {
		//TODO AFTER-TEST-VALIDATION-SEQ
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
		details.add(new DecomListFolderDetail().setFolderId(zeFolder.getId()));
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
		assertThat(frenchMessages(systemCheckResults.errors)).isEmpty();
	}

	private static enum RecordStatus {ACTIVE, LOGICALLY_DELETED, DELETED}

	private RecordStatus statusOf(String id) {
		try {
			Record record = getModelLayerFactory().newRecordServices().getDocumentById(id);
			return record.<Boolean>get(Schemas.LOGICALLY_DELETED_STATUS) ? RecordStatus.LOGICALLY_DELETED : RecordStatus.ACTIVE;
		} catch (Exception e) {
			return RecordStatus.DELETED;
		}

	}
}
