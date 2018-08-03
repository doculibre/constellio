package com.constellio.app.modules.rm.services.decommissioning;

import com.constellio.app.modules.rm.RMConfigs;
import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.model.CopyRetentionRule;
import com.constellio.app.modules.rm.model.CopyRetentionRuleBuilder;
import com.constellio.app.modules.rm.model.enums.DecommissioningType;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.RetentionRule;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.records.RecordServices;
import com.constellio.sdk.tests.ConstellioTest;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

public class DecommissioningServiceAcceptTest extends ConstellioTest {
	DecommissioningService service;
	RMSchemasRecordsServices rm;
	RecordServices recordServices;
	RMTestRecords records = new RMTestRecords(zeCollection);
	String bobId, chuckId, aliceId;

	CopyRetentionRuleBuilder copyBuilder = CopyRetentionRuleBuilder.UUID();

	@Before
	public void setUp()
			throws Exception {

		prepareSystem(
				withZeCollection().withConstellioRMModule().withAllTestUsers().withRMTest(records)
						.withFoldersAndContainersOfEveryStatus()
		);

		rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
		service = new DecommissioningService(zeCollection, getAppLayerFactory());
		recordServices = getModelLayerFactory().newRecordServices();
		bobId = records.getBob_userInAC().getId();
		chuckId = records.getChuckNorris().getId();
		aliceId = records.getAlice().getId();
	}

	@Test
	public void whenHasFolderToDepositThenOk() {
		assertThat(service.hasFolderToDeposit(records.getContainerBac01())).isTrue();
		assertThat(service.hasFolderToDeposit(records.getContainerBac13())).isFalse();
	}

	@Test
	public void whenGetMediumTypesOfContainerThenOk() {
		assertThat(service.getMediumTypesOf(records.getContainerBac11())).containsOnly(rm.PA(), rm.DM());
	}

	@Test
	public void whenGetDispositionYearThenOk() {
		givenConfig(RMConfigs.POPULATE_BORDEREAUX_WITH_LESSER_DISPOSITION_DATE, true);
		assertThat(service.getDispositionDate(records.getContainerBac10())).isEqualTo(new LocalDate(2007, 10, 31));

		givenConfig(RMConfigs.POPULATE_BORDEREAUX_WITH_LESSER_DISPOSITION_DATE, false);
		assertThat(service.getDispositionDate(records.getContainerBac10())).isEqualTo(new LocalDate(2009, 10, 31));
	}

	@Test
	public void givenPopulateBordereauxWithLesserDispositionDateWhenGetDispositonDateAndMinimumDateIsDestructionDateThenReturnDestructionDate() {
		Folder folderA01 = records.getFolder_A01();
		folderA01.setActualDepositDate(new LocalDate(2008, 10, 31));
		folderA01.setActualDestructionDate(new LocalDate(2006, 10, 31));
		folderA01.setActualTransferDate(new LocalDate(2007, 10, 31));
		givenConfig(RMConfigs.POPULATE_BORDEREAUX_WITH_LESSER_DISPOSITION_DATE, true);

		assertThat(service.getDispositionDate(folderA01.getWrappedRecord())).isEqualTo(new LocalDate(2006, 10, 31));
	}

	@Test
	public void givenPopulateBordereauxWithLesserDispositionDateWhenGetDispositonDateAndMinimumDateIsDepositDateThenReturnDepositDate() {
		Folder folderA01 = records.getFolder_A01();
		folderA01.setActualDepositDate(new LocalDate(2006, 8, 31));
		folderA01.setActualDestructionDate(new LocalDate(2006, 10, 31));
		folderA01.setActualTransferDate(new LocalDate(2007, 10, 31));
		givenConfig(RMConfigs.POPULATE_BORDEREAUX_WITH_LESSER_DISPOSITION_DATE, true);

		assertThat(service.getDispositionDate(folderA01.getWrappedRecord())).isEqualTo(new LocalDate(2006, 8, 31));
	}

	@Test
	public void givenPopulateBordereauxWithLesserDispositionDateWhenGetDispositonDateAndMinimumDateIsTransferDateThenReturnTransferDate() {
		Folder folderA01 = records.getFolder_A01();
		folderA01.setActualDepositDate(new LocalDate(2008, 10, 31));
		folderA01.setActualDestructionDate(new LocalDate(2006, 10, 31));
		folderA01.setActualTransferDate(new LocalDate(2005, 10, 31));
		givenConfig(RMConfigs.POPULATE_BORDEREAUX_WITH_LESSER_DISPOSITION_DATE, true);

		assertThat(service.getDispositionDate(folderA01.getWrappedRecord())).isEqualTo(new LocalDate(2006, 10, 31));
	}

