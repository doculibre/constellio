package com.constellio.app.modules.rm.services.decommissioning;

import com.constellio.app.modules.rm.RMConfigs;
import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.RetentionRule;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.search.SearchServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.setups.Users;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;

import static com.constellio.app.modules.rm.model.enums.CopyType.PRINCIPAL;
import static org.assertj.core.api.Assertions.assertThat;

public class DecommissioningService_openHolder_AcceptTest extends ConstellioTest {
	Users users = new Users();
	DecommissioningService service;
	RMSchemasRecordsServices rm;
	RMTestRecords records = new RMTestRecords(zeCollection);
	RecordServices recordServices;
	SearchServices searchServices;

	RetentionRule ruleResponsible_withResponsible;
	RetentionRule ruleUnits_withAdministrativeUnitsAndAdminUnits10_20;
	RetentionRule ruleBoth_withResponsibleAndAdministrativeUnitsAndAdminUnits10_20;

	User bobInAdminUnit10AndOthers, aliceInNoAdminUnit, edouardInSunAdminUnit10AndOthers;

	Folder folderInCreationWithRuleUnits_WithCreatorInAdminUnit10AndOthers;
	Folder folderInCreationWithRuleUnits_WithCreatorInSubAdminUnit10AndOthers;
	Folder folderInCreationWithRuleResponsible_WithCreatorInNoAdminUnit;
	Folder folderInCreationWithRuleBoth_WithCreatorInNoAdminUnit;
	Folder folderInCreationWithRuleBoth_WithCreatorInAdminUnit10AndOthers;
	Folder folderInCreationWithRuleBoth_WithCreatorInSubAdminUnit10AndOthers;

	Folder folderInModificationWithRuleUnits_WithCreatorInAdminUnit10AndOthers;
	Folder folderInModificationWithRuleUnits_WithCreatorInSubAdminUnit10AndOthers;
	Folder folderInModificationWithRuleResponsible_WithCreatorInNoAdminUnit;
	Folder folderInModificationWithRuleBoth_WithCreatorInNoAdminUnit;
	Folder folderInModificationWithRuleBoth_WithCreatorInAdminUnit10AndOthers;
	Folder folderInModificationWithRuleBoth_WithCreatorInSubAdminUnit10AndOthers;

