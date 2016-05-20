package com.constellio.app.ui.pages.search.batchProcessing;

import static com.constellio.app.modules.rm.model.enums.FolderStatus.INACTIVE_DEPOSITED;
import static com.constellio.model.entities.schemas.MetadataValueType.BOOLEAN;
import static com.constellio.model.entities.schemas.MetadataValueType.DATE;
import static com.constellio.model.entities.schemas.MetadataValueType.DATE_TIME;
import static com.constellio.model.entities.schemas.MetadataValueType.ENUM;
import static com.constellio.model.entities.schemas.MetadataValueType.NUMBER;
import static com.constellio.model.entities.schemas.MetadataValueType.STRING;
import static com.constellio.model.entities.schemas.MetadataValueType.TEXT;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.where;
import static com.constellio.sdk.tests.TestUtils.extractingSimpleCodeAndParameters;
import static java.util.Arrays.asList;
import static junit.framework.Assert.fail;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;

import java.util.List;
import java.util.Locale;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.model.enums.CopyType;
import com.constellio.app.modules.rm.model.enums.FolderStatus;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.ui.pages.search.batchProcessing.entities.BatchProcessRequest;
import com.constellio.app.ui.pages.search.batchProcessing.entities.BatchProcessResults;
import com.constellio.app.ui.util.DateFormatUtils;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.schemas.MetadataSchemaTypesAlteration;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.search.SearchServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.setups.Users;

public class BatchProcessingPresenterServiceAcceptanceTest extends ConstellioTest {

	RMSchemasRecordsServices rm;
	RMTestRecords records = new RMTestRecords(zeCollection);
	Users users = new Users();
	BatchProcessingPresenterService presenterService;
	MetadataSchema folderSchema;
	MetadataSchemaType folderSchemaType;
	SearchServices searchServices;

	LocalDate date1 = aDate();
	LocalDate date2 = aDate();
	LocalDate date3 = aDate();

	LocalDateTime dateTime1 = aDateTime();
	LocalDateTime dateTime2 = aDateTime();
	LocalDateTime dateTime3 = aDateTime();

	String date1String, date2String, date3String, dateTime1String, dateTime2String, dateTime3String;

	@Before
	public void setUp()
			throws Exception {
		prepareSystem(withZeCollection().withConstellioRMModule().withRMTest(records).withFoldersAndContainersOfEveryStatus()
				.withAllTest(users));
		rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());

		folderSchemaType = rm.folderSchemaType();
		searchServices = getModelLayerFactory().newSearchServices();
		presenterService = new BatchProcessingPresenterService(zeCollection, getAppLayerFactory(), Locale.FRENCH);

		Transaction transaction = new Transaction();

		LocalDate now = new LocalDate();
		Folder subFolder = rm.newFolder().setTitle("Ze sub folder").setParentFolder(records.folder_A03).setOpenDate(now);
		transaction.add(subFolder);
		transaction.add(rm.newFolder().setTitle("Ze sub folder").setParentFolder(subFolder)).setOpenDate(now);
		transaction.add(rm.newDocument().setTitle("Ze document 1").setFolder(records.folder_A03));
		transaction.add(rm.newDocument().setTitle("Ze document 1").setFolder(subFolder));

		getModelLayerFactory().newRecordServices().execute(transaction);

