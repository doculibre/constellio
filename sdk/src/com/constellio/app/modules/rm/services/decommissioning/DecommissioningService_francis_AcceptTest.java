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

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.constellio.app.modules.rm.RMConfigs;
import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.sdk.tests.ConstellioTest;

public class DecommissioningService_francis_AcceptTest extends ConstellioTest {
	DecommissioningService service;
	RMSchemasRecordsServices rm;
	RMTestRecords records = new RMTestRecords(zeCollection);
	RecordServices recordServices;
	SearchServices searchServices;

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
		searchServices = getModelLayerFactory().newSearchServices();
	}

	@Test
	public void whenGetUserAdministrativeUnitsThenValidValues()
			throws Exception {

		assertThat(service.getAdministrativeUnitsForUser(records.getBob_userInAC()))
				.containsOnly(records.unitId_10, records.unitId_10a, records.unitId_11, records.unitId_11b, records.unitId_12,
						records.unitId_12b, records.unitId_12c, records.unitId_30, records.unitId_30c);

		assertThat(service.getAdministrativeUnitsForUser(records.getCharles_userInA()))
				.containsOnly(records.unitId_10, records.unitId_10a, records.unitId_11, records.unitId_11b, records.unitId_12,
						records.unitId_12b, records.unitId_12c);

		assertThat(service.getAdministrativeUnitsForUser(records.getDakota_managerInA_userInB()))
				.containsOnly(records.unitId_10, records.unitId_10a, records.unitId_11, records.unitId_11b, records.unitId_12,
						records.unitId_12b, records.unitId_12c);

		assertThat(service.getAdministrativeUnitsForUser(records.getEdouard_managerInB_userInC()))
				.containsOnly(records.unitId_30, records.unitId_30c, records.unitId_11, records.unitId_11b, records.unitId_12,
						records.unitId_12b,
						records.unitId_12c);

		assertThat(service.getAdministrativeUnitsForUser(records.getGandalf_managerInABC()))
				.containsOnly(records.unitId_10, records.unitId_10a, records.unitId_11, records.unitId_11b, records.unitId_12,
						records.unitId_12b, records.unitId_12c, records.unitId_30, records.unitId_30c);

		assertThat(service.getAdministrativeUnitsForUser(records.getChuckNorris()))
				.containsOnly(records.unitId_10, records.unitId_10a, records.unitId_11, records.unitId_11b, records.unitId_12,
						records.unitId_12b, records.unitId_12c,
						records.unitId_20, records.unitId_20d, records.unitId_20e, records.unitId_30, records.unitId_30c);
	}

	@Test
	public void whenDuplicateFolderThenAllMetadataDuplicated()
			throws Exception {

		Folder a13 = records.getFolder_A13();
		a13.setParentFolder(records.folder_A04);
		a13.setCategoryEntered((String) null);
		a13.setAdministrativeUnitEntered((String) null);
		recordServices.update(a13);

		Folder a04 = records.getFolder_A04();
		Folder duplicatedFolder = service.duplicateAndSave(records.getFolder_A04());

		assertThat(duplicatedFolder.getDescription()).isEqualTo(a04.getDescription());
		assertThat(duplicatedFolder.getTitle()).isEqualTo(a04.getTitle() + " (Copie)");
		assertThat(duplicatedFolder.getMediumTypes()).isEqualTo(a04.getMediumTypes());
		assertThat(duplicatedFolder.getUniformSubdivisionEntered()).isEqualTo(a04.getUniformSubdivisionEntered());
		assertThat(duplicatedFolder.getArchivisticStatus()).isEqualTo(a04.getArchivisticStatus());
		assertThat(duplicatedFolder.getFilingSpaceEntered()).isEqualTo(a04.getFilingSpaceEntered());
		assertThat(duplicatedFolder.getActualDepositDate()).isEqualTo(a04.getActualDepositDate());
		assertThat(duplicatedFolder.getActualDestructionDate()).isEqualTo(a04.getActualDestructionDate());
		assertThat(duplicatedFolder.getActualTransferDate()).isEqualTo(a04.getActualTransferDate());
		assertThat(duplicatedFolder.getCloseDateEntered()).isEqualTo(a04.getCloseDateEntered());
		assertThat(duplicatedFolder.getOpenDate()).isEqualTo(a04.getOpenDate());
		assertThat(duplicatedFolder.getCopyStatusEntered()).isEqualTo(a04.getCopyStatusEntered());
		assertThat(duplicatedFolder.getKeywords()).isEqualTo(a04.getKeywords());
		assertThat(duplicatedFolder.getRetentionRule()).isEqualTo(a04.getRetentionRule());
		assertThat(duplicatedFolder.getType()).isEqualTo(a04.getType());

		assertThat(duplicatedFolder.getCategory()).isEqualTo(a04.getCategory());
		assertThat(duplicatedFolder.getApplicableAdministrative()).isEqualTo(a04.getApplicableAdministrative());
		assertThat(duplicatedFolder.getParentFolder()).isNull();

		List<String> children = searchServices.searchRecordIds(new LogicalSearchQuery()
				.setCondition(from(rm.folderSchemaType()).where(rm.folderParentFolder()).isEqualTo(duplicatedFolder)));

		assertThat(children).isEmpty();
	}

	@Test
	public void whenDuplicateFolderStructureThenAllMetadataDuplicated()
			throws Exception {

		Folder a13 = records.getFolder_A13();
		a13.setParentFolder(records.folder_A04);
		a13.setCategoryEntered((String) null);
		a13.setAdministrativeUnitEntered((String) null);
		recordServices.update(a13);

		Folder a04 = records.getFolder_A04();
		Folder duplicatedFolder = service.duplicateStructureAndSave(records.getFolder_A04());

		assertThat(duplicatedFolder.getDescription()).isEqualTo(a04.getDescription());
		assertThat(duplicatedFolder.getTitle()).isEqualTo(a04.getTitle() + " (Copie)");
		assertThat(duplicatedFolder.getMediumTypes()).isEqualTo(a04.getMediumTypes());
		assertThat(duplicatedFolder.getUniformSubdivisionEntered()).isEqualTo(a04.getUniformSubdivisionEntered());
		assertThat(duplicatedFolder.getArchivisticStatus()).isEqualTo(a04.getArchivisticStatus());
		assertThat(duplicatedFolder.getFilingSpaceEntered()).isEqualTo(a04.getFilingSpaceEntered());
		assertThat(duplicatedFolder.getActualDepositDate()).isEqualTo(a04.getActualDepositDate());
		assertThat(duplicatedFolder.getActualDestructionDate()).isEqualTo(a04.getActualDestructionDate());
		assertThat(duplicatedFolder.getActualTransferDate()).isEqualTo(a04.getActualTransferDate());
		assertThat(duplicatedFolder.getCloseDateEntered()).isEqualTo(a04.getCloseDateEntered());
		assertThat(duplicatedFolder.getOpenDate()).isEqualTo(a04.getOpenDate());
		assertThat(duplicatedFolder.getCopyStatusEntered()).isEqualTo(a04.getCopyStatusEntered());
		assertThat(duplicatedFolder.getKeywords()).isEqualTo(a04.getKeywords());
		assertThat(duplicatedFolder.getRetentionRule()).isEqualTo(a04.getRetentionRule());
		assertThat(duplicatedFolder.getType()).isEqualTo(a04.getType());

		assertThat(duplicatedFolder.getCategory()).isEqualTo(a04.getCategory());
		assertThat(duplicatedFolder.getApplicableAdministrative()).isEqualTo(a04.getApplicableAdministrative());
		assertThat(duplicatedFolder.getParentFolder()).isNull();

		List<String> children = searchServices.searchRecordIds(new LogicalSearchQuery()
				.setCondition(from(rm.folderSchemaType()).where(rm.folderParentFolder()).isEqualTo(duplicatedFolder)));

		assertThat(children).hasSize(1);
		Folder duplicatedSubFolder = rm.getFolder(children.get(0));

		assertThat(duplicatedSubFolder.getDescription()).isEqualTo(a13.getDescription());
		assertThat(duplicatedSubFolder.getTitle()).isEqualTo(a13.getTitle());
		assertThat(duplicatedSubFolder.getMediumTypes()).isEqualTo(a13.getMediumTypes());
		assertThat(duplicatedSubFolder.getUniformSubdivisionEntered()).isEqualTo(a13.getUniformSubdivisionEntered());
		assertThat(duplicatedSubFolder.getArchivisticStatus()).isEqualTo(a13.getArchivisticStatus());
		assertThat(duplicatedSubFolder.getFilingSpaceEntered()).isEqualTo(a13.getFilingSpaceEntered());
		assertThat(duplicatedSubFolder.getActualDepositDate()).isEqualTo(a13.getActualDepositDate());
		assertThat(duplicatedSubFolder.getActualDestructionDate()).isEqualTo(a13.getActualDestructionDate());
		assertThat(duplicatedSubFolder.getActualTransferDate()).isEqualTo(a13.getActualTransferDate());
		assertThat(duplicatedSubFolder.getCloseDateEntered()).isEqualTo(a13.getCloseDateEntered());
		assertThat(duplicatedSubFolder.getOpenDate()).isEqualTo(a13.getOpenDate());
		assertThat(duplicatedSubFolder.getCopyStatusEntered()).isEqualTo(a13.getCopyStatusEntered());
		assertThat(duplicatedSubFolder.getKeywords()).isEqualTo(a13.getKeywords());
		assertThat(duplicatedSubFolder.getRetentionRule()).isEqualTo(a13.getRetentionRule());
		assertThat(duplicatedSubFolder.getType()).isEqualTo(a13.getType());

		assertThat(duplicatedSubFolder.getCategory()).isEqualTo(a13.getCategory());
		assertThat(duplicatedSubFolder.getApplicableAdministrative()).isEqualTo(a13.getApplicableAdministrative());
		assertThat(duplicatedSubFolder.getParentFolder()).isEqualTo(duplicatedFolder.getId());
	}

	@Test
	public void whenDuplicateSubFolderThenAllMetadataDuplicated()
			throws Exception {

		Folder a13 = records.getFolder_A13();
		a13.setParentFolder(records.folder_A04);
		a13.setCategoryEntered((String) null);
		a13.setAdministrativeUnitEntered((String) null);
		recordServices.update(a13);

		Folder duplicatedFolder = service.duplicateAndSave(a13);

		assertThat(duplicatedFolder.getDescription()).isEqualTo(a13.getDescription());
		assertThat(duplicatedFolder.getTitle()).isEqualTo(a13.getTitle() + " (Copie)");
		assertThat(duplicatedFolder.getMediumTypes()).isEqualTo(a13.getMediumTypes());
		assertThat(duplicatedFolder.getUniformSubdivisionEntered()).isEqualTo(a13.getUniformSubdivisionEntered());
		assertThat(duplicatedFolder.getArchivisticStatus()).isEqualTo(a13.getArchivisticStatus());
		assertThat(duplicatedFolder.getFilingSpaceEntered()).isEqualTo(a13.getFilingSpaceEntered());
		assertThat(duplicatedFolder.getActualDepositDate()).isEqualTo(a13.getActualDepositDate());
		assertThat(duplicatedFolder.getActualDestructionDate()).isEqualTo(a13.getActualDestructionDate());
		assertThat(duplicatedFolder.getActualTransferDate()).isEqualTo(a13.getActualTransferDate());
		assertThat(duplicatedFolder.getCloseDateEntered()).isEqualTo(a13.getCloseDateEntered());
		assertThat(duplicatedFolder.getOpenDate()).isEqualTo(a13.getOpenDate());
		assertThat(duplicatedFolder.getCopyStatusEntered()).isEqualTo(a13.getCopyStatusEntered());
		assertThat(duplicatedFolder.getKeywords()).isEqualTo(a13.getKeywords());
		assertThat(duplicatedFolder.getRetentionRule()).isEqualTo(a13.getRetentionRule());
		assertThat(duplicatedFolder.getType()).isEqualTo(a13.getType());

		assertThat(duplicatedFolder.getCategory()).isEqualTo(a13.getCategory());
		assertThat(duplicatedFolder.getApplicableAdministrative()).isEqualTo(a13.getApplicableAdministrative());
		assertThat(duplicatedFolder.getParentFolder()).isEqualTo(records.folder_A04);
	}

	@Test
	public void whenGetRetentionRulesForCategoryOrUniformSubdivision()
			throws Exception {
		assertThat(service.getRetentionRulesForCategory(null, null))
				.isEmpty();

		assertThat(service.getRetentionRulesForCategory(records.categoryId_X100, null))
				.containsExactly(records.ruleId_1);

		assertThat(service.getRetentionRulesForCategory(records.categoryId_X110, null))
				.hasSize(2).containsOnly(records.ruleId_1, records.ruleId_2);

		assertThat(service.getRetentionRulesForCategory(records.categoryId_X100, records.subdivId_2))
				.containsExactly(records.ruleId_1);

		assertThat(service.getRetentionRulesForCategory(records.categoryId_X110, records.subdivId_2))
				.hasSize(2).containsOnly(records.ruleId_1, records.ruleId_2);

		assertThat(service.getRetentionRulesForCategory(records.categoryId_X100, records.subdivId_1))
				.containsExactly(records.ruleId_2);

		assertThat(service.getRetentionRulesForCategory(records.categoryId_X110, records.subdivId_1))
				.containsExactly(records.ruleId_2);

	}

	@Test
	public void givenCopyRuleTypeAlwaysModifiableWhenDeterminingIfCopyTypeVisibleThenAlwaysTrue() {
		givenConfig(RMConfigs.COPY_RULE_TYPE_ALWAYS_MODIFIABLE, true);

		assertThat(service.isCopyStatusInputPossible(records.getFolder_A04())).isTrue();
		assertThat(service.isCopyStatusInputPossible(records.getFolder_A10())).isTrue();
		assertThat(service.isCopyStatusInputPossible(records.getFolder_A12().setRetentionRuleEntered((String) null))).isTrue();
	}

	@Test
	public void givenCopyRuleTypeNOTAlwaysModifiableWhenDeterminingIfCopyTypeVisibleThenAlwaysTrue() {
		givenConfig(RMConfigs.COPY_RULE_TYPE_ALWAYS_MODIFIABLE, false);

		assertThat(service.isCopyStatusInputPossible(records.getFolder_A04())).isFalse();
		assertThat(service.isCopyStatusInputPossible(records.getFolder_A10())).isTrue();
	}

	//TODO Ajouter medium type when updating document
}
