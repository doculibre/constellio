package com.constellio.app.modules.rm.extensions;

import com.constellio.app.modules.rm.RMConfigs;
import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.model.enums.CopyType;
import com.constellio.app.modules.rm.model.enums.FolderStatus;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.services.decommissioning.DecommissioningService;
import com.constellio.app.modules.rm.wrappers.Cart;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.RetentionRule;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.records.RecordServicesException.ValidationException;
import com.constellio.model.services.search.SearchServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.TestUtils;
import com.constellio.sdk.tests.setups.Users;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static com.constellio.sdk.tests.TestUtils.asList;
import static org.assertj.core.api.Assertions.assertThat;

public class RMFolderExtensionAcceptanceTest extends ConstellioTest {
	private static final List<String> NON_EXISTING_CART_IDS = asList("01", "02");

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
		service = new DecommissioningService(zeCollection, getAppLayerFactory());
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
			assertThat(TestUtils.frenchMessages(e)).containsOnly(
					"Métadonnée «Statut d'exemplaire» requise",
					"Métadonnée «Exemplaire» requise");
		}
		try {
			saveAndReloadFolder(folderWithRuleBoth_WithCreatorInNoAdminUnit);
		} catch (ValidationException e) {
			assertThat(TestUtils.frenchMessages(e)).containsOnly(
					"Métadonnée «Statut d'exemplaire» requise",
					"Métadonnée «Exemplaire» requise");
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
			saveAndReloadFolder(folderWithRuleResponsible_WithCreatorInNoAdminUnit);
		} catch (ValidationException e) {
			assertThat(TestUtils.frenchMessages(e)).containsOnly(
					"Métadonnée «Statut d'exemplaire» requise",
					"Métadonnée «Exemplaire» requise");
		}
	}

	@Test
	public void givenFolderWithParentAndEnteredFieldsThenEnteredFieldsDeleted()
			throws Exception {
		Folder folder = newFolder("Test").setOpenDate(aDate()).setRetentionRuleEntered(records.getRule1())
				.setAdministrativeUnitEntered(records.getUnit10a()).setCategoryEntered(records.categoryId_X13)
				.setParentFolder(records.folder_A06).setCopyStatusEntered(CopyType.PRINCIPAL);
		recordServices.add(folder);

		assertThat(folder.getAdministrativeUnitEntered()).isNull();
		assertThat(folder.getCategoryEntered()).isNull();
		assertThat(folder.getRetentionRuleEntered()).isNull();
		assertThat(folder.getCopyStatusEntered()).isNull();

		folder.setOpenDate(aDate()).setRetentionRuleEntered(records.getRule1())
				.setAdministrativeUnitEntered(records.getUnit10a()).setCategoryEntered(records.categoryId_X13)
				.setParentFolder(records.folder_A06).setCopyStatusEntered(CopyType.PRINCIPAL);
		recordServices.update(folder);

		assertThat(folder.getAdministrativeUnitEntered()).isNull();
		assertThat(folder.getCategoryEntered()).isNull();
		assertThat(folder.getRetentionRuleEntered()).isNull();
		assertThat(folder.getCopyStatusEntered()).isNull();

	}

	@Test
	public void whenModifyingFolderWithInexistentFavoritesIdsThenIdsAreDeleted() throws RecordServicesException {
		Folder testFolder = getTestFolder().setFavorites(NON_EXISTING_CART_IDS);
		saveAndReloadFolder(testFolder);

		testFolder.setTitle("TestModifié");
		recordServices.update(testFolder);

		assertThat(testFolder.getFavorites()).isEmpty();
	}

	@Test
	public void whenModifyingFolderWithSomeExistingFavoritesIdsThenNonExistingIdsAreDeleted()
			throws RecordServicesException {
		Cart cart = rm.newCart().setOwner(users.adminIn(zeCollection).getId());
		cart.setTitle("Sugar");
		recordServices.add(cart);
		String existingId = cart.getId();
		List<String> listWithOneExistingId = new ArrayList<>();
		listWithOneExistingId.add(existingId);
		listWithOneExistingId.addAll(NON_EXISTING_CART_IDS);

		Folder testFolder = getTestFolder().setFavorites(listWithOneExistingId);
		saveAndReloadFolder(testFolder);

		testFolder.setTitle("TestModifié");
		recordServices.update(testFolder);

		assertThat(testFolder.getFavorites()).containsOnly(existingId);
	}

	@Test
	public void whenModifyingFolderWithExistentFavoritesIdsThenFavoritesListStaysTheSame()
			throws RecordServicesException {
		Cart firstCart = rm.newCart().setOwner(users.adminIn(zeCollection).getId());
		firstCart.setTitle("First");
		Cart secondCart = rm.newCart().setOwner(users.adminIn(zeCollection).getId());
		secondCart.setTitle("Second");
		recordServices.add(firstCart);
		recordServices.add(secondCart);
		List<String> listWithExistingIds = asList(firstCart.getId(), secondCart.getId());

		Folder testFolder = getTestFolder().setFavorites(listWithExistingIds);
		saveAndReloadFolder(testFolder);

		testFolder.setTitle("TestModifié");
		recordServices.update(testFolder);

		assertThat(testFolder.getFavorites()).containsOnly(firstCart.getId(), secondCart.getId());
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
				.setCopyStatusEntered(CopyType.PRINCIPAL)
				.setRetentionRuleEntered(ruleResponsible_withResponsible).setCreatedBy(
						aliceInNoAdminUnit.getId());
		folderWithRuleBoth_WithCreatorInNoAdminUnit = (Folder) newFolder("3")
				.setRetentionRuleEntered(ruleBoth_withResponsibleAndAdministrativeUnitsAndAdminUnits10_20)
				.setCopyStatusEntered(CopyType.PRINCIPAL).setCreatedBy(
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