	@Test
	public void givenPopulateBordereauxWithGreaterDispositionDateWhenGetDispositonDateAndMaximumDateIsDestructionDateThenReturnDestructionDate() {
		Folder folderA01 = records.getFolder_A01();
		folderA01.setActualDepositDate(new LocalDate(2008, 10, 31));
		folderA01.setActualDestructionDate(new LocalDate(2009, 10, 31));
		folderA01.setActualTransferDate(new LocalDate(2007, 10, 31));

		givenConfig(RMConfigs.POPULATE_BORDEREAUX_WITH_LESSER_DISPOSITION_DATE, false);

		assertThat(service.getDispositionDate(folderA01.getWrappedRecord())).isEqualTo(new LocalDate(2009, 10, 31));
	}

	@Test
	public void givenPopulateBordereauxWithGreaterDispositionDateWhenGetDispositonDateAndMaximumDateIsDepositDateThenReturnDepositDate() {
		Folder folderA01 = records.getFolder_A01();
		folderA01.setActualDepositDate(new LocalDate(2009, 12, 31));
		folderA01.setActualDestructionDate(new LocalDate(2009, 10, 31));
		folderA01.setActualTransferDate(new LocalDate(2007, 10, 31));

		givenConfig(RMConfigs.POPULATE_BORDEREAUX_WITH_LESSER_DISPOSITION_DATE, false);

		assertThat(service.getDispositionDate(folderA01.getWrappedRecord())).isEqualTo(new LocalDate(2009, 12, 31));
	}

	@Test
	public void givenPopulateBordereauxWithGreaterDispositionDateWhenGetDispositonDateAndMaximumDateIsTransferDateThenReturnTransferDate() {
		Folder folderA01 = records.getFolder_A01();
		folderA01.setActualDepositDate(new LocalDate(2009, 12, 31));
		folderA01.setActualDestructionDate(new LocalDate(2009, 10, 31));
		folderA01.setActualTransferDate(new LocalDate(2010, 10, 31));

		givenConfig(RMConfigs.POPULATE_BORDEREAUX_WITH_LESSER_DISPOSITION_DATE, false);

		assertThat(service.getDispositionDate(folderA01.getWrappedRecord())).isEqualTo(new LocalDate(2010, 10, 31));
	}

	@Test
	public void givenContainerBac07WhenGetContainerRecordExtremeDatesThenReturnExtremeDates()
			throws Exception {
		ContainerRecord container = records.getContainerBac07();

		assertThat(service.getContainerRecordExtremeDates(container)).isEqualTo("2002-2009");
	}

	@Test
	public void whenGetUniformRuleForContainer13ThenReturnIt() {
		assertThat(service.getUniformRuleOf(records.getContainerBac13())).isEqualTo("ruleId_2");
	}

	@Test
	public void whenGetUniformRuleForContainer10ThenReturnNull() {
		assertThat(service.getUniformRuleOf(records.getContainerBac10())).isNull();
	}

	@Test
	public void whenGetFoldersForAdministrativeUnitThenOk() {
		List<Folder> folders = service.getFoldersForAdministrativeUnit("unitId_12b");
		assertThat(folders).extracting("title").containsExactly(
				"Avocat", "Banane", "Datte", "Framboise", "Mangue", "Mûre", "Nectarine", "Pêche", "Pomme", "Tomate");
	}

	@Test
	public void givenDeletedFolderWhenGetFolderForAdministrativeUnitThenOk() {
		List<Folder> folders = service.getFoldersForAdministrativeUnit("unitId_12b");
		recordServices.logicallyDelete(folders.get(0).getWrappedRecord(), User.GOD);
		folders = service.getFoldersForAdministrativeUnit("unitId_12b");
		assertThat(folders).extracting("title").containsExactly(
				"Banane", "Datte", "Framboise", "Mangue", "Mûre", "Nectarine", "Pêche", "Pomme", "Tomate");
	}

	@Test
	public void whenGetFoldersForClassificationPlanThenOk() {
		List<Folder> folders = service.getFoldersForClassificationPlan("categoryId_Z112");
		assertThat(folders).extracting("title").containsExactly("Boeuf", "Bouc", "Buffle", "Carotte", "Citron");
	}

	@Test
	public void givenDeletedFolderWhenGetFolderForClassificationPlanThenOk() {
		List<Folder> folders = service.getFoldersForClassificationPlan("categoryId_Z112");
		recordServices.logicallyDelete(folders.get(0).getWrappedRecord(), User.GOD);
		folders = service.getFoldersForClassificationPlan("categoryId_Z112");
		assertThat(folders).extracting("title").containsExactly("Bouc", "Buffle", "Carotte", "Citron");
	}

