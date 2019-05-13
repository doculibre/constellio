package com.constellio.app.modules.rm.model.calculators.document;

import com.constellio.app.modules.rm.RMConfigs;
import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.model.CopyRetentionRule;
import com.constellio.app.modules.rm.model.CopyRetentionRuleBuilder;
import com.constellio.app.modules.rm.model.enums.AllowModificationOfArchivisticStatusAndExpectedDatesChoice;
import com.constellio.app.modules.rm.model.enums.CopyType;
import com.constellio.app.modules.rm.model.enums.FolderStatus;
import com.constellio.app.modules.rm.model.enums.RetentionRuleScope;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Category;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.RetentionRule;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.validation.RecordMetadataValidator;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.schemas.MetadataSchemaTypesAlteration;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.sdk.tests.ConstellioTest;
import org.assertj.core.api.ObjectAssert;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static com.constellio.app.modules.rm.model.enums.DecommissioningDateBasedOn.CLOSE_DATE;
import static com.constellio.app.modules.rm.model.enums.RetentionRuleScope.DOCUMENTS;
import static com.constellio.app.modules.rm.model.enums.RetentionRuleScope.DOCUMENTS_AND_FOLDER;
import static com.constellio.model.entities.schemas.MetadataValueType.DATE;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

public class DocumentCalculatorsAcceptTest extends ConstellioTest {

	LocalDate squatreNovembre = new LocalDate(2014, 11, 4);

	String zeCategory = "zeCategory";
	String type1 = "type1";
	String type2 = "type2";
	String type3 = "type3";
	String type4 = "type4";
	String type5 = "type5";
	String w = "w";
	String w100 = "w100";
	String w110 = "w110";
	String w120 = "w120";

	RMSchemasRecordsServices rm;
	RMTestRecords records = new RMTestRecords(zeCollection);
	RecordServices recordServices;

	SearchServices searchServices;
	MetadataSchemasManager metadataSchemasManager;

	Document document;

	CopyRetentionRuleBuilder copyBuilder = CopyRetentionRuleBuilder.UUID();

	@Before
	public void setUp()
			throws Exception {
		prepareSystem(
				withZeCollection().withConstellioRMModule().withRMTest(records).withFoldersAndContainersOfEveryStatus()
		);

		metadataSchemasManager = getModelLayerFactory().getMetadataSchemasManager();
		searchServices = getModelLayerFactory().newSearchServices();
		rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
		recordServices = getModelLayerFactory().newRecordServices();

		Transaction transaction = new Transaction();
		transaction.add(rm.newDocumentTypeWithId(type1).setCode("type1Code").setTitle("Ze type 1"));
		transaction.add(rm.newDocumentTypeWithId(type2).setCode("type2Code").setTitle("Ze type 2"));
		transaction.add(rm.newDocumentTypeWithId(type3).setCode("type3Code").setTitle("Ze type 3"));
		transaction.add(rm.newDocumentTypeWithId(type4).setCode("type4Code").setTitle("Ze type 4"));
		transaction.add(rm.newDocumentTypeWithId(type5).setCode("type5Code").setTitle("Ze type 5"));
		transaction.add(rm.newCategoryWithId(zeCategory).setCode("ZeCategory").setTitle("Ze category"));
		transaction.add(rm.newCategoryWithId(w).setCode("W").setTitle("W"));
		transaction.add(rm.newCategoryWithId(w100).setCode("W-100").setTitle("W-100").setParent(w));
		transaction.add(rm.newCategoryWithId(w110).setCode("W-110").setTitle("W-110").setParent(w100));
		transaction.add(rm.newCategoryWithId(w120).setCode("W-120").setTitle("W-120").setParent(w100));
		recordServices.execute(transaction);

	}

	@Test
	public void givenActiveDocumentWhenCalculateValuesThenEqualsToParentFolder()
			throws Exception {

		List<Record> documentsRecords = getDocumentsFromFolder(records.folder_A10);
		document = rm.wrapDocument(documentsRecords.get(0));

		assertThat(document.isSemiActiveSameFateAsFolder()).isTrue();
		assertThat(document.isInactiveSameFateAsFolder()).isTrue();

		assertThat(document.getArchivisticStatus())
				.isEqualTo(records.getFolder_A10().getArchivisticStatus()).isEqualTo(FolderStatus.ACTIVE);

		assertThat(document.getRetentionRule())
				.isEqualTo(records.getFolder_A10().getRetentionRule()).isEqualTo(records.ruleId_2);

		assertThat(document.getFolderExpectedTransferDate())
				.isEqualTo(records.getFolder_A10().getExpectedTransferDate()).isEqualTo(new LocalDate("2006-10-31"));

		assertThat(document.getFolderExpectedDestructionDate())
				.isEqualTo(records.getFolder_A10().getExpectedDestructionDate()).isEqualTo(new LocalDate("2008-10-31"));

		assertThat(document.getFolderExpectedDepositDate())
				.isEqualTo(records.getFolder_A10().getExpectedDepositDate()).isEqualTo(new LocalDate("2008-10-31"));
	}

	@Test
	public void givenSemiActiveDocumentWhenCalculateValuesThenEqualsToParentFolder()
			throws Exception {

		List<Record> documentsRecords = getDocumentsFromFolder(records.folder_A42);
		document = rm.wrapDocument(documentsRecords.get(0));

		assertThat(document.isSemiActiveSameFateAsFolder()).isTrue();
		assertThat(document.isInactiveSameFateAsFolder()).isTrue();

		assertThat(document.getArchivisticStatus())
				.isEqualTo(records.getFolder_A42().getArchivisticStatus()).isEqualTo(FolderStatus.SEMI_ACTIVE);

		assertThat(document.getRetentionRule())
				.isEqualTo(records.getFolder_A42().getRetentionRule()).isEqualTo(records.ruleId_2);

		assertThat(document.getFolderActualTransferDate())
				.isEqualTo(records.getFolder_A42().getActualTransferDate()).isEqualTo(new LocalDate("2007-10-31"));

		assertThat(document.getFolderExpectedTransferDate())
				.isEqualTo(records.getFolder_A42().getExpectedTransferDate()).isNull();

		assertThat(document.getFolderExpectedDestructionDate())
				.isEqualTo(records.getFolder_A42().getExpectedDestructionDate()).isEqualTo(new LocalDate("2009-10-31"));

		assertThat(document.getFolderExpectedDepositDate())
				.isEqualTo(records.getFolder_A42().getExpectedDepositDate()).isEqualTo(new LocalDate("2009-10-31"));

		givenConfig(RMConfigs.DOCUMENT_RETENTION_RULES, true);
		waitForBatchProcess();
		recordServices.refresh(document);

		assertThat(document.getArchivisticStatus())
				.isEqualTo(records.getFolder_A42().getArchivisticStatus()).isEqualTo(FolderStatus.SEMI_ACTIVE);

		assertThat(document.getRetentionRule())
				.isEqualTo(records.getFolder_A42().getRetentionRule()).isEqualTo(records.ruleId_2);

		assertThat(document.getMainCopyRule())
				.isEqualTo(records.getFolder_A42().getMainCopyRule());

		assertThat(document.getFolderActualTransferDate())
				.isEqualTo(records.getFolder_A42().getActualTransferDate()).isEqualTo(new LocalDate("2007-10-31"));

		assertThat(document.getFolderExpectedTransferDate())
				.isEqualTo(records.getFolder_A42().getExpectedTransferDate()).isNull();

		assertThat(document.getFolderExpectedDestructionDate())
				.isEqualTo(records.getFolder_A42().getExpectedDestructionDate()).isEqualTo(new LocalDate("2009-10-31"));

		assertThat(document.getFolderExpectedDepositDate())
				.isEqualTo(records.getFolder_A42().getExpectedDepositDate()).isEqualTo(new LocalDate("2009-10-31"));

		assertThat(document.isSemiActiveSameFateAsFolder()).isTrue();
		assertThat(document.isInactiveSameFateAsFolder()).isTrue();
	}

	@Test
	public void givenDestroyedDocumentWhenCalculateExpectedDepositDateThenEqualsToParentFolder()
			throws Exception {

		List<Record> documentsRecords = getDocumentsFromFolder(records.folder_A80);
		document = rm.wrapDocument(documentsRecords.get(0));

		assertThat(document.isSemiActiveSameFateAsFolder()).isTrue();
		assertThat(document.isInactiveSameFateAsFolder()).isTrue();

		assertThat(document.getArchivisticStatus())
				.isEqualTo(records.getFolder_A80().getArchivisticStatus()).isEqualTo(FolderStatus.INACTIVE_DESTROYED);

		assertThat(document.getRetentionRule())
				.isEqualTo(records.getFolder_A80().getRetentionRule()).isEqualTo(records.ruleId_2);

		assertThat(document.getFolderActualTransferDate())
				.isEqualTo(records.getFolder_A80().getActualTransferDate()).isEqualTo(new LocalDate("2007-10-31"));

		assertThat(document.getFolderActualDestructionDate())
				.isEqualTo(records.getFolder_A80().getActualDestructionDate()).isEqualTo(new LocalDate("2011-02-13"));
	}

	@Test
	public void givenDepositedDocumentWhenCalculateExpectedDepositDateThenEqualsToParentFolder()
			throws Exception {

		List<Record> documentsRecords = getDocumentsFromFolder(records.folder_A79);
		document = rm.wrapDocument(documentsRecords.get(0));

		assertThat(document.isSemiActiveSameFateAsFolder()).isTrue();
		assertThat(document.isInactiveSameFateAsFolder()).isTrue();

		assertThat(document.getArchivisticStatus())
				.isEqualTo(records.getFolder_A79().getArchivisticStatus()).isEqualTo(FolderStatus.INACTIVE_DEPOSITED);

		assertThat(document.getRetentionRule())
				.isEqualTo(records.getFolder_A79().getRetentionRule()).isEqualTo(records.ruleId_2);

		assertThat(document.getFolderActualTransferDate())
				.isEqualTo(records.getFolder_A79().getActualTransferDate()).isEqualTo(new LocalDate("2007-10-31"));

		assertThat(document.getFolderActualDepositDate())
				.isEqualTo(records.getFolder_A79().getActualDepositDate()).isEqualTo(new LocalDate("2011-02-13"));
	}

	//Scenario #2
	@Test
	public void givenFolderAndDocumentRetentionRuleWithDefinedRulesForTypesThenDelaysCalculatedInFunctionOfDocumentType()
			throws Exception {

		givenConfig(RMConfigs.CALCULATED_CLOSING_DATE_NUMBER_OF_YEAR_WHEN_VARIABLE_RULE, 1);
		givenConfig(RMConfigs.CALCULATED_SEMIACTIVE_DATE_NUMBER_OF_YEAR_WHEN_VARIABLE_PERIOD, 1);
		givenConfig(RMConfigs.CALCULATED_INACTIVE_DATE_NUMBER_OF_YEAR_WHEN_VARIABLE_PERIOD, 1);
		givenConfig(RMConfigs.CALCULATED_CLOSING_DATE, true);

		givenConfig(RMConfigs.DOCUMENT_RETENTION_RULES, true);
		CopyRetentionRule principal888_1_C = copyBuilder.newPrincipal(asList(records.PA), "888-1-C");
		CopyRetentionRule secondary888_2_C = copyBuilder.newSecondary(asList(records.PA), "888-2-C");
		CopyRetentionRule principal5_3_C = copyBuilder.newPrincipal(asList(records.PA), "5-3-C").setTypeId(type1);
		CopyRetentionRule principal888_4_C = copyBuilder.newPrincipal(asList(records.PA), "888-4-C").setTypeId(type2);

		Transaction transaction = new Transaction();
		RetentionRule rule = transaction.add(rm.newRetentionRuleWithId("zeRule").setCode("zeRule").setTitle("zeRule"));
		rule.setScope(RetentionRuleScope.DOCUMENTS_AND_FOLDER);
		rule.setResponsibleAdministrativeUnits(true);
		rule.setCopyRetentionRules(principal888_1_C, secondary888_2_C);
		rule.setDocumentCopyRetentionRules(principal5_3_C, principal888_4_C
		);

		Folder principalFolder = transaction.add(newPrincipalFolderWithRule("principalFolder", rule));
		Document principalDocumentWithoutType = transaction.add(newDocumentInFolderWithType(principalFolder, null));
		Document principalDocumentWithType1 = transaction.add(newDocumentInFolderWithType(principalFolder, type1));
		Document principalDocumentWithType2 = transaction.add(newDocumentInFolderWithType(principalFolder, type2));
		Document principalDocumentWithType3 = transaction.add(newDocumentInFolderWithType(principalFolder, type3));

		Folder secondaryFolder = transaction.add(newSecondaryFolderWithRule("secondaryFolder", rule));
		Document secondaryDocumentWithoutType = transaction.add(newDocumentInFolderWithType(secondaryFolder, null));
		Document secondaryDocumentWithType1 = transaction.add(newDocumentInFolderWithType(secondaryFolder, type1));
		Document secondaryDocumentWithType2 = transaction.add(newDocumentInFolderWithType(secondaryFolder, type2));
		Document secondaryDocumentWithType3 = transaction.add(newDocumentInFolderWithType(secondaryFolder, type3));

		Category category = transaction.add(rm.getCategory(zeCategory).setRetentionRules(asList(rule)));

		recordServices.execute(transaction);

		assertThat(principalFolder.getApplicableCopyRules()).containsOnly(principal888_1_C);

		assertThat(principalDocumentWithoutType.getApplicableCopyRules()).containsOnly(principal888_1_C.in(rule, category));
		assertThat(principalDocumentWithType1.getApplicableCopyRules()).containsOnly(principal5_3_C.in(rule, category));
		assertThat(principalDocumentWithType2.getApplicableCopyRules()).containsOnly(principal888_4_C.in(rule, category));
		assertThat(principalDocumentWithType3.getApplicableCopyRules()).containsOnly(principal888_1_C.in(rule, category));

		assertThat(secondaryFolder.getApplicableCopyRules()).containsOnly(secondary888_2_C);
		assertThat(secondaryDocumentWithoutType.getApplicableCopyRules()).containsOnly(secondary888_2_C.in(rule, category));
		assertThat(secondaryDocumentWithType1.getApplicableCopyRules()).containsOnly(secondary888_2_C.in(rule, category));
		assertThat(secondaryDocumentWithType2.getApplicableCopyRules()).containsOnly(secondary888_2_C.in(rule, category));
		assertThat(secondaryDocumentWithType3.getApplicableCopyRules()).containsOnly(secondary888_2_C.in(rule, category));

		assertThat(principalFolder.getExpectedTransferDate()).isEqualTo(date(2017, 10, 31));
		assertThat(principalFolder.getExpectedDepositDate()).isEqualTo(date(2018, 10, 31));
		assertThat(principalFolder.getExpectedDestructionDate()).isNull();

		assertThat(principalDocumentWithoutType.getFolderExpectedTransferDate()).isEqualTo(date(2017, 10, 31));

		assertThat(principalDocumentWithoutType.isSemiActiveSameFateAsFolder()).isTrue();
		assertThat(principalDocumentWithoutType.isInactiveSameFateAsFolder()).isTrue();

		assertThat(principalDocumentWithType1.isSemiActiveSameFateAsFolder()).isFalse();
		assertThat(principalDocumentWithType1.isInactiveSameFateAsFolder()).isFalse();

		assertThat(principalDocumentWithType2.isSemiActiveSameFateAsFolder()).isTrue();
		assertThat(principalDocumentWithType2.isInactiveSameFateAsFolder()).isFalse();

		assertThat(principalDocumentWithType3.isSemiActiveSameFateAsFolder()).isTrue();
		assertThat(principalDocumentWithType3.isInactiveSameFateAsFolder()).isTrue();

		assertThat(secondaryDocumentWithType1.isSemiActiveSameFateAsFolder()).isTrue();
		assertThat(secondaryDocumentWithType1.isInactiveSameFateAsFolder()).isTrue();
	}

