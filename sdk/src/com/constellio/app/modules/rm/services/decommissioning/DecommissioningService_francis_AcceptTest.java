package com.constellio.app.modules.rm.services.decommissioning;

import com.constellio.app.modules.rm.RMConfigs;
import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.schemas.MetadataSchemaTypesAlteration;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.setups.Users;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static org.assertj.core.api.Assertions.assertThat;

public class DecommissioningService_francis_AcceptTest extends ConstellioTest {
	Users users = new Users();
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
						.withFoldersAndContainersOfEveryStatus().withAllTestUsers()
		);

		rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
		service = new DecommissioningService(zeCollection, getAppLayerFactory());
		recordServices = getModelLayerFactory().newRecordServices();
		searchServices = getModelLayerFactory().newSearchServices();
		users.setUp(getModelLayerFactory().newUserServices(), zeCollection);
	}

	@Test
	public void whenDuplicateFolderThenAllDuplicableMetadataDuplicated()
			throws Exception {

		Folder a13 = records.getFolder_A13();
		a13.setParentFolder(records.folder_A04);
		a13.setCategoryEntered((String) null);
		a13.setAdministrativeUnitEntered((String) null);
		recordServices.update(a13);

		getModelLayerFactory().getMetadataSchemasManager().modify(zeCollection, new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				types.getSchema(Folder.DEFAULT_SCHEMA).get(Folder.DESCRIPTION).setDuplicable(false);
			}
		});

		Folder a04 = records.getFolder_A04();
		Folder duplicatedFolder = service.duplicate(records.getFolder_A04(), users.adminIn(zeCollection), false);

		assertThat(duplicatedFolder.getActualDepositDate()).isEqualTo(a04.getActualDepositDate());
		assertThat(duplicatedFolder.getActualDestructionDate()).isEqualTo(a04.getActualDestructionDate());
		assertThat(duplicatedFolder.getActualTransferDate()).isEqualTo(a04.getActualTransferDate());
		assertThat(duplicatedFolder.getAdministrativeUnitEntered()).isEqualTo(a04.getAdministrativeUnitEntered());
		assertThat(duplicatedFolder.getAlertUsersWhenAvailable()).isEqualTo(a04.getAlertUsersWhenAvailable());
		assertThat(duplicatedFolder.getBorrowDate()).isEqualTo(a04.getBorrowDate());
		assertThat(duplicatedFolder.getBorrowPreviewReturnDate()).isEqualTo(a04.getBorrowPreviewReturnDate());
		assertThat(duplicatedFolder.getBorrowPreviewReturnDate()).isEqualTo(a04.getBorrowPreviewReturnDate());
		assertThat(duplicatedFolder.getBorrowUser()).isEqualTo(a04.getBorrowUser());
		assertThat(duplicatedFolder.getBorrowed()).isEqualTo(a04.getBorrowed());
		assertThat(duplicatedFolder.getBorrowType()).isEqualTo(a04.getBorrowType());
		//assertThat(duplicatedFolder.getCalendarYearEntered()).isEqualTo(a04.getCalendarYearEntered());
		assertThat(duplicatedFolder.getCategoryEntered()).isEqualTo(a04.getCategoryEntered());
		assertThat(duplicatedFolder.getComments()).isEqualTo(a04.getComments());
		assertThat(duplicatedFolder.getContainer()).isEqualTo(a04.getContainer());
		assertThat(duplicatedFolder.getCopyStatusEntered()).isEqualTo(a04.getCopyStatusEntered());
		assertThat(duplicatedFolder.getCloseDateEntered()).isEqualTo(a04.getCloseDateEntered());
		assertThat(duplicatedFolder.getKeywords()).isEqualTo(a04.getKeywords());
		assertThat(duplicatedFolder.getLinearSize()).isEqualTo(a04.getLinearSize());
		assertThat(duplicatedFolder.getMainCopyRuleIdEntered()).isEqualTo(a04.getMainCopyRuleIdEntered());
		assertThat(duplicatedFolder.getMediumTypes()).isEqualTo(a04.getMediumTypes());
		assertThat(duplicatedFolder.getOpeningDate()).isEqualTo(a04.getOpeningDate());
		assertThat(duplicatedFolder.getParentFolder()).isEqualTo(a04.getParentFolder());
		assertThat(duplicatedFolder.getRetentionRuleEntered()).isEqualTo(a04.getRetentionRuleEntered());
		assertThat(duplicatedFolder.getTitle()).isEqualTo(a04.getTitle() + " (Copie)");
		assertThat(duplicatedFolder.getType()).isEqualTo(a04.getType());
		assertThat(duplicatedFolder.getUniformSubdivisionEntered()).isEqualTo(a04.getUniformSubdivisionEntered());

		List<String> children = searchServices.searchRecordIds(new LogicalSearchQuery()
				.setCondition(from(rm.folderSchemaType()).where(rm.folder.parentFolder()).isEqualTo(duplicatedFolder)));

		assertThat(children).isEmpty();
	}

	@Test
	public void givenTitleNotDuplicableWhenDuplicateFolderThenAllDuplicableMetadataDuplicatedExceptTitle()
			throws Exception {

		Folder a13 = records.getFolder_A13();
		a13.setParentFolder(records.folder_A04);
		a13.setCategoryEntered((String) null);
		a13.setAdministrativeUnitEntered((String) null);
		recordServices.update(a13);

		getModelLayerFactory().getMetadataSchemasManager().modify(zeCollection, new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				types.getSchema(Folder.DEFAULT_SCHEMA).get(Folder.DESCRIPTION).setDuplicable(true);
				types.getSchema(Folder.DEFAULT_SCHEMA).get(Folder.TITLE).setDuplicable(false);
			}
		});

		Folder a04 = records.getFolder_A04();
		Folder duplicatedFolder = service.duplicate(records.getFolder_A04(), users.adminIn(zeCollection), false);

		assertThat(duplicatedFolder.getActualDepositDate()).isEqualTo(a04.getActualDepositDate());
		assertThat(duplicatedFolder.getActualDestructionDate()).isEqualTo(a04.getActualDestructionDate());
		assertThat(duplicatedFolder.getActualTransferDate()).isEqualTo(a04.getActualTransferDate());
		assertThat(duplicatedFolder.getAdministrativeUnitEntered()).isEqualTo(a04.getAdministrativeUnitEntered());
		assertThat(duplicatedFolder.getAlertUsersWhenAvailable()).isEqualTo(a04.getAlertUsersWhenAvailable());
		assertThat(duplicatedFolder.getBorrowDate()).isEqualTo(a04.getBorrowDate());
		assertThat(duplicatedFolder.getBorrowPreviewReturnDate()).isEqualTo(a04.getBorrowPreviewReturnDate());
		assertThat(duplicatedFolder.getBorrowPreviewReturnDate()).isEqualTo(a04.getBorrowPreviewReturnDate());
		assertThat(duplicatedFolder.getBorrowUser()).isEqualTo(a04.getBorrowUser());
		assertThat(duplicatedFolder.getBorrowed()).isEqualTo(a04.getBorrowed());
		assertThat(duplicatedFolder.getBorrowType()).isEqualTo(a04.getBorrowType());
		//assertThat(duplicatedFolder.getCalendarYearEntered()).isEqualTo(a04.getCalendarYearEntered());
		assertThat(duplicatedFolder.getCategoryEntered()).isEqualTo(a04.getCategoryEntered());
		assertThat(duplicatedFolder.getComments()).isEqualTo(a04.getComments());
		assertThat(duplicatedFolder.getContainer()).isEqualTo(a04.getContainer());
		assertThat(duplicatedFolder.getCopyStatusEntered()).isEqualTo(a04.getCopyStatusEntered());
		assertThat(duplicatedFolder.getCloseDateEntered()).isEqualTo(a04.getCloseDateEntered());
		assertThat(duplicatedFolder.getKeywords()).isEqualTo(a04.getKeywords());
		assertThat(duplicatedFolder.getLinearSize()).isEqualTo(a04.getLinearSize());
		assertThat(duplicatedFolder.getMainCopyRuleIdEntered()).isEqualTo(a04.getMainCopyRuleIdEntered());
		assertThat(duplicatedFolder.getMediumTypes()).isEqualTo(a04.getMediumTypes());
		assertThat(duplicatedFolder.getOpeningDate()).isEqualTo(a04.getOpeningDate());
		assertThat(duplicatedFolder.getParentFolder()).isEqualTo(a04.getParentFolder());
		assertThat(duplicatedFolder.getRetentionRuleEntered()).isEqualTo(a04.getRetentionRuleEntered());
		assertThat(duplicatedFolder.getTitle()).isNull();
		assertThat(duplicatedFolder.getType()).isEqualTo(a04.getType());
		assertThat(duplicatedFolder.getUniformSubdivisionEntered()).isEqualTo(a04.getUniformSubdivisionEntered());

		List<String> children = searchServices.searchRecordIds(new LogicalSearchQuery()
				.setCondition(from(rm.folderSchemaType()).where(rm.folder.parentFolder()).isEqualTo(duplicatedFolder)));

		assertThat(children).isEmpty();
	}

	@Test
	public void whenDuplicateFolderStructureThenAllDuplicableMetadataDuplicated()
			throws Exception {

		Folder a13 = records.getFolder_A13();
		a13.setParentFolder(records.folder_A04);
		a13.setCategoryEntered((String) null);
		a13.setAdministrativeUnitEntered((String) null);
		recordServices.update(a13);

		Folder a04 = records.getFolder_A04();
		Folder duplicatedFolder = service.duplicateStructureAndSave(records.getFolder_A04(), users.adminIn(zeCollection));

		assertThat(duplicatedFolder.getActualDepositDate()).isEqualTo(a04.getActualDepositDate());
		assertThat(duplicatedFolder.getActualDestructionDate()).isEqualTo(a04.getActualDestructionDate());
		assertThat(duplicatedFolder.getActualTransferDate()).isEqualTo(a04.getActualTransferDate());
		assertThat(duplicatedFolder.getAdministrativeUnitEntered()).isEqualTo(a04.getAdministrativeUnitEntered());
		assertThat(duplicatedFolder.getAlertUsersWhenAvailable()).isEqualTo(a04.getAlertUsersWhenAvailable());
		assertThat(duplicatedFolder.getBorrowDate()).isEqualTo(a04.getBorrowDate());
		assertThat(duplicatedFolder.getBorrowPreviewReturnDate()).isEqualTo(a04.getBorrowPreviewReturnDate());
		assertThat(duplicatedFolder.getBorrowPreviewReturnDate()).isEqualTo(a04.getBorrowPreviewReturnDate());
		assertThat(duplicatedFolder.getBorrowUser()).isEqualTo(a04.getBorrowUser());
		assertThat(duplicatedFolder.getBorrowed()).isEqualTo(a04.getBorrowed());
		assertThat(duplicatedFolder.getBorrowType()).isEqualTo(a04.getBorrowType());
		//assertThat(duplicatedFolder.getCalendarYearEntered()).isEqualTo(a04.getCalendarYearEntered());
		assertThat(duplicatedFolder.getCategoryEntered()).isEqualTo(a04.getCategoryEntered());
		assertThat(duplicatedFolder.getComments()).isEqualTo(a04.getComments());
		assertThat(duplicatedFolder.getContainer()).isEqualTo(a04.getContainer());
		assertThat(duplicatedFolder.getCopyStatusEntered()).isEqualTo(a04.getCopyStatusEntered());
		assertThat(duplicatedFolder.getCloseDateEntered()).isEqualTo(a04.getCloseDateEntered());
		assertThat(duplicatedFolder.getKeywords()).isEqualTo(a04.getKeywords());
		assertThat(duplicatedFolder.getLinearSize()).isEqualTo(a04.getLinearSize());
		assertThat(duplicatedFolder.getMainCopyRuleIdEntered()).isEqualTo(a04.getMainCopyRuleIdEntered());
		assertThat(duplicatedFolder.getMediumTypes()).isEqualTo(a04.getMediumTypes());
		assertThat(duplicatedFolder.getOpeningDate()).isEqualTo(a04.getOpeningDate());
		assertThat(duplicatedFolder.getParentFolder()).isEqualTo(a04.getParentFolder());
		assertThat(duplicatedFolder.getRetentionRuleEntered()).isEqualTo(a04.getRetentionRuleEntered());
		assertThat(duplicatedFolder.getTitle()).isEqualTo(a04.getTitle() + " (Copie)");
		assertThat(duplicatedFolder.getType()).isEqualTo(a04.getType());
		assertThat(duplicatedFolder.getUniformSubdivisionEntered()).isEqualTo(a04.getUniformSubdivisionEntered());

		List<String> children = searchServices.searchRecordIds(new LogicalSearchQuery()
				.setCondition(from(rm.folderSchemaType()).where(rm.folder.parentFolder()).isEqualTo(duplicatedFolder)));

		assertThat(children).hasSize(1);
		Folder duplicatedSubFolder = rm.getFolder(children.get(0));

		assertThat(duplicatedSubFolder.getActualDepositDate()).isEqualTo(a13.getActualDepositDate());
		assertThat(duplicatedSubFolder.getActualDestructionDate()).isEqualTo(a13.getActualDestructionDate());
		assertThat(duplicatedSubFolder.getActualTransferDate()).isEqualTo(a13.getActualTransferDate());
		assertThat(duplicatedSubFolder.getAdministrativeUnitEntered()).isEqualTo(a13.getAdministrativeUnitEntered());
		assertThat(duplicatedSubFolder.getAlertUsersWhenAvailable()).isEqualTo(a13.getAlertUsersWhenAvailable());
		assertThat(duplicatedSubFolder.getBorrowDate()).isEqualTo(a13.getBorrowDate());
		assertThat(duplicatedSubFolder.getBorrowPreviewReturnDate()).isEqualTo(a13.getBorrowPreviewReturnDate());
		assertThat(duplicatedSubFolder.getBorrowPreviewReturnDate()).isEqualTo(a13.getBorrowPreviewReturnDate());
		assertThat(duplicatedSubFolder.getBorrowUser()).isEqualTo(a13.getBorrowUser());
		assertThat(duplicatedSubFolder.getBorrowed()).isEqualTo(a13.getBorrowed());
		assertThat(duplicatedSubFolder.getBorrowType()).isEqualTo(a13.getBorrowType());
		//assertThat(duplicatedSubFolder.getCalendarYearEntered()).isEqualTo(a13.getCalendarYearEntered());
		assertThat(duplicatedSubFolder.getCategoryEntered()).isEqualTo(a13.getCategoryEntered());
		assertThat(duplicatedSubFolder.getComments()).isEqualTo(a13.getComments());
		assertThat(duplicatedSubFolder.getContainer()).isEqualTo(a13.getContainer());
		assertThat(duplicatedSubFolder.getCopyStatusEntered()).isEqualTo(a13.getCopyStatusEntered());
		assertThat(duplicatedSubFolder.getCloseDateEntered()).isEqualTo(a13.getCloseDateEntered());
		assertThat(duplicatedSubFolder.getKeywords()).isEqualTo(a13.getKeywords());
		assertThat(duplicatedSubFolder.getLinearSize()).isEqualTo(a13.getLinearSize());
		assertThat(duplicatedSubFolder.getMainCopyRuleIdEntered()).isEqualTo(a13.getMainCopyRuleIdEntered());
		assertThat(duplicatedSubFolder.getMediumTypes()).isEqualTo(a13.getMediumTypes());
		assertThat(duplicatedSubFolder.getOpeningDate()).isEqualTo(a13.getOpeningDate());
		assertThat(duplicatedSubFolder.getParentFolder()).isEqualTo(duplicatedFolder.getId());
		assertThat(duplicatedSubFolder.getRetentionRuleEntered()).isEqualTo(a13.getRetentionRuleEntered());
		assertThat(duplicatedSubFolder.getTitle()).isEqualTo(a13.getTitle());
		assertThat(duplicatedSubFolder.getType()).isEqualTo(a13.getType());
		assertThat(duplicatedSubFolder.getUniformSubdivisionEntered()).isEqualTo(a13.getUniformSubdivisionEntered());
	}

	@Test
	public void whenDuplicateSubFolderThenAllDuplicableMetadataDuplicated()
			throws Exception {

		Folder a13 = records.getFolder_A13();
		a13.setParentFolder(records.folder_A04);
		a13.setCategoryEntered((String) null);
		a13.setAdministrativeUnitEntered((String) null);
		recordServices.update(a13);

		Folder duplicatedFolder = service.duplicate(a13, users.adminIn(zeCollection), false);

		assertThat(duplicatedFolder.getActualDepositDate()).isEqualTo(a13.getActualDepositDate());
		assertThat(duplicatedFolder.getActualDestructionDate()).isEqualTo(a13.getActualDestructionDate());
		assertThat(duplicatedFolder.getActualTransferDate()).isEqualTo(a13.getActualTransferDate());
		assertThat(duplicatedFolder.getAdministrativeUnitEntered()).isEqualTo(a13.getAdministrativeUnitEntered());
		assertThat(duplicatedFolder.getAlertUsersWhenAvailable()).isEqualTo(a13.getAlertUsersWhenAvailable());
		assertThat(duplicatedFolder.getBorrowDate()).isEqualTo(a13.getBorrowDate());
		assertThat(duplicatedFolder.getBorrowPreviewReturnDate()).isEqualTo(a13.getBorrowPreviewReturnDate());
		assertThat(duplicatedFolder.getBorrowPreviewReturnDate()).isEqualTo(a13.getBorrowPreviewReturnDate());
		assertThat(duplicatedFolder.getBorrowUser()).isEqualTo(a13.getBorrowUser());
		assertThat(duplicatedFolder.getBorrowed()).isEqualTo(a13.getBorrowed());
		assertThat(duplicatedFolder.getBorrowType()).isEqualTo(a13.getBorrowType());
		//assertThat(duplicatedFolder.getCalendarYearEntered()).isEqualTo(a13.getCalendarYearEntered());
		assertThat(duplicatedFolder.getCategoryEntered()).isEqualTo(a13.getCategoryEntered());
		assertThat(duplicatedFolder.getComments()).isEqualTo(a13.getComments());
		assertThat(duplicatedFolder.getContainer()).isEqualTo(a13.getContainer());
		assertThat(duplicatedFolder.getCopyStatusEntered()).isEqualTo(a13.getCopyStatusEntered());
		assertThat(duplicatedFolder.getCloseDateEntered()).isEqualTo(a13.getCloseDateEntered());
		assertThat(duplicatedFolder.getKeywords()).isEqualTo(a13.getKeywords());
		assertThat(duplicatedFolder.getLinearSize()).isEqualTo(a13.getLinearSize());
		assertThat(duplicatedFolder.getMainCopyRuleIdEntered()).isEqualTo(a13.getMainCopyRuleIdEntered());
		assertThat(duplicatedFolder.getMediumTypes()).isEqualTo(a13.getMediumTypes());
		assertThat(duplicatedFolder.getOpeningDate()).isEqualTo(a13.getOpeningDate());
		assertThat(duplicatedFolder.getParentFolder()).isEqualTo(a13.getParentFolder());
		assertThat(duplicatedFolder.getRetentionRuleEntered()).isEqualTo(a13.getRetentionRuleEntered());
		assertThat(duplicatedFolder.getTitle()).isEqualTo(a13.getTitle() + " (Copie)");
		assertThat(duplicatedFolder.getType()).isEqualTo(a13.getType());
		assertThat(duplicatedFolder.getUniformSubdivisionEntered()).isEqualTo(a13.getUniformSubdivisionEntered());
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
	public void givenCopyRuleTypeNOTAlwaysModifiableWhenDeterminingIfCopyTypeVisibleThenNotAlwaysTrue() {
		givenConfig(RMConfigs.COPY_RULE_TYPE_ALWAYS_MODIFIABLE, false);

		assertThat(service.isCopyStatusInputPossible(records.getFolder_A04())).isFalse();
		assertThat(service.isCopyStatusInputPossible(records.getFolder_A10())).isTrue();
	}

	//TODO Ajouter medium type when updating document
}
