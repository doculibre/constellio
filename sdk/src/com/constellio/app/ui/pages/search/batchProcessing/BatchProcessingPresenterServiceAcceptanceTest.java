package com.constellio.app.ui.pages.search.batchProcessing;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.modules.rm.model.CopyRetentionRule;
import com.constellio.app.modules.rm.model.CopyRetentionRuleBuilder;
import com.constellio.app.modules.rm.model.CopyRetentionRuleFactory;
import com.constellio.app.modules.rm.model.enums.CopyType;
import com.constellio.app.modules.rm.model.enums.DisposalType;
import com.constellio.app.modules.rm.model.enums.FolderStatus;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.RetentionRule;
import com.constellio.app.ui.pages.search.batchProcessing.entities.BatchProcessRecordFieldModification;
import com.constellio.app.ui.pages.search.batchProcessing.entities.BatchProcessRequest;
import com.constellio.app.ui.pages.search.batchProcessing.entities.BatchProcessResults;
import com.constellio.app.ui.util.DateFormatUtils;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.security.Role;
import com.constellio.model.services.batch.actions.ChangeValueOfMetadataBatchProcessAction;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.schemas.MetadataSchemaTypesAlteration;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.security.AuthorizationsServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.setups.Users;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.constellio.app.modules.rm.model.enums.FolderStatus.INACTIVE_DEPOSITED;
import static com.constellio.model.entities.schemas.MetadataValueType.BOOLEAN;
import static com.constellio.model.entities.schemas.MetadataValueType.DATE;
import static com.constellio.model.entities.schemas.MetadataValueType.DATE_TIME;
import static com.constellio.model.entities.schemas.MetadataValueType.ENUM;
import static com.constellio.model.entities.schemas.MetadataValueType.NUMBER;
import static com.constellio.model.entities.schemas.MetadataValueType.STRING;
import static com.constellio.model.entities.schemas.MetadataValueType.TEXT;
import static com.constellio.model.entities.security.global.AuthorizationAddRequest.authorizationForUsers;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.fromAllSchemasIn;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.where;
import static com.constellio.sdk.tests.TestUtils.assertThatRecord;
import static com.constellio.sdk.tests.TestUtils.extractingSimpleCodeAndParameters;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static org.junit.Assert.fail;

public class BatchProcessingPresenterServiceAcceptanceTest extends ConstellioTest {

	RMSchemasRecordsServices rm;
	RMTestRecords records = new RMTestRecords(zeCollection);
	Users users = new Users();
	BatchProcessingPresenterService presenterService;
	MetadataSchema folderSchema;
	MetadataSchemaType folderSchemaType;
	SearchServices searchServices;
	CopyRetentionRuleFactory copyRetentionRuleFactory;
	RecordServices recordService;

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
		givenDisabledAfterTestValidations();
		givenBackgroundThreadsEnabled();
		givenRollbackCheckDisabled();
		prepareSystem(withZeCollection().withConstellioRMModule().withRMTest(records).withFoldersAndContainersOfEveryStatus()
				.withAllTest(users));

		rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
		recordService = getModelLayerFactory().newRecordServices();
		folderSchemaType = rm.folderSchemaType();
		searchServices = getModelLayerFactory().newSearchServices();
		presenterService = new BatchProcessingPresenterService(zeCollection, getAppLayerFactory(), Locale.FRENCH);
		copyRetentionRuleFactory = new CopyRetentionRuleFactory();

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
	public void givenTwoFolderOnePrincipalAndOneSecondaryAndThreePossibleDelaiWhenSelectingPrincipalDelaiThenOnlyPrincpalRuleChanged()
			throws Exception {

		Folder folder1 = rm.getFolder(records.folder_A04).setAdministrativeUnitEntered(records.unitId_10);

		RetentionRule retentionRule1 = rm.getRetentionRule(records.ruleId_2);
		retentionRule1.setResponsibleAdministrativeUnits(false);
		retentionRule1.setAdministrativeUnits(asList(records.unitId_30));
		recordService.update(retentionRule1);

		recordService.update(folder1);
		recordService.recalculate(folder1);

		Folder folder2 = rm.getFolder(records.folder_A05).setAdministrativeUnitEntered(records.unitId_30);
		folder2.setRetentionRuleEntered(records.ruleId_2);
		recordService.update(folder2);

		CopyRetentionRuleBuilder copyBuilder = CopyRetentionRuleBuilder.UUID();
		CopyRetentionRule principal5_2_C = copyBuilder.newPrincipal(asList(rm.PA(), rm.DM()), "5-2-T");

		principal5_2_C.setInactiveDisposalType(DisposalType.DEPOSIT);

		RetentionRule retentionRule2 = rm.getRetentionRule(records.ruleId_1);

		List newRetentionRuleList = new ArrayList();
		for (CopyRetentionRule currentCopyRetentionRule : retentionRule2.getCopyRetentionRules()) {
			newRetentionRuleList.add(currentCopyRetentionRule);
		}
		newRetentionRuleList.add(principal5_2_C);

		retentionRule2.setCopyRetentionRules(newRetentionRuleList);

		recordService.update(retentionRule2);

		assertThat(folder1.getMainCopyRule().getCopyType() == CopyType.PRINCIPAL);

		BatchProcessRequest request = new BatchProcessRequest().setUser(users.adminIn(zeCollection))
				.setQuery(new LogicalSearchQuery().setCondition(fromAllSchemasIn(zeCollection).where(Schemas.IDENTIFIER)
						.isIn(asList(records.folder_A04, records.folder_A05))))
				.addModifiedMetadata(Folder.DEFAULT_SCHEMA + "_" + Folder.RETENTION_RULE_ENTERED, records.ruleId_1)
				.addModifiedMetadata(Folder.DEFAULT_SCHEMA + "_" + Folder.MAIN_COPY_RULE_ID_ENTERED, principal5_2_C.getId());

		BatchProcessResults results = presenterService.simulateWithQuery(request);

		BatchProcessRecordFieldModification batchProcessRecordFieldModification = getFieldByKey(results.getRecordModifications(folder2.getId()).getFieldsModifications(), "folder_default_formModifiedBy");
		String valueAfterFormModifedBy = batchProcessRecordFieldModification.getValueAfter();
		String idForFormModifedBy = valueAfterFormModifedBy.substring(0, valueAfterFormModifedBy.indexOf(' '));


		Map<String, Map<String, Object>> mapSpecialCase = request.getSpecialCaseModifiedMetadatas();

		assertThat(mapSpecialCase.size() == 1);
		assertThat(mapSpecialCase.get("A05").get("folder_default_mainCopyRuleIdEntered").equals(retentionRule1.getSecondaryCopy().getId()));
		assertThat(mapSpecialCase.get("A04")).isNull();
		assertThat(removeMetadataCodeAndConfirmPresence("folder_default_formModifiedOn", results.getRecordModifications(folder2.getId()).getFieldsModifications())).extracting("valueBefore", "valueAfter", "metadata.code").containsOnly(
				tuple("Principal", "Secondaire", "folder_default_copyStatus"),
				tuple("5-2-T", "888-0-D", "folder_default_mainCopyRule"),
				tuple("2 (Règle de conservation #2)", "1 (Règle de conservation #1)", "folder_default_retentionRule"),
				tuple(null, idForFormModifedBy + " (System Admin)", "folder_default_formModifiedBy"));
		assertThat(results.getRecordModifications(folder1.getId()).getFieldsModifications()).extracting("valueBefore", "valueAfter", "metadata.code").containsOnly(
				tuple("42-5-C", "5-2-C", "folder_default_mainCopyRule"));
	}