	@Test
	public void whenGetRetentionRulesForAdministrativeUnitThenOk() {
		List<RetentionRule> retentionRules = service.getRetentionRulesForAdministrativeUnit("unitId_12b");
		assertThat(retentionRules).hasSize(3).extracting("id").containsOnly("ruleId_4", "ruleId_2", "ruleId_1");
	}

	@Test
	public void givenDeletedRetentionRuleWhenGetRetentionRulesForAdministrativeUnitThenOk()
			throws Exception {

		CopyRetentionRule principal_PA_3_888_D = copyBuilder.newPrincipal(asList(rm.PA()), "3-888-D");
		CopyRetentionRule secondary_MD_3_888_C = copyBuilder.newSecondary(asList(rm.DM()), "3-888-C");
		recordServices.add(rm.newRetentionRuleWithId("zeRule").setCode("zeRule").setTitle("Ze rule!")
				.setAdministrativeUnits(asList(records.unitId_12)).setApproved(true)
				.setCopyRetentionRules(asList(principal_PA_3_888_D, secondary_MD_3_888_C)));
		recordServices.logicallyDelete(recordServices.getDocumentById("zeRule"), User.GOD);
		List<RetentionRule> retentionRules = service.getRetentionRulesForAdministrativeUnit("unitId_12b");
		assertThat(retentionRules).hasSize(3).extracting("id").containsOnly("ruleId_2", "ruleId_1", "ruleId_4");
	}

	@Test
	public void whenGetFoldersForRetentionRuleThenOk() {
		long folders = service.getFolderCountForRetentionRule("ruleId_1");
		assertThat(folders).isEqualTo(20);
	}

	@Test
	public void givenDeletedFolderWhenGetFoldersForRetentionRuleThenOk()
			throws Exception {
		recordServices.logicallyDelete(records.getFolder_A04().getWrappedRecord(), User.GOD);
		long folders = service.getFolderCountForRetentionRule("ruleId_1");
		assertThat(folders).isEqualTo(19);
	}

	@Test
	public void givenDecomissioningTypeTryingToGetLabel()
			throws Exception {
		ContainerRecord containerRecordDesctruction = rm.newContainerRecord();
		containerRecordDesctruction.setDecommissioningType(DecommissioningType.DESTRUCTION);
		containerRecordDesctruction.setIdentifier("D1");
		containerRecordDesctruction.setTemporaryIdentifier("D1");
		containerRecordDesctruction.setType(records.containerTypeId_boite22x22);
		containerRecordDesctruction.setAdministrativeUnits(asList(records.unitId_10a));
		ContainerRecord containerRecordTransfert = rm.newContainerRecord();
		containerRecordTransfert.setDecommissioningType(DecommissioningType.TRANSFERT_TO_SEMI_ACTIVE);
		containerRecordTransfert.setIdentifier("T1");
		containerRecordTransfert.setTemporaryIdentifier("T1");
		containerRecordTransfert.setType(records.containerTypeId_boite22x22);
		containerRecordTransfert.setAdministrativeUnits(asList(records.unitId_10a));
		ContainerRecord containerRecordDeposit = rm.newContainerRecord();
		containerRecordDeposit.setDecommissioningType(DecommissioningType.DEPOSIT);
		containerRecordDeposit.setIdentifier("C1");
		containerRecordDeposit.setTemporaryIdentifier("C1");
		containerRecordDeposit.setType(records.containerTypeId_boite22x22);
		containerRecordDeposit.setAdministrativeUnits(asList(records.unitId_10a));

		Transaction t = new Transaction();
		t.addAll(containerRecordDeposit, containerRecordDesctruction, containerRecordTransfert);
		recordServices.execute(t);

		assertThat(containerRecordDesctruction.getDecommissioningType().getLabel()).isEqualTo($("DecommissioningType.D"));
		assertThat(containerRecordDeposit.getDecommissioningType().getLabel()).isEqualTo($("DecommissioningType.C"));
		assertThat(containerRecordTransfert.getDecommissioningType().getLabel()).isEqualTo($("DecommissioningType.T"));
	}

	@Test
	public void givenUnusedContainersThenRemoveFromListWhenProcessed() {
		//		getModelLayerFactory().newSearchServices().search(new LogicalSearchQuery().setCondition(LogicalSearchQueryOperators.from()))
		//		service.decommission(list01, records.getAdmin());
	}
}