	@Test
	public void givenFolderWithRetentionRuleBasedOnMetadataWhichIsNotInDocumentF()
			throws Exception {

		givenConfig(RMConfigs.CALCULATED_CLOSING_DATE_NUMBER_OF_YEAR_WHEN_VARIABLE_RULE, 1);
		givenConfig(RMConfigs.CALCULATED_SEMIACTIVE_DATE_NUMBER_OF_YEAR_WHEN_VARIABLE_PERIOD, 1);
		givenConfig(RMConfigs.CALCULATED_INACTIVE_DATE_NUMBER_OF_YEAR_WHEN_VARIABLE_PERIOD, 1);
		givenConfig(RMConfigs.CALCULATED_CLOSING_DATE, true);

		givenConfig(RMConfigs.DOCUMENT_RETENTION_RULES, true);

		getModelLayerFactory().getMetadataSchemasManager().modify(zeCollection, new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				types.getSchema(Folder.DEFAULT_SCHEMA).create("zeDate").setType(DATE);
			}
		});

		CopyRetentionRule principal1_1_C = copyBuilder.newPrincipal(asList(records.PA), "1-1-C").setActiveDateMetadata("zeDate");
		CopyRetentionRule secondary2_2_C = copyBuilder.newSecondary(asList(records.PA), "2-2-C");
		CopyRetentionRule principal3_3_C = copyBuilder.newPrincipal(asList(records.PA), "3-3-C").setTypeId(type1);

		Transaction transaction = new Transaction();
		RetentionRule rule = transaction.add(rm.newRetentionRuleWithId("zeRule").setCode("zeRule").setTitle("zeRule"));
		rule.setScope(RetentionRuleScope.DOCUMENTS_AND_FOLDER);
		rule.setResponsibleAdministrativeUnits(true);
		rule.setCopyRetentionRules(principal1_1_C, secondary2_2_C);
		rule.setDocumentCopyRetentionRules(principal3_3_C);

		Folder principalFolder = transaction.add(newPrincipalFolderWithRule("principalFolder", rule));
		principalFolder.set("zeDate", date(2015, 1, 1));
		Document principalDocumentWithoutType = transaction.add(newDocumentInFolderWithType(principalFolder, null));
		Category category = transaction.add(rm.getCategory(zeCategory).setRetentionRules(asList(rule)));

		recordServices.execute(transaction);

		assertThat(principalDocumentWithoutType.isSemiActiveSameFateAsFolder()).isTrue();
		assertThat(principalDocumentWithoutType.isInactiveSameFateAsFolder()).isTrue();

	}

	@Test
	public void givenSemiActiveFolderWithRetentionRuleBasedOnMetadataWhichIsNotInDocumentF()
			throws Exception {

		givenConfig(RMConfigs.CALCULATED_CLOSING_DATE_NUMBER_OF_YEAR_WHEN_VARIABLE_RULE, 1);
		givenConfig(RMConfigs.CALCULATED_SEMIACTIVE_DATE_NUMBER_OF_YEAR_WHEN_VARIABLE_PERIOD, 1);
		givenConfig(RMConfigs.CALCULATED_INACTIVE_DATE_NUMBER_OF_YEAR_WHEN_VARIABLE_PERIOD, 1);
		givenConfig(RMConfigs.CALCULATED_CLOSING_DATE, true);

		givenConfig(RMConfigs.DOCUMENT_RETENTION_RULES, true);

		getModelLayerFactory().getMetadataSchemasManager().modify(zeCollection, new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				types.getSchema(Folder.DEFAULT_SCHEMA).create("zeDate").setType(DATE);
			}
		});

		CopyRetentionRule principal1_1_C = copyBuilder.newPrincipal(asList(records.PA), "1-1-C").setActiveDateMetadata("zeDate");
		CopyRetentionRule secondary2_2_C = copyBuilder.newSecondary(asList(records.PA), "2-2-C");
		CopyRetentionRule principal3_3_C = copyBuilder.newPrincipal(asList(records.PA), "3-3-C").setTypeId(type1);

		Transaction transaction = new Transaction();
		RetentionRule rule = transaction.add(rm.newRetentionRuleWithId("zeRule").setCode("zeRule").setTitle("zeRule"));
		rule.setScope(RetentionRuleScope.DOCUMENTS_AND_FOLDER);
		rule.setResponsibleAdministrativeUnits(true);
		rule.setCopyRetentionRules(principal1_1_C, secondary2_2_C);
		rule.setDocumentCopyRetentionRules(principal3_3_C);

		Folder principalFolder = transaction
				.add(newPrincipalFolderWithRule("principalFolder", rule).setActualTransferDate(date(2015, 1, 1)));
		principalFolder.set("zeDate", date(2015, 1, 1));
		Document principalDocumentWithoutType = transaction.add(newDocumentInFolderWithType(principalFolder, null));
		Category category = transaction.add(rm.getCategory(zeCategory).setRetentionRules(asList(rule)));

		recordServices.execute(transaction);

		assertThat(principalDocumentWithoutType.getArchivisticStatus()).isEqualTo(FolderStatus.SEMI_ACTIVE);
		assertThat(principalDocumentWithoutType.isInactiveSameFateAsFolder()).isTrue();

	}

	@Test
	public void givenManuallyEnteredSemiActiveFolderWithRetentionRuleBasedOnMetadataWhichIsNotInDocumentF()
			throws Exception {

		givenConfig(RMConfigs.CALCULATED_CLOSING_DATE_NUMBER_OF_YEAR_WHEN_VARIABLE_RULE, 1);
		givenConfig(RMConfigs.CALCULATED_SEMIACTIVE_DATE_NUMBER_OF_YEAR_WHEN_VARIABLE_PERIOD, 1);
		givenConfig(RMConfigs.CALCULATED_INACTIVE_DATE_NUMBER_OF_YEAR_WHEN_VARIABLE_PERIOD, 1);
		givenConfig(RMConfigs.CALCULATED_CLOSING_DATE, true);
		givenConfig(RMConfigs.ALLOW_MODIFICATION_OF_ARCHIVISTIC_STATUS_AND_EXPECTED_DATES,
				AllowModificationOfArchivisticStatusAndExpectedDatesChoice.ENABLED);
		givenConfig(RMConfigs.DOCUMENT_RETENTION_RULES, true);

		getModelLayerFactory().getMetadataSchemasManager().modify(zeCollection, new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				types.getSchema(Folder.DEFAULT_SCHEMA).create("zeDate").setType(DATE);
			}
		});

		CopyRetentionRule principal1_1_C = copyBuilder.newPrincipal(asList(records.PA), "1-1-C").setActiveDateMetadata("zeDate");
		CopyRetentionRule secondary2_2_C = copyBuilder.newSecondary(asList(records.PA), "2-2-C");
		CopyRetentionRule principal3_3_C = copyBuilder.newPrincipal(asList(records.PA), "3-3-C").setTypeId(type1);

		Transaction transaction = new Transaction();
		RetentionRule rule = transaction.add(rm.newRetentionRuleWithId("zeRule").setCode("zeRule").setTitle("zeRule"));
		rule.setScope(RetentionRuleScope.DOCUMENTS_AND_FOLDER);
		rule.setResponsibleAdministrativeUnits(true);
		rule.setCopyRetentionRules(principal1_1_C, secondary2_2_C);
		rule.setDocumentCopyRetentionRules(principal3_3_C);

		Folder principalFolder = transaction
				.add(newPrincipalFolderWithRule("principalFolder", rule).setManualArchivisticStatus(FolderStatus.SEMI_ACTIVE));
		principalFolder.set("zeDate", date(2015, 1, 1));
		Document principalDocumentWithoutType = transaction.add(newDocumentInFolderWithType(principalFolder, null));
		Category category = transaction.add(rm.getCategory(zeCategory).setRetentionRules(asList(rule)));

		recordServices.execute(transaction);

		assertThat(principalDocumentWithoutType.getArchivisticStatus()).isEqualTo(FolderStatus.SEMI_ACTIVE);
		assertThat(principalDocumentWithoutType.isInactiveSameFateAsFolder()).isTrue();

	}

	//Scenario #3
	@Test
	public void givenDocumentRetentionRuleWithDefinedRulesForTypesThenDelaysCalculatedInFunctionOfDocumentType()
			throws Exception {

		givenConfig(RMConfigs.DOCUMENT_RETENTION_RULES, true);
		waitForBatchProcess();

		CopyRetentionRule secondary888_1_C = copyBuilder.newSecondary(asList(records.PA), "888-1-C");
		CopyRetentionRule principal888_2_C = copyBuilder.newPrincipal(asList(records.PA), "888-2-C");
		CopyRetentionRule principal888_3_C = copyBuilder.newPrincipal(asList(records.PA), "888-3-C").setTypeId(type1);
		CopyRetentionRule principal888_4_C = copyBuilder.newPrincipal(asList(records.PA), "888-4-C").setTypeId(type2);

		Transaction transaction = new Transaction();
		RetentionRule rule = transaction.add(rm.newRetentionRuleWithId("zeRule").setCode("zeRule").setTitle("zeRule"));
		rule.setScope(DOCUMENTS);
		rule.setResponsibleAdministrativeUnits(true);
		rule.setPrincipalDefaultDocumentCopyRetentionRule(principal888_2_C);
		rule.setSecondaryDefaultDocumentCopyRetentionRule(secondary888_1_C);
		rule.setDocumentCopyRetentionRules(principal888_3_C, principal888_4_C);

		Category category = transaction.add(rm.getCategory(zeCategory).setRetentionRules(asList(rule)));

		Folder principalFolder = transaction.add(newPrincipalFolderWithRule(rule));
		Document principalDocumentWithoutType = transaction.add(newDocumentInFolderWithType(principalFolder, null));
		Document principalDocumentWithType1 = transaction.add(newDocumentInFolderWithType(principalFolder, type1));
		Document principalDocumentWithType2 = transaction.add(newDocumentInFolderWithType(principalFolder, type2));
		Document principalDocumentWithType3 = transaction.add(newDocumentInFolderWithType(principalFolder, type3));

		Folder secondaryFolder = transaction.add(newSecondaryFolderWithRule(rule));
		Document secondaryDocumentWithoutType = transaction.add(newDocumentInFolderWithType(secondaryFolder, null));
		Document secondaryDocumentWithType1 = transaction.add(newDocumentInFolderWithType(secondaryFolder, type1));
		Document secondaryDocumentWithType2 = transaction.add(newDocumentInFolderWithType(secondaryFolder, type2));
		Document secondaryDocumentWithType3 = transaction.add(newDocumentInFolderWithType(secondaryFolder, type3));

		recordServices.execute(transaction);

		assertThat(principalFolder.getCopyStatus()).isEqualTo(CopyType.PRINCIPAL);
		assertThat(principalFolder.getApplicableCopyRules()).isEmpty();
		assertThat(principalFolder.getMainCopyRule()).isNull();
		assertThat(principalDocumentWithoutType.getApplicableCopyRules()).containsOnly(principal888_2_C.in(rule, category));
		assertThat(principalDocumentWithType1.getApplicableCopyRules()).containsOnly(principal888_3_C.in(rule, category));
		assertThat(principalDocumentWithType2.getApplicableCopyRules()).containsOnly(principal888_4_C.in(rule, category));
		assertThat(principalDocumentWithType3.getApplicableCopyRules()).containsOnly(principal888_2_C.in(rule, category));
		assertThat(principalDocumentWithoutType.getMainCopyRule()).isNotNull();
		assertThat(principalDocumentWithType1.getMainCopyRule()).isNotNull();
		assertThat(principalDocumentWithType2.getMainCopyRule()).isNotNull();
		assertThat(principalDocumentWithType3.getMainCopyRule()).isNotNull();

		assertThat(secondaryFolder.getCopyStatus()).isEqualTo(CopyType.SECONDARY);
		assertThat(secondaryFolder.getApplicableCopyRules()).isEmpty();
		assertThat(principalFolder.getMainCopyRule()).isNull();
		assertThat(secondaryDocumentWithoutType.getApplicableCopyRules()).containsOnly(secondary888_1_C.in(rule, category));
		assertThat(secondaryDocumentWithType1.getApplicableCopyRules()).containsOnly(secondary888_1_C.in(rule, category));
		assertThat(secondaryDocumentWithType2.getApplicableCopyRules()).containsOnly(secondary888_1_C.in(rule, category));
		assertThat(secondaryDocumentWithType3.getApplicableCopyRules()).containsOnly(secondary888_1_C.in(rule, category));
		assertThat(secondaryDocumentWithoutType.getMainCopyRule()).isNotNull();
		assertThat(secondaryDocumentWithType1.getMainCopyRule()).isNotNull();
		assertThat(secondaryDocumentWithType2.getMainCopyRule()).isNotNull();
		assertThat(secondaryDocumentWithType3.getMainCopyRule()).isNotNull();
	}

	//#Scenario #4
	@Test
	public void givenFolderInCategoryWithMultipleRulesThenDocumentsHaveAlwaysTheSameRule()
			throws Exception {

		givenConfig(RMConfigs.DOCUMENT_RETENTION_RULES, true);
		waitForBatchProcess();
		CopyRetentionRule principal888_1_C = copyBuilder.newPrincipal(asList(records.PA), "888-1-C");
		CopyRetentionRule secondary888_2_C = copyBuilder.newSecondary(asList(records.PA), "888-2-C");
		CopyRetentionRule principal888_3_C = copyBuilder.newPrincipal(asList(records.PA), "888-3-C").setTypeId(type1);
		CopyRetentionRule principal888_4_C = copyBuilder.newPrincipal(asList(records.PA), "888-4-C");
		CopyRetentionRule secondary888_5_C = copyBuilder.newSecondary(asList(records.PA), "888-5-C");
		CopyRetentionRule principal888_6_C = copyBuilder.newPrincipal(asList(records.PA), "888-6-C").setTypeId(type1);
		CopyRetentionRule principal888_7_C = copyBuilder.newPrincipal(asList(records.PA), "888-7-C").setTypeId(type2);
		CopyRetentionRule principal888_8_C = copyBuilder.newPrincipal(asList(records.PA), "888-8-C").setTypeId(type1);
		CopyRetentionRule principal888_9_C = copyBuilder.newPrincipal(asList(records.PA), "888-9-C").setTypeId(type3);
		CopyRetentionRule principal888_10_C = copyBuilder.newPrincipal(asList(records.PA), "888-10-C");
		CopyRetentionRule secondary888_11_C = copyBuilder.newSecondary(asList(records.PA), "888-11-C");

		Transaction transaction = new Transaction();
		RetentionRule rule1 = transaction.add(rm.newRetentionRuleWithId("rule1").setCode("rule1").setTitle("rule1"));
		rule1.setScope(DOCUMENTS_AND_FOLDER);
		rule1.setResponsibleAdministrativeUnits(true);
		rule1.setCopyRetentionRules(principal888_1_C, secondary888_2_C);
		rule1.setDocumentCopyRetentionRules(principal888_3_C);

		RetentionRule rule2 = transaction.add(rm.newRetentionRuleWithId("rule2").setCode("rule2").setTitle("rule2"));
		rule2.setScope(DOCUMENTS_AND_FOLDER);
		rule2.setResponsibleAdministrativeUnits(true);
		rule2.setCopyRetentionRules(principal888_4_C, secondary888_5_C);
		rule2.setDocumentCopyRetentionRules(principal888_6_C, principal888_7_C);

		RetentionRule rule3 = transaction.add(rm.newRetentionRuleWithId("rule3").setCode("rule3").setTitle("rule3"));
		rule3.setScope(DOCUMENTS);
		rule3.setResponsibleAdministrativeUnits(true);
		rule3.setDocumentCopyRetentionRules(principal888_8_C, principal888_9_C);
		rule3.setPrincipalDefaultDocumentCopyRetentionRule(principal888_10_C);
		rule3.setSecondaryDefaultDocumentCopyRetentionRule(secondary888_11_C);

		Folder folder1 = transaction.add(newPrincipalFolderWithRule(rule1));
		Document folder1DocumentWithoutType = transaction.add(newDocumentInFolderWithType(folder1, null));
		Document folder1DocumentWithType1 = transaction.add(newDocumentInFolderWithType(folder1, type1));
		Document folder1DocumentWithType2 = transaction.add(newDocumentInFolderWithType(folder1, type2));
		Document folder1DocumentWithType3 = transaction.add(newDocumentInFolderWithType(folder1, type3));
		Document folder1DocumentWithType4 = transaction.add(newDocumentInFolderWithType(folder1, type4));

		Folder folder2 = transaction.add(newPrincipalFolderWithRule(rule2));
		Document folder2DocumentWithoutType = transaction.add(newDocumentInFolderWithType(folder2, null));
		Document folder2DocumentWithType1 = transaction.add(newDocumentInFolderWithType(folder2, type1));
		Document folder2DocumentWithType2 = transaction.add(newDocumentInFolderWithType(folder2, type2));
		Document folder2DocumentWithType3 = transaction.add(newDocumentInFolderWithType(folder2, type3));
		Document folder2DocumentWithType4 = transaction.add(newDocumentInFolderWithType(folder2, type4));

		Folder folder3 = transaction.add(newPrincipalFolderWithRule(rule3));
		Document folder3DocumentWithoutType = transaction.add(newDocumentInFolderWithType(folder3, null));
		Document folder3DocumentWithType1 = transaction.add(newDocumentInFolderWithType(folder3, type1));
		Document folder3DocumentWithType2 = transaction.add(newDocumentInFolderWithType(folder3, type2));
		Document folder3DocumentWithType3 = transaction.add(newDocumentInFolderWithType(folder3, type3));
		Document folder3DocumentWithType4 = transaction.add(newDocumentInFolderWithType(folder3, type4));

		Category category = transaction.add(rm.getCategory(zeCategory).setRetentionRules(asList(rule1, rule2, rule3)));

		recordServices.execute(transaction);

		assertThat(folder1.getApplicableCopyRules()).containsOnly(principal888_1_C);
		assertThat(folder1DocumentWithoutType.getApplicableCopyRules()).containsOnly(principal888_1_C.in(rule1, category));
		assertThat(folder1DocumentWithType1.getApplicableCopyRules()).containsOnly(principal888_8_C.in(rule3, category)); //TODO
		assertThat(folder1DocumentWithType2.getApplicableCopyRules()).containsOnly(principal888_1_C.in(rule1, category));
		assertThat(folder1DocumentWithType3.getApplicableCopyRules()).containsOnly(principal888_9_C.in(rule3, category));
		assertThat(folder1DocumentWithType4.getApplicableCopyRules()).containsOnly(principal888_1_C.in(rule1, category));

		assertThat(folder2.getApplicableCopyRules()).containsOnly(principal888_4_C);
		assertThat(folder2DocumentWithoutType.getApplicableCopyRules()).containsOnly(principal888_4_C.in(rule2, category));
		assertThat(folder2DocumentWithType1.getApplicableCopyRules()).containsOnly(principal888_8_C.in(rule3, category)); //TODO
		assertThat(folder2DocumentWithType2.getApplicableCopyRules()).containsOnly(principal888_7_C.in(rule2, category));
		assertThat(folder2DocumentWithType3.getApplicableCopyRules()).containsOnly(principal888_9_C.in(rule3, category));
		assertThat(folder2DocumentWithType4.getApplicableCopyRules()).containsOnly(principal888_4_C.in(rule2, category));

		assertThat(folder3.getApplicableCopyRules()).isEmpty();
		assertThat(folder3DocumentWithoutType.getApplicableCopyRules()).containsOnly(principal888_10_C.in(rule3, category));
		assertThat(folder3DocumentWithType1.getApplicableCopyRules()).containsOnly(principal888_8_C.in(rule3, category));
		assertThat(folder3DocumentWithType2.getApplicableCopyRules()).containsOnly(principal888_10_C.in(rule3, category));
		assertThat(folder3DocumentWithType3.getApplicableCopyRules()).containsOnly(principal888_9_C.in(rule3, category));
		assertThat(folder3DocumentWithType4.getApplicableCopyRules()).containsOnly(principal888_10_C.in(rule3, category));

	}

	//#Scenario #5
	@Test
	public void givenRuleOnMultipleLayersOfCategoriesThenCopyRuleChoosedByTakingTheMostSpecificOneThenTheFirstOrderRuleInCaseOfEquality()
			throws Exception {

		getModelLayerFactory().getModelLayerLogger().logRecord("w120");

		givenConfig(RMConfigs.DOCUMENT_RETENTION_RULES, true);
		waitForBatchProcess();
		CopyRetentionRule principal888_1_C = copyBuilder.newPrincipal(asList(records.PA), "888-1-C").setTypeId(type1);
		CopyRetentionRule principal888_2_C = copyBuilder.newPrincipal(asList(records.PA), "888-2-C").setTypeId(type2);
		CopyRetentionRule principal888_3_C = copyBuilder.newPrincipal(asList(records.PA), "888-3-C").setTypeId(type3);
		CopyRetentionRule principal888_4_C = copyBuilder.newPrincipal(asList(records.PA), "888-4-C");
		CopyRetentionRule secondary888_5_C = copyBuilder.newSecondary(asList(records.PA), "888-5-C");
		CopyRetentionRule principal888_6_C = copyBuilder.newPrincipal(asList(records.PA), "888-6-C");
		CopyRetentionRule secondary888_7_C = copyBuilder.newSecondary(asList(records.PA), "888-7-C");
		CopyRetentionRule principal888_8_C = copyBuilder.newPrincipal(asList(records.PA), "888-8-C").setTypeId(type1);
		CopyRetentionRule principal888_9_C = copyBuilder.newPrincipal(asList(records.PA), "888-9-C").setTypeId(type4);
		CopyRetentionRule principal888_10_C = copyBuilder.newPrincipal(asList(records.PA), "888-10-C").setTypeId(type3);
		CopyRetentionRule principal888_11_C = copyBuilder.newPrincipal(asList(records.PA), "888-11-C");
		CopyRetentionRule secondary888_12_C = copyBuilder.newSecondary(asList(records.PA), "888-12-C");

		CopyRetentionRule principal888_14_C = copyBuilder.newPrincipal(asList(records.PA), "888-14-C");
		CopyRetentionRule secondary888_15_C = copyBuilder.newSecondary(asList(records.PA), "888-15-C");
		CopyRetentionRule principal888_16_C = copyBuilder.newPrincipal(asList(records.PA), "888-16-C").setTypeId(type1);
		CopyRetentionRule principal888_17_C = copyBuilder.newPrincipal(asList(records.PA), "888-17-C").setTypeId(type2);
		CopyRetentionRule principal888_18_C = copyBuilder.newPrincipal(asList(records.PA), "888-18-C");
		CopyRetentionRule principal888_19_C = copyBuilder.newPrincipal(asList(records.PA), "888-19-C").setTypeId(type1);
		CopyRetentionRule principal888_20_C = copyBuilder.newPrincipal(asList(records.PA), "888-20-C").setTypeId(type5);
		CopyRetentionRule principal888_21_C = copyBuilder.newPrincipal(asList(records.PA), "888-21-C");
		CopyRetentionRule secondary888_22_C = copyBuilder.newSecondary(asList(records.PA), "888-22-C");

		Transaction transaction = new Transaction();
		RetentionRule rule1 = transaction.add(rm.newRetentionRuleWithId("rule1").setCode("rule1").setTitle("rule1"));
		rule1.setScope(DOCUMENTS);
		rule1.setResponsibleAdministrativeUnits(true);
		rule1.setDocumentCopyRetentionRules(principal888_1_C, principal888_2_C, principal888_3_C);
		rule1.setPrincipalDefaultDocumentCopyRetentionRule(principal888_4_C);
		rule1.setSecondaryDefaultDocumentCopyRetentionRule(secondary888_5_C);

		RetentionRule rule2 = transaction.add(rm.newRetentionRuleWithId("rule2").setCode("rule2").setTitle("rule2"));
		rule2.setScope(DOCUMENTS_AND_FOLDER);
		rule2.setResponsibleAdministrativeUnits(true);
		rule2.setCopyRetentionRules(principal888_6_C, secondary888_7_C);
		rule2.setDocumentCopyRetentionRules(principal888_8_C, principal888_9_C);

		RetentionRule rule3 = transaction.add(rm.newRetentionRuleWithId("rule3").setCode("rule3").setTitle("rule3"));
		rule3.setScope(DOCUMENTS);
		rule3.setResponsibleAdministrativeUnits(true);
		rule3.setDocumentCopyRetentionRules(principal888_10_C);
		rule3.setPrincipalDefaultDocumentCopyRetentionRule(principal888_11_C);
		rule3.setSecondaryDefaultDocumentCopyRetentionRule(secondary888_12_C);

		RetentionRule rule4 = transaction.add(rm.newRetentionRuleWithId("rule4").setCode("rule4").setTitle("rule4"));
		rule4.setScope(DOCUMENTS_AND_FOLDER);
		rule4.setResponsibleAdministrativeUnits(true);
		rule4.setCopyRetentionRules(principal888_14_C, secondary888_15_C);
		rule4.setDocumentCopyRetentionRules(principal888_16_C, principal888_17_C);

		RetentionRule rule5 = transaction.add(rm.newRetentionRuleWithId("rule5").setCode("rule5").setTitle("rule5"));
		rule5.setScope(DOCUMENTS);
		rule5.setResponsibleAdministrativeUnits(true);
		rule5.setDocumentCopyRetentionRules(principal888_19_C, principal888_20_C);
		rule5.setPrincipalDefaultDocumentCopyRetentionRule(principal888_21_C);
		rule5.setSecondaryDefaultDocumentCopyRetentionRule(secondary888_22_C);

		Category w = transaction.add(rm.getCategory("w").setRetentionRules(asList(rule1)));
		Category w100 = transaction.add(rm.getCategory("w100").setRetentionRules(asList(rule2, rule3)));
		Category w110 = transaction.add(rm.getCategory("w110").setRetentionRules(asList(rule4, rule5)));
		Category w120 = transaction.add(rm.getCategory("w120"));

		Folder folder1 = transaction.add(newPrincipalFolderInCategoryWithRule("w", rule1));
		Document folder1DocumentWithoutType = transaction.add(newDocumentInFolderWithType(folder1, null));
		Document folder1DocumentWithType1 = transaction.add(newDocumentInFolderWithType(folder1, type1));
		Document folder1DocumentWithType2 = transaction.add(newDocumentInFolderWithType(folder1, type2));
		Document folder1DocumentWithType3 = transaction.add(newDocumentInFolderWithType(folder1, type3));
		Document folder1DocumentWithType4 = transaction.add(newDocumentInFolderWithType(folder1, type4));
		Document folder1DocumentWithType5 = transaction.add(newDocumentInFolderWithType(folder1, type5));

		Folder folder2 = transaction.add(newPrincipalFolderInCategoryWithRule("w100", rule2));
		Document folder2DocumentWithoutType = transaction.add(newDocumentInFolderWithType(folder2, null));
		Document folder2DocumentWithType1 = transaction.add(newDocumentInFolderWithType(folder2, type1));
		Document folder2DocumentWithType2 = transaction.add(newDocumentInFolderWithType(folder2, type2));
		Document folder2DocumentWithType3 = transaction.add(newDocumentInFolderWithType(folder2, type3));
		Document folder2DocumentWithType4 = transaction.add(newDocumentInFolderWithType(folder2, type4));
		Document folder2DocumentWithType5 = transaction.add(newDocumentInFolderWithType(folder2, type5));

		Folder folder3 = transaction.add(newPrincipalFolderInCategoryWithRule("w100", rule3));
		Document folder3DocumentWithoutType = transaction.add(newDocumentInFolderWithType(folder3, null));
		Document folder3DocumentWithType1 = transaction.add(newDocumentInFolderWithType(folder3, type1));
		Document folder3DocumentWithType2 = transaction.add(newDocumentInFolderWithType(folder3, type2));
		Document folder3DocumentWithType3 = transaction.add(newDocumentInFolderWithType(folder3, type3));
		Document folder3DocumentWithType4 = transaction.add(newDocumentInFolderWithType(folder3, type4));
		Document folder3DocumentWithType5 = transaction.add(newDocumentInFolderWithType(folder3, type5));

		Folder folder4 = transaction.add(newPrincipalFolderInCategoryWithRule("w110", rule2));
		Document folder4DocumentWithoutType = transaction.add(newDocumentInFolderWithType(folder4, null));
		Document folder4DocumentWithType1 = transaction.add(newDocumentInFolderWithType(folder4, type1));
		Document folder4DocumentWithType2 = transaction.add(newDocumentInFolderWithType(folder4, type2));
		Document folder4DocumentWithType3 = transaction.add(newDocumentInFolderWithType(folder4, type3));
		Document folder4DocumentWithType4 = transaction.add(newDocumentInFolderWithType(folder4, type4));
		Document folder4DocumentWithType5 = transaction.add(newDocumentInFolderWithType(folder4, type5));

		Folder folder5 = transaction.add(newPrincipalFolderInCategoryWithRule("w110", rule3));
		Document folder5DocumentWithoutType = transaction.add(newDocumentInFolderWithType(folder5, null));
		Document folder5DocumentWithType1 = transaction.add(newDocumentInFolderWithType(folder5, type1));
		Document folder5DocumentWithType2 = transaction.add(newDocumentInFolderWithType(folder5, type2));
		Document folder5DocumentWithType3 = transaction.add(newDocumentInFolderWithType(folder5, type3));
		Document folder5DocumentWithType4 = transaction.add(newDocumentInFolderWithType(folder5, type4));
		Document folder5DocumentWithType5 = transaction.add(newDocumentInFolderWithType(folder5, type5));

		Folder folder6 = transaction.add(newPrincipalFolderInCategoryWithRule("w110", rule4));
		Document folder6DocumentWithoutType = transaction.add(newDocumentInFolderWithType(folder6, null));
		Document folder6DocumentWithType1 = transaction.add(newDocumentInFolderWithType(folder6, type1));
		Document folder6DocumentWithType2 = transaction.add(newDocumentInFolderWithType(folder6, type2));
		Document folder6DocumentWithType3 = transaction.add(newDocumentInFolderWithType(folder6, type3));
		Document folder6DocumentWithType4 = transaction.add(newDocumentInFolderWithType(folder6, type4));
		Document folder6DocumentWithType5 = transaction.add(newDocumentInFolderWithType(folder6, type5));

		Folder folder7 = transaction.add(newPrincipalFolderInCategoryWithRule("w120", rule2));
		Document folder7DocumentWithoutType = transaction.add(newDocumentInFolderWithType(folder7, null));
		Document folder7DocumentWithType1 = transaction.add(newDocumentInFolderWithType(folder7, type1));
		Document folder7DocumentWithType2 = transaction.add(newDocumentInFolderWithIdAndType(folder7, "zeProblematic", type2));
		Document folder7DocumentWithType3 = transaction.add(newDocumentInFolderWithType(folder7, type3));
		Document folder7DocumentWithType4 = transaction.add(newDocumentInFolderWithType(folder7, type4));
		Document folder7DocumentWithType5 = transaction.add(newDocumentInFolderWithType(folder7, type5));

		Folder folder8 = transaction.add(newPrincipalFolderInCategoryWithRule("w120", rule3));
		Document folder8DocumentWithoutType = transaction.add(newDocumentInFolderWithType(folder8, null));
		Document folder8DocumentWithType1 = transaction.add(newDocumentInFolderWithType(folder8, type1));
		Document folder8DocumentWithType2 = transaction.add(newDocumentInFolderWithType(folder8, type2));
		Document folder8DocumentWithType3 = transaction.add(newDocumentInFolderWithType(folder8, type3));
		Document folder8DocumentWithType4 = transaction.add(newDocumentInFolderWithType(folder8, type4));
		Document folder8DocumentWithType5 = transaction.add(newDocumentInFolderWithType(folder8, type5));

		recordServices.execute(transaction);

		assertThat(folder1.getApplicableCopyRules()).isEmpty();
		assertThat(folder1DocumentWithoutType.getApplicableCopyRules()).containsOnly(principal888_4_C.in(rule1, w));
		assertThat(folder1DocumentWithType1.getApplicableCopyRules()).containsOnly(principal888_1_C.in(rule1, w));
		assertThat(folder1DocumentWithType2.getApplicableCopyRules()).containsOnly(principal888_2_C.in(rule1, w));
		assertThat(folder1DocumentWithType3.getApplicableCopyRules()).containsOnly(principal888_3_C.in(rule1, w));
		assertThat(folder1DocumentWithType4.getApplicableCopyRules()).containsOnly(principal888_4_C.in(rule1, w));
		assertThat(folder1DocumentWithType5.getApplicableCopyRules()).containsOnly(principal888_4_C.in(rule1, w));

		assertThat(folder2.getApplicableCopyRules()).containsOnly(principal888_6_C);
		assertThat(folder2DocumentWithoutType.getApplicableCopyRules()).containsOnly(principal888_6_C.in(rule2, w100));
		assertThat(folder2DocumentWithType1.getApplicableCopyRules()).containsOnly(principal888_8_C.in(rule2, w100));
		assertThat(folder2DocumentWithType2.getApplicableCopyRules()).containsOnly(principal888_2_C.in(rule1, w));
		assertThat(folder2DocumentWithType3.getApplicableCopyRules()).containsOnly(principal888_10_C.in(rule3, w100)); //TODO
		assertThat(folder2DocumentWithType4.getApplicableCopyRules()).containsOnly(principal888_9_C.in(rule2, w100));
		assertThat(folder2DocumentWithType5.getApplicableCopyRules()).containsOnly(principal888_6_C.in(rule2, w100));

		assertThat(folder3.getApplicableCopyRules()).isEmpty();
		assertThat(folder3DocumentWithoutType.getApplicableCopyRules()).containsOnly(principal888_11_C.in(rule3, w100));
		assertThat(folder3DocumentWithType1.getApplicableCopyRules()).containsOnly(principal888_1_C.in(rule1, w));
		assertThat(folder3DocumentWithType2.getApplicableCopyRules()).containsOnly(principal888_2_C.in(rule1, w));
		assertThat(folder3DocumentWithType3.getApplicableCopyRules()).containsOnly(principal888_10_C.in(rule3, w100));
		assertThat(folder3DocumentWithType4.getApplicableCopyRules()).containsOnly(principal888_11_C.in(rule3, w100));
		assertThat(folder3DocumentWithType5.getApplicableCopyRules()).containsOnly(principal888_11_C.in(rule3, w100));

		assertThat(folder4.getApplicableCopyRules()).containsOnly(principal888_6_C);
		assertThat(folder4DocumentWithoutType.getApplicableCopyRules())
				.containsOnly(principal888_6_C.in(rule2, w110)); //Bad rubric, but not a problem
		assertThat(folder4DocumentWithType1.getApplicableCopyRules()).containsOnly(principal888_19_C.in(rule5, w110));
		assertThat(folder4DocumentWithType2.getApplicableCopyRules()).containsOnly(principal888_2_C.in(rule1, w));
		assertThat(folder4DocumentWithType3.getApplicableCopyRules()).containsOnly(principal888_10_C.in(rule3, w100));
		assertThat(folder4DocumentWithType4.getApplicableCopyRules())
				.containsOnly(principal888_9_C.in(rule2, w110)); //Bad rubric, but not a problem
		assertThat(folder4DocumentWithType5.getApplicableCopyRules()).containsOnly(principal888_20_C.in(rule5, w110));

		assertThat(folder4.getApplicableCopyRules()).containsOnly(principal888_6_C);
		assertThat(folder4DocumentWithoutType.getApplicableCopyRules())
				.containsOnly(principal888_6_C.in(rule2, w110)); //Bad rubric, but not a problem
		assertThat(folder4DocumentWithType1.getApplicableCopyRules()).containsOnly(principal888_19_C.in(rule5, w110));
		assertThat(folder4DocumentWithType2.getApplicableCopyRules()).containsOnly(principal888_2_C.in(rule1, w));
		assertThat(folder4DocumentWithType3.getApplicableCopyRules()).containsOnly(principal888_10_C.in(rule3, w100));
		assertThat(folder4DocumentWithType4.getApplicableCopyRules())
				.containsOnly(principal888_9_C.in(rule2, w110)); //Bad rubric, but not a problem

		assertThat(folder4DocumentWithoutType.getRetentionRule()).isEqualTo(rule2.getId());
		assertThat(folder4DocumentWithType1.getRetentionRule()).isEqualTo(rule5.getId());
		assertThat(folder4DocumentWithType2.getRetentionRule()).isEqualTo(rule1.getId());
		assertThat(folder4DocumentWithType3.getRetentionRule()).isEqualTo(rule3.getId());
		assertThat(folder4DocumentWithType4.getRetentionRule()).isEqualTo(rule2.getId());
		assertThat(folder4DocumentWithType5.getRetentionRule()).isEqualTo(rule5.getId());

		assertThat(folder5.getApplicableCopyRules()).isEmpty();
		assertThat(folder5DocumentWithoutType.getApplicableCopyRules())
				.containsOnly(principal888_11_C.in(rule3, w110)); //Bad rubric, but not a problem
		assertThat(folder5DocumentWithType1.getApplicableCopyRules()).containsOnly(principal888_19_C.in(rule5, w110));
		assertThat(folder5DocumentWithType2.getApplicableCopyRules()).containsOnly(principal888_2_C.in(rule1, w));
		assertThat(folder5DocumentWithType3.getApplicableCopyRules())
				.containsOnly(principal888_10_C.in(rule3, w110)); //Bad rubric, but not a problem
		assertThat(folder5DocumentWithType4.getApplicableCopyRules())
				.containsOnly(principal888_11_C.in(rule3, w110)); //Bad rubric, but not a problem
		assertThat(folder5DocumentWithType5.getApplicableCopyRules()).containsOnly(principal888_20_C.in(rule5, w110));

		assertThat(folder6.getApplicableCopyRules()).containsOnly(principal888_14_C);
		assertThat(folder6DocumentWithoutType.getApplicableCopyRules()).containsOnly(principal888_14_C.in(rule4, w110));
		assertThat(folder6DocumentWithType1.getApplicableCopyRules()).containsOnly(principal888_19_C.in(rule5, w110)); //TODO
		assertThat(folder6DocumentWithType2.getApplicableCopyRules()).containsOnly(principal888_17_C.in(rule4, w110));
		assertThat(folder6DocumentWithType3.getApplicableCopyRules()).containsOnly(principal888_10_C.in(rule3, w100));
		assertThat(folder6DocumentWithType4.getApplicableCopyRules()).containsOnly(principal888_14_C.in(rule4, w110));
		assertThat(folder6DocumentWithType5.getApplicableCopyRules()).containsOnly(principal888_20_C.in(rule5, w110));

		assertThat(folder7.getCopyStatus()).isEqualTo(CopyType.PRINCIPAL);
		assertThat(folder7.getApplicableCopyRules()).containsOnly(principal888_6_C);
		assertThat(folder7.getCloseDate()).isEqualTo(null);
		assertThat(folder7.getCategory()).isEqualTo(w120.getId());
		assertThat(folder7.getRetentionRule()).isEqualTo("rule2");
		assertThat(folder7.getExpectedDestructionDate()).isEqualTo(null);
		assertThat(folder7.getExpectedDepositDate()).isEqualTo(null);
		assertThat(folder7.getExpectedDestructionDate()).isEqualTo(null);

		assertThat(folder7DocumentWithoutType.getApplicableCopyRules())
				.containsOnly(principal888_6_C.in(rule2, w120));//Bad rubric, but not a problem
		assertThat(folder7DocumentWithType1.getApplicableCopyRules())
				.containsOnly(principal888_8_C.in(rule2, w120));//Bad rubric, but not a problem
		assertThat(folder7DocumentWithType2.getApplicableCopyRules()).containsOnly(principal888_2_C.in(rule1, w));
		assertThat(folder7DocumentWithType3.getApplicableCopyRules()).containsOnly(principal888_10_C.in(rule3, w100));
		assertThat(folder7DocumentWithType4.getApplicableCopyRules()).containsOnly(principal888_9_C.in(rule2, w120));
		assertThat(folder7DocumentWithType5.getApplicableCopyRules()).containsOnly(principal888_6_C.in(rule2, w120));

		assertThat(folder8.getApplicableCopyRules()).isEmpty();
		assertThat(folder8DocumentWithoutType.getApplicableCopyRules()).containsOnly(principal888_11_C.in(rule3, w120));
		assertThat(folder8DocumentWithType1.getApplicableCopyRules()).containsOnly(principal888_1_C.in(rule1, w));
		assertThat(folder8DocumentWithType2.getApplicableCopyRules()).containsOnly(principal888_2_C.in(rule1, w));
		assertThat(folder8DocumentWithType3.getApplicableCopyRules()).containsOnly(principal888_10_C.in(rule3, w120));
		assertThat(folder8DocumentWithType4.getApplicableCopyRules()).containsOnly(principal888_11_C.in(rule3, w120));
		assertThat(folder8DocumentWithType5.getApplicableCopyRules()).containsOnly(principal888_11_C.in(rule3, w120));
	}

	@Test
	public void givedRulesWithFixedCopyRulesThenValidDates()
			throws Exception {

		// Mêmes dates que FolderAcceptanceTest.givenPrincipalFolderWithTwoMediumTypesAndYearEndInSufficientPeriodThenHasValidCalculedDates

		givenConfig(RMConfigs.CALCULATED_CLOSING_DATE, true);
		givenConfig(RMConfigs.DECOMMISSIONING_DATE_BASED_ON, CLOSE_DATE);
		givenConfig(RMConfigs.YEAR_END_DATE, "03/31");
		givenConfig(RMConfigs.REQUIRED_DAYS_BEFORE_YEAR_END_FOR_NOT_ADDING_A_YEAR, 60);
		givenConfig(RMConfigs.DOCUMENT_RETENTION_RULES, true);
		waitForBatchProcess();
		createDateMetadatasAndCustomSchemas();

		CopyRetentionRule principal2_1_C = copyBuilder.newPrincipal(asList(records.PA), "2-1-T").setTypeId(type1);
		CopyRetentionRule principal3_2_C = copyBuilder.newPrincipal(asList(records.PA), "3-2-C").setTypeId(type2);
		CopyRetentionRule principal4_3_C = copyBuilder.newPrincipal(asList(records.PA), "4-3-C").setTypeId(type3);
		CopyRetentionRule principal5_5_D = copyBuilder.newPrincipal(asList(records.PA), "5-5-D").setTypeId(type4);
		CopyRetentionRule principal6_5_C = copyBuilder.newPrincipal(asList(records.PA), "6-5-C").setTypeId(type5);
		CopyRetentionRule principal888_6_C = copyBuilder.newPrincipal(asList(records.PA), "888-1-C");
		CopyRetentionRule secondary888_7_C = copyBuilder.newSecondary(asList(records.PA), "888-2-C");

		Transaction transaction = new Transaction();
		RetentionRule rule1 = transaction.add(rm.newRetentionRuleWithId("rule1").setCode("rule1").setTitle("rule1"));
		rule1.setScope(DOCUMENTS);
		rule1.setResponsibleAdministrativeUnits(true);
		rule1.setDocumentCopyRetentionRules(principal2_1_C, principal3_2_C, principal4_3_C, principal5_5_D,
				principal6_5_C);
		rule1.setPrincipalDefaultDocumentCopyRetentionRule(principal888_6_C);
		rule1.setSecondaryDefaultDocumentCopyRetentionRule(secondary888_7_C);
		transaction.add(rm.getCategory(zeCategory).setRetentionRules(asList(rule1)));
		transaction.add(rule1);

		Folder folder1 = transaction.add(newPrincipalFolderWithRule(rule1)).setOpenDate(new LocalDate(2015, 1, 1));
		Folder folder2 = transaction.add(newPrincipalFolderWithRule(rule1)).setOpenDate(new LocalDate(2015, 2, 1));

		Document folder1ActiveDocument_2_1_T = transaction.add(newDocumentInFolderWithType(folder1, type1));
		Document folder1ActiveDocument_4_3_C = transaction.add(newDocumentInFolderWithType(folder1, type3));
		Document folder1ActiveDocument_5_5_D = transaction.add(newDocumentInFolderWithType(folder1, type4));
		Document folder2ActiveDocument_4_3_C = transaction.add(newDocumentInFolderWithType(folder2, type3));

		Document semiActiveDocument_2_1_T = transaction.add(newDocumentInFolderWithType(folder1, type1)
				.setActualTransferDateEntered(new LocalDate(3015, 1, 1)));
		Document semiActiveDocument_4_3_C = transaction.add(newDocumentInFolderWithType(folder1, type3)
				.setActualTransferDateEntered(new LocalDate(3015, 1, 1)));
		Document semiActiveDocumentTransferedNearYearEnd_5_5_D = transaction.add(newDocumentInFolderWithType(folder1, type4)
				.setActualTransferDateEntered(new LocalDate(3015, 2, 1)));

		Document depositedDocument = transaction.add(newDocumentInFolderWithType(folder1, type1)
				.setActualTransferDateEntered(date(3015, 1, 1)).setActualDepositDateEntered(date(4015, 1, 1)));

		Document destroyedDocument = transaction.add(newDocumentInFolderWithType(folder1, type1)
				.setActualTransferDateEntered(date(3015, 1, 1)).setActualDestructionDateEntered(date(4015, 2, 1)));

		recordServices.execute(transaction);

		assertThatDocument(folder1ActiveDocument_2_1_T).isActiveDocument()
				.withExpectedTransfer(date(2017, 3, 31)).withExpectedDestroyAndDeposit(date(2018, 3, 31));

		assertThatDocument(folder1ActiveDocument_4_3_C).isActiveDocument()
				.withExpectedTransfer(date(2019, 3, 31)).withExpectedDestroy(null).withExpectedDeposit(date(2022, 3, 31));

		assertThatDocument(folder1ActiveDocument_5_5_D).isActiveDocument()
				.withExpectedTransfer(date(2020, 3, 31)).withExpectedDestroy(date(2025, 3, 31)).withExpectedDeposit(null);

		assertThatDocument(folder2ActiveDocument_4_3_C).isActiveDocument()
				.withExpectedTransfer(date(2020, 3, 31)).withExpectedDestroy(null).withExpectedDeposit(date(2023, 3, 31));

		assertThatDocument(semiActiveDocument_2_1_T).isSemiActiveDocument(date(3015, 1, 1))
				.withExpectedDestroyAndDeposit(date(3016, 3, 31));

		assertThatDocument(semiActiveDocument_4_3_C).isSemiActiveDocument(date(3015, 1, 1))
				.withExpectedDestroy(null).withExpectedDeposit(date(3018, 3, 31));

		assertThatDocument(semiActiveDocumentTransferedNearYearEnd_5_5_D).isSemiActiveDocument(date(3015, 2, 1))
				.withExpectedDestroy(date(3021, 3, 31)).withExpectedDeposit(null);

		assertThatDocument(depositedDocument).isDepositedDocument(date(3015, 1, 1), date(4015, 1, 1));

		assertThatDocument(destroyedDocument).isDestroyedDocument(date(3015, 1, 1), date(4015, 2, 1));

	}

	@Test
	public void givedRulesWithOpenCopyRulesThenValidDates()
			throws Exception {

		// Mêmes dates que FolderAcceptanceTest.givenPrincipalFolderWithTwoMediumTypesAndYearEndInSufficientPeriodThenHasValidCalculedDates

		givenConfig(RMConfigs.CALCULATED_CLOSING_DATE, true);
		givenConfig(RMConfigs.DECOMMISSIONING_DATE_BASED_ON, CLOSE_DATE);
		givenConfig(RMConfigs.YEAR_END_DATE, "03/31");
		givenConfig(RMConfigs.REQUIRED_DAYS_BEFORE_YEAR_END_FOR_NOT_ADDING_A_YEAR, 60);
		givenConfig(RMConfigs.DOCUMENT_RETENTION_RULES, true);
		givenConfig(RMConfigs.CALCULATED_SEMIACTIVE_DATE_NUMBER_OF_YEAR_WHEN_VARIABLE_PERIOD, 20);
		givenConfig(RMConfigs.CALCULATED_INACTIVE_DATE_NUMBER_OF_YEAR_WHEN_VARIABLE_PERIOD, 30);
		waitForBatchProcess();
		createDateMetadatasAndCustomSchemas();

		CopyRetentionRule principal888_1_C = copyBuilder.newPrincipal(asList(records.PA), "888-1-C").setTypeId(type1);
		CopyRetentionRule principal3_2_C = copyBuilder.newPrincipal(asList(records.PA), "3-2-C").setTypeId(type2);
		CopyRetentionRule principal4_999_C = copyBuilder.newPrincipal(asList(records.PA), "4-999-C").setTypeId(type3);
		CopyRetentionRule principal888_888_D = copyBuilder.newPrincipal(asList(records.PA), "888-888-D").setTypeId(type4);
		CopyRetentionRule principal6_5_C = copyBuilder.newPrincipal(asList(records.PA), "6-5-C").setTypeId(type5);
		CopyRetentionRule principal888_5_C = copyBuilder.newPrincipal(asList(records.PA), "888-5-C");
		CopyRetentionRule secondary888_6_C = copyBuilder.newSecondary(asList(records.PA), "888-6-C");

		Transaction transaction = new Transaction();
		RetentionRule rule1 = transaction.add(rm.newRetentionRuleWithId("rule1").setCode("rule1").setTitle("rule1"));
		rule1.setScope(DOCUMENTS);
		rule1.setResponsibleAdministrativeUnits(true);
		rule1.setDocumentCopyRetentionRules(principal888_1_C, principal3_2_C, principal4_999_C, principal888_888_D,
				principal6_5_C);
		rule1.setPrincipalDefaultDocumentCopyRetentionRule(principal888_5_C);
		rule1.setSecondaryDefaultDocumentCopyRetentionRule(secondary888_6_C);
		transaction.add(rm.getCategory(zeCategory).setRetentionRules(asList(rule1)));
		transaction.add(rule1);

		Folder folder1 = transaction.add(newPrincipalFolderWithRule(rule1)).setOpenDate(new LocalDate(2015, 1, 1));
		Folder folder2 = transaction.add(newPrincipalFolderWithRule(rule1)).setOpenDate(new LocalDate(2015, 2, 1));

		Document folder1ActiveDocument_888_1_C = transaction.add(newDocumentInFolderWithType(folder1, type1));
		Document folder1ActiveDocument_4_999_C = transaction.add(newDocumentInFolderWithType(folder1, type3));
		Document folder1ActiveDocument_888_888_D = transaction.add(newDocumentInFolderWithType(folder1, type4));
		Document folder2ActiveDocument_4_999_C = transaction.add(newDocumentInFolderWithType(folder2, type3));

		Document semiActiveDocument_888_1_C = transaction.add(newDocumentInFolderWithType(folder1, type1)
				.setActualTransferDateEntered(new LocalDate(3015, 1, 1)));
		Document semiActiveDocument_4_999_C = transaction.add(newDocumentInFolderWithType(folder1, type3)
				.setActualTransferDateEntered(new LocalDate(3015, 1, 1)));
		Document semiActiveDocumentNearYearEnd_888_888_D = transaction.add(newDocumentInFolderWithType(folder1, type4)
				.setActualTransferDateEntered(new LocalDate(3015, 2, 1)));

		recordServices.execute(transaction);
		waitForBatchProcess();
		//-----

		assertThatDocument(folder1ActiveDocument_888_1_C).isActiveDocument()
				.withExpectedTransfer(date(2035, 3, 31)).withExpectedDestroy(null).withExpectedDeposit(date(2036, 3, 31));

		assertThatDocument(folder1ActiveDocument_4_999_C).isActiveDocument()
				.withExpectedTransfer(date(2019, 3, 31)).withExpectedDestroy(null).withExpectedDeposit(date(2049, 3, 31));

		assertThatDocument(folder1ActiveDocument_888_888_D).isActiveDocument()
				.withExpectedTransfer(date(2035, 3, 31)).withExpectedDestroy(date(2065, 3, 31)).withExpectedDeposit(null);

		assertThatDocument(folder2ActiveDocument_4_999_C).isActiveDocument()
				.withExpectedTransfer(date(2020, 3, 31)).withExpectedDestroy(null).withExpectedDeposit(date(2050, 3, 31));

		assertThatDocument(semiActiveDocument_888_1_C).isSemiActiveDocument(date(3015, 1, 1))
				.withExpectedDestroy(null).withExpectedDeposit(date(3016, 3, 31));

		assertThatDocument(semiActiveDocument_4_999_C).isSemiActiveDocument(date(3015, 1, 1))
				.withExpectedDestroy(null).withExpectedDeposit(date(3045, 3, 31));

		assertThatDocument(semiActiveDocumentNearYearEnd_888_888_D).isSemiActiveDocument(date(3015, 2, 1))
				.withExpectedDestroy(date(3046, 3, 31)).withExpectedDeposit(null);

		//-----

		givenConfig(RMConfigs.CALCULATED_SEMIACTIVE_DATE_NUMBER_OF_YEAR_WHEN_VARIABLE_PERIOD, 20);
		givenConfig(RMConfigs.CALCULATED_INACTIVE_DATE_NUMBER_OF_YEAR_WHEN_VARIABLE_PERIOD, -1);
		waitForBatchProcess();
		recordServices.refresh(transaction.getRecords());

		assertThatDocument(folder1ActiveDocument_888_1_C).isActiveDocument()
				.withExpectedTransfer(date(2035, 3, 31)).withExpectedDestroy(null).withExpectedDeposit(date(2036, 3, 31));

		assertThatDocument(folder1ActiveDocument_4_999_C).isActiveDocument()
				.withExpectedTransfer(date(2019, 3, 31)).withExpectedDestroy(null).withExpectedDeposit(null);

		assertThatDocument(folder1ActiveDocument_888_888_D).isActiveDocument()
				.withExpectedTransfer(date(2035, 3, 31)).withExpectedDestroy(null).withExpectedDeposit(null);

		assertThatDocument(folder2ActiveDocument_4_999_C).isActiveDocument()
				.withExpectedTransfer(date(2020, 3, 31)).withExpectedDestroy(null).withExpectedDeposit(null);

		assertThatDocument(semiActiveDocument_888_1_C).isSemiActiveDocument(date(3015, 1, 1))
				.withExpectedDestroy(null).withExpectedDeposit(date(3016, 3, 31));

		assertThatDocument(semiActiveDocument_4_999_C).isSemiActiveDocument(date(3015, 1, 1))
				.withExpectedDestroy(null).withExpectedDeposit(null);

		assertThatDocument(semiActiveDocumentNearYearEnd_888_888_D).isSemiActiveDocument(date(3015, 2, 1))
				.withExpectedDestroy(null).withExpectedDeposit(null);

		//-----

		givenConfig(RMConfigs.CALCULATED_SEMIACTIVE_DATE_NUMBER_OF_YEAR_WHEN_VARIABLE_PERIOD, -1);
		givenConfig(RMConfigs.CALCULATED_INACTIVE_DATE_NUMBER_OF_YEAR_WHEN_VARIABLE_PERIOD, 100);
		waitForBatchProcess();
		recordServices.refresh(transaction.getRecords());

		assertThatDocument(folder1ActiveDocument_888_1_C).isActiveDocument()
				.withExpectedTransfer(null).withExpectedDestroy(null).withExpectedDeposit(null);

		assertThatDocument(folder1ActiveDocument_4_999_C).isActiveDocument()
				.withExpectedTransfer(date(2019, 3, 31)).withExpectedDestroy(null).withExpectedDeposit(date(2119, 3, 31));

		assertThatDocument(folder1ActiveDocument_888_888_D).isActiveDocument()
				.withExpectedTransfer(null).withExpectedDestroy(null).withExpectedDeposit(null);

		assertThatDocument(folder2ActiveDocument_4_999_C).isActiveDocument()
				.withExpectedTransfer(date(2020, 3, 31)).withExpectedDestroy(null).withExpectedDeposit(date(2120, 3, 31));

		assertThatDocument(semiActiveDocument_888_1_C).isSemiActiveDocument(date(3015, 1, 1))
				.withExpectedDestroy(null).withExpectedDeposit(date(3016, 3, 31));

		assertThatDocument(semiActiveDocument_4_999_C).isSemiActiveDocument(date(3015, 1, 1))
				.withExpectedDestroy(null).withExpectedDeposit(date(3115, 3, 31));

		assertThatDocument(semiActiveDocumentNearYearEnd_888_888_D).isSemiActiveDocument(date(3015, 2, 1))
				.withExpectedDestroy(date(3116, 3, 31)).withExpectedDeposit(null);
	}

	@Test
	public void givedRulesWithCopyRulesUsingDateFieldsAndIgnoringActiveThenValidCalculatedDates()
			throws Exception {

		// Mêmes dates que FolderAcceptanceTest.givenPrincipalFolderWithTwoMediumTypesAndYearEndInSufficientPeriodThenHasValidCalculedDates

		givenConfig(RMConfigs.CALCULATED_CLOSING_DATE, true);
		givenConfig(RMConfigs.DECOMMISSIONING_DATE_BASED_ON, CLOSE_DATE);
		givenConfig(RMConfigs.YEAR_END_DATE, "03/31");
		givenConfig(RMConfigs.REQUIRED_DAYS_BEFORE_YEAR_END_FOR_NOT_ADDING_A_YEAR, 60);
		givenConfig(RMConfigs.DOCUMENT_RETENTION_RULES, true);
		givenConfig(RMConfigs.CALCULATED_SEMIACTIVE_DATE_NUMBER_OF_YEAR_WHEN_VARIABLE_PERIOD, 20);
		givenConfig(RMConfigs.CALCULATED_INACTIVE_DATE_NUMBER_OF_YEAR_WHEN_VARIABLE_PERIOD, 30);
		waitForBatchProcess();
		createDateMetadatasAndCustomSchemas();

		CopyRetentionRule copy1 = copyBuilder.newPrincipal(asList(records.PA), "2-3-C").setTypeId(type1)
				.setActiveDateMetadata(documentDateA().getLocalCode())
				.setSemiActiveDateMetadata(customDocument1DateD().getLocalCode()).setIgnoreActivePeriod(true);
		CopyRetentionRule copy2 = copyBuilder.newPrincipal(asList(records.PA), "2-3-D").setTypeId(type2)
				.setActiveDateMetadata(documentDateA().getLocalCode())
				.setSemiActiveDateMetadata(documentDateA().getLocalCode()).setIgnoreActivePeriod(true);
		CopyRetentionRule copy3 = copyBuilder.newPrincipal(asList(records.PA), "2-3-C").setTypeId(type3)
				.setActiveDateMetadata(customDocument2DateE().getLocalCode()).setIgnoreActivePeriod(true);
		CopyRetentionRule copy4 = copyBuilder.newPrincipal(asList(records.PA), "2-3-D").setTypeId(type4)
				.setSemiActiveDateMetadata(documentDateB().getLocalCode()).setIgnoreActivePeriod(true);
		CopyRetentionRule copy5 = copyBuilder.newPrincipal(asList(records.PA), "2-3-D").setTypeId(type5)
				.setActiveDateMetadata(documentDateTimeC().getLocalCode())
				.setSemiActiveDateMetadata(documentDateTimeF().getLocalCode()).setIgnoreActivePeriod(true);

		CopyRetentionRule principal888_5_C = copyBuilder.newPrincipal(asList(records.PA), "888-5-C");
		CopyRetentionRule secondary888_6_C = copyBuilder.newSecondary(asList(records.PA), "888-6-C");

		Transaction transaction = new Transaction();
		RetentionRule rule1 = transaction.add(rm.newRetentionRuleWithId("rule1").setCode("rule1").setTitle("rule1"));
		rule1.setScope(DOCUMENTS);
		rule1.setResponsibleAdministrativeUnits(true);
		rule1.setDocumentCopyRetentionRules(copy1, copy2, copy3, copy4, copy5);
		rule1.setPrincipalDefaultDocumentCopyRetentionRule(principal888_5_C);
		rule1.setSecondaryDefaultDocumentCopyRetentionRule(secondary888_6_C);
		transaction.add(rm.getCategory(zeCategory).setRetentionRules(asList(rule1)));
		transaction.add(rule1);

		transaction.add(rm.getDocumentType(type1).setLinkedSchema(customDocument1Schema().getCode()));
		transaction.add(rm.getDocumentType(type2).setLinkedSchema(customDocument1Schema().getCode()));
		transaction.add(rm.getDocumentType(type3).setLinkedSchema(customDocument2Schema().getCode()));
		transaction.add(rm.getDocumentType(type4).setLinkedSchema(customDocument2Schema().getCode()));

		Folder folder1 = transaction.add(newPrincipalFolderWithRule(rule1)).setOpenDate(new LocalDate(2015, 1, 1));

		Document type1DocWithBothDate = transaction.add(newCustomDocument1(folder1, type1))
				.set(documentDateA(), date(2020, 1, 1)).set(customDocument1DateD(), date(2030, 1, 1));

		Document type1DocWithoutActiveDate = transaction.add(newCustomDocument1(folder1, type1))
				.set(customDocument1DateD(), date(2020, 2, 1));
		Document type1DocWithoutSemiActiveDate = transaction.add(newCustomDocument1(folder1, type1))
				.set(documentDateA(), date(2022, 2, 1));
		Document type1DocWithoutDates = transaction.add(newCustomDocument1(folder1, type1));
		Document type1CalculatedSemiActiveEqualToAjustedInactive = transaction.add(newCustomDocument1(folder1, type1))
				.set(documentDateA(), date(2026, 2, 1)).set(customDocument1DateD(), date(2029, 1, 1));

		Document type1CalculatedSemiActiveAfterAjustedInactive = transaction.add(newCustomDocument1(folder1, type1))
				.set(documentDateA(), date(2026, 2, 1)).set(customDocument1DateD(), date(2029, 2, 1));

		// ------

		Document type2DocWithDate = transaction.add(newCustomDocument1(folder1, type2))
				.set(documentDateA(), date(2020, 1, 1));

		Document type2DocWithoutDate = transaction.add(newCustomDocument1(folder1, type2));

		// ------

		Document type3DocWithActiveDate = transaction.add(newCustomDocument2(folder1, type3))
				.set(customDocument2DateE(), date(2020, 1, 1)).set(documentDateA(), date(2030, 1, 1));

		Document type3DocWithoutActiveDate = transaction.add(newCustomDocument2(folder1, type3))
				.set(documentDateA(), date(2020, 2, 1));

		// ------

		Document type4DocWithSemiActiveDate = transaction.add(newCustomDocument2(folder1, type4))
				.set(documentDateB(), date(2020, 1, 1)).set(documentDateA(), date(2030, 1, 1));

		Document type4DocWithoutSemiActiveDate = transaction.add(newCustomDocument2(folder1, type4))
				.set(documentDateA(), date(2020, 2, 1));

		Document type4CalculatedSemiActiveEqualToAjustedInactive = transaction.add(newCustomDocument2(folder1, type4))
				.set(documentDateB(), date(2017, 1, 1)).set(documentDateA(), date(2029, 1, 1));

		Document type4CalculatedSemiActiveAfterAjustedInactive = transaction.add(newCustomDocument2(folder1, type4))
				.set(documentDateB(), date(2016, 1, 1)).set(documentDateA(), date(2029, 2, 1));

		Document type5WithBothDateTimeValues = transaction.add(newCustomDocument2(folder1, type5))
				.set(documentDateTimeC(), new LocalDateTime(2016, 1, 1, 1, 2, 3))
				.set(documentDateTimeF(), new LocalDateTime(2029, 2, 1, 4, 5, 6));

		recordServices.execute(transaction);

		// ------

		assertThat(type1DocWithBothDate.getMainCopyRule()).isEqualTo(copy1);

		assertThatDocument(type1DocWithBothDate).isActiveDocument()
				.withExpectedTransfer(date(2022, 3, 31)).withExpectedDestroy(null).withExpectedDeposit(date(2033, 3, 31));

		assertThatDocument(type1DocWithoutActiveDate).isActiveDocument()
				.withExpectedTransfer(date(2017, 3, 31)).withExpectedDestroy(null).withExpectedDeposit(date(2024, 3, 31));

		assertThatDocument(type1DocWithoutSemiActiveDate).isActiveDocument()
				.withExpectedTransfer(date(2025, 3, 31)).withExpectedDestroy(null).withExpectedDeposit(date(2028, 3, 31));

		assertThatDocument(type1DocWithoutDates).isActiveDocument()
				.withExpectedTransfer(date(2017, 3, 31)).withExpectedDestroy(null).withExpectedDeposit(date(2020, 3, 31));

		assertThatDocument(type1CalculatedSemiActiveEqualToAjustedInactive).isActiveDocument()
				.withExpectedTransfer(date(2029, 3, 31)).withExpectedDestroy(null).withExpectedDeposit(date(2032, 3, 31));

		assertThatDocument(type1CalculatedSemiActiveAfterAjustedInactive).isActiveDocument()
				.withExpectedTransfer(date(2029, 3, 31)).withExpectedDestroy(null).withExpectedDeposit(date(2033, 3, 31));

		// -------

		assertThatDocument(type2DocWithDate).isActiveDocument()
				.withExpectedTransfer(date(2022, 3, 31)).withExpectedDeposit(null).withExpectedDestroy(date(2025, 3, 31));

		assertThatDocument(type2DocWithoutDate).isActiveDocument()
				.withExpectedTransfer(date(2017, 3, 31)).withExpectedDeposit(null).withExpectedDestroy(date(2020, 3, 31));

		// -------

		assertThatDocument(type3DocWithActiveDate).isActiveDocument()
				.withExpectedTransfer(date(2022, 3, 31)).withExpectedDestroy(null).withExpectedDeposit(date(2025, 3, 31));

		assertThatDocument(type3DocWithoutActiveDate).isActiveDocument()
				.withExpectedTransfer(date(2017, 3, 31)).withExpectedDestroy(null).withExpectedDeposit(date(2020, 3, 31));

		// -------

		assertThatDocument(type4DocWithSemiActiveDate).isActiveDocument()
				.withExpectedTransfer(date(2017, 3, 31)).withExpectedDeposit(null).withExpectedDestroy(date(2023, 3, 31));

		assertThatDocument(type4DocWithoutSemiActiveDate).isActiveDocument()
				.withExpectedTransfer(date(2017, 3, 31)).withExpectedDeposit(null).withExpectedDestroy(date(2020, 3, 31));

		assertThatDocument(type4CalculatedSemiActiveEqualToAjustedInactive).isActiveDocument()
				.withExpectedTransfer(date(2017, 3, 31)).withExpectedDeposit(null).withExpectedDestroy(date(2020, 3, 31));

		assertThatDocument(type4CalculatedSemiActiveAfterAjustedInactive).isActiveDocument()
				.withExpectedTransfer(date(2017, 3, 31)).withExpectedDeposit(null).withExpectedDestroy(date(2020, 3, 31));

		// -------

		assertThatDocument(type5WithBothDateTimeValues).isActiveDocument()
				.withExpectedTransfer(date(2018, 3, 31)).withExpectedDeposit(null).withExpectedDestroy(date(2033, 3, 31));

	}

	@Test
	public void givedRulesWithCopyRulesUsingDateFieldsThenValidCalculatedDates()
			throws Exception {

		// Mêmes dates que FolderAcceptanceTest.givenPrincipalFolderWithTwoMediumTypesAndYearEndInSufficientPeriodThenHasValidCalculedDates

		givenConfig(RMConfigs.CALCULATED_CLOSING_DATE, true);
		givenConfig(RMConfigs.DECOMMISSIONING_DATE_BASED_ON, CLOSE_DATE);
		givenConfig(RMConfigs.YEAR_END_DATE, "03/31");
		givenConfig(RMConfigs.REQUIRED_DAYS_BEFORE_YEAR_END_FOR_NOT_ADDING_A_YEAR, 60);
		givenConfig(RMConfigs.DOCUMENT_RETENTION_RULES, true);
		givenConfig(RMConfigs.CALCULATED_SEMIACTIVE_DATE_NUMBER_OF_YEAR_WHEN_VARIABLE_PERIOD, 20);
		givenConfig(RMConfigs.CALCULATED_INACTIVE_DATE_NUMBER_OF_YEAR_WHEN_VARIABLE_PERIOD, 30);
		waitForBatchProcess();
		createDateMetadatasAndCustomSchemas();

		CopyRetentionRule copy1 = copyBuilder.newPrincipal(asList(records.PA), "2-3-C").setTypeId(type1)
				.setActiveDateMetadata(documentDateA().getLocalCode())
				.setSemiActiveDateMetadata(customDocument1DateD().getLocalCode()).setIgnoreActivePeriod(false);
		CopyRetentionRule copy2 = copyBuilder.newPrincipal(asList(records.PA), "2-3-D").setTypeId(type2)
				.setActiveDateMetadata(documentDateA().getLocalCode())
				.setSemiActiveDateMetadata(documentDateA().getLocalCode()).setIgnoreActivePeriod(false);
		CopyRetentionRule copy3 = copyBuilder.newPrincipal(asList(records.PA), "2-3-C").setTypeId(type3)
				.setActiveDateMetadata(customDocument2DateE().getLocalCode()).setIgnoreActivePeriod(false);
		CopyRetentionRule copy4 = copyBuilder.newPrincipal(asList(records.PA), "2-3-D").setTypeId(type4)
				.setSemiActiveDateMetadata(documentDateB().getLocalCode()).setIgnoreActivePeriod(false);
		CopyRetentionRule copy5 = copyBuilder.newPrincipal(asList(records.PA), "2-3-D").setTypeId(type5)
				.setActiveDateMetadata(documentDateTimeC().getLocalCode())
				.setSemiActiveDateMetadata(documentDateTimeF().getLocalCode()).setIgnoreActivePeriod(false);

		CopyRetentionRule principal888_5_C = copyBuilder.newPrincipal(asList(records.PA), "888-5-C");
		CopyRetentionRule secondary888_6_C = copyBuilder.newSecondary(asList(records.PA), "888-6-C");

		Transaction transaction = new Transaction();
		RetentionRule rule1 = transaction.add(rm.newRetentionRuleWithId("rule1").setCode("rule1").setTitle("rule1"));
		rule1.setScope(DOCUMENTS);
		rule1.setResponsibleAdministrativeUnits(true);
		rule1.setDocumentCopyRetentionRules(copy1, copy2, copy3, copy4, copy5);
		rule1.setPrincipalDefaultDocumentCopyRetentionRule(principal888_5_C);
		rule1.setSecondaryDefaultDocumentCopyRetentionRule(secondary888_6_C);
		transaction.add(rm.getCategory(zeCategory).setRetentionRules(asList(rule1)));
		transaction.add(rule1);

		transaction.add(rm.getDocumentType(type1).setLinkedSchema(customDocument1Schema().getCode()));
		transaction.add(rm.getDocumentType(type2).setLinkedSchema(customDocument1Schema().getCode()));
		transaction.add(rm.getDocumentType(type3).setLinkedSchema(customDocument2Schema().getCode()));
		transaction.add(rm.getDocumentType(type4).setLinkedSchema(customDocument2Schema().getCode()));

		Folder folder1 = transaction.add(newPrincipalFolderWithRule(rule1)).setOpenDate(new LocalDate(2015, 1, 1));

		Document type1DocWithBothDate = transaction.add(newCustomDocument1(folder1, type1))
				.set(documentDateA(), date(2020, 1, 1)).set(customDocument1DateD(), date(2030, 1, 1));

		Document type1DocWithoutActiveDate = transaction.add(newCustomDocument1(folder1, type1))
				.set(customDocument1DateD(), date(2020, 2, 1));
		Document type1DocWithoutSemiActiveDate = transaction.add(newCustomDocument1(folder1, type1))
				.set(documentDateA(), date(2022, 2, 1));
		Document type1DocWithoutDates = transaction.add(newCustomDocument1(folder1, type1));
		Document type1CalculatedSemiActiveEqualToAjustedInactive = transaction.add(newCustomDocument1(folder1, type1))
				.set(documentDateA(), date(2026, 2, 1)).set(customDocument1DateD(), date(2027, 1, 1));

		Document type1CalculatedSemiActiveAfterAjustedInactive = transaction.add(newCustomDocument1(folder1, type1))
				.set(documentDateA(), date(2026, 2, 1)).set(customDocument1DateD(), date(2027, 2, 1));

		// ------

		Document type2DocWithDate = transaction.add(newCustomDocument1(folder1, type2))
				.set(documentDateA(), date(2020, 1, 1));

		Document type2DocWithoutDate = transaction.add(newCustomDocument1(folder1, type2));

		// ------

		Document type3DocWithActiveDate = transaction.add(newCustomDocument2(folder1, type3))
				.set(customDocument2DateE(), date(2020, 1, 1)).set(documentDateA(), date(2030, 1, 1));

		Document type3DocWithoutActiveDate = transaction.add(newCustomDocument2(folder1, type3))
				.set(documentDateA(), date(2020, 2, 1));

		// ------

		Document type4DocWithSemiActiveDate = transaction.add(newCustomDocument2(folder1, type4))
				.set(documentDateB(), date(2020, 1, 1)).set(documentDateA(), date(2030, 1, 1));

		Document type4DocWithoutSemiActiveDate = transaction.add(newCustomDocument2(folder1, type4))
				.set(documentDateA(), date(2020, 2, 1));

		Document type4CalculatedSemiActiveEqualToAjustedInactive = transaction.add(newCustomDocument2(folder1, type4))
				.set(documentDateB(), date(2015, 1, 1)).set(documentDateA(), date(2029, 1, 1));

		Document type4CalculatedSemiActiveAfterAjustedInactive = transaction.add(newCustomDocument2(folder1, type4))
				.set(documentDateB(), date(2014, 1, 1)).set(documentDateA(), date(2029, 2, 1));

		Document type5WithBothDateTimeValues = transaction.add(newCustomDocument2(folder1, type5))
				.set(documentDateTimeC(), new LocalDateTime(2016, 1, 1, 1, 2, 3))
				.set(documentDateTimeF(), new LocalDateTime(2029, 2, 1, 4, 5, 6));

		recordServices.execute(transaction);

		// ------

		assertThat(type1DocWithBothDate.getMainCopyRule()).isEqualTo(copy1);

		assertThatDocument(type1DocWithBothDate).isActiveDocument()
				.withExpectedTransfer(date(2022, 3, 31)).withExpectedDestroy(null).withExpectedDeposit(date(2035, 3, 31));

		assertThatDocument(type1DocWithoutActiveDate).isActiveDocument()
				.withExpectedTransfer(date(2017, 3, 31)).withExpectedDestroy(null).withExpectedDeposit(date(2026, 3, 31));

		assertThatDocument(type1DocWithoutSemiActiveDate).isActiveDocument()
				.withExpectedTransfer(date(2025, 3, 31)).withExpectedDestroy(null).withExpectedDeposit(date(2028, 3, 31));

		assertThatDocument(type1DocWithoutDates).isActiveDocument()
				.withExpectedTransfer(date(2017, 3, 31)).withExpectedDestroy(null).withExpectedDeposit(date(2020, 3, 31));

		assertThatDocument(type1CalculatedSemiActiveEqualToAjustedInactive).isActiveDocument()
				.withExpectedTransfer(date(2029, 3, 31)).withExpectedDestroy(null).withExpectedDeposit(date(2032, 3, 31));

		assertThatDocument(type1CalculatedSemiActiveAfterAjustedInactive).isActiveDocument()
				.withExpectedTransfer(date(2029, 3, 31)).withExpectedDestroy(null).withExpectedDeposit(date(2033, 3, 31));

		// -------

		assertThatDocument(type2DocWithDate).isActiveDocument()
				.withExpectedTransfer(date(2022, 3, 31)).withExpectedDeposit(null).withExpectedDestroy(date(2025, 3, 31));

		assertThatDocument(type2DocWithoutDate).isActiveDocument()
				.withExpectedTransfer(date(2017, 3, 31)).withExpectedDeposit(null).withExpectedDestroy(date(2020, 3, 31));

		// -------

		assertThatDocument(type3DocWithActiveDate).isActiveDocument()
				.withExpectedTransfer(date(2022, 3, 31)).withExpectedDestroy(null).withExpectedDeposit(date(2025, 3, 31));

		assertThatDocument(type3DocWithoutActiveDate).isActiveDocument()
				.withExpectedTransfer(date(2017, 3, 31)).withExpectedDestroy(null).withExpectedDeposit(date(2020, 3, 31));

		// -------

		assertThatDocument(type4DocWithSemiActiveDate).isActiveDocument()
				.withExpectedTransfer(date(2017, 3, 31)).withExpectedDeposit(null).withExpectedDestroy(date(2025, 3, 31));

		assertThatDocument(type4DocWithoutSemiActiveDate).isActiveDocument()
				.withExpectedTransfer(date(2017, 3, 31)).withExpectedDeposit(null).withExpectedDestroy(date(2020, 3, 31));

		assertThatDocument(type4CalculatedSemiActiveEqualToAjustedInactive).isActiveDocument()
				.withExpectedTransfer(date(2017, 3, 31)).withExpectedDeposit(null).withExpectedDestroy(date(2020, 3, 31));

		assertThatDocument(type4CalculatedSemiActiveAfterAjustedInactive).isActiveDocument()
				.withExpectedTransfer(date(2017, 3, 31)).withExpectedDeposit(null).withExpectedDestroy(date(2020, 3, 31));

		// -------

		assertThatDocument(type5WithBothDateTimeValues).isActiveDocument()
				.withExpectedTransfer(date(2018, 3, 31)).withExpectedDeposit(null).withExpectedDestroy(date(2035, 3, 31));
	}

	@Test
	public void givedRulesWithCopyRulesUsingDateValidationFoNumbersFieldsThenValidCalculatedDates()
			throws Exception {

		// Mêmes dates que FolderAcceptanceTest.givenPrincipalFolderWithTwoMediumTypesAndYearEndInSufficientPeriodThenHasValidCalculedDates

		givenConfig(RMConfigs.CALCULATED_CLOSING_DATE, true);
		givenConfig(RMConfigs.DECOMMISSIONING_DATE_BASED_ON, CLOSE_DATE);
		givenConfig(RMConfigs.YEAR_END_DATE, "03/31");
		givenConfig(RMConfigs.REQUIRED_DAYS_BEFORE_YEAR_END_FOR_NOT_ADDING_A_YEAR, 60);
		givenConfig(RMConfigs.DOCUMENT_RETENTION_RULES, true);
		givenConfig(RMConfigs.CALCULATED_SEMIACTIVE_DATE_NUMBER_OF_YEAR_WHEN_VARIABLE_PERIOD, 20);
		givenConfig(RMConfigs.CALCULATED_INACTIVE_DATE_NUMBER_OF_YEAR_WHEN_VARIABLE_PERIOD, 30);
		waitForBatchProcess();
		createDateMetadatasAndCustomSchemas();

		CopyRetentionRule copy1 = copyBuilder.newPrincipal(asList(records.PA), "2-3-C").setTypeId(type1)
				.setActiveDateMetadata(documentDateA().getLocalCode())
				.setSemiActiveDateMetadata(customDocument1DateD().getLocalCode()).setIgnoreActivePeriod(false);
		CopyRetentionRule copy2 = copyBuilder.newPrincipal(asList(records.PA), "2-3-D").setTypeId(type2)
				.setActiveDateMetadata(documentDateA().getLocalCode())
				.setSemiActiveDateMetadata(documentDateA().getLocalCode()).setIgnoreActivePeriod(false);
		CopyRetentionRule copy3 = copyBuilder.newPrincipal(asList(records.PA), "2-3-C").setTypeId(type3)
				.setActiveDateMetadata(customDocument2DateE().getLocalCode()).setIgnoreActivePeriod(false);
		CopyRetentionRule copy4 = copyBuilder.newPrincipal(asList(records.PA), "2-3-D").setTypeId(type4)
				.setSemiActiveDateMetadata(documentDateB().getLocalCode()).setIgnoreActivePeriod(false);
		CopyRetentionRule copy5 = copyBuilder.newPrincipal(asList(records.PA), "2-3-D").setTypeId(type5)
				.setActiveDateMetadata(documentDateTimeC().getLocalCode())
				.setSemiActiveDateMetadata(documentDateTimeF().getLocalCode()).setIgnoreActivePeriod(false);

		CopyRetentionRule principal888_5_C = copyBuilder.newPrincipal(asList(records.PA), "888-5-C");
		CopyRetentionRule secondary888_6_C = copyBuilder.newSecondary(asList(records.PA), "888-6-C");

		Transaction transaction = new Transaction();
		RetentionRule rule1 = transaction.add(rm.newRetentionRuleWithId("rule1").setCode("rule1").setTitle("rule1"));
		rule1.setScope(DOCUMENTS);
		rule1.setResponsibleAdministrativeUnits(true);
		rule1.setDocumentCopyRetentionRules(copy1, copy2, copy3, copy4, copy5);
		rule1.setPrincipalDefaultDocumentCopyRetentionRule(principal888_5_C);
		rule1.setSecondaryDefaultDocumentCopyRetentionRule(secondary888_6_C);
		transaction.add(rm.getCategory(zeCategory).setRetentionRules(asList(rule1)));
		transaction.add(rule1);

		transaction.add(rm.getDocumentType(type1).setLinkedSchema(customDocument1Schema().getCode()));
		transaction.add(rm.getDocumentType(type2).setLinkedSchema(customDocument1Schema().getCode()));
		transaction.add(rm.getDocumentType(type3).setLinkedSchema(customDocument2Schema().getCode()));
		transaction.add(rm.getDocumentType(type4).setLinkedSchema(customDocument2Schema().getCode()));

		Folder folder1 = transaction.add(newPrincipalFolderWithRule(rule1)).setOpenDate(new LocalDate(2015, 1, 1));

		Document type1DocWithBothDate = transaction.add(newCustomDocument1(folder1, type1))
				.set(documentDateA(), date(2020, 3, 31)).set(customDocument1DateD(), date(2030, 3, 31));

		Document type1DocWithoutActiveDate = transaction.add(newCustomDocument1(folder1, type1))
				.set(customDocument1DateD(), date(2020, 3, 31));
		Document type1DocWithoutSemiActiveDate = transaction.add(newCustomDocument1(folder1, type1))
				.set(documentDateA(), date(2022, 3, 31));
		Document type1DocWithoutDates = transaction.add(newCustomDocument1(folder1, type1));
		Document type1CalculatedSemiActiveEqualToAjustedInactive = transaction.add(newCustomDocument1(folder1, type1))
				.set(documentDateA(), date(2026, 3, 31)).set(customDocument1DateD(), date(2027, 3, 31));

		Document type1CalculatedSemiActiveAfterAjustedInactive = transaction.add(newCustomDocument1(folder1, type1))
				.set(documentDateA(), date(2026, 3, 31)).set(customDocument1DateD(), date(2027, 3, 31));

		// ------

		Document type2DocWithDate = transaction.add(newCustomDocument1(folder1, type2))
				.set(documentDateA(), date(2020, 3, 31));

		Document type2DocWithoutDate = transaction.add(newCustomDocument1(folder1, type2));

		// ------

		Document type3DocWithActiveDate = transaction.add(newCustomDocument2(folder1, type3))
				.set(customDocument2DateE(), date(2020, 3, 31)).set(documentDateA(), date(2030, 3, 31));

		Document type3DocWithoutActiveDate = transaction.add(newCustomDocument2(folder1, type3))
				.set(documentDateA(), date(2020, 3, 31));

		// ------

		Document type4DocWithSemiActiveDate = transaction.add(newCustomDocument2(folder1, type4))
				.set(documentDateB(), date(2020, 3, 31)).set(documentDateA(), date(2030, 3, 31));

		Document type4DocWithoutSemiActiveDate = transaction.add(newCustomDocument2(folder1, type4))
				.set(documentDateA(), date(2020, 3, 31));

		Document type4CalculatedSemiActiveEqualToAjustedInactive = transaction.add(newCustomDocument2(folder1, type4))
				.set(documentDateB(), date(2015, 3, 31)).set(documentDateA(), date(2029, 3, 31));

		Document type4CalculatedSemiActiveAfterAjustedInactive = transaction.add(newCustomDocument2(folder1, type4))
				.set(documentDateB(), date(2014, 3, 31)).set(documentDateA(), date(2029, 3, 31));

		Document type5WithBothDateTimeValues = transaction.add(newCustomDocument2(folder1, type5))
				.set(documentDateTimeC(), new LocalDateTime(2016, 3, 31, 1, 2, 3))
				.set(documentDateTimeF(), new LocalDateTime(2029, 3, 31, 4, 5, 6));

		recordServices.execute(transaction);

		// ------

		assertThat(type1DocWithBothDate.getMainCopyRule()).isEqualTo(copy1);

		assertThatDocument(type1DocWithBothDate).isActiveDocument()
				.withExpectedTransfer(date(2022, 3, 31)).withExpectedDestroy(null).withExpectedDeposit(date(2035, 3, 31));

		//will not be adjusted since we use CalculatorUtils.toNextEndOfYearDate in document calculators
		assertThatDocument(type1DocWithoutActiveDate).isActiveDocument()
				.withExpectedTransfer(date(2017, 3, 31)).withExpectedDestroy(null).withExpectedDeposit(date(2025, 3, 31));

		assertThatDocument(type1DocWithoutSemiActiveDate).isActiveDocument()
				.withExpectedTransfer(date(2024, 3, 31)).withExpectedDestroy(null).withExpectedDeposit(date(2027, 3, 31));

		assertThatDocument(type1DocWithoutDates).isActiveDocument()
				.withExpectedTransfer(date(2017, 3, 31)).withExpectedDestroy(null).withExpectedDeposit(date(2020, 3, 31));

		//will not be adjusted since we use CalculatorUtils.toNextEndOfYearDate in document calculators
		assertThatDocument(type1CalculatedSemiActiveEqualToAjustedInactive).isActiveDocument()
				.withExpectedTransfer(date(2028, 3, 31)).withExpectedDestroy(null).withExpectedDeposit(date(2032, 3, 31));

		assertThatDocument(type1CalculatedSemiActiveAfterAjustedInactive).isActiveDocument()
				.withExpectedTransfer(date(2028, 3, 31)).withExpectedDestroy(null).withExpectedDeposit(date(2032, 3, 31));

		// -------

		assertThatDocument(type2DocWithDate).isActiveDocument()
				.withExpectedTransfer(date(2022, 3, 31)).withExpectedDeposit(null).withExpectedDestroy(date(2025, 3, 31));

		assertThatDocument(type2DocWithoutDate).isActiveDocument()
				.withExpectedTransfer(date(2017, 3, 31)).withExpectedDeposit(null).withExpectedDestroy(date(2020, 3, 31));

		// -------

		assertThatDocument(type3DocWithActiveDate).isActiveDocument()
				.withExpectedTransfer(date(2022, 3, 31)).withExpectedDestroy(null).withExpectedDeposit(date(2025, 3, 31));

		assertThatDocument(type3DocWithoutActiveDate).isActiveDocument()
				.withExpectedTransfer(date(2017, 3, 31)).withExpectedDestroy(null).withExpectedDeposit(date(2020, 3, 31));

		// -------

		assertThatDocument(type4DocWithSemiActiveDate).isActiveDocument()
				.withExpectedTransfer(date(2017, 3, 31)).withExpectedDeposit(null).withExpectedDestroy(date(2025, 3, 31));

		assertThatDocument(type4DocWithoutSemiActiveDate).isActiveDocument()
				.withExpectedTransfer(date(2017, 3, 31)).withExpectedDeposit(null).withExpectedDestroy(date(2020, 3, 31));

		assertThatDocument(type4CalculatedSemiActiveEqualToAjustedInactive).isActiveDocument()
				.withExpectedTransfer(date(2017, 3, 31)).withExpectedDeposit(null).withExpectedDestroy(date(2020, 3, 31));

		assertThatDocument(type4CalculatedSemiActiveAfterAjustedInactive).isActiveDocument()
				.withExpectedTransfer(date(2017, 3, 31)).withExpectedDeposit(null).withExpectedDestroy(date(2020, 3, 31));

		// -------

		assertThatDocument(type5WithBothDateTimeValues).isActiveDocument()
				.withExpectedTransfer(date(2018, 3, 31)).withExpectedDeposit(null).withExpectedDestroy(date(2034, 3, 31));
	}

	@Test
	public void givedRulesWithCopyRulesUsingNumberFieldsBasedOnNumbersThenValidCalculatedDates()
			throws Exception {

		// Mêmes dates que FolderAcceptanceTest.givenPrincipalFolderWithTwoMediumTypesAndYearEndInSufficientPeriodThenHasValidCalculedDates

		givenConfig(RMConfigs.CALCULATED_CLOSING_DATE, true);
		givenConfig(RMConfigs.DECOMMISSIONING_DATE_BASED_ON, CLOSE_DATE);
		givenConfig(RMConfigs.YEAR_END_DATE, "03/31");
		givenConfig(RMConfigs.REQUIRED_DAYS_BEFORE_YEAR_END_FOR_NOT_ADDING_A_YEAR, 60);
		givenConfig(RMConfigs.DOCUMENT_RETENTION_RULES, true);
		givenConfig(RMConfigs.CALCULATED_SEMIACTIVE_DATE_NUMBER_OF_YEAR_WHEN_VARIABLE_PERIOD, 20);
		givenConfig(RMConfigs.CALCULATED_INACTIVE_DATE_NUMBER_OF_YEAR_WHEN_VARIABLE_PERIOD, 30);
		waitForBatchProcess();
		createDateMetadatasAndCustomSchemas();

		CopyRetentionRule copy1 = copyBuilder.newPrincipal(asList(records.PA), "2-3-C").setTypeId(type1)
				.setActiveDateMetadata(documentNumberA().getLocalCode())
				.setSemiActiveDateMetadata(customDocument1NumberD().getLocalCode()).setIgnoreActivePeriod(false);
		CopyRetentionRule copy2 = copyBuilder.newPrincipal(asList(records.PA), "2-3-D").setTypeId(type2)
				.setActiveDateMetadata(documentNumberA().getLocalCode())
				.setSemiActiveDateMetadata(documentNumberA().getLocalCode()).setIgnoreActivePeriod(false);
		CopyRetentionRule copy3 = copyBuilder.newPrincipal(asList(records.PA), "2-3-C").setTypeId(type3)
				.setActiveDateMetadata(customDocument2NumberE().getLocalCode()).setIgnoreActivePeriod(false);
		CopyRetentionRule copy4 = copyBuilder.newPrincipal(asList(records.PA), "2-3-D").setTypeId(type4)
				.setSemiActiveDateMetadata(documentNumberB().getLocalCode()).setIgnoreActivePeriod(false);
		CopyRetentionRule copy5 = copyBuilder.newPrincipal(asList(records.PA), "2-3-D").setTypeId(type5)
				.setActiveDateMetadata(documentNumberC().getLocalCode())
				.setSemiActiveDateMetadata(documentNumberF().getLocalCode()).setIgnoreActivePeriod(false);

		CopyRetentionRule principal888_5_C = copyBuilder.newPrincipal(asList(records.PA), "888-5-C");
		CopyRetentionRule secondary888_6_C = copyBuilder.newSecondary(asList(records.PA), "888-6-C");

		Transaction transaction = new Transaction();
		RetentionRule rule1 = transaction.add(rm.newRetentionRuleWithId("rule1").setCode("rule1").setTitle("rule1"));
		rule1.setScope(DOCUMENTS);
		rule1.setResponsibleAdministrativeUnits(true);
		rule1.setDocumentCopyRetentionRules(copy1, copy2, copy3, copy4, copy5);
		rule1.setPrincipalDefaultDocumentCopyRetentionRule(principal888_5_C);
		rule1.setSecondaryDefaultDocumentCopyRetentionRule(secondary888_6_C);
		transaction.add(rm.getCategory(zeCategory).setRetentionRules(asList(rule1)));
		transaction.add(rule1);

		transaction.add(rm.getDocumentType(type1).setLinkedSchema(customDocument1Schema().getCode()));
		transaction.add(rm.getDocumentType(type2).setLinkedSchema(customDocument1Schema().getCode()));
		transaction.add(rm.getDocumentType(type3).setLinkedSchema(customDocument2Schema().getCode()));
		transaction.add(rm.getDocumentType(type4).setLinkedSchema(customDocument2Schema().getCode()));

		Folder folder1 = transaction.add(newPrincipalFolderWithRule(rule1)).setOpenDate(new LocalDate(2015, 1, 1));

		Document type1DocWithBothDate = transaction.add(newCustomDocument1(folder1, type1))
				.set(documentNumberA(), 2020).set(customDocument1NumberD(), 2030);

		Document type1DocWithoutActiveDate = transaction.add(newCustomDocument1(folder1, type1))
				.set(customDocument1NumberD(), 2020);
		Document type1DocWithoutSemiActiveDate = transaction.add(newCustomDocument1(folder1, type1))
				.set(documentNumberA(), 2022);
		Document type1DocWithoutDates = transaction.add(newCustomDocument1(folder1, type1));
		Document type1CalculatedSemiActiveEqualToAjustedInactive = transaction.add(newCustomDocument1(folder1, type1))
				.set(documentNumberA(), 2026).set(customDocument1NumberD(), 2027);

		Document type1CalculatedSemiActiveAfterAjustedInactive = transaction.add(newCustomDocument1(folder1, type1))
				.set(documentNumberA(), 2026).set(customDocument1NumberD(), 2027);

		// ------

		Document type2DocWithDate = transaction.add(newCustomDocument1(folder1, type2))
				.set(documentNumberA(), 2020);

		Document type2DocWithoutDate = transaction.add(newCustomDocument1(folder1, type2));

		// ------

		Document type3DocWithActiveDate = transaction.add(newCustomDocument2(folder1, type3))
				.set(customDocument2NumberE(), 2020).set(documentNumberA(), 2030);

		Document type3DocWithoutActiveDate = transaction.add(newCustomDocument2(folder1, type3))
				.set(documentNumberA(), 2020);

		// ------

		Document type4DocWithSemiActiveDate = transaction.add(newCustomDocument2(folder1, type4))
				.set(documentNumberB(), 2020).set(documentNumberA(), 2030);

		Document type4DocWithoutSemiActiveDate = transaction.add(newCustomDocument2(folder1, type4))
				.set(documentNumberA(), 2020);

		Document type4CalculatedSemiActiveEqualToAjustedInactive = transaction.add(newCustomDocument2(folder1, type4))
				.set(documentNumberB(), 2015).set(documentNumberA(), 2029);

		Document type4CalculatedSemiActiveAfterAjustedInactive = transaction.add(newCustomDocument2(folder1, type4))
				.set(documentNumberB(), 2014).set(documentNumberA(), 2029);

		Document type5WithBothDateTimeValues = transaction.add(newCustomDocument2(folder1, type5))
				.set(documentNumberC(), 2016)
				.set(documentNumberF(), 2029);

		recordServices.execute(transaction);

		// ------

		assertThat(type1DocWithBothDate.getMainCopyRule()).isEqualTo(copy1);

		assertThatDocument(type1DocWithBothDate).isActiveDocument()
				.withExpectedTransfer(date(2022, 3, 31)).withExpectedDestroy(null).withExpectedDeposit(date(2035, 3, 31));

		//will not be adjusted since we use CalculatorUtils.toNextEndOfYearDate in document calculators
		assertThatDocument(type1DocWithoutActiveDate).isActiveDocument()
				.withExpectedTransfer(date(2017, 3, 31)).withExpectedDestroy(null).withExpectedDeposit(date(2025, 3, 31));

		assertThatDocument(type1DocWithoutSemiActiveDate).isActiveDocument()
				.withExpectedTransfer(date(2024, 3, 31)).withExpectedDestroy(null).withExpectedDeposit(date(2027, 3, 31));

		assertThatDocument(type1DocWithoutDates).isActiveDocument()
				.withExpectedTransfer(date(2017, 3, 31)).withExpectedDestroy(null).withExpectedDeposit(date(2020, 3, 31));

		//will not be adjusted since we use CalculatorUtils.toNextEndOfYearDate in document calculators
		assertThatDocument(type1CalculatedSemiActiveEqualToAjustedInactive).isActiveDocument()
				.withExpectedTransfer(date(2028, 3, 31)).withExpectedDestroy(null).withExpectedDeposit(date(2032, 3, 31));

		assertThatDocument(type1CalculatedSemiActiveAfterAjustedInactive).isActiveDocument()
				.withExpectedTransfer(date(2028, 3, 31)).withExpectedDestroy(null).withExpectedDeposit(date(2032, 3, 31));

		// -------

		assertThatDocument(type2DocWithDate).isActiveDocument()
				.withExpectedTransfer(date(2022, 3, 31)).withExpectedDeposit(null).withExpectedDestroy(date(2025, 3, 31));

		assertThatDocument(type2DocWithoutDate).isActiveDocument()
				.withExpectedTransfer(date(2017, 3, 31)).withExpectedDeposit(null).withExpectedDestroy(date(2020, 3, 31));

		// -------

		assertThatDocument(type3DocWithActiveDate).isActiveDocument()
				.withExpectedTransfer(date(2022, 3, 31)).withExpectedDestroy(null).withExpectedDeposit(date(2025, 3, 31));

		assertThatDocument(type3DocWithoutActiveDate).isActiveDocument()
				.withExpectedTransfer(date(2017, 3, 31)).withExpectedDestroy(null).withExpectedDeposit(date(2020, 3, 31));

		// -------

		assertThatDocument(type4DocWithSemiActiveDate).isActiveDocument()
				.withExpectedTransfer(date(2017, 3, 31)).withExpectedDeposit(null).withExpectedDestroy(date(2025, 3, 31));

		assertThatDocument(type4DocWithoutSemiActiveDate).isActiveDocument()
				.withExpectedTransfer(date(2017, 3, 31)).withExpectedDeposit(null).withExpectedDestroy(date(2020, 3, 31));

		assertThatDocument(type4CalculatedSemiActiveEqualToAjustedInactive).isActiveDocument()
				.withExpectedTransfer(date(2017, 3, 31)).withExpectedDeposit(null).withExpectedDestroy(date(2020, 3, 31));

		assertThatDocument(type4CalculatedSemiActiveAfterAjustedInactive).isActiveDocument()
				.withExpectedTransfer(date(2017, 3, 31)).withExpectedDeposit(null).withExpectedDestroy(date(2020, 3, 31));

		// -------

		assertThatDocument(type5WithBothDateTimeValues).isActiveDocument()
				.withExpectedTransfer(date(2018, 3, 31)).withExpectedDeposit(null).withExpectedDestroy(date(2034, 3, 31));
	}

	@Test
	public void whenDecommissioningFoldersInDocumentRetentionRulesModeThenDatesCopiedToDocuments()
			throws Exception {

		givenConfig(RMConfigs.CALCULATED_CLOSING_DATE, true);
		givenConfig(RMConfigs.DECOMMISSIONING_DATE_BASED_ON, CLOSE_DATE);
		givenConfig(RMConfigs.YEAR_END_DATE, "03/31");
		givenConfig(RMConfigs.REQUIRED_DAYS_BEFORE_YEAR_END_FOR_NOT_ADDING_A_YEAR, 60);
		givenConfig(RMConfigs.DOCUMENT_RETENTION_RULES, true);
		givenConfig(RMConfigs.CALCULATED_SEMIACTIVE_DATE_NUMBER_OF_YEAR_WHEN_VARIABLE_PERIOD, 20);
		givenConfig(RMConfigs.CALCULATED_INACTIVE_DATE_NUMBER_OF_YEAR_WHEN_VARIABLE_PERIOD, 30);
		waitForBatchProcess();
		createDateMetadatasAndCustomSchemas();

		CopyRetentionRule copy1 = copyBuilder.newPrincipal(asList(records.PA), "2-3-C").setTypeId(type1)
				.setActiveDateMetadata(documentDateA().getLocalCode())
				.setSemiActiveDateMetadata(customDocument1DateD().getLocalCode());
		CopyRetentionRule copy2 = copyBuilder.newPrincipal(asList(records.PA), "2-3-D").setTypeId(type2)
				.setActiveDateMetadata(documentDateB().getLocalCode())
				.setSemiActiveDateMetadata(documentDateA().getLocalCode());
		CopyRetentionRule copy3 = copyBuilder.newPrincipal(asList(records.PA), "2-3-C").setTypeId(type3)
				.setActiveDateMetadata(customDocument2DateE().getLocalCode());
		CopyRetentionRule copy4 = copyBuilder.newPrincipal(asList(records.PA), "2-3-D").setTypeId(type4)
				.setSemiActiveDateMetadata(documentDateB().getLocalCode());
		CopyRetentionRule copy5 = copyBuilder.newPrincipal(asList(records.PA), "2-3-D").setTypeId(type5)
				.setActiveDateMetadata(documentDateTimeC().getLocalCode())
				.setSemiActiveDateMetadata(documentDateTimeF().getLocalCode());

		CopyRetentionRule principal888_5_C = copyBuilder.newPrincipal(asList(records.PA), "888-5-C");
		CopyRetentionRule secondary888_6_C = copyBuilder.newSecondary(asList(records.PA), "888-6-C");

		Transaction transaction = new Transaction();
		RetentionRule rule1 = transaction.add(rm.newRetentionRuleWithId("rule1").setCode("rule1").setTitle("rule1"));
		rule1.setScope(DOCUMENTS);
		rule1.setResponsibleAdministrativeUnits(true);
		rule1.setDocumentCopyRetentionRules(copy1, copy2, copy3, copy4, copy5);
		rule1.setPrincipalDefaultDocumentCopyRetentionRule(principal888_5_C);
		rule1.setSecondaryDefaultDocumentCopyRetentionRule(secondary888_6_C);
		transaction.add(rm.getCategory(zeCategory).setRetentionRules(asList(rule1)));
		transaction.add(rule1);

		transaction.add(rm.getDocumentType(type1).setLinkedSchema(customDocument1Schema().getCode()));
		transaction.add(rm.getDocumentType(type2).setLinkedSchema(customDocument1Schema().getCode()));
		transaction.add(rm.getDocumentType(type3).setLinkedSchema(customDocument2Schema().getCode()));
		transaction.add(rm.getDocumentType(type4).setLinkedSchema(customDocument2Schema().getCode()));

		Folder activeFolder = transaction.add(newPrincipalFolderWithRule(rule1)).setOpenDate(date(2010, 1, 1));

		Document activeDocumentInActiveFolder = transaction.add(newCustomDocument1(activeFolder, type1));
		Document semiActiveDocumentInActiveFolder = transaction.add(newCustomDocument1(activeFolder, type1))
				.setActualTransferDateEntered(date(2015, 1, 1));
		Document depositedDocumentInActiveFolder = transaction.add(newCustomDocument1(activeFolder, type1))
				.setActualTransferDateEntered(date(2015, 1, 1)).setActualDepositDateEntered(date(2020, 1, 1));
		Document destroyedDocumentInActiveFolder = transaction.add(newCustomDocument1(activeFolder, type1))
				.setActualTransferDateEntered(date(2015, 1, 1)).setActualDestructionDateEntered(date(2020, 1, 1));

		Folder semiActiveFolder = transaction.add(newPrincipalFolderWithRule(rule1)).setOpenDate(date(2015, 1, 1))
				.setActualTransferDate(date(2025, 1, 1));

		Document activeDocumentInSemiActiveFolder = transaction.add(newCustomDocument1(semiActiveFolder, type1));
		Document semiActiveDocumentInSemiActiveFolder = transaction.add(newCustomDocument1(semiActiveFolder, type1))
				.setActualTransferDateEntered(date(2015, 1, 1));
		Document depositedDocumentInSemiActiveFolder = transaction.add(newCustomDocument1(semiActiveFolder, type1))
				.setActualTransferDateEntered(date(2015, 1, 1)).setActualDepositDateEntered(date(2020, 1, 1));
		Document destroyedDocumentInSemiActiveFolder = transaction.add(newCustomDocument1(semiActiveFolder, type1))
				.setActualTransferDateEntered(date(2015, 1, 1)).setActualDestructionDateEntered(date(2020, 1, 1));

		Folder depositedFolder = transaction.add(newPrincipalFolderWithRule(rule1)).setOpenDate(date(2015, 1, 1))
				.setActualTransferDate(date(2020, 1, 1)).setActualDepositDate(date(2025, 1, 1));

		Document activeDocumentInDepositedFolder = transaction.add(newCustomDocument1(depositedFolder, type1));
		Document semiActiveDocumentInDepositedFolder = transaction.add(newCustomDocument1(depositedFolder, type1))
				.setActualTransferDateEntered(date(2015, 1, 1));
		Document depositedDocumentInDepositedeFolder = transaction.add(newCustomDocument1(depositedFolder, type1))
				.setActualTransferDateEntered(date(2015, 1, 1)).setActualDepositDateEntered(date(2020, 1, 1));
		Document destroyedDocumentInDepositedFolder = transaction.add(newCustomDocument1(depositedFolder, type1))
				.setActualTransferDateEntered(date(2015, 1, 1)).setActualDestructionDateEntered(date(2020, 1, 1));

		Folder destroyedFolder = transaction.add(newPrincipalFolderWithRule(rule1)).setOpenDate(date(2015, 1, 1))
				.setActualTransferDate(date(2020, 1, 1)).setActualDestructionDate(date(2025, 1, 1));

		Document activeDocumentInDestroyedFolder = transaction.add(newCustomDocument1(destroyedFolder, type1));
		Document semiActiveDocumentInDestroyedFolder = transaction.add(newCustomDocument1(destroyedFolder, type1))
				.setActualTransferDateEntered(date(2015, 1, 1));
		Document depositedDocumentInDestroyedFolder = transaction.add(newCustomDocument1(destroyedFolder, type1))
				.setActualTransferDateEntered(date(2015, 1, 1)).setActualDepositDateEntered(date(2020, 1, 1));
		Document destroyedDocumentInDestroyedFolder = transaction.add(newCustomDocument1(destroyedFolder, type1))
				.setActualTransferDateEntered(date(2015, 1, 1)).setActualDestructionDateEntered(date(2020, 1, 1));

		recordServices.execute(transaction);

		assertThatDocument(activeDocumentInActiveFolder).isActiveDocument();
		assertThatDocument(semiActiveDocumentInActiveFolder).isSemiActiveDocument(date(2015, 1, 1));
		assertThatDocument(depositedDocumentInActiveFolder).isDepositedDocument(date(2015, 1, 1), date(2020, 1, 1));
		assertThatDocument(destroyedDocumentInActiveFolder).isDestroyedDocument(date(2015, 1, 1), date(2020, 1, 1));

		assertThatDocument(activeDocumentInSemiActiveFolder).isSemiActiveDocument(date(2025, 1, 1));
		assertThatDocument(semiActiveDocumentInSemiActiveFolder).isSemiActiveDocument(date(2015, 1, 1));
		assertThatDocument(depositedDocumentInSemiActiveFolder).isDepositedDocument(date(2015, 1, 1), date(2020, 1, 1));
		assertThatDocument(destroyedDocumentInSemiActiveFolder).isDestroyedDocument(date(2015, 1, 1), date(2020, 1, 1));

		assertThatDocument(activeDocumentInDepositedFolder).isDepositedDocument(date(2020, 1, 1), date(2025, 1, 1));
		assertThatDocument(semiActiveDocumentInDepositedFolder).isDepositedDocument(date(2015, 1, 1), date(2025, 1, 1));
		assertThatDocument(depositedDocumentInDepositedeFolder).isDepositedDocument(date(2015, 1, 1), date(2020, 1, 1));
		assertThatDocument(destroyedDocumentInDepositedFolder).isDestroyedDocument(date(2015, 1, 1), date(2020, 1, 1));

		assertThatDocument(activeDocumentInDestroyedFolder).isDestroyedDocument(date(2020, 1, 1), date(2025, 1, 1));
		assertThatDocument(semiActiveDocumentInDestroyedFolder).isDestroyedDocument(date(2015, 1, 1), date(2025, 1, 1));
		assertThatDocument(depositedDocumentInDestroyedFolder).isDepositedDocument(date(2015, 1, 1), date(2020, 1, 1));
		assertThatDocument(destroyedDocumentInDestroyedFolder).isDestroyedDocument(date(2015, 1, 1), date(2020, 1, 1));
	}

	@Test
	public void givenDocumentWithMultipleApplicableCopyRuleThenTakeTheEnteredOneOrValidationException()
			throws Exception {

		givenConfig(RMConfigs.CALCULATED_CLOSING_DATE, true);
		givenConfig(RMConfigs.DECOMMISSIONING_DATE_BASED_ON, CLOSE_DATE);
		givenConfig(RMConfigs.YEAR_END_DATE, "03/31");
		givenConfig(RMConfigs.REQUIRED_DAYS_BEFORE_YEAR_END_FOR_NOT_ADDING_A_YEAR, 60);
		givenConfig(RMConfigs.DOCUMENT_RETENTION_RULES, true);
		givenConfig(RMConfigs.CALCULATED_SEMIACTIVE_DATE_NUMBER_OF_YEAR_WHEN_VARIABLE_PERIOD, 20);
		givenConfig(RMConfigs.CALCULATED_INACTIVE_DATE_NUMBER_OF_YEAR_WHEN_VARIABLE_PERIOD, 30);
		waitForBatchProcess();
		createDateMetadatasAndCustomSchemas();

		CopyRetentionRule copy1 = copyBuilder.newPrincipal(asList(records.PA), "1-3-C").setTypeId(type1);
		CopyRetentionRule copy2 = copyBuilder.newPrincipal(asList(records.PA), "2-3-D").setTypeId(type1);
		CopyRetentionRule copy3 = copyBuilder.newPrincipal(asList(records.PA), "3-3-C").setTypeId(type1);
		CopyRetentionRule copy4 = copyBuilder.newPrincipal(asList(records.PA), "4-3-D").setTypeId(type1);
		CopyRetentionRule copy5 = copyBuilder.newPrincipal(asList(records.PA), "5-3-T").setTypeId(type1);
		CopyRetentionRule copy6 = copyBuilder.newPrincipal(asList(records.PA), "6-3-D").setTypeId(type2);

		CopyRetentionRule principal888_5_C = copyBuilder.newPrincipal(asList(records.PA), "888-5-C");
		CopyRetentionRule secondary888_6_C = copyBuilder.newSecondary(asList(records.PA), "888-6-C");

		Transaction transaction = new Transaction();
		RetentionRule rule1 = transaction.add(rm.newRetentionRuleWithId("rule1").setCode("rule1").setTitle("rule1"));
		rule1.setScope(DOCUMENTS);
		rule1.setResponsibleAdministrativeUnits(true);
		rule1.setDocumentCopyRetentionRules(copy1, copy2, copy3, copy4, copy5, copy6);
		rule1.setPrincipalDefaultDocumentCopyRetentionRule(principal888_5_C);
		rule1.setSecondaryDefaultDocumentCopyRetentionRule(secondary888_6_C);
		transaction.add(rm.getCategory(zeCategory).setRetentionRules(asList(rule1)));
		transaction.add(rule1);

		transaction.add(rm.getDocumentType(type1).setLinkedSchema(customDocument1Schema().getCode()));
		transaction.add(rm.getDocumentType(type2).setLinkedSchema(customDocument2Schema().getCode()));

		Folder folder = transaction.add(newPrincipalFolderWithRule(rule1)).setOpenDate(date(2010, 1, 1));

		Document documentWithCopy1 = transaction.add(newCustomDocument1(folder, type1).setMainCopyRuleIdEntered(copy1.getId()));
		Document documentWithCopy2 = transaction.add(newCustomDocument1(folder, type1).setMainCopyRuleIdEntered(copy2.getId()));
		Document documentWithCopy3 = transaction.add(newCustomDocument1(folder, type1).setMainCopyRuleIdEntered(copy3.getId()));
		Document documentWithCopy4 = transaction.add(newCustomDocument1(folder, type1).setMainCopyRuleIdEntered(copy4.getId()));
		Document documentWithCopy5 = transaction.add(newCustomDocument1(folder, type1).setMainCopyRuleIdEntered(copy5.getId()));
		recordServices.execute(transaction);

		assertThatDocument(documentWithCopy1).isActiveDocument()
				.withMainCopyRetentionRule(copy1)
				.withExpectedTransfer(date(2011, 3, 31))
				.withExpectedDeposit(date(2014, 3, 31));

		assertThatDocument(documentWithCopy2).isActiveDocument()
				.withMainCopyRetentionRule(copy2)
				.withExpectedTransfer(date(2012, 3, 31))
				.withExpectedDestroy(date(2015, 3, 31));

		assertThatDocument(documentWithCopy3).isActiveDocument()
				.withMainCopyRetentionRule(copy3)
				.withExpectedTransfer(date(2013, 3, 31))
				.withExpectedDeposit(date(2016, 3, 31));

		assertThatDocument(documentWithCopy4).isActiveDocument()
				.withMainCopyRetentionRule(copy4)
				.withExpectedTransfer(date(2014, 3, 31))
				.withExpectedDestroy(date(2017, 3, 31));

		assertThatDocument(documentWithCopy5).isActiveDocument()
				.withMainCopyRetentionRule(copy5)
				.withExpectedTransfer(date(2015, 3, 31))
				.withExpectedDestroyAndDeposit(date(2018, 3, 31));

		try {
			recordServices.add(newCustomDocument1(folder, type1));
			fail("Validation exception expected");
		} catch (RecordServicesException.ValidationException e) {
			assertThat(e.getErrors().getValidationErrors()).hasSize(1);
			assertThat(e.getErrors().getValidationErrors().get(0).getParameters())
					.containsEntry(RecordMetadataValidator.METADATA_CODE, "document_custom1_mainCopyRule");
		}

		try {
			recordServices.add(newCustomDocument1(folder, type1).setMainCopyRuleIdEntered(copy6.getId()));
			fail("Validation exception expected");
		} catch (RecordServicesException.ValidationException e) {
			assertThat(e.getErrors().getValidationErrors()).hasSize(1);
			assertThat(e.getErrors().getValidationErrors().get(0).getParameters())
					.containsEntry(RecordMetadataValidator.METADATA_CODE, "document_custom1_mainCopyRule");
		}

		try {
			recordServices.add(newCustomDocument1(folder, type1).setMainCopyRuleIdEntered("invalidID"));
			fail("Validation exception expected");
		} catch (RecordServicesException.ValidationException e) {
			assertThat(e.getErrors().getValidationErrors()).hasSize(1);
			assertThat(e.getErrors().getValidationErrors().get(0).getParameters())
					.containsEntry(RecordMetadataValidator.METADATA_CODE, "document_custom1_mainCopyRule");
		}
	}

	private DocumentAssert assertThatDocument(Document document) {
		return new DocumentAssert(document);
	}

	private Folder newPrincipalFolderInCategoryWithRule(String category, RetentionRule rule) {
		return rm.newFolder().setCategoryEntered(category).setRetentionRuleEntered(rule).setOpenDate(squatreNovembre)
				.setCopyStatusEntered(CopyType.PRINCIPAL).setAdministrativeUnitEntered(records.getUnit10()).setTitle("test");
	}

	private Folder newPrincipalFolderWithRule(RetentionRule rule) {
		return rm.newFolder().setCategoryEntered(zeCategory).setRetentionRuleEntered(rule)
				.setOpenDate(squatreNovembre)
				.setCopyStatusEntered(CopyType.PRINCIPAL).setAdministrativeUnitEntered(records.getUnit10()).setTitle("test");
	}

	private Folder newSecondaryFolderWithRule(RetentionRule rule) {
		return rm.newFolder().setCategoryEntered(zeCategory).setRetentionRuleEntered(rule)
				.setOpenDate(squatreNovembre)
				.setCopyStatusEntered(CopyType.SECONDARY).setAdministrativeUnitEntered(records.getUnit10()).setTitle("test");
	}

	private Folder newPrincipalFolderWithRule(String folderId, RetentionRule rule) {
		return rm.newFolderWithId(folderId).setCategoryEntered(zeCategory).setRetentionRuleEntered(rule)
				.setOpenDate(squatreNovembre)
				.setCopyStatusEntered(CopyType.PRINCIPAL).setAdministrativeUnitEntered(records.getUnit10()).setTitle("test");
	}

	private Folder newSecondaryFolderWithRule(String folderId, RetentionRule rule) {
		return rm.newFolderWithId(folderId).setCategoryEntered(zeCategory).setRetentionRuleEntered(rule)
				.setOpenDate(squatreNovembre)
				.setCopyStatusEntered(CopyType.SECONDARY).setAdministrativeUnitEntered(records.getUnit10()).setTitle("test");
	}

	private Document newDocumentInFolderWithType(Folder folder, String typeId) {
		return rm.newDocument().setTitle("test").setFolder(folder).setType(typeId);
	}

	private Document newDocumentInFolderWithIdAndType(Folder folder, String id, String typeId) {
		return rm.newDocumentWithId(id).setTitle("test").setFolder(folder).setType(typeId);
	}

	private Document newCustomDocument1(Folder folder, String typeId) {
		Record record = recordServices.newRecordWithSchema(customDocument1Schema());
		return rm.wrapDocument(record).setTitle("test").setFolder(folder).setType(typeId);
	}

	private Document newCustomDocument2(Folder folder, String typeId) {
		Record record = recordServices.newRecordWithSchema(customDocument2Schema());
		return rm.wrapDocument(record).setTitle("test").setFolder(folder).setType(typeId);
	}

	//
	private List<Record> getDocumentsFromFolder(String folderId) {
		LogicalSearchCondition condition = from(
				metadataSchemasManager.getSchemaTypes(zeCollection).getSchemaType(Document.SCHEMA_TYPE))
				.where(metadataSchemasManager.getSchemaTypes(zeCollection)
						.getMetadata(Document.DEFAULT_SCHEMA + "_" + Document.FOLDER)).is(folderId);
		LogicalSearchQuery query = new LogicalSearchQuery();
		query.setCondition(condition);
		return searchServices.search(query);
	}

	private void createDateMetadatasAndCustomSchemas() {
		metadataSchemasManager.modify(zeCollection, new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				MetadataSchemaTypeBuilder documentSchemaType = types.getSchemaType(Document.SCHEMA_TYPE);
				MetadataSchemaBuilder document = documentSchemaType.getDefaultSchema();
				MetadataSchemaBuilder custom1 = documentSchemaType.createCustomSchema("custom1");
				MetadataSchemaBuilder custom2 = documentSchemaType.createCustomSchema("custom2");

				document.create("dateA").setType(DATE);
				document.create("dateB").setType(DATE);
				document.create("dateTimeC").setType(MetadataValueType.DATE_TIME);
				document.create("dateTimeF").setType(MetadataValueType.DATE_TIME);

				custom1.create("dateD").setType(DATE);
				custom2.create("dateE").setType(DATE);

				document.create("numberA").setType(MetadataValueType.NUMBER);
				document.create("numberB").setType(MetadataValueType.NUMBER);
				document.create("numberC").setType(MetadataValueType.NUMBER);
				document.create("numberF").setType(MetadataValueType.NUMBER);

				custom1.create("numberD").setType(MetadataValueType.NUMBER);
				custom2.create("numberE").setType(MetadataValueType.NUMBER);
			}
		});

	}

	private class DocumentAssert extends ObjectAssert<Document> {

		protected DocumentAssert(Document actual) {
			super(actual);
		}

		public DocumentAssert isActiveDocument() {
			assertThat(actual.getArchivisticStatus()).isEqualTo(FolderStatus.ACTIVE);
			//			assertThat(actual.getActualDepositDateEntered()).isNull();
			//			assertThat(actual.getActualDestructionDateEntered()).isNull();
			//			assertThat(actual.getActualTransferDateEntered()).isNull();

			assertThat(actual.getFolderActualDepositDate()).isNull();
			assertThat(actual.getFolderActualDestructionDate()).isNull();
			assertThat(actual.getFolderActualTransferDate()).isNull();
			return this;
		}

		public DocumentAssert isSemiActiveDocument(LocalDate transferDate) {
			assertThat(actual.getArchivisticStatus()).isEqualTo(FolderStatus.SEMI_ACTIVE);
			//			assertThat(actual.getActualTransferDateEntered()).isEqualTo(transferDate);
			//			assertThat(actual.getActualDepositDateEntered()).isNull();
			//			assertThat(actual.getActualDestructionDateEntered()).isNull();

			assertThat(actual.getFolderActualTransferDate()).isEqualTo(transferDate);
			assertThat(actual.getFolderActualDepositDate()).isNull();
			assertThat(actual.getFolderActualDestructionDate()).isNull();

			assertThat(actual.getFolderExpectedTransferDate()).isNull();
			return this;
		}

		public DocumentAssert isDepositedDocument(LocalDate transferDate, LocalDate depositedDocument) {
			assertThat(actual.getArchivisticStatus()).isEqualTo(FolderStatus.INACTIVE_DEPOSITED);
			//			assertThat(actual.getActualTransferDateEntered()).isEqualTo(transferDate);
			//			assertThat(actual.getActualDepositDateEntered()).isEqualTo(depositedDocument);
			//			assertThat(actual.getActualDestructionDateEntered()).isNull();

			assertThat(actual.getFolderActualTransferDate()).isEqualTo(transferDate);
			assertThat(actual.getFolderActualDepositDate()).isEqualTo(depositedDocument);
			assertThat(actual.getFolderActualDestructionDate()).isNull();

			assertThat(actual.getFolderExpectedTransferDate()).isNull();
			assertThat(actual.getFolderExpectedDepositDate()).isNull();
			assertThat(actual.getFolderExpectedDestructionDate()).isNull();
			return this;
		}

		public DocumentAssert isDestroyedDocument(LocalDate transferDate, LocalDate destroyedDate) {
			assertThat(actual.getArchivisticStatus()).isEqualTo(FolderStatus.INACTIVE_DESTROYED);

			//			assertThat(actual.getActualTransferDateEntered()).isEqualTo(transferDate);
			//			assertThat(actual.getActualDepositDateEntered()).isNull();
			//			assertThat(actual.getActualDestructionDateEntered()).isEqualTo(destroyedDate);

			assertThat(actual.getFolderActualTransferDate()).isEqualTo(transferDate);
			assertThat(actual.getFolderActualDepositDate()).isNull();
			assertThat(actual.getFolderActualDestructionDate()).isEqualTo(destroyedDate);

			assertThat(actual.getFolderExpectedTransferDate()).isNull();
			assertThat(actual.getFolderExpectedDestructionDate()).isNull();
			assertThat(actual.getFolderExpectedDestructionDate()).isNull();
			return this;
		}

		public DocumentAssert withMainCopyRetentionRule(CopyRetentionRule copyRetentionRule) {
			assertThat(actual.getMainCopyRule()).isEqualTo(copyRetentionRule);
			return this;
		}

		public DocumentAssert withExpectedTransfer(LocalDate localDate) {
			assertThat(actual.getFolderExpectedTransferDate()).isEqualTo(localDate);
			return this;
		}

		public DocumentAssert withExpectedDeposit(LocalDate localDate) {
			assertThat(actual.getFolderExpectedDepositDate()).isEqualTo(localDate);
			return this;
		}

		public DocumentAssert withExpectedDestroy(LocalDate localDate) {
			assertThat(actual.getFolderExpectedDestructionDate()).isEqualTo(localDate);
			return this;
		}

		public DocumentAssert withExpectedDestroyAndDeposit(LocalDate localDate) {
			assertThat(actual.getFolderExpectedDepositDate()).isEqualTo(localDate);
			assertThat(actual.getFolderExpectedDestructionDate()).isEqualTo(localDate);
			return this;
		}

	}

	MetadataSchema customDocument1Schema() {
		return rm.documentSchemaType().getCustomSchema("custom1");
	}

	MetadataSchema customDocument2Schema() {
		return rm.documentSchemaType().getCustomSchema("custom2");
	}

	Metadata documentDateA() {
		return rm.defaultDocumentSchema().get("dateA");
	}

	Metadata documentDateB() {
		return rm.defaultDocumentSchema().get("dateB");
	}

	Metadata documentDateTimeC() {
		return rm.defaultDocumentSchema().get("dateTimeC");
	}

	Metadata documentDateTimeF() {
		return rm.defaultDocumentSchema().get("dateTimeF");
	}

	Metadata customDocument1DateD() {
		return customDocument1Schema().get("dateD");
	}

	Metadata customDocument2DateE() {
		return customDocument2Schema().get("dateE");
	}

	Metadata documentNumberA() {
		return rm.defaultDocumentSchema().get("numberA");
	}

	Metadata documentNumberB() {
		return rm.defaultDocumentSchema().get("numberB");
	}

	Metadata documentNumberC() {
		return rm.defaultDocumentSchema().get("numberC");
	}

	Metadata documentNumberF() {
		return rm.defaultDocumentSchema().get("numberF");
	}

	Metadata customDocument1NumberD() {
		return customDocument1Schema().get("numberD");
	}

	Metadata customDocument2NumberE() {
		return customDocument2Schema().get("numberE");
	}

}