	@Test
	public void givenFolderWithTwoPossibleDelaiAndWithPrincipalCopyTypeWhenBatchProcessRequestForAdministrativeUnitChangeThenCopyTypeChange()
			throws Exception {

		Folder folder1 = rm.getFolder(records.folder_A04).setAdministrativeUnitEntered(records.unitId_10);
		recordService.update(folder1);
		recordService.recalculate(folder1);

		assertThat(folder1.getMainCopyRule().getCopyType() == CopyType.PRINCIPAL);

		BatchProcessRequest request = new BatchProcessRequest().setUser(users.adminIn(zeCollection))
				.setQuery(new LogicalSearchQuery().setCondition(fromAllSchemasIn(zeCollection).where(Schemas.IDENTIFIER)
						.isIn(asList(records.folder_A04))))
				.addModifiedMetadata(Folder.DEFAULT_SCHEMA + "_" + Folder.ADMINISTRATIVE_UNIT_ENTERED, records.unitId_30);

		BatchProcessResults results = presenterService.simulateWithQuery(request);
		BatchProcessRecordFieldModification batchProcessRecordFieldModification = getFieldByKey(results.getRecordModifications(folder1.getId()).getFieldsModifications(), "folder_default_formModifiedBy");
		String valueAfterFormModifedBy = batchProcessRecordFieldModification.getValueAfter();
		String idForFormModifedBy = valueAfterFormModifedBy.substring(0, valueAfterFormModifedBy.indexOf(' '));

		assertThat(removeMetadataCodeAndConfirmPresence("folder_default_formModifiedOn", results.getRecordModifications(folder1.getId()).getFieldsModifications())).extracting("valueBefore", "valueAfter", "metadata.code").containsOnly(
				tuple("10", "30", "folder_default_administrativeUnitCode"),
				tuple("Principal", "Secondaire", "folder_default_copyStatus"),
				tuple("10 (Unité 10)", "30 (Unité 30)", "folder_default_administrativeUnit"),
				tuple("42-5-C", "888-0-D", "folder_default_mainCopyRule"),
				tuple(null, idForFormModifedBy + " (System Admin)", "folder_default_formModifiedBy")
		);
	}

	public static BatchProcessRecordFieldModification getFieldByKey(
			List<BatchProcessRecordFieldModification> batchProcessRecordFieldModificationList, String metadataCode) {
		for (BatchProcessRecordFieldModification batchProcessRecordFieldModification : batchProcessRecordFieldModificationList) {
			if (batchProcessRecordFieldModification.getMetadata().getCode().equals(metadataCode)) {
				return batchProcessRecordFieldModification;
			}
		}

		return null;
	}

	@Test
	public void givenFolderWithThreePossibleDelaiAndWithSecondCopyTypeWhenBatchProcessRequestForAdministrativeUnitChangeThenCopyTypeChange()
			throws Exception {
		Folder folder1 = rm.getFolder(records.folder_A04).setAdministrativeUnitEntered(records.unitId_30);
		recordService.update(folder1);
		recordService.recalculate(folder1);

		CopyRetentionRuleBuilder copyBuilder = CopyRetentionRuleBuilder.UUID();
		CopyRetentionRule principal5_2_T = copyBuilder.newPrincipal(asList(rm.PA(), rm.DM()), "5-2-T");

		principal5_2_T.setInactiveDisposalType(DisposalType.DEPOSIT);

		RetentionRule retentionRule = rm.getRetentionRule(records.ruleId_1);

		List newRetentionRuleList = new ArrayList();
		for (CopyRetentionRule currentCopyRetentionRule : retentionRule.getCopyRetentionRules()) {
			newRetentionRuleList.add(currentCopyRetentionRule);
		}
		newRetentionRuleList.add(principal5_2_T);

		retentionRule.setCopyRetentionRules(newRetentionRuleList);

		recordService.update(retentionRule);

		BatchProcessRequest request = new BatchProcessRequest().setUser(users.adminIn(zeCollection))
				.setQuery(new LogicalSearchQuery().setCondition(fromAllSchemasIn(zeCollection).where(Schemas.IDENTIFIER)
						.isIn(asList(records.folder_A04))))
				.addModifiedMetadata(Folder.DEFAULT_SCHEMA + "_" + Folder.ADMINISTRATIVE_UNIT_ENTERED, records.unitId_10);

		BatchProcessResults results = presenterService.simulateWithQuery(request);

		assertThat(results.getRecordModifications(folder1.getId()).getFieldsModifications())
				.extracting("valueBefore", "valueAfter", "metadata.code").containsOnly(
				tuple("30", "10", "folder_default_administrativeUnitCode"),
				tuple("Secondaire", "Principal", "folder_default_copyStatus"),
				tuple("30 (Unité 30)", "10 (Unité 10)", "folder_default_administrativeUnit"),
				tuple("888-0-D", "42-5-C", "folder_default_mainCopyRule")
		);

	}

	@Test
	public void givenFolderWithTwoPossibleDelaiAndWithSecondCopyTypeWhenBatchProcessRequestForAdministrativeUnitChangeThenCopyTypeChange()
			throws Exception {

		Folder folder1 = rm.getFolder(records.folder_A04).setAdministrativeUnitEntered(records.unitId_30);
		recordService.update(folder1);
		recordService.recalculate(folder1);

		assertThat(folder1.getMainCopyRule().getCopyType() == CopyType.PRINCIPAL);

		BatchProcessRequest request = new BatchProcessRequest().setUser(users.adminIn(zeCollection))
				.setQuery(new LogicalSearchQuery().setCondition(fromAllSchemasIn(zeCollection).where(Schemas.IDENTIFIER)
						.isIn(asList(records.folder_A04))))
				.addModifiedMetadata(Folder.DEFAULT_SCHEMA + "_" + Folder.ADMINISTRATIVE_UNIT_ENTERED, records.unitId_10);

		BatchProcessResults results = presenterService.simulateWithQuery(request);

		BatchProcessRecordFieldModification batchProcessRecordFieldModification = getFieldByKey(results.getRecordModifications(folder1.getId()).getFieldsModifications(), "folder_default_formModifiedBy");
		String valueAfterFormModifedBy = batchProcessRecordFieldModification.getValueAfter();
		String idForFormModifedBy = valueAfterFormModifedBy.substring(0, valueAfterFormModifedBy.indexOf(' '));


		assertThat(removeMetadataCodeAndConfirmPresence("folder_default_formModifiedOn", results.getRecordModifications(folder1.getId()).getFieldsModifications()))
				.extracting("valueBefore", "valueAfter", "metadata.code").containsOnly(

				tuple("30", "10", "folder_default_administrativeUnitCode"),
				tuple("Secondaire", "Principal", "folder_default_copyStatus"),
				tuple("30 (Unité 30)", "10 (Unité 10)", "folder_default_administrativeUnit"),
				tuple("888-0-D", "42-5-C", "folder_default_mainCopyRule"),
				tuple(null, idForFormModifedBy + " (System Admin)", "folder_default_formModifiedBy")
		);
	}