	@Before
	public void setUp()
			throws Exception {

		prepareSystem(
				withZeCollection().withConstellioRMModule().withAllTestUsers().withRMTest(records)
						.withFoldersAndContainersOfEveryStatus().withAllTestUsers()
		);

		rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
		service = new DecommissioningService(zeCollection, getAppLayerFactory());
		recordServices = getModelLayerFactory().newRecordServices();
		searchServices = getModelLayerFactory().newSearchServices();
		users.setUp(getModelLayerFactory().newUserServices(), zeCollection);

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
		edouardInSunAdminUnit10AndOthers = users.edouardIn(zeCollection);

		folderInCreationWithRuleUnits_WithCreatorInAdminUnit10AndOthers = (Folder) newFolder("1")
				.setRetentionRuleEntered(ruleUnits_withAdministrativeUnitsAndAdminUnits10_20).setCreatedBy(
						bobInAdminUnit10AndOthers.getId());
		folderInCreationWithRuleUnits_WithCreatorInSubAdminUnit10AndOthers = (Folder) newFolder("1")
				.setRetentionRuleEntered(ruleUnits_withAdministrativeUnitsAndAdminUnits10_20).setCreatedBy(
						edouardInSunAdminUnit10AndOthers.getId());
		folderInCreationWithRuleResponsible_WithCreatorInNoAdminUnit = (Folder) newFolder("2")
				.setRetentionRuleEntered(ruleResponsible_withResponsible).setCreatedBy(
						aliceInNoAdminUnit.getId());
		folderInCreationWithRuleBoth_WithCreatorInNoAdminUnit = (Folder) newFolder("3")
				.setRetentionRuleEntered(ruleBoth_withResponsibleAndAdministrativeUnitsAndAdminUnits10_20).setCreatedBy(
						aliceInNoAdminUnit.getId());
		folderInCreationWithRuleBoth_WithCreatorInAdminUnit10AndOthers = (Folder) newFolder("4")
				.setRetentionRuleEntered(ruleBoth_withResponsibleAndAdministrativeUnitsAndAdminUnits10_20).setCreatedBy(
						bobInAdminUnit10AndOthers.getId());
		folderInCreationWithRuleBoth_WithCreatorInSubAdminUnit10AndOthers = (Folder) newFolder("4")
				.setRetentionRuleEntered(ruleBoth_withResponsibleAndAdministrativeUnitsAndAdminUnits10_20).setCreatedBy(
						edouardInSunAdminUnit10AndOthers.getId());

		folderInModificationWithRuleUnits_WithCreatorInAdminUnit10AndOthers = saveAndReloadFolder((Folder) newFolder("1M")
				.setRetentionRuleEntered(ruleUnits_withAdministrativeUnitsAndAdminUnits10_20).setCreatedBy(
						bobInAdminUnit10AndOthers.getId()));
		folderInModificationWithRuleUnits_WithCreatorInSubAdminUnit10AndOthers = saveAndReloadFolder((Folder) newFolder("1M")
				.setRetentionRuleEntered(ruleUnits_withAdministrativeUnitsAndAdminUnits10_20).setCreatedBy(
						edouardInSunAdminUnit10AndOthers.getId()));
		folderInModificationWithRuleResponsible_WithCreatorInNoAdminUnit = saveAndReloadFolder(
				(Folder) newFolder("2M").setRetentionRuleEntered(ruleResponsible_withResponsible).setCopyStatusEntered(PRINCIPAL)
						.setCreatedBy(aliceInNoAdminUnit.getId()));
		folderInModificationWithRuleBoth_WithCreatorInNoAdminUnit = saveAndReloadFolder(
				(Folder) newFolder("3M").setCopyStatusEntered(PRINCIPAL)
						.setRetentionRuleEntered(ruleBoth_withResponsibleAndAdministrativeUnitsAndAdminUnits10_20).setCreatedBy(
								aliceInNoAdminUnit.getId()));
		folderInModificationWithRuleBoth_WithCreatorInAdminUnit10AndOthers = saveAndReloadFolder(
				(Folder) newFolder("4M").setCopyStatusEntered(PRINCIPAL)
						.setRetentionRuleEntered(ruleBoth_withResponsibleAndAdministrativeUnitsAndAdminUnits10_20).setCreatedBy(
								bobInAdminUnit10AndOthers.getId()));
		folderInModificationWithRuleBoth_WithCreatorInSubAdminUnit10AndOthers = saveAndReloadFolder(
				(Folder) newFolder("4M").setCopyStatusEntered(PRINCIPAL)
						.setRetentionRuleEntered(ruleBoth_withResponsibleAndAdministrativeUnitsAndAdminUnits10_20).setCreatedBy(
								edouardInSunAdminUnit10AndOthers.getId()));
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

	@Test
	public void givenOpenHolderAndCopyRuleTypeAlwaysModifiableWhenDeterminingIfCopyTypeVisibleThenAlwaysTrue()
			throws Exception {
		givenConfig(RMConfigs.COPY_RULE_TYPE_ALWAYS_MODIFIABLE, true);
		givenConfig(RMConfigs.OPEN_HOLDER, true);
		initTestData(true);

		assertThat(service.isCopyStatusInputPossible(folderInCreationWithRuleUnits_WithCreatorInAdminUnit10AndOthers,
				bobInAdminUnit10AndOthers)).isTrue();
		assertThat(service.isCopyStatusInputPossible(folderInCreationWithRuleUnits_WithCreatorInSubAdminUnit10AndOthers,
				edouardInSunAdminUnit10AndOthers)).isTrue();
		assertThat(service.isCopyStatusInputPossible(folderInCreationWithRuleResponsible_WithCreatorInNoAdminUnit,
				aliceInNoAdminUnit)).isTrue();
		assertThat(service.isCopyStatusInputPossible(folderInCreationWithRuleBoth_WithCreatorInNoAdminUnit, aliceInNoAdminUnit))
				.isTrue();
		assertThat(service.isCopyStatusInputPossible(folderInCreationWithRuleBoth_WithCreatorInAdminUnit10AndOthers,
				bobInAdminUnit10AndOthers)).isTrue();
		assertThat(service.isCopyStatusInputPossible(folderInCreationWithRuleBoth_WithCreatorInSubAdminUnit10AndOthers,
				edouardInSunAdminUnit10AndOthers)).isTrue();

		assertThat(service.isCopyStatusInputPossible(folderInModificationWithRuleUnits_WithCreatorInAdminUnit10AndOthers,
				bobInAdminUnit10AndOthers)).isTrue();
		assertThat(service.isCopyStatusInputPossible(folderInModificationWithRuleUnits_WithCreatorInSubAdminUnit10AndOthers,
				edouardInSunAdminUnit10AndOthers)).isTrue();
		assertThat(service.isCopyStatusInputPossible(folderInModificationWithRuleResponsible_WithCreatorInNoAdminUnit,
				aliceInNoAdminUnit)).isTrue();
		assertThat(service.isCopyStatusInputPossible(folderInModificationWithRuleBoth_WithCreatorInNoAdminUnit, aliceInNoAdminUnit))
				.isTrue();
		assertThat(service.isCopyStatusInputPossible(folderInModificationWithRuleBoth_WithCreatorInAdminUnit10AndOthers,
				bobInAdminUnit10AndOthers))
				.isTrue();
		assertThat(service.isCopyStatusInputPossible(folderInModificationWithRuleBoth_WithCreatorInSubAdminUnit10AndOthers,
				edouardInSunAdminUnit10AndOthers)).isTrue();

	}

	@Test
	public void givenOpenHolderAndCopyRuleTypeNOTAlwaysModifiableWhenDeterminingIfCopyTypeVisibleThenNotAlwaysTrue()
			throws Exception {
		givenConfig(RMConfigs.COPY_RULE_TYPE_ALWAYS_MODIFIABLE, false);
		givenConfig(RMConfigs.OPEN_HOLDER, true);
		initTestData(true);

		assertThat(service.isCopyStatusInputPossible(folderInCreationWithRuleUnits_WithCreatorInAdminUnit10AndOthers,
				bobInAdminUnit10AndOthers)).isFalse();
		assertThat(service.isCopyStatusInputPossible(folderInCreationWithRuleUnits_WithCreatorInSubAdminUnit10AndOthers,
				edouardInSunAdminUnit10AndOthers)).isFalse();
		assertThat(service.isCopyStatusInputPossible(folderInCreationWithRuleResponsible_WithCreatorInNoAdminUnit,
				aliceInNoAdminUnit)).isTrue();
		assertThat(service.isCopyStatusInputPossible(folderInCreationWithRuleBoth_WithCreatorInNoAdminUnit, aliceInNoAdminUnit))
				.isTrue();
		assertThat(service.isCopyStatusInputPossible(folderInCreationWithRuleBoth_WithCreatorInAdminUnit10AndOthers,
				bobInAdminUnit10AndOthers)).isFalse();
		assertThat(service.isCopyStatusInputPossible(folderInCreationWithRuleBoth_WithCreatorInSubAdminUnit10AndOthers,
				edouardInSunAdminUnit10AndOthers)).isFalse();

		assertThat(service.isCopyStatusInputPossible(folderInModificationWithRuleUnits_WithCreatorInAdminUnit10AndOthers,
				bobInAdminUnit10AndOthers))
				.isFalse();
		assertThat(service.isCopyStatusInputPossible(folderInModificationWithRuleUnits_WithCreatorInSubAdminUnit10AndOthers,
				edouardInSunAdminUnit10AndOthers)).isFalse();
		assertThat(service.isCopyStatusInputPossible(folderInModificationWithRuleResponsible_WithCreatorInNoAdminUnit,
				aliceInNoAdminUnit)).isTrue();
		assertThat(
				service.isCopyStatusInputPossible(folderInModificationWithRuleBoth_WithCreatorInNoAdminUnit, aliceInNoAdminUnit))
				.isTrue();
		assertThat(service.isCopyStatusInputPossible(folderInModificationWithRuleBoth_WithCreatorInAdminUnit10AndOthers,
				bobInAdminUnit10AndOthers))
				.isTrue();
		assertThat(service.isCopyStatusInputPossible(folderInModificationWithRuleBoth_WithCreatorInSubAdminUnit10AndOthers,
				edouardInSunAdminUnit10AndOthers)).isTrue();
	}

	@Test
	public void givenOpenHolderInactiveAndCopyRuleTypeNOTAlwaysModifiableWhenDeterminingIfCopyTypeVisibleThenNotAlwaysTrue()
			throws Exception {
		givenConfig(RMConfigs.COPY_RULE_TYPE_ALWAYS_MODIFIABLE, false);
		givenConfig(RMConfigs.OPEN_HOLDER, false);
		initTestData(false);

		assertThat(service.isCopyStatusInputPossible(folderInCreationWithRuleUnits_WithCreatorInAdminUnit10AndOthers)).isFalse();
		assertThat(service.isCopyStatusInputPossible(folderInCreationWithRuleResponsible_WithCreatorInNoAdminUnit)).isTrue();
		//not possible
		//assertThat(service.isCopyStatusInputPossible(folderInCreationWithRuleBoth_WithCreatorInNoAdminUnit)).isTrue();
		//assertThat(service.isCopyStatusInputPossible(folderInCreationWithRuleBoth_WithCreatorInAdminUnit10AndOthers)).isTrue();

		assertThat(service.isCopyStatusInputPossible(folderInModificationWithRuleUnits_WithCreatorInAdminUnit10AndOthers))
				.isFalse();
		assertThat(service.isCopyStatusInputPossible(folderInModificationWithRuleResponsible_WithCreatorInNoAdminUnit)).isTrue();
		//not possible
		//assertThat(service.isCopyStatusInputPossible(folderInModificationWithRuleBoth_WithCreatorInNoAdminUnit)).isTrue();
		//assertThat(service.isCopyStatusInputPossible(folderInModificationWithRuleBoth_WithCreatorInAdminUnit10AndOthers)).isFalse();
	}
}
