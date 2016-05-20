package com.constellio.app.modules.rm.extensions;

import static com.constellio.model.frameworks.validation.Validator.METADATA_CODE;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;

import com.constellio.app.modules.rm.RMConfigs;
import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.model.enums.CopyType;
import com.constellio.app.modules.rm.model.enums.FolderStatus;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.services.decommissioning.DecommissioningService;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.RetentionRule;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.frameworks.validation.ValidationError;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.records.RecordServicesException.ValidationException;
import com.constellio.model.services.search.SearchServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.setups.Users;

public class FolderExtensionAcceptanceTest extends ConstellioTest {
	Users users = new Users();
	DecommissioningService service;
	RMSchemasRecordsServices rm;
	RMTestRecords records = new RMTestRecords(zeCollection);
	RecordServices recordServices;
	SearchServices searchServices;

	RetentionRule ruleResponsible_withResponsible;
	RetentionRule ruleUnits_withAdministrativeUnitsAndAdminUnits10_20;
	RetentionRule ruleBoth_withResponsibleAndAdministrativeUnitsAndAdminUnits10_20;

	User bobInAdminUnit10AndOthers, aliceInNoAdminUnit, edouardInSubAdminUnit10AndOthers;

	Folder folderWithRuleUnitsAndAdminUnit10_WithCreatorInAdminUnit10AndOthers;
	Folder folderWithRuleResponsible_WithCreatorInNoAdminUnit;
	Folder folderWithRuleBoth_WithCreatorInNoAdminUnit;
	Folder folderWithRuleBoth_WithCreatorInAdminUnit10AndOthers;
	Folder folderWithRuleBoth_WithCreatorInSubAdminUnit10AndOthers;

	@Before
	public void setUp()
			throws Exception {

		prepareSystem(
				withZeCollection().withConstellioRMModule().withAllTestUsers().withRMTest(records)
						.withFoldersAndContainersOfEveryStatus().withAllTestUsers()
		);

		rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
		service = new DecommissioningService(zeCollection, getModelLayerFactory());
		recordServices = getModelLayerFactory().newRecordServices();
		searchServices = getModelLayerFactory().newSearchServices();
		users.setUp(getModelLayerFactory().newUserServices());
	}

	@Test
	public void givenNewlyCreatedFolderPermissionStatusIsSetToArchivisticStatus()
			throws Exception {
		Folder folder = saveAndReloadFolder(getTestFolder());
		assertThat(folder.getPermissionStatus()).isEqualTo(FolderStatus.ACTIVE);

		folder = saveAndReloadFolder(getTestFolder().setActualTransferDate(aDate()));
		assertThat(folder.getPermissionStatus()).isEqualTo(FolderStatus.SEMI_ACTIVE);

		folder = saveAndReloadFolder(getTestFolder().setActualDepositDate(aDate()));
		assertThat(folder.getPermissionStatus()).isEqualTo(FolderStatus.INACTIVE_DEPOSITED);

		folder = saveAndReloadFolder(getTestFolder().setActualDestructionDate(aDate()));
		assertThat(folder.getPermissionStatus()).isEqualTo(FolderStatus.INACTIVE_DESTROYED);
	}

	@Test
	public void givenUpdatedFolderWhenTheArchivisticStatusChangesThenChangeThePermissionStatus()
			throws Exception {
		Folder folder = saveAndReloadFolder(getTestFolder());
		folder = saveAndReloadFolder(folder.setActualTransferDate(aDate()));
		assertThat(folder.getPermissionStatus()).isEqualTo(FolderStatus.SEMI_ACTIVE);
	}

	@Test
	public void givenUpdatedFolderWhenTheArchivisticStatusDoesNotChangeThenKeepThePermissionStatus()
			throws Exception {
		Folder folder = saveAndReloadFolder(getTestFolder());
		// DecommissioningList is approved
		folder = saveAndReloadFolder(folder.setPermissionStatus(FolderStatus.SEMI_ACTIVE));
		assertThat(folder.getPermissionStatus()).isEqualTo(FolderStatus.SEMI_ACTIVE);
		// Folder is edited afterwards
		folder = saveAndReloadFolder(folder.setTitle("modified"));
		assertThat(folder.getPermissionStatus()).isEqualTo(FolderStatus.SEMI_ACTIVE);
	}

	public Folder getTestFolder() {
		return newFolder("Test").setOpenDate(aDate()).setRetentionRuleEntered(records.getRule1())
				.setAdministrativeUnitEntered(records.getUnit10a());
	}