	@Test
	public void givenValidationExceptionsThenThrownInSimulation()
			throws Exception {
		givenRollbackCheckDisabled();
		BatchProcessRequest request = new BatchProcessRequest().setUser(users.adminIn(zeCollection))
				.setQuery(new LogicalSearchQuery().setCondition(fromAllSchemasIn(zeCollection).where(Schemas.IDENTIFIER)
						.isIn(asList(records.folder_A05, records.folder_A16))))
				.addModifiedMetadata(Folder.RETENTION_RULE_ENTERED, records.ruleId_2);

		try {
			BatchProcessResults results = presenterService.simulateWithQuery(request);
			fail("error expected!");
		} catch (RecordServicesException.ValidationException e) {
			assertThat(extractingSimpleCodeAndParameters(e.getErrors(), "record", "metadataCode")).containsOnly(
					tuple("ValueRequirementValidator_requiredValueForMetadata", records.folder_A05, "folder_default_copyStatus"),
					tuple("ValueRequirementValidator_requiredValueForMetadata", records.folder_A16, "folder_default_copyStatus"),
					tuple("ValueRequirementValidator_requiredValueForMetadata", records.folder_A05,
							"folder_default_mainCopyRule"),
					tuple("ValueRequirementValidator_requiredValueForMetadata", records.folder_A16, "folder_default_mainCopyRule")
			);
		}

		try {
			Map<String, Object> modifications = new HashMap<>();
			modifications.put(Folder.RETENTION_RULE_ENTERED, records.ruleId_2);
			presenterService.execute(request, new ChangeValueOfMetadataBatchProcessAction(modifications), request.getQuery(),
					records.getAdmin(), "Edit records");
			fail("error expected!");
		} catch (RecordServicesException.ValidationException e) {
			assertThat(extractingSimpleCodeAndParameters(e.getErrors(), "record", "metadataCode")).containsOnly(
					tuple("ValueRequirementValidator_requiredValueForMetadata", records.folder_A05, "folder_default_copyStatus"),
					tuple("ValueRequirementValidator_requiredValueForMetadata", records.folder_A16, "folder_default_copyStatus"),
					tuple("ValueRequirementValidator_requiredValueForMetadata", records.folder_A05,
							"folder_default_mainCopyRule"),
					tuple("ValueRequirementValidator_requiredValueForMetadata", records.folder_A16, "folder_default_mainCopyRule")
			);
		}
		waitForBatchProcess();
		assertThat(records.getFolder_A05().getRetentionRuleEntered()).isNotEqualTo(records.ruleId_2);

	}

	@Test
	public void givenValidationExceptionsThenThrownInSimulationWithIds()
			throws Exception {

		BatchProcessRequest request = new BatchProcessRequest().setUser(users.adminIn(zeCollection))
				.setIds(asList(records.folder_A05, records.folder_A16))
				.addModifiedMetadata(Folder.RETENTION_RULE_ENTERED, records.ruleId_2);

		try {
			BatchProcessResults results = presenterService.simulateWithIds(request);
			fail("error expected!");
		} catch (RecordServicesException.ValidationException e) {
			assertThat(extractingSimpleCodeAndParameters(e.getErrors(), "record", "metadataCode")).containsOnly(
					tuple("ValueRequirementValidator_requiredValueForMetadata", records.folder_A05, "folder_default_copyStatus"),
					tuple("ValueRequirementValidator_requiredValueForMetadata", records.folder_A16, "folder_default_copyStatus"),
					tuple("ValueRequirementValidator_requiredValueForMetadata", records.folder_A05,
							"folder_default_mainCopyRule"),
					tuple("ValueRequirementValidator_requiredValueForMetadata", records.folder_A16, "folder_default_mainCopyRule")
			);
		}

		try {
			Map<String, Object> modifications = new HashMap<>();
			modifications.put(Folder.RETENTION_RULE_ENTERED, records.ruleId_2);
			presenterService.execute(request, new ChangeValueOfMetadataBatchProcessAction(modifications), request.getIds(),
					records.getAdmin(), "Edit records");
			fail("error expected!");
		} catch (RecordServicesException.ValidationException e) {
			assertThat(extractingSimpleCodeAndParameters(e.getErrors(), "record", "metadataCode")).containsOnly(
					tuple("ValueRequirementValidator_requiredValueForMetadata", records.folder_A05, "folder_default_copyStatus"),
					tuple("ValueRequirementValidator_requiredValueForMetadata", records.folder_A16, "folder_default_copyStatus"),
					tuple("ValueRequirementValidator_requiredValueForMetadata", records.folder_A05,
							"folder_default_mainCopyRule"),
					tuple("ValueRequirementValidator_requiredValueForMetadata", records.folder_A16, "folder_default_mainCopyRule")
			);
		}
		waitForBatchProcess();
		assertThat(records.getFolder_A05().getRetentionRuleEntered()).isNotEqualTo(records.ruleId_2);

	}

	@Test
	public void whenSetCopyRuleEnteredThenApplied()
			throws Exception {
		givenRollbackCheckDisabled();
		BatchProcessRequest request = new BatchProcessRequest().setUser(users.adminIn(zeCollection))
				.setQuery(new LogicalSearchQuery().setCondition(fromAllSchemasIn(zeCollection).where(Schemas.IDENTIFIER)
						.isIn(asList(records.folder_A05, records.folder_A16))))
				.addModifiedMetadata(Folder.RETENTION_RULE_ENTERED, records.ruleId_2);

		try {
			BatchProcessResults results = presenterService.simulateWithQuery(request);
			fail("error expected!");
		} catch (RecordServicesException.ValidationException e) {
			assertThat(extractingSimpleCodeAndParameters(e.getErrors(), "record", "metadataCode")).containsOnly(
					tuple("ValueRequirementValidator_requiredValueForMetadata", records.folder_A05, "folder_default_copyStatus"),
					tuple("ValueRequirementValidator_requiredValueForMetadata", records.folder_A16, "folder_default_copyStatus"),
					tuple("ValueRequirementValidator_requiredValueForMetadata", records.folder_A05,
							"folder_default_mainCopyRule"),
					tuple("ValueRequirementValidator_requiredValueForMetadata", records.folder_A16, "folder_default_mainCopyRule")
			);
		}

		try {
			Map<String, Object> modifications = new HashMap<>();
			modifications.put(Folder.RETENTION_RULE_ENTERED, records.ruleId_2);
			presenterService.execute(request, new ChangeValueOfMetadataBatchProcessAction(modifications), request.getQuery(),
					records.getAdmin(), "Edit records");
			fail("error expected!");
		} catch (RecordServicesException.ValidationException e) {
			assertThat(extractingSimpleCodeAndParameters(e.getErrors(), "record", "metadataCode")).containsOnly(
					tuple("ValueRequirementValidator_requiredValueForMetadata", records.folder_A05, "folder_default_copyStatus"),
					tuple("ValueRequirementValidator_requiredValueForMetadata", records.folder_A16, "folder_default_copyStatus"),
					tuple("ValueRequirementValidator_requiredValueForMetadata", records.folder_A05,
							"folder_default_mainCopyRule"),
					tuple("ValueRequirementValidator_requiredValueForMetadata", records.folder_A16, "folder_default_mainCopyRule")
			);
		}
		waitForBatchProcess();
		assertThat(records.getFolder_A05().getRetentionRuleEntered()).isNotEqualTo(records.ruleId_2);

	}