		givenConfig(ConstellioEIMConfigs.DATE_FORMAT, "yyyy-MM-dd");
		givenConfig(ConstellioEIMConfigs.DATE_TIME_FORMAT, "yyyy-MM-dd-HH-mm-ss");
		date1String = DateFormatUtils.format(date1);
		date2String = DateFormatUtils.format(date2);
		date3String = DateFormatUtils.format(date3);
		dateTime1String = DateFormatUtils.format(dateTime1);
		dateTime2String = DateFormatUtils.format(dateTime2);
		dateTime3String = DateFormatUtils.format(dateTime3);
	}

	@Test
	public void givenValidationExceptionsThenThrownInSimulation()
			throws Exception {
		BatchProcessRequest request = new BatchProcessRequest().setUser(users.adminIn(zeCollection))
				.setIds(asList(records.folder_A05, records.folder_A16))
				.addModifiedMetadata(Folder.RETENTION_RULE_ENTERED, records.ruleId_2);

		try {
			BatchProcessResults results = presenterService.simulate(request);
			fail("error expected!");
		} catch (RecordServicesException.ValidationException e) {
			assertThat(extractingSimpleCodeAndParameters(e.getErrors(), "record", "metadataCode")).containsOnly(
					tuple("ValueRequirementValidator_requiredValueForMetadata", records.folder_A05, "folder_default_copyStatus"),
					tuple("ValueRequirementValidator_requiredValueForMetadata", records.folder_A16, "folder_default_copyStatus")
			);
		}

		try {
			BatchProcessResults results = presenterService.execute(request);
			fail("error expected!");
		} catch (RecordServicesException.ValidationException e) {
			assertThat(extractingSimpleCodeAndParameters(e.getErrors(), "record", "metadataCode")).containsOnly(
					tuple("ValueRequirementValidator_requiredValueForMetadata", records.folder_A05, "folder_default_copyStatus"),
					tuple("ValueRequirementValidator_requiredValueForMetadata", records.folder_A16, "folder_default_copyStatus")
			);
		}
		assertThat(records.getFolder_A05().getRetentionRuleEntered()).isNotEqualTo(records.ruleId_2);

	}

	@Test
	public void whenSetCopyRuleEnteredThenApplied()
			throws Exception {
		BatchProcessRequest request = new BatchProcessRequest().setUser(users.adminIn(zeCollection))
				.setIds(asList(records.folder_A05, records.folder_A16))
				.addModifiedMetadata(Folder.RETENTION_RULE_ENTERED, records.ruleId_2);

		try {
			BatchProcessResults results = presenterService.simulate(request);
			fail("error expected!");
		} catch (RecordServicesException.ValidationException e) {
			assertThat(extractingSimpleCodeAndParameters(e.getErrors(), "record", "metadataCode")).containsOnly(
					tuple("ValueRequirementValidator_requiredValueForMetadata", records.folder_A05, "folder_default_copyStatus"),
					tuple("ValueRequirementValidator_requiredValueForMetadata", records.folder_A16, "folder_default_copyStatus")
			);
		}

		try {
			BatchProcessResults results = presenterService.execute(request);
			fail("error expected!");
		} catch (RecordServicesException.ValidationException e) {
			assertThat(extractingSimpleCodeAndParameters(e.getErrors(), "record", "metadataCode")).containsOnly(
					tuple("ValueRequirementValidator_requiredValueForMetadata", records.folder_A05, "folder_default_copyStatus"),
					tuple("ValueRequirementValidator_requiredValueForMetadata", records.folder_A16, "folder_default_copyStatus")
			);
		}
		assertThat(records.getFolder_A05().getRetentionRuleEntered()).isNotEqualTo(records.ruleId_2);

	}

	private String error(Class<?> validatorClass, String code) {
		return validatorClass.getCanonicalName() + "." + code;
	}

	@Test
	public void whenModifyingValuesWithImpactsInHierarchyInHierarchyThenCalculated()
			throws Exception {

		BatchProcessRequest request = new BatchProcessRequest().setUser(users.adminIn(zeCollection))
				.setIds(asList(records.folder_A04, records.folder_A16))
				.addModifiedMetadata(Folder.RETENTION_RULE_ENTERED, records.ruleId_2)
				.addModifiedMetadata(Folder.COPY_STATUS_ENTERED, CopyType.SECONDARY);

		BatchProcessResults results = presenterService.execute(request);

		assertThat(results.getRecordModifications()).extracting("recordId", "recordTitle").containsOnly(
				tuple(records.folder_A04, "Baleine"),
				tuple(records.folder_A16, "Chat")
		);

		assertThat(results.getRecordModifications(records.folder_A04).getFieldsModifications())
				.extracting("metadata.code", "valueBefore", "valueAfter").containsOnly(
				tuple("folder_default_retentionRule", "1 (Rule #1)", "2 (Rule #2)"),
				tuple("folder_default_mainCopyRule", "42-5-C", "2-0-D"),
				tuple("folder_default_copyStatus", "Principal", "Secondaire")
		);

		assertThat(results.getRecordModifications(records.folder_A16).getFieldsModifications())
				.extracting("metadata.code", "valueBefore", "valueAfter").containsOnly(
				tuple("folder_default_retentionRule", "1 (Rule #1)", "2 (Rule #2)"),
				tuple("folder_default_mainCopyRule", "42-5-C", "2-0-D"),
				tuple("folder_default_copyStatus", "Principal", "Secondaire"),
				tuple("folder_default_expectedTransferDate", "2002-10-31", "2003-10-31"),
				tuple("folder_default_expectedDestructionDate", null, "2003-10-31"),
				tuple("folder_default_expectedDepositDate", "2007-10-31", null)
		);

	}

	@Test
	public void whenModifyDocumentParentFolderInBatchOnlyHumanFriendlyMetadataAreShown()
			throws Exception {

		List<Document> folderA04Documents = rm.searchDocuments(where(rm.document.folder()).isEqualTo(records.folder_A04));
		Document document1 = folderA04Documents.get(0);
		Document document2 = folderA04Documents.get(1);
		Document document3 = folderA04Documents.get(2);

		BatchProcessRequest request = new BatchProcessRequest().setUser(users.adminIn(zeCollection))
				.setIds(asList(document1.getId(), document2.getId(), document3.getId()))
				.addModifiedMetadata(Document.FOLDER, records.folder_A07);

		BatchProcessResults results = presenterService.simulate(request);

		assertThat(results.getRecordModifications()).extracting("recordId", "recordTitle").containsOnly(
				tuple(document1.getId(), document1.getTitle()),
				tuple(document2.getId(), document2.getTitle()),
				tuple(document3.getId(), document3.getTitle()));

		assertThat(results.getRecordModifications(document1.getId()).getFieldsModifications())
				.extracting("metadata.code", "valueBefore", "valueAfter").containsOnly(

				tuple("document_default_retentionRule", "1 (Rule #1)", "3 (Rule #3)"),
				tuple("document_default_category", "X110 (X110)", "Z112 (Z112)"),
				tuple("document_default_folder", "A04 (Baleine)", "A07 (Bouc)"),
				tuple("document_default_mainCopyRule", "42-5-C", "999-4-T")
		);
	}

	@Test
	public void whenModifyContainerParentFolderInBatchOnlyHumanFriendlyMetadataAreShown()
			throws Exception {

		ContainerRecord container1 = records.getContainerBac01();
		ContainerRecord container2 = records.getContainerBac02();
		ContainerRecord container3 = records.getContainerBac03();

		BatchProcessRequest request = new BatchProcessRequest().setUser(users.adminIn(zeCollection))
				.setIds(asList(container1.getId(), container2.getId(), container3.getId()))
				.addModifiedMetadata(ContainerRecord.CAPACITY, 42.0)
				.addModifiedMetadata(ContainerRecord.ADMINISTRATIVE_UNIT, records.unitId_20d);

		BatchProcessResults results = presenterService.simulate(request);

		assertThat(results.getRecordModifications()).extracting("recordId", "recordTitle").containsOnly(
				tuple(container1.getId(), container1.getTitle()),
				tuple(container2.getId(), container2.getTitle()),
				tuple(container3.getId(), container3.getTitle()));

		assertThat(results.getRecordModifications(container1.getId()).getFieldsModifications())
				.extracting("metadata.code", "valueBefore", "valueAfter").containsOnly(
				tuple("containerRecord_default_administrativeUnit", "30C (Unité 30-C)", "20D (Unité 20-D)"),
				tuple("containerRecord_default_capacity", null, "42.0")
		);
	}

	@Test
	public void whenSimulateBatchProcessThenNoModificationsOccur()
			throws Exception {

		BatchProcessRequest request = new BatchProcessRequest().setUser(users.adminIn(zeCollection))
				.setIds(asList(records.folder_A03, records.folder_A04))
				.addModifiedMetadata("default_folder_title", "Mon dossier");

		BatchProcessResults results = presenterService.simulate(request);

		assertThat(results.getRecordModifications()).extracting("recordId", "recordTitle").containsOnly(
				tuple(records.folder_A03, "Alouette"),
				tuple(records.folder_A04, "Baleine")
		);

		assertThat(results.getRecordModifications(records.folder_A03).getImpacts()).isEmpty();
		assertThat(results.getRecordModifications(records.folder_A03).getFieldsModifications())
				.extracting("metadata.code", "valueBefore", "valueAfter").containsOnly(
				tuple("folder_default_title", "Alouette", "Mon dossier")
		);

		assertThat(results.getRecordModifications(records.folder_A04).getImpacts()).isEmpty();
		assertThat(results.getRecordModifications(records.folder_A04).getFieldsModifications())
				.extracting("metadata.code", "valueBefore", "valueAfter").containsOnly(
				tuple("folder_default_title", "Baleine", "Mon dossier")
		);

		assertThat(records.getFolder_A03().getTitle()).isEqualTo("Alouette");
		assertThat(records.getFolder_A04().getTitle()).isEqualTo("Baleine");

		results = presenterService.execute(request);

		assertThat(results.getRecordModifications()).extracting("recordId", "recordTitle").containsOnly(
				tuple(records.folder_A03, "Alouette"),
				tuple(records.folder_A04, "Baleine")
		);

		assertThat(results.getRecordModifications(records.folder_A03).getImpacts()).isEmpty();
		assertThat(results.getRecordModifications(records.folder_A03).getFieldsModifications())
				.extracting("metadata.code", "valueBefore", "valueAfter").containsOnly(
				tuple("folder_default_title", "Alouette", "Mon dossier")
		);

		assertThat(results.getRecordModifications(records.folder_A04).getImpacts()).isEmpty();
		assertThat(results.getRecordModifications(records.folder_A04).getFieldsModifications())
				.extracting("metadata.code", "valueBefore", "valueAfter").containsOnly(
				tuple("folder_default_title", "Baleine", "Mon dossier")
		);

		assertThat(records.getFolder_A03().getTitle()).isEqualTo("Mon dossier");
		assertThat(records.getFolder_A04().getTitle()).isEqualTo("Mon dossier");
	}

	@Test
	public void givenValuesOfEveryTypeAreModifiedThenAppliedAndShownInResults()
			throws Exception {

		getModelLayerFactory().getMetadataSchemasManager().modify(zeCollection, new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				MetadataSchemaTypeBuilder folderSchemaType = types.getSchemaType(Folder.SCHEMA_TYPE);
				MetadataSchemaBuilder defaultSchema = folderSchemaType.getDefaultSchema();

				defaultSchema.create("stringsMeta").setType(STRING).setMultivalue(true);
				defaultSchema.create("textMeta").setType(TEXT);
				defaultSchema.create("textsMeta").setType(TEXT).setMultivalue(true);
				defaultSchema.create("dateMeta").setType(DATE);
				defaultSchema.create("datesMeta").setType(DATE).setMultivalue(true);
				defaultSchema.create("dateTimeMeta").setType(DATE_TIME);
				defaultSchema.create("dateTimesMeta").setType(DATE_TIME).setMultivalue(true);
				defaultSchema.create("booleanMeta").setType(BOOLEAN);
				defaultSchema.create("booleansMeta").setType(BOOLEAN).setMultivalue(true);
				defaultSchema.create("numberMeta").setType(NUMBER);
				defaultSchema.create("numbersMeta").setType(NUMBER).setMultivalue(true);
				defaultSchema.create("enumMeta").setType(ENUM).defineAsEnum(FolderStatus.class);
				defaultSchema.create("enumsMeta").setType(ENUM).defineAsEnum(FolderStatus.class).setMultivalue(true);
				defaultSchema.create("referencedFolderMeta").setType(MetadataValueType.REFERENCE)
						.defineReferencesTo(folderSchemaType);
				defaultSchema.create("referencedFoldersMeta").setType(MetadataValueType.REFERENCE)
						.defineReferencesTo(folderSchemaType).setMultivalue(true);
			}
		});

		BatchProcessRequest request = new BatchProcessRequest().setUser(users.adminIn(zeCollection))
				.setIds(asList(records.folder_A03, records.folder_A04))
				.addModifiedMetadata("default_folder_title", "Mon dossier")
				.addModifiedMetadata("stringsMeta", asList("stringValue1", "stringValue2"))
				.addModifiedMetadata("textMeta", "zeTextValue")
				.addModifiedMetadata("textsMeta", asList("textValue1", "textValue2"))
				.addModifiedMetadata("dateMeta", date1)
				.addModifiedMetadata("datesMeta", asList(date2, date3))
				.addModifiedMetadata("dateTimeMeta", dateTime1)
				.addModifiedMetadata("dateTimesMeta", asList(dateTime2, dateTime3))
				.addModifiedMetadata("booleanMeta", true)
				.addModifiedMetadata("booleansMeta", asList(true, false))
				.addModifiedMetadata("numberMeta", 66.6)
				.addModifiedMetadata("numbersMeta", asList(66.6, 42))
				.addModifiedMetadata("enumMeta", INACTIVE_DEPOSITED)
				.addModifiedMetadata("enumsMeta", asList(FolderStatus.SEMI_ACTIVE, FolderStatus.ACTIVE))
				.addModifiedMetadata("referencedFolderMeta", records.folder_A06)
				.addModifiedMetadata("referencedFoldersMeta", asList(records.folder_A07, records.folder_A08));

		BatchProcessResults results = presenterService.simulate(request);

		assertThat(results.getRecordModifications()).extracting("recordId", "recordTitle").containsOnly(
				tuple(records.folder_A03, "Alouette"),
				tuple(records.folder_A04, "Baleine")
		);

		assertThat(results.getRecordModifications(records.folder_A03).getImpacts()).isEmpty();
		assertThat(results.getRecordModifications(records.folder_A03).getFieldsModifications())
				.extracting("metadata.code", "valueBefore", "valueAfter").containsOnly(
				tuple("folder_default_title", "Alouette", "Mon dossier"),
				tuple("folder_default_stringsMeta", "[]", "[stringValue1, stringValue2]"),
				tuple("folder_default_textMeta", null, "zeTextValue"),
				tuple("folder_default_textsMeta", "[]", "[textValue1, textValue2]"),
				tuple("folder_default_dateMeta", null, date1String),
				tuple("folder_default_datesMeta", "[]", "[" + date2String + ", " + date3String + "]"),
				tuple("folder_default_dateTimeMeta", null, dateTime1String),
				tuple("folder_default_dateTimesMeta", "[]", "[" + dateTime2String + ", " + dateTime3String + "]"),
				tuple("folder_default_booleanMeta", null, "Oui"),
				tuple("folder_default_booleansMeta", "[]", "[Oui, Non]"),
				tuple("folder_default_numberMeta", null, "66.6"),
				tuple("folder_default_numbersMeta", "[]", "[66.6, 42]"),
				tuple("folder_default_enumMeta", null, "Versé"),
				tuple("folder_default_enumsMeta", "[]", "[Semi-actif, Actif]"),
				tuple("folder_default_referencedFolderMeta", null, "A06 (Bison)"),
				tuple("folder_default_referencedFoldersMeta", "[]", "[A07 (Bouc), A08 (Boeuf)]")
		);

		assertThat(results.getRecordModifications(records.folder_A04).getImpacts()).isEmpty();
		assertThat(results.getRecordModifications(records.folder_A04).getFieldsModifications())
				.extracting("metadata.code", "valueBefore", "valueAfter").containsOnly(
				tuple("folder_default_title", "Baleine", "Mon dossier"),
				tuple("folder_default_stringsMeta", "[]", "[stringValue1, stringValue2]"),
				tuple("folder_default_textMeta", null, "zeTextValue"),
				tuple("folder_default_textsMeta", "[]", "[textValue1, textValue2]"),
				tuple("folder_default_dateMeta", null, date1String),
				tuple("folder_default_datesMeta", "[]", "[" + date2String + ", " + date3String + "]"),
				tuple("folder_default_dateTimeMeta", null, dateTime1String),
				tuple("folder_default_dateTimesMeta", "[]", "[" + dateTime2String + ", " + dateTime3String + "]"),
				tuple("folder_default_booleanMeta", null, "Oui"),
				tuple("folder_default_booleansMeta", "[]", "[Oui, Non]"),
				tuple("folder_default_numberMeta", null, "66.6"),
				tuple("folder_default_numbersMeta", "[]", "[66.6, 42]"),
				tuple("folder_default_enumMeta", null, "Versé"),
				tuple("folder_default_enumsMeta", "[]", "[Semi-actif, Actif]"),
				tuple("folder_default_referencedFolderMeta", null, "A06 (Bison)"),
				tuple("folder_default_referencedFoldersMeta", "[]", "[A07 (Bouc), A08 (Boeuf)]")
		);

	}

	@Test
	public void whenChangingTypeThenKeepValuesWithSharedField()
			throws Exception {

		Transaction transaction = new Transaction();
		transaction.add(rm.setType(records.getFolder_A01(), records.folderTypeEmploye())).set("subType", "customSubType")
				.setTitle("zetest");
		transaction.add(rm.setType(records.getFolder_A02(), records.folderTypeEmploye())).setTitle("zetest");
		getModelLayerFactory().newRecordServices().execute(transaction);

		assertThat(records.getFolder_A01().get("subType")).isEqualTo("customSubType");
		assertThat(records.getFolder_A02().get("subType")).isEqualTo("Dossier d'employé général");

		BatchProcessRequest request = new BatchProcessRequest().setUser(users.adminIn(zeCollection))
				.setIds(asList(records.folder_A01, records.folder_A02))
				.addModifiedMetadata(Folder.TYPE, records.folderTypeMeeting());

		BatchProcessResults results = presenterService.execute(request);

		assertThat(results.getRecordModifications(records.folder_A01).getFieldsModifications())
				.extracting("metadata.code", "valueBefore", "valueAfter").containsOnly(
				tuple("folder_meetingFolder_type", "employe (Dossier employé)", "meetingFolder (Réunion employé)"),
				tuple("folder_meetingFolder_meetingDateTime", null, "2010-12-20-01-02-03"),
				tuple("folder_employe_hireDate", "2010-12-20", null)
		);

		assertThat(results.getRecordModifications(records.folder_A02).getFieldsModifications())
				.extracting("metadata.code", "valueBefore", "valueAfter").containsOnly(
				tuple("folder_meetingFolder_type", "employe (Dossier employé)", "meetingFolder (Réunion employé)"),
				tuple("folder_meetingFolder_subType", "Dossier d'employé général", "Meeting important"),
				tuple("folder_meetingFolder_meetingDateTime", null, "2010-12-20-01-02-03"),
				tuple("folder_employe_hireDate", "2010-12-20", null)
		);

		assertThat(records.getFolder_A01().get("subType")).isEqualTo("customSubType");
		assertThat(records.getFolder_A02().get("subType")).isEqualTo("Meeting important");

		request = new BatchProcessRequest().setUser(users.adminIn(zeCollection))
				.setIds(asList(records.folder_A01, records.folder_A02))
				.addModifiedMetadata(Folder.TYPE, records.folderTypeEmploye())
				.addModifiedMetadata("subType", "");

		results = presenterService.simulate(request);

		assertThat(results.getRecordModifications(records.folder_A01).getFieldsModifications())
				.extracting("metadata.code", "valueBefore", "valueAfter").containsOnly(
				tuple("folder_employe_type", "meetingFolder (Réunion employé)", "employe (Dossier employé)"),
				tuple("folder_meetingFolder_meetingDateTime", "2010-12-20-01-02-03", null),
				tuple("folder_employe_hireDate", null, "2010-12-20")
		);

		assertThat(results.getRecordModifications(records.folder_A02).getFieldsModifications())
				.extracting("metadata.code", "valueBefore", "valueAfter").containsOnly(
				tuple("folder_employe_type", "meetingFolder (Réunion employé)", "employe (Dossier employé)"),
				tuple("folder_employe_subType", "Meeting important", "Dossier d'employé général"),
				tuple("folder_meetingFolder_meetingDateTime", "2010-12-20-01-02-03", null),
				tuple("folder_employe_hireDate", null, "2010-12-20")
		);

		assertThat(records.getFolder_A01().get("subType")).isEqualTo("customSubType");
		assertThat(records.getFolder_A02().get("subType")).isEqualTo("Meeting important");

	}

	@Test
	public void whenBatchProcessingThenOriginalTypeIsNonNullIfEachRecordsHaveTheSameType()
			throws Exception {

		Transaction transaction = new Transaction();
		transaction.add(rm.setType(records.getFolder_A01(), records.folderTypeEmploye()));
		transaction.add(rm.setType(records.getFolder_A02(), records.folderTypeEmploye()));
		transaction.add(rm.setType(records.getFolder_A03(), records.folderTypeMeeting()));
		transaction.add(rm.setType(records.getFolder_A04(), records.folderTypeOther()));
		transaction.add(rm.setType(records.getFolder_A05(), null));
		transaction.add(rm.setType(records.getFolder_A06(), null));

		getModelLayerFactory().newRecordServices().execute(transaction);

		assertThat(presenterService.getOriginType(asList(records.folder_A01, records.folder_A02, records.folder_A03,
				records.folder_A04, records.folder_A05, records.folder_A06))).isNull();

		assertThat(presenterService.getOriginType(asList(records.folder_A04, records.folder_A06))).isNull();
		assertThat(presenterService.getOriginType(asList(records.folder_A05, records.folder_A06))).isNull();
		assertThat(presenterService.getOriginType(asList(records.folder_A01, records.folder_A02, records.folder_A03))).isNull();
		assertThat(presenterService.getOriginType(asList(records.folder_A01, records.folder_A02, records.folder_A05))).isNull();
		assertThat(presenterService.getOriginType(asList(records.folder_A05, records.folder_A01, records.folder_A02))).isNull();
		assertThat(presenterService.getOriginType(asList(records.folder_A04))).isEqualTo(records.folderTypeOther().getId());
		assertThat(presenterService.getOriginType(asList(records.folder_A03))).isEqualTo(records.folderTypeMeeting().getId());
		assertThat(presenterService.getOriginType(asList(records.folder_A01, records.folder_A02)))
				.isEqualTo(records.folderTypeEmploye().getId());
	}

}
