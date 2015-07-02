/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package com.constellio.app.modules.rm.services.decommissioning;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.model.CopyRetentionRule;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.RetentionRule;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.records.RecordServices;
import com.constellio.sdk.tests.ConstellioTest;

public class DecommissioningServiceAcceptTest extends ConstellioTest {
	DecommissioningService service;
	RMSchemasRecordsServices rm;
	RecordServices recordServices;
	RMTestRecords records = new RMTestRecords(zeCollection);

	@Before
	public void setUp()
			throws Exception {

		prepareSystem(
				withZeCollection().withConstellioRMModule().withAllTestUsers().withRMTest(records)
						.withFoldersAndContainersOfEveryStatus()
		);

		rm = new RMSchemasRecordsServices(zeCollection, getModelLayerFactory());
		service = new DecommissioningService(zeCollection, getModelLayerFactory());
		recordServices = getModelLayerFactory().newRecordServices();
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
		assertThat(service.getDispositionDate(records.getContainerBac10())).isEqualTo(new LocalDate(2007, 10, 31));
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
		List<Folder> folders = service.getFoldersForAdministrativeUnit("unitId_12");
		assertThat(folders).extracting("title").containsExactly(
				"Avocat", "Banane", "Datte", "Framboise", "Mangue", "Mûre", "Nectarine", "Pêche", "Pomme", "Tomate");
	}

	@Test
	public void givenDeletedFolderWhenGetFolderForAdministrativeUnitThenOk() {
		List<Folder> folders = service.getFoldersForAdministrativeUnit("unitId_12");
		recordServices.logicallyDelete(folders.get(0).getWrappedRecord(), User.GOD);
		folders = service.getFoldersForAdministrativeUnit("unitId_12");
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
		List<RetentionRule> retentionRules = service.getRetentionRulesForAdministrativeUnit("unitId_12");
		assertThat(retentionRules).hasSize(3).extracting("id").containsOnly("ruleId_4", "ruleId_2", "ruleId_1");
	}

	@Test
	public void givenDeletedRetentionRuleWhenGetRetentionRulesForAdministrativeUnitThenOk()
			throws Exception {

		CopyRetentionRule principal_PA_3_888_D = CopyRetentionRule.newPrincipal(asList(rm.PA()), "3-888-D");
		CopyRetentionRule secondary_MD_3_888_C = CopyRetentionRule.newSecondary(asList(rm.DM()), "3-888-C");
		recordServices.add(rm.newRetentionRuleWithId("zeRule").setCode("zeRule").setTitle("Ze rule!")
				.setAdministrativeUnits(asList(records.unitId_12)).setApproved(true)
				.setCopyRetentionRules(asList(principal_PA_3_888_D, secondary_MD_3_888_C)));
		recordServices.logicallyDelete(recordServices.getDocumentById("zeRule"), User.GOD);
		List<RetentionRule> retentionRules = service.getRetentionRulesForAdministrativeUnit("unitId_12");
		assertThat(retentionRules).hasSize(3).extracting("id").containsOnly("ruleId_2", "ruleId_1", "ruleId_4");
	}

	@Test
	public void whenGetFoldersForRetentionRuleThenOk() {
		List<Folder> folders = service.getFoldersForRetentionRule("ruleId_1");
		assertThat(folders).extracting("title").containsExactly(
				"Avocat", "Baleine", "Banane", "Belette", "Bison", "Brocoli", "Chat", "Chauve-souris", "Cheval", "Chou-fleur",
				"Framboise", "Gorille", "Grenouille", "Hamster", "Maïs", "Panda", "Pêche", "Perroquet", "Phoque",
				"Pomme de terre");
	}

	@Test
	public void givenDeletedFolderWhenGetFoldersForRetentionRuleThenOk()
			throws Exception {
		recordServices.logicallyDelete(records.getFolder_A04().getWrappedRecord(), User.GOD);
		List<Folder> folders = service.getFoldersForRetentionRule("ruleId_1");
		assertThat(folders).extracting("title").containsExactly(
				"Avocat", "Banane", "Belette", "Bison", "Brocoli", "Chat", "Chauve-souris", "Cheval", "Chou-fleur", "Framboise",
				"Gorille", "Grenouille", "Hamster", "Maïs", "Panda", "Pêche", "Perroquet", "Phoque", "Pomme de terre");
	}
}