	@Test
	public void whenSetCopyRuleEnteredWithIdsThenApplied()
			throws Exception {
		BatchProcessRequest request = new BatchProcessRequest().setUser(users.adminIn(zeCollection))
				.setIds(asList(records.folder_A05, records.folder_A16))
				.addModifiedMetadata(Folder.RETENTION_RULE_ENTERED, records.ruleId_2);

		try {
			BatchProcessResults results = presenterService.simulateWithIds(request);
			fail("error expected!");
		} catch (RecordServicesException.ValidationException e) {
			assertThat(extractingSimpleCodeAndParameters(e.getErrors(), "record", "metadataCode")).containsOnly(
					tuple("ValueRequirementValidator_requiredValueForMetadata", records.folder_A05, "folder_default_copyStatus"),
					tuple("ValueRequirementValidator_requiredValueForMetadata", records.folder_A16, "folder_default_copyStatus"),
					tuple("ValueRequirementValidator_requiredValueForMetadata", records.folder_A05,
							"folder_default_mainCopyRule"),
					tuple("ValueRequirementValidator_requiredValueForMetadata", records.folder_A16, "folder_default_mainCopyRule")
			);
		}

		try {
			Map<String, Object> modifications = new HashMap<>();
			modifications.put(Folder.RETENTION_RULE_ENTERED, records.ruleId_2);
			presenterService.execute(request, new ChangeValueOfMetadataBatchProcessAction(modifications), request.getIds(),
					records.getAdmin(), "Edit records");
			fail("error expected!");
		} catch (RecordServicesException.ValidationException e) {
			assertThat(extractingSimpleCodeAndParameters(e.getErrors(), "record", "metadataCode")).containsOnly(
					tuple("ValueRequirementValidator_requiredValueForMetadata", records.folder_A05, "folder_default_copyStatus"),
					tuple("ValueRequirementValidator_requiredValueForMetadata", records.folder_A16, "folder_default_copyStatus"),
					tuple("ValueRequirementValidator_requiredValueForMetadata", records.folder_A05,
							"folder_default_mainCopyRule"),
					tuple("ValueRequirementValidator_requiredValueForMetadata", records.folder_A16, "folder_default_mainCopyRule")
			);
		}
		waitForBatchProcess();
		assertThat(records.getFolder_A05().getRetentionRuleEntered()).isNotEqualTo(records.ruleId_2);

	}

	private String error(Class<?> validatorClass, String code) {
		return validatorClass.getCanonicalName() + "." + code;
	}

	@Test
	public void whenModifyingValuesWithImpactsInHierarchyInHierarchyThenCalculated()
			throws Exception {

		BatchProcessRequest request = new BatchProcessRequest().setUser(users.adminIn(zeCollection))
				.setQuery(new LogicalSearchQuery().setCondition(fromAllSchemasIn(zeCollection).where(Schemas.IDENTIFIER)
						.isIn(asList(records.folder_A04, records.folder_A16))))
				.addModifiedMetadata(Folder.RETENTION_RULE_ENTERED, records.ruleId_2)
				.addModifiedMetadata(Folder.COPY_STATUS_ENTERED, CopyType.SECONDARY);

		Map<String, Object> modifications = new HashMap<>();
		modifications.put(Folder.DEFAULT_SCHEMA + "_" + Folder.RETENTION_RULE_ENTERED, records.ruleId_2);
		modifications.put(Folder.DEFAULT_SCHEMA + "_" + Folder.COPY_STATUS_ENTERED, CopyType.SECONDARY);
		request.setModifiedMetadatas(modifications);
		presenterService.execute(request, new ChangeValueOfMetadataBatchProcessAction(modifications), request.getQuery(),
				records.getAdmin(), "Edit records");

		waitForBatchProcess();

		assertThatRecord(records.getFolder_A04()).extracting(Folder.RETENTION_RULE, Folder.MAIN_COPY_RULE, Folder.COPY_STATUS)
				.containsOnly(
						records.ruleId_2, records.getRule2().getCopyRetentionRuleByString("2-0-D"), CopyType.SECONDARY
				);
		assertThatRecord(records.getFolder_A16()).extracting(Folder.RETENTION_RULE, Folder.MAIN_COPY_RULE, Folder.COPY_STATUS,
				Folder.EXPECTED_TRANSFER_DATE, Folder.EXPECTED_DESTRUCTION_DATE, Folder.EXPECTED_DEPOSIT_DATE).containsOnly(
				records.ruleId_2, records.getRule2().getCopyRetentionRuleByString("2-0-D"), CopyType.SECONDARY, null,
				new LocalDate(2003, 10, 31), null
		);
	}

	@Test
	public void whenModifyingValuesWithImpactsInHierarchyInHierarchyWithIdsThenCalculated()
			throws Exception {

		BatchProcessRequest request = new BatchProcessRequest().setUser(users.adminIn(zeCollection))
				.setIds(asList(records.folder_A04, records.folder_A16))
				.addModifiedMetadata(Folder.RETENTION_RULE_ENTERED, records.ruleId_2)
				.addModifiedMetadata(Folder.COPY_STATUS_ENTERED, CopyType.SECONDARY);

		Map<String, Object> modifications = new HashMap<>();
		modifications.put(Folder.DEFAULT_SCHEMA + "_" + Folder.RETENTION_RULE_ENTERED, records.ruleId_2);
		modifications.put(Folder.DEFAULT_SCHEMA + "_" + Folder.COPY_STATUS_ENTERED, CopyType.SECONDARY);
		request.setModifiedMetadatas(modifications);
		presenterService.execute(request, new ChangeValueOfMetadataBatchProcessAction(modifications), request.getIds(),
				records.getAdmin(), "Edit records");

		waitForBatchProcess();

		assertThatRecord(records.getFolder_A04()).extracting(Folder.RETENTION_RULE, Folder.MAIN_COPY_RULE, Folder.COPY_STATUS)
				.containsOnly(
						records.ruleId_2, records.getRule2().getCopyRetentionRuleByString("2-0-D"), CopyType.SECONDARY
				);
		assertThatRecord(records.getFolder_A16()).extracting(Folder.RETENTION_RULE, Folder.MAIN_COPY_RULE, Folder.COPY_STATUS,
				Folder.EXPECTED_TRANSFER_DATE, Folder.EXPECTED_DESTRUCTION_DATE, Folder.EXPECTED_DEPOSIT_DATE).containsOnly(
				records.ruleId_2, records.getRule2().getCopyRetentionRuleByString("2-0-D"), CopyType.SECONDARY, null,
				new LocalDate(2003, 10, 31), null
		);
	}