	@Test
	public void givenOpenHolderWhenSaveFolderThenStatusEnteredIsCorrectlySet()
			throws Exception {
		givenConfig(RMConfigs.OPEN_HOLDER, true);
		initTestData(true);

		folderWithRuleUnitsAndAdminUnit10_WithCreatorInAdminUnit10AndOthers = saveAndReloadFolder(
				folderWithRuleUnitsAndAdminUnit10_WithCreatorInAdminUnit10AndOthers);
		assertThatFolderIsPrincipalCopy(folderWithRuleUnitsAndAdminUnit10_WithCreatorInAdminUnit10AndOthers);
		try {
			saveAndReloadFolder(
					folderWithRuleResponsible_WithCreatorInNoAdminUnit);
		} catch (ValidationException e) {
			assertErrorIsOnCopyStatus(e);
		}
		try {
			saveAndReloadFolder(folderWithRuleBoth_WithCreatorInNoAdminUnit);
		} catch (ValidationException e) {
			assertErrorIsOnCopyStatus(e);
		}

		folderWithRuleBoth_WithCreatorInAdminUnit10AndOthers = saveAndReloadFolder(
				folderWithRuleBoth_WithCreatorInAdminUnit10AndOthers);
		assertThatFolderIsPrincipalCopy(folderWithRuleBoth_WithCreatorInAdminUnit10AndOthers);

		folderWithRuleBoth_WithCreatorInSubAdminUnit10AndOthers = saveAndReloadFolder(
				folderWithRuleBoth_WithCreatorInSubAdminUnit10AndOthers);
		assertThatFolderIsPrincipalCopy(folderWithRuleBoth_WithCreatorInSubAdminUnit10AndOthers);
	}

	@Test
	public void givenOpenHolderNotActivatedWhenSaveFolderThenOldBehaviour()
			throws Exception {
		givenConfig(RMConfigs.OPEN_HOLDER, false);
		initTestData(false);

		folderWithRuleUnitsAndAdminUnit10_WithCreatorInAdminUnit10AndOthers = saveAndReloadFolder(
				folderWithRuleUnitsAndAdminUnit10_WithCreatorInAdminUnit10AndOthers);
		assertThatFolderIsPrincipalCopy(folderWithRuleUnitsAndAdminUnit10_WithCreatorInAdminUnit10AndOthers);
		try {
			saveAndReloadFolder(
					folderWithRuleResponsible_WithCreatorInNoAdminUnit);
		} catch (ValidationException e) {
			assertErrorIsOnCopyStatus(e);
		}
	}

	private void assertErrorIsOnCopyStatus(ValidationException e) {
		List<ValidationError> errors = e.getErrors()
				.getValidationErrors();
		assertThat(errors.size()).isEqualTo(1);
		assertThat((String)errors.get(0).getParameters().get(METADATA_CODE)).endsWith("_" + Folder.COPY_STATUS);
	}

	private void assertThatFolderIsPrincipalCopy(Folder folder) {
		assertThat(folder.getCopyStatus()).isEqualTo(CopyType.PRINCIPAL);
	}

	private void initTestData(boolean openHolderActive)
			throws RecordServicesException, InterruptedException {
		ruleResponsible_withResponsible = records.getRule2();
		ruleUnits_withAdministrativeUnitsAndAdminUnits10_20 = records.getRule1();
		ruleBoth_withResponsibleAndAdministrativeUnitsAndAdminUnits10_20 = records.getRule3();
		if (openHolderActive) {
			recordServices.add(ruleBoth_withResponsibleAndAdministrativeUnitsAndAdminUnits10_20
					.setAdministrativeUnits(ruleUnits_withAdministrativeUnitsAndAdminUnits10_20.getAdministrativeUnits())
					.getWrappedRecord());
		}
		waitForBatchProcess();

		bobInAdminUnit10AndOthers = users.bobIn(zeCollection);
		aliceInNoAdminUnit = users.aliceIn(zeCollection);
		edouardInSubAdminUnit10AndOthers = users.edouardIn(zeCollection);

		folderWithRuleUnitsAndAdminUnit10_WithCreatorInAdminUnit10AndOthers = (Folder) newFolder("1")
				.setRetentionRuleEntered(ruleUnits_withAdministrativeUnitsAndAdminUnits10_20).setCreatedBy(
						bobInAdminUnit10AndOthers.getId());
		folderWithRuleResponsible_WithCreatorInNoAdminUnit = (Folder) newFolder("2")
				.setRetentionRuleEntered(ruleResponsible_withResponsible).setCreatedBy(
						aliceInNoAdminUnit.getId());
		folderWithRuleBoth_WithCreatorInNoAdminUnit = (Folder) newFolder("3")
				.setRetentionRuleEntered(ruleBoth_withResponsibleAndAdministrativeUnitsAndAdminUnits10_20).setCreatedBy(
						aliceInNoAdminUnit.getId());
		folderWithRuleBoth_WithCreatorInAdminUnit10AndOthers = (Folder) newFolder("4")
				.setRetentionRuleEntered(ruleBoth_withResponsibleAndAdministrativeUnitsAndAdminUnits10_20).setCreatedBy(
						bobInAdminUnit10AndOthers.getId());
		folderWithRuleBoth_WithCreatorInSubAdminUnit10AndOthers = (Folder) newFolder("5")
				.setRetentionRuleEntered(ruleBoth_withResponsibleAndAdministrativeUnitsAndAdminUnits10_20).setCreatedBy(
						edouardInSubAdminUnit10AndOthers.getId());

	}

	private Folder saveAndReloadFolder(Folder folder)
			throws RecordServicesException {
		recordServices.add(folder.getWrappedRecord());
		return rm.getFolder(folder.getId());
	}

	private Folder newFolder(String title) {
		return rm.newFolder().setTitle(title).setOpenDate(LocalDate.now())
				.setAdministrativeUnitEntered(records.unitId_10a)
				.setCategoryEntered(records.categoryId_X110);
	}
}
