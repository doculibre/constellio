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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;

import com.constellio.app.modules.rm.RMTestRecords;
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

		givenCollection(zeCollection).withConstellioRMModule().withAllTestUsers();
		records.setup(getModelLayerFactory()).withFoldersAndContainersOfEveryStatus();

		rm = new RMSchemasRecordsServices(zeCollection, getModelLayerFactory());
		service = new DecommissioningService(zeCollection, getModelLayerFactory());
		recordServices = getModelLayerFactory().newRecordServices();
	}

	@Test
	public void whenHasFolderToDepositThenOk()
			throws Exception {

		assertThat(service.hasFolderToDeposit(records.getContainerBac01())).isTrue();
		assertThat(service.hasFolderToDeposit(records.getContainerBac13())).isFalse();
	}

	@Test
	public void whenGetMediumTypesOfContainerThenOk()
			throws Exception {

		assertThat(service.getMediumTypesOf(records.getContainerBac11())).containsOnly(rm.PA(), rm.DM());
	}

	@Test
	public void whenGetDispositionYearThenOk()
			throws Exception {

		assertThat(service.getDispositionDate(records.getContainerBac10())).isEqualTo(new LocalDate(2007, 10, 31));
	}

	@Test
	public void whenGetUniformRuleForContainer13ThenReturnIt()
			throws Exception {

		assertThat(service.getUniformRuleOf(records.getContainerBac13())).isEqualTo("ruleId_2");
	}

	@Test
	public void whenGetUniformRuleForContainer10ThenReturnNull()
			throws Exception {

		assertThat(service.getUniformRuleOf(records.getContainerBac10())).isNull();
	}

	@Test
	public void whenGetFoldersForAdministrativeUnitThenOk()
			throws Exception {

		List<Folder> folders = service.getFoldersForAdministrativeUnit("unitId_12");
		List<String> folderTitles = new ArrayList<>();
		for (Folder folder : folders) {
			folderTitles.add(folder.getTitle());
		}
		assertThat(folders).hasSize(10);
		assertThat(folderTitles)
				.containsOnly("Banane", "Datte", "Framboise", "Mangue", "Nectarine", "Pêche", "Pomme", "Tomate", "Avocat",
						"Mûre");
	}

	@Test
	public void givenDeletedFolderWhenGetFolderForAdministrativeUnitThenOk()
			throws Exception {

		List<Folder> folders = service.getFoldersForAdministrativeUnit("unitId_12");
		recordServices.logicallyDelete(folders.get(0).getWrappedRecord(), User.GOD);

		folders = service.getFoldersForAdministrativeUnit("unitId_12");
		List<String> folderTitles = new ArrayList<>();
		for (Folder folder : folders) {
			folderTitles.add(folder.getTitle());
		}
		assertThat(folders).hasSize(9);
		assertThat(folderTitles).containsOnly("Datte", "Framboise", "Mangue", "Nectarine", "Pêche", "Pomme", "Tomate", "Avocat",
				"Mûre");
	}

	@Test
	public void whenGetFoldersForClassificationPlanThenOk()
			throws Exception {

		List<Folder> folders = service.getFoldersForClassificationPlan("categoryId_Z112");
		List<String> folderTitles = new ArrayList<>();
		for (Folder folder : folders) {
			folderTitles.add(folder.getTitle());
		}
		assertThat(folders).hasSize(5);
		assertThat(folderTitles)
				.containsOnly("Bouc", "Boeuf", "Buffle", "Citron", "Carotte");
	}

	@Test
	public void givenDeletedFolderWhenGetFolderForClassificationPlanThenOk()
			throws Exception {

		List<Folder> folders = service.getFoldersForClassificationPlan("categoryId_Z112");
		recordServices.logicallyDelete(folders.get(0).getWrappedRecord(), User.GOD);

		folders = service.getFoldersForClassificationPlan("categoryId_Z112");
		List<String> folderTitles = new ArrayList<>();
		for (Folder folder : folders) {
			folderTitles.add(folder.getTitle());
		}
		assertThat(folders).hasSize(4);
		assertThat(folderTitles).containsOnly("Boeuf", "Buffle", "Citron", "Carotte");
	}

	@Test
	public void whenGetRetentionRulesForAdministrativeUnitThenOk()
			throws Exception {
		List<RetentionRule> retentionRules = service.getRetentionRulesForAdministrativeUnit("unitId_12");
		List<String> retentionRulesIds = new ArrayList<>();
		for (RetentionRule retentionRule : retentionRules) {
			retentionRulesIds.add(retentionRule.getId());
		}

		assertThat(retentionRules).hasSize(3);
		assertThat(retentionRulesIds).containsOnly("ruleId_4", "ruleId_2", "ruleId_1");
	}

	@Test
	public void givenDeletedRetentionRuleWhenGetRetentionRulesForAdministrativeUnitThenOk()
			throws Exception {

		recordServices.logicallyDelete(recordServices.getDocumentById("ruleId_4"), User.GOD);

		List<RetentionRule> retentionRules = service.getRetentionRulesForAdministrativeUnit("unitId_12");
		List<String> retentionRulesIds = new ArrayList<>();
		for (RetentionRule retentionRule : retentionRules) {
			retentionRulesIds.add(retentionRule.getId());
		}
		assertThat(retentionRules).hasSize(2);
		assertThat(retentionRulesIds).containsOnly("ruleId_2", "ruleId_1");
	}

	@Test
	public void whenGetFoldersForRetentionRuleThenOk()
			throws Exception {

		List<Folder> folders = service.getFoldersForRetentionRule("ruleId_1");
		List<String> folderTitles = new ArrayList<>();
		for (Folder folder : folders) {
			folderTitles.add(folder.getTitle());
		}
		assertThat(folders).hasSize(20);
		assertThat(folderTitles)
				.containsOnly("Baleine", "Belette", "Bison", "Chat", "Chauve-souris", "Cheval", "Gorille", "Grenouille",
						"Hamster",
						"Panda", "Perroquet", "Phoque", "Banane", "Framboise", "Pêche", "Avocat", "Brocoli", "Chou-fleur", "Maïs",
						"Pomme de terre");
	}

	@Test
	public void givenDeletedFolderWhenGetFoldersForRetentionRuleThenOk()
			throws Exception {

		recordServices.logicallyDelete(records.getFolder_A04().getWrappedRecord(), User.GOD);

		List<Folder> folders = service.getFoldersForRetentionRule("ruleId_1");
		List<String> folderTitles = new ArrayList<>();
		for (Folder folder : folders) {
			folderTitles.add(folder.getTitle());
		}
		assertThat(folders).hasSize(19);
		assertThat(folderTitles)
				.containsOnly("Belette", "Bison", "Chat", "Chauve-souris", "Cheval", "Gorille", "Grenouille",
						"Hamster",
						"Panda", "Perroquet", "Phoque", "Banane", "Framboise", "Pêche", "Avocat", "Brocoli", "Chou-fleur", "Maïs",
						"Pomme de terre");
	}

}