	@Test
	public void whenModifyDocumentParentFolderInBatchOnlyHumanFriendlyMetadataAreShown()
			throws Exception {
		givenRollbackCheckDisabled();
		List<Document> folderA04Documents = rm.searchDocuments(where(rm.document.folder()).isEqualTo(records.folder_A04));
		Document document1 = folderA04Documents.get(0);
		Document document2 = folderA04Documents.get(1);
		Document document3 = folderA04Documents.get(2);

		BatchProcessRequest request = new BatchProcessRequest().setUser(users.adminIn(zeCollection))
				.setQuery(new LogicalSearchQuery().setCondition(fromAllSchemasIn(zeCollection).where(Schemas.IDENTIFIER)
						.isIn(asList(document1.getId(), document2.getId(), document3.getId()))))
				.addModifiedMetadata(Document.FOLDER, records.folder_A07);

		BatchProcessResults results = presenterService.simulateWithQuery(request);

		assertThat(results.getRecordModifications()).extracting("recordId", "recordTitle").containsOnly(
				tuple(document1.getId(), document1.getTitle()),
				tuple(document2.getId(), document2.getTitle()),
				tuple(document3.getId(), document3.getTitle()));


		assertThat(results.getRecordModifications(document1.getId()).getFieldsModifications())
				.extracting("metadata.code", "valueBefore", "valueAfter").containsOnly(

				tuple("document_default_category", "X110 (X110)", "Z112 (Z112)"),
				tuple("document_default_folder", "A04 (Baleine)", "A07 (Bouc)"),
				tuple("document_default_mainCopyRule", "42-5-C", "999-4-T"),
				tuple("document_default_categoryCode", "X110", "Z112"),
				tuple("document_default_retentionRule", "1 (Règle de conservation #1)", "3 (Règle de conservation #3)")
		);
	}

	public List<BatchProcessRecordFieldModification> removeMetadataCodeAndConfirmPresence(String code,
																						  List<BatchProcessRecordFieldModification> batchProcessRecordFieldModificationList) {

		List<BatchProcessRecordFieldModification> newbatchProcessRecordFieldModificationsList = new ArrayList<>();

		for (BatchProcessRecordFieldModification batchProcessRecordFieldModification : batchProcessRecordFieldModificationList) {
			if (!batchProcessRecordFieldModification.getMetadata().getCode().equals(code)) {
				newbatchProcessRecordFieldModificationsList.add(batchProcessRecordFieldModification);
			}
		}

		// Test if the code have been found and not added to the new list.
		assertThat(batchProcessRecordFieldModificationList.size() - newbatchProcessRecordFieldModificationsList.size()).isEqualTo(1);

		return newbatchProcessRecordFieldModificationsList;
	}

	@Test
	public void whenModifyDocumentParentFolderInBatchWithIdsOnlyHumanFriendlyMetadataAreShown()
			throws Exception {

		List<Document> folderA04Documents = rm.searchDocuments(where(rm.document.folder()).isEqualTo(records.folder_A04));
		Document document1 = folderA04Documents.get(0);
		Document document2 = folderA04Documents.get(1);
		Document document3 = folderA04Documents.get(2);

		BatchProcessRequest request = new BatchProcessRequest().setUser(users.adminIn(zeCollection))
				.setIds(asList(document1.getId(), document2.getId(), document3.getId()))
				.addModifiedMetadata(Document.FOLDER, records.folder_A07);

		BatchProcessResults results = presenterService.simulateWithIds(request);

		assertThat(results.getRecordModifications()).extracting("recordId", "recordTitle").containsOnly(
				tuple(document1.getId(), document1.getTitle()),
				tuple(document2.getId(), document2.getTitle()),
				tuple(document3.getId(), document3.getTitle()));

		assertThat(results.getRecordModifications(document1.getId()).getFieldsModifications())
				.extracting("metadata.code", "valueBefore", "valueAfter").containsOnly(

				tuple("document_default_category", "X110 (X110)", "Z112 (Z112)"),
				tuple("document_default_folder", "A04 (Baleine)", "A07 (Bouc)"),
				tuple("document_default_mainCopyRule", "42-5-C", "999-4-T"),
				tuple("document_default_categoryCode", "X110", "Z112"),
				tuple("document_default_retentionRule", "1 (Règle de conservation #1)", "3 (Règle de conservation #3)")

		);
	}

	@Test
	public void whenModifyContainerParentFolderInBatchOnlyHumanFriendlyMetadataAreShown()
			throws Exception {

		ContainerRecord container1 = records.getContainerBac01();
		ContainerRecord container2 = records.getContainerBac02();
		ContainerRecord container3 = records.getContainerBac03();

		getModelLayerFactory().newRecordServices().update(container1.setFull(null));
		getModelLayerFactory().newRecordServices().update(container2.setFull(null));
		getModelLayerFactory().newRecordServices().update(container3.setFull(null));

		BatchProcessRequest request = new BatchProcessRequest().setUser(users.adminIn(zeCollection))
				.setQuery(new LogicalSearchQuery().setCondition(fromAllSchemasIn(zeCollection).where(Schemas.IDENTIFIER)
						.isIn(asList(container1.getId(), container2.getId(), container3.getId()))))
				.addModifiedMetadata(ContainerRecord.CAPACITY, 42.0)
				.addModifiedMetadata(ContainerRecord.ADMINISTRATIVE_UNITS, asList(records.unitId_20d));

		BatchProcessResults results = presenterService.simulateWithQuery(request);

		assertThat(results.getRecordModifications()).extracting("recordId", "recordTitle").containsOnly(
				tuple(container1.getId(), container1.getTitle()),
				tuple(container2.getId(), container2.getTitle()),
				tuple(container3.getId(), container3.getTitle()));

		assertThat(results.getRecordModifications(container1.getId()).getFieldsModifications())
				.extracting("metadata.code", "valueBefore", "valueAfter").containsOnly(
				tuple("containerRecord_default_administrativeUnits", "[30C (Unité 30-C)]", "[20D (Unité 20-D)]"),
				tuple("containerRecord_default_capacity", null, "42.0"),
				tuple("containerRecord_default_availableSize", null, "42.0")
		);
	}

	@Test
	public void whenModifyContainerParentFolderInBatchWithIdsOnlyHumanFriendlyMetadataAreShown()
			throws Exception {

		ContainerRecord container1 = records.getContainerBac01();
		ContainerRecord container2 = records.getContainerBac02();
		ContainerRecord container3 = records.getContainerBac03();

		getModelLayerFactory().newRecordServices().update(container1.setFull(null));
		getModelLayerFactory().newRecordServices().update(container2.setFull(null));
		getModelLayerFactory().newRecordServices().update(container3.setFull(null));

		BatchProcessRequest request = new BatchProcessRequest().setUser(users.adminIn(zeCollection))
				.setIds(asList(container1.getId(), container2.getId(), container3.getId()))
				.addModifiedMetadata(ContainerRecord.CAPACITY, 42.0)
				.addModifiedMetadata(ContainerRecord.ADMINISTRATIVE_UNITS, asList(records.unitId_20d));

		BatchProcessResults results = presenterService.simulateWithIds(request);

		assertThat(results.getRecordModifications()).extracting("recordId", "recordTitle").containsOnly(
				tuple(container1.getId(), container1.getTitle()),
				tuple(container2.getId(), container2.getTitle()),
				tuple(container3.getId(), container3.getTitle()));

		assertThat(results.getRecordModifications(container1.getId()).getFieldsModifications())
				.extracting("metadata.code", "valueBefore", "valueAfter").containsOnly(
				tuple("containerRecord_default_administrativeUnits", "[30C (Unité 30-C)]", "[20D (Unité 20-D)]"),
				tuple("containerRecord_default_capacity", null, "42.0"),
				tuple("containerRecord_default_availableSize", null, "42.0")
		);
	}

	@Test
	public void whenSimulateBatchProcessThenNoModificationsOccur()
			throws Exception {

		BatchProcessRequest request = new BatchProcessRequest().setUser(users.adminIn(zeCollection))
				.setQuery(new LogicalSearchQuery().setCondition(fromAllSchemasIn(zeCollection).where(Schemas.IDENTIFIER)
						.isIn(asList(records.folder_A03, records.folder_A04))))
				.addModifiedMetadata(Folder.DEFAULT_SCHEMA + "_" + Folder.TITLE, "Mon dossier");

		BatchProcessResults results = presenterService.simulateWithQuery(request);

		assertThat(results.getRecordModifications()).extracting("recordId", "recordTitle").containsOnly(
				tuple(records.folder_A03, "Alouette"),
				tuple(records.folder_A04, "Baleine")
		);

		assertThat(results.getRecordModifications(records.folder_A03).getImpacts()).hasSize(2);
		assertThat(results.getRecordModifications(records.folder_A03).getFieldsModifications())
				.extracting("metadata.code", "valueBefore", "valueAfter").containsOnly(
				tuple("folder_default_title", "Alouette", "Mon dossier")
		);

		assertThat(results.getRecordModifications(records.folder_A04).getImpacts()).hasSize(2);
		assertThat(results.getRecordModifications(records.folder_A04).getFieldsModifications())
				.extracting("metadata.code", "valueBefore", "valueAfter").containsOnly(
				tuple("folder_default_title", "Baleine", "Mon dossier")
		);

		assertThat(records.getFolder_A03().getTitle()).isEqualTo("Alouette");
		assertThat(records.getFolder_A04().getTitle()).isEqualTo("Baleine");

		Map<String, Object> modifications = new HashMap<>();
		modifications.put(Folder.DEFAULT_SCHEMA + "_" + Folder.TITLE, "Mon dossier");
		presenterService.execute(request, new ChangeValueOfMetadataBatchProcessAction(modifications), request.getQuery(),
				records.getAdmin(), "Edit records");

		waitForBatchProcess();
		assertThat(records.getFolder_A03().getTitle()).isEqualTo("Mon dossier");
		assertThat(records.getFolder_A04().getTitle()).isEqualTo("Mon dossier");
	}

	@Test
	public void whenSimulateBatchProcessWithIdsThenNoModificationsOccur()
			throws Exception {

		BatchProcessRequest request = new BatchProcessRequest().setUser(users.adminIn(zeCollection))
				.setIds(asList(records.folder_A03, records.folder_A04))
				.addModifiedMetadata(Folder.DEFAULT_SCHEMA + "_" + Folder.TITLE, "Mon dossier");

		BatchProcessResults results = presenterService.simulateWithIds(request);

		assertThat(results.getRecordModifications()).extracting("recordId", "recordTitle").containsOnly(
				tuple(records.folder_A03, "Alouette"),
				tuple(records.folder_A04, "Baleine")
		);

		assertThat(results.getRecordModifications(records.folder_A03).getImpacts()).hasSize(2);
		assertThat(results.getRecordModifications(records.folder_A03).getFieldsModifications())
				.extracting("metadata.code", "valueBefore", "valueAfter").containsOnly(
				tuple("folder_default_title", "Alouette", "Mon dossier")
		);

		assertThat(results.getRecordModifications(records.folder_A04).getImpacts()).hasSize(2);
		assertThat(results.getRecordModifications(records.folder_A04).getFieldsModifications())
				.extracting("metadata.code", "valueBefore", "valueAfter").containsOnly(
				tuple("folder_default_title", "Baleine", "Mon dossier")
		);

		assertThat(records.getFolder_A03().getTitle()).isEqualTo("Alouette");
		assertThat(records.getFolder_A04().getTitle()).isEqualTo("Baleine");

		Map<String, Object> modifications = new HashMap<>();
		modifications.put(Folder.DEFAULT_SCHEMA + "_" + Folder.TITLE, "Mon dossier");
		presenterService.execute(request, new ChangeValueOfMetadataBatchProcessAction(modifications), request.getIds(),
				records.getAdmin(), "Edit records");

		waitForBatchProcess();
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
				.setQuery(new LogicalSearchQuery().setCondition(fromAllSchemasIn(zeCollection).where(Schemas.IDENTIFIER)
						.isIn(asList(records.folder_A03, records.folder_A04))))
				.addModifiedMetadata(Folder.TITLE, "Mon dossier")
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

		BatchProcessResults results = presenterService.simulateWithQuery(request);

		assertThat(results.getRecordModifications()).extracting("recordId", "recordTitle").containsOnly(
				tuple(records.folder_A03, "Alouette"),
				tuple(records.folder_A04, "Baleine")
		);

		assertThat(results.getRecordModifications(records.folder_A03).getImpacts()).isNotEmpty();
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

		assertThat(results.getRecordModifications(records.folder_A04).getImpacts()).isNotEmpty();
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

	//@Test
	public void givenValuesOfEveryTypeAreModifiedWithIdsThenAppliedAndShownInResults()
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
				.addModifiedMetadata(Folder.TITLE, "Mon dossier")
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

		BatchProcessResults results = presenterService.simulateWithIds(request);

		assertThat(results.getRecordModifications()).extracting("recordId", "recordTitle").containsOnly(
				tuple(records.folder_A03, "Alouette"),
				tuple(records.folder_A04, "Baleine")
		);

		assertThat(results.getRecordModifications(records.folder_A03).getImpacts()).hasSize(2);
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

		assertThat(results.getRecordModifications(records.folder_A04).getImpacts()).hasSize(2);
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

		assertThat(records.getFolder_A01().<String>get("subType")).isEqualTo("customSubType");
		assertThat(records.getFolder_A02().<String>get("subType")).isEqualTo("Dossier d'employé général");

		BatchProcessRequest request = new BatchProcessRequest().setUser(users.adminIn(zeCollection))
				.setQuery(new LogicalSearchQuery().setCondition(fromAllSchemasIn(zeCollection).where(Schemas.IDENTIFIER)
						.isIn(asList(records.folder_A01, records.folder_A02))))
				.addModifiedMetadata(Folder.TYPE, records.folderTypeMeeting().getId());

		Map<String, Object> modifications = new HashMap<>();
		modifications.put(Folder.DEFAULT_SCHEMA + "_" + Folder.TYPE, records.folderTypeMeeting().getId());
		request.setModifiedMetadatas(modifications);
		presenterService.execute(request, new ChangeValueOfMetadataBatchProcessAction(modifications), request.getQuery(),
				records.getAdmin(), "Edit records");

		waitForBatchProcess();
		assertThat(records.getFolder_A01().<String>get("subType")).isEqualTo("customSubType");
		assertThat(records.getFolder_A02().<String>get("subType")).isEqualTo("Meeting important");

		request = new BatchProcessRequest().setUser(users.adminIn(zeCollection))
				.setQuery(new LogicalSearchQuery().setCondition(fromAllSchemasIn(zeCollection).where(Schemas.IDENTIFIER)
						.isIn(asList(records.folder_A01, records.folder_A02))))
				.addModifiedMetadata(Folder.TYPE, records.folderTypeEmploye())
				.addModifiedMetadata("subType", "");

		BatchProcessResults results = presenterService.simulateWithQuery(request);

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

		assertThat(records.getFolder_A01().<String>get("subType")).isEqualTo("customSubType");
		assertThat(records.getFolder_A02().<String>get("subType")).isEqualTo("Meeting important");

	}

	//@Test
	public void whenChangingTypeWithIdsThenKeepValuesWithSharedField()
			throws Exception {

		Transaction transaction = new Transaction();
		transaction.add(rm.setType(records.getFolder_A01(), records.folderTypeEmploye())).set("subType", "customSubType")
				.setTitle("zetest");
		transaction.add(rm.setType(records.getFolder_A02(), records.folderTypeEmploye())).setTitle("zetest");
		getModelLayerFactory().newRecordServices().execute(transaction);

		assertThat(records.getFolder_A01().<String>get("subType")).isEqualTo("customSubType");
		assertThat(records.getFolder_A02().<String>get("subType")).isEqualTo("Dossier d'employé général");

		BatchProcessRequest request = new BatchProcessRequest().setUser(users.adminIn(zeCollection))
				.setIds(asList(records.folder_A01, records.folder_A02))
				.addModifiedMetadata(Folder.TYPE, records.folderTypeMeeting().getId());

		Map<String, Object> modifications = new HashMap<>();
		modifications.put(Folder.DEFAULT_SCHEMA + "_" + Folder.TYPE, records.folderTypeMeeting().getId());
		presenterService.execute(request, new ChangeValueOfMetadataBatchProcessAction(modifications), request.getIds(),
				records.getAdmin(), "Edit records");

		waitForBatchProcess();
		assertThat(records.getFolder_A01().<String>get("subType")).isEqualTo("customSubType");
		assertThat(records.getFolder_A02().<String>get("subType")).isEqualTo("Meeting important");

		request = new BatchProcessRequest().setUser(users.adminIn(zeCollection))
				.setIds(asList(records.folder_A01, records.folder_A02))
				.addModifiedMetadata(Folder.TYPE, records.folderTypeEmploye())
				.addModifiedMetadata("subType", "");

		BatchProcessResults results = presenterService.simulateWithIds(request);

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

		assertThat(records.getFolder_A01().<String>get("subType")).isEqualTo("customSubType");
		assertThat(records.getFolder_A02().<String>get("subType")).isEqualTo("Meeting important");
	}

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

		assertThat(presenterService.getOriginType(new LogicalSearchQuery().setCondition(
				fromAllSchemasIn(zeCollection).where(Schemas.IDENTIFIER)
						.isIn(asList(records.folder_A01, records.folder_A02, records.folder_A03,
								records.folder_A04, records.folder_A05, records.folder_A06))))).isNull();

		assertThat(presenterService.getOriginType(new LogicalSearchQuery().setCondition(
				fromAllSchemasIn(zeCollection).where(Schemas.IDENTIFIER).isIn(asList(records.folder_A04, records.folder_A06)))))
				.isNull();
		assertThat(presenterService.getOriginType(new LogicalSearchQuery().setCondition(
				fromAllSchemasIn(zeCollection).where(Schemas.IDENTIFIER).isIn(asList(records.folder_A05, records.folder_A06)))))
				.isNull();
		assertThat(presenterService.getOriginType(new LogicalSearchQuery().setCondition(
				fromAllSchemasIn(zeCollection).where(Schemas.IDENTIFIER)
						.isIn(asList(records.folder_A01, records.folder_A02, records.folder_A03))))).isNull();
		assertThat(presenterService.getOriginType(new LogicalSearchQuery().setCondition(
				fromAllSchemasIn(zeCollection).where(Schemas.IDENTIFIER)
						.isIn(asList(records.folder_A01, records.folder_A02, records.folder_A05))))).isNull();
		assertThat(presenterService.getOriginType(new LogicalSearchQuery().setCondition(
				fromAllSchemasIn(zeCollection).where(Schemas.IDENTIFIER)
						.isIn(asList(records.folder_A05, records.folder_A01, records.folder_A02))))).isNull();
		assertThat(presenterService.getOriginType(new LogicalSearchQuery()
				.setCondition(fromAllSchemasIn(zeCollection).where(Schemas.IDENTIFIER).isIn(asList(records.folder_A04)))))
				.isEqualTo(records.folderTypeOther().getId());
		assertThat(presenterService.getOriginType(new LogicalSearchQuery()
				.setCondition(fromAllSchemasIn(zeCollection).where(Schemas.IDENTIFIER).isIn(asList(records.folder_A03)))))
				.isEqualTo(records.folderTypeMeeting().getId());
		assertThat(presenterService.getOriginType(new LogicalSearchQuery().setCondition(
				fromAllSchemasIn(zeCollection).where(Schemas.IDENTIFIER).isIn(asList(records.folder_A01, records.folder_A02)))))
				.isEqualTo(records.folderTypeEmploye().getId());
	}

	@Test
	public void whenModifyingFoldersThenUserCanOnlyModifyThemIfItHasThePermission()
			throws Exception {

		AuthorizationsServices authServices = getModelLayerFactory().newAuthorizationsServices();

		Role role1 = new Role(zeCollection, "1", "1", asList(RMPermissionsTo.MODIFY_SEMIACTIVE_FOLDERS));
		Role role2 = new Role(zeCollection, "2", "2", asList(RMPermissionsTo.MODIFY_INACTIVE_FOLDERS));
		getModelLayerFactory().getRolesManager().addRole(role1);
		getModelLayerFactory().getRolesManager().addRole(role2);

		User admin = users.adminIn(zeCollection);
		User alice = users.aliceIn(zeCollection);

		assertThat(presenterService.hasWriteAccessOnAllRecords(admin, asList("A42", "A84", "A04", "C02"))).isTrue();
		assertThat(presenterService.hasWriteAccessOnAllRecords(alice, asList("A42"))).isFalse();
		assertThat(presenterService.hasWriteAccessOnAllRecords(alice, asList("A84"))).isFalse();
		assertThat(presenterService.hasWriteAccessOnAllRecords(alice, asList("A04"))).isFalse();
		assertThat(presenterService.hasWriteAccessOnAllRecords(alice, asList("C02"))).isFalse();

		authServices.add(authorizationForUsers(alice).on("A42").givingReadWriteAccess(), admin);
		authServices.add(authorizationForUsers(alice).on("A48").givingReadWriteAccess(), admin);
		authServices.add(authorizationForUsers(alice).on("A04").givingReadWriteAccess(), admin);
		authServices.add(authorizationForUsers(alice).on("A84").givingReadWriteAccess(), admin);
		authServices.add(authorizationForUsers(alice).on("A85").givingReadWriteAccess(), admin);

		waitForBatchProcess();
		alice = users.aliceIn(zeCollection);

		assertThat(presenterService.hasWriteAccessOnAllRecords(alice, asList("A04"))).isTrue();
		assertThat(presenterService.hasWriteAccessOnAllRecords(alice, asList("A42"))).isFalse();
		assertThat(presenterService.hasWriteAccessOnAllRecords(alice, asList("A84"))).isFalse();
		assertThat(presenterService.hasWriteAccessOnAllRecords(alice, asList("C02"))).isFalse();
		assertThat(presenterService.hasWriteAccessOnAllRecords(alice, asList("A42", "A84"))).isFalse();

		authServices.add(authorizationForUsers(alice).on("A42").giving(role1), admin);
		authServices.add(authorizationForUsers(alice).on("A84").giving(role1), admin);
		waitForBatchProcess();
		alice = users.aliceIn(zeCollection);

		assertThat(presenterService.hasWriteAccessOnAllRecords(alice, asList("A04"))).isTrue();
		assertThat(presenterService.hasWriteAccessOnAllRecords(alice, asList("A42"))).isTrue();
		assertThat(presenterService.hasWriteAccessOnAllRecords(alice, asList("A48"))).isFalse();
		assertThat(presenterService.hasWriteAccessOnAllRecords(alice, asList("A84"))).isFalse();
		assertThat(presenterService.hasWriteAccessOnAllRecords(alice, asList("A85"))).isFalse();
		assertThat(presenterService.hasWriteAccessOnAllRecords(alice, asList("A42", "A48"))).isFalse();

		authServices.add(authorizationForUsers(alice).on("A42").giving(role2), admin);
		authServices.add(authorizationForUsers(alice).on("A84").giving(role2), admin);
		waitForBatchProcess();
		alice = users.aliceIn(zeCollection);

		assertThat(presenterService.hasWriteAccessOnAllRecords(alice, asList("A42"))).isTrue();
		assertThat(presenterService.hasWriteAccessOnAllRecords(alice, asList("A48"))).isFalse();
		assertThat(presenterService.hasWriteAccessOnAllRecords(alice, asList("A84"))).isTrue();
		assertThat(presenterService.hasWriteAccessOnAllRecords(alice, asList("A85"))).isFalse();
		assertThat(presenterService.hasWriteAccessOnAllRecords(alice, asList("A42", "A84"))).isTrue();
		assertThat(presenterService.hasWriteAccessOnAllRecords(alice, asList("A84", "A85"))).isFalse();
	}

	@Test
	public void whenModifyingDocumentsThenUserCanOnlyModifyThemIfItHasThePermission()
			throws Exception {

		Transaction transaction = new Transaction();
		transaction.add(rm.newDocumentWithId("dA04")).setFolder("A04").setTitle("dA04");
		transaction.add(rm.newDocumentWithId("dA42")).setFolder("A42").setTitle("dA42");
		transaction.add(rm.newDocumentWithId("dA48")).setFolder("A48").setTitle("dA48");
		transaction.add(rm.newDocumentWithId("dA84")).setFolder("A84").setTitle("dA84");
		transaction.add(rm.newDocumentWithId("dA85")).setFolder("A85").setTitle("dA85");
		transaction.add(rm.newDocumentWithId("dC02")).setFolder("C02").setTitle("dC02");

		getModelLayerFactory().newRecordServices().execute(transaction);

		AuthorizationsServices authServices = getModelLayerFactory().newAuthorizationsServices();

		Role role1 = new Role(zeCollection, "1", "1", asList(RMPermissionsTo.MODIFY_SEMIACTIVE_DOCUMENT));
		Role role2 = new Role(zeCollection, "2", "2", asList(RMPermissionsTo.MODIFY_INACTIVE_DOCUMENT));
		getModelLayerFactory().getRolesManager().addRole(role1);
		getModelLayerFactory().getRolesManager().addRole(role2);

		User admin = users.adminIn(zeCollection);
		User alice = users.aliceIn(zeCollection);

		assertThat(presenterService.hasWriteAccessOnAllRecords(admin, asList("dA42", "dA84", "dA04", "dC02"))).isTrue();
		assertThat(presenterService.hasWriteAccessOnAllRecords(alice, asList("dA42"))).isFalse();
		assertThat(presenterService.hasWriteAccessOnAllRecords(alice, asList("dA84"))).isFalse();
		assertThat(presenterService.hasWriteAccessOnAllRecords(alice, asList("dA04"))).isFalse();
		assertThat(presenterService.hasWriteAccessOnAllRecords(alice, asList("dC02"))).isFalse();

		authServices.add(authorizationForUsers(alice).on("dA42").givingReadWriteAccess(), admin);
		authServices.add(authorizationForUsers(alice).on("dA48").givingReadWriteAccess(), admin);
		authServices.add(authorizationForUsers(alice).on("dA04").givingReadWriteAccess(), admin);
		authServices.add(authorizationForUsers(alice).on("dA84").givingReadWriteAccess(), admin);
		authServices.add(authorizationForUsers(alice).on("dA85").givingReadWriteAccess(), admin);
		waitForBatchProcess();
		alice = users.aliceIn(zeCollection);

		assertThat(presenterService.hasWriteAccessOnAllRecords(alice, asList("dA04"))).isTrue();
		assertThat(presenterService.hasWriteAccessOnAllRecords(alice, asList("dA42"))).isFalse();
		assertThat(presenterService.hasWriteAccessOnAllRecords(alice, asList("dA84"))).isFalse();
		assertThat(presenterService.hasWriteAccessOnAllRecords(alice, asList("dC02"))).isFalse();
		assertThat(presenterService.hasWriteAccessOnAllRecords(alice, asList("dA42", "dA84"))).isFalse();

		authServices.add(authorizationForUsers(alice).on("dA42").giving(role1), admin);
		authServices.add(authorizationForUsers(alice).on("dA84").giving(role1), admin);
		waitForBatchProcess();
		alice = users.aliceIn(zeCollection);

		assertThat(presenterService.hasWriteAccessOnAllRecords(alice, asList("dA04"))).isTrue();
		assertThat(presenterService.hasWriteAccessOnAllRecords(alice, asList("dA42"))).isTrue();
		assertThat(presenterService.hasWriteAccessOnAllRecords(alice, asList("dA48"))).isFalse();
		assertThat(presenterService.hasWriteAccessOnAllRecords(alice, asList("dA84"))).isFalse();
		assertThat(presenterService.hasWriteAccessOnAllRecords(alice, asList("dA85"))).isFalse();
		assertThat(presenterService.hasWriteAccessOnAllRecords(alice, asList("dA42", "dA48"))).isFalse();

		authServices.add(authorizationForUsers(alice).on("dA42").giving(role2), admin);
		authServices.add(authorizationForUsers(alice).on("dA84").giving(role2), admin);
		waitForBatchProcess();
		alice = users.aliceIn(zeCollection);

		assertThat(presenterService.hasWriteAccessOnAllRecords(alice, asList("dA42"))).isTrue();
		assertThat(presenterService.hasWriteAccessOnAllRecords(alice, asList("dA48"))).isFalse();
		assertThat(presenterService.hasWriteAccessOnAllRecords(alice, asList("dA84"))).isTrue();
		assertThat(presenterService.hasWriteAccessOnAllRecords(alice, asList("dA85"))).isFalse();
		assertThat(presenterService.hasWriteAccessOnAllRecords(alice, asList("dA42", "dA84"))).isTrue();
		assertThat(presenterService.hasWriteAccessOnAllRecords(alice, asList("dA84", "dA85"))).isFalse();

	}
}
