package com.constellio.app.modules.rm;

import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.modules.rm.model.CopyRetentionRule;
import com.constellio.app.modules.rm.model.CopyRetentionRuleBuilder;
import com.constellio.app.modules.rm.model.CopyRetentionRuleBuilderWithDefinedIds;
import com.constellio.app.modules.rm.model.enums.CopyType;
import com.constellio.app.modules.rm.model.enums.DecomListStatus;
import com.constellio.app.modules.rm.model.enums.DecommissioningListType;
import com.constellio.app.modules.rm.model.enums.DecommissioningType;
import com.constellio.app.modules.rm.model.enums.FolderMediaType;
import com.constellio.app.modules.rm.model.enums.FolderStatus;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.DecommissioningList;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.records.RecordServicesRuntimeException.NoSuchRecordWithId;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.annotations.IntermittentFailureTest;
import com.constellio.sdk.tests.setups.Users;
import org.assertj.core.api.Condition;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static com.constellio.app.modules.rm.model.enums.FolderMediaType.ANALOG;
import static com.constellio.app.modules.rm.model.enums.FolderMediaType.ELECTRONIC;
import static com.constellio.app.modules.rm.model.enums.FolderMediaType.HYBRID;
import static com.constellio.app.modules.rm.model.enums.FolderMediaType.UNKNOWN;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

public class RMTestRecordsAcceptanceTest extends ConstellioTest {

	Users users = new Users();
	RMTestRecords records = new RMTestRecords(zeCollection);

	CopyRetentionRuleBuilder copyBuilder = new CopyRetentionRuleBuilderWithDefinedIds();

	@Before
	public void setUp()
			throws Exception {

	}

	@Test
	// Confirm @SlowTest
	public void givenTestRecordsWithoutFoldersThenNoFolders()
			throws Exception {

		prepareSystem(
				withZeCollection().withConstellioRMModule().withAllTest(users).withRMTest(records)
		);
		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
		SearchServices searchServices = getModelLayerFactory().newSearchServices();
		LogicalSearchCondition condition = from(rm.folderSchemaType()).returnAll();
		assertThat(searchServices.getResultsCount(condition)).isEqualTo(0);

	}

	@Test
	public void givenTestRecordsThenChuchAndAdminHasContentVersionDeletePermissions()
			throws Exception {

		prepareSystem(
				withZeCollection().withConstellioRMModule().withAllTest(users).withRMTest(records)
						.withFoldersAndContainersOfEveryStatus()
		);
		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
		assertThat(records.getAdmin().has(CorePermissions.DELETE_CONTENT_VERSION).globally()).isTrue();
		assertThat(records.getBob_userInAC().has(CorePermissions.DELETE_CONTENT_VERSION).globally()).isFalse();
		assertThat(records.getCharles_userInA().has(CorePermissions.DELETE_CONTENT_VERSION).globally()).isFalse();
		assertThat(records.getDakota_managerInA_userInB().has(CorePermissions.DELETE_CONTENT_VERSION).globally()).isFalse();
		assertThat(records.getEdouard_managerInB_userInC().has(CorePermissions.DELETE_CONTENT_VERSION).globally()).isFalse();
		assertThat(records.getGandalf_managerInABC().has(CorePermissions.DELETE_CONTENT_VERSION).globally()).isFalse();
		assertThat(records.getChuckNorris().has(CorePermissions.DELETE_CONTENT_VERSION).globally()).isTrue();

	}

	@Test
	public void givenTestRecordsWithFoldersThenUsersAuthorizationsAndRolesOnFolder()
			throws Exception {

		assertThat("_M_3cd468d8-ddc4-46ae-b7ab-9bdf5d9d5a7a".split("_").length).isEqualTo(3);

		prepareSystem(withZeCollection().withConstellioRMModule().withAllTest(users).withRMTest(records)
				.withFoldersAndContainersOfEveryStatus()
		);
		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());

		Folder banane = rm.getFolder(records.folder_B02);
		User edouard = users.edouardIn(zeCollection);
		assertThat(edouard.has(RMPermissionsTo.MANAGE_FOLDER_AUTHORIZATIONS).on(banane)).isTrue();
	}

	@Test
	@IntermittentFailureTest
	public void givenTestRecordsWithFoldersThenUsersHaveAuthorizationToTheirFilingSpacesFolders()
			throws Exception {

		prepareSystem(withZeCollection().withConstellioRMModule().withAllTest(users).withRMTest(records)
				.withFoldersAndContainersOfEveryStatus()
		);
		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
		SearchServices searchServices = getModelLayerFactory().newSearchServices();
		long unit10FolderCount = searchServices.getResultsCount(from(rm.folderSchemaType())
				.where(rm.folder.administrativeUnit()).isEqualTo(records.unitId_10a));

		long unit11FolderCount = searchServices.getResultsCount(from(rm.folderSchemaType())
				.where(rm.folder.administrativeUnit()).isEqualTo(records.unitId_11b));

		long unit12FolderCount = searchServices.getResultsCount(from(rm.folderSchemaType())
				.where(rm.folder.administrativeUnit()).isIn(asList(records.unitId_12b, records.unitId_12c)));

		long unit20FolderCount = searchServices.getResultsCount(from(rm.folderSchemaType())
				.where(rm.folder.administrativeUnit()).isEqualTo(asList(records.unitId_20d, records.unitId_20e)));

		long unit30FolderCount = searchServices.getResultsCount(from(rm.folderSchemaType())
				.where(rm.folder.administrativeUnit()).isEqualTo(records.unitId_30c));

		assertThatCountOfFoldersVisibleBy(records.getBob_userInAC())
				.isEqualTo(unit10FolderCount + unit11FolderCount + unit12FolderCount + unit30FolderCount);
		assertThatCountOfFoldersVisibleBy(records.getCharles_userInA())
				.isEqualTo(unit10FolderCount + unit11FolderCount + unit12FolderCount);
		assertThatCountOfFoldersVisibleBy(records.getDakota_managerInA_userInB())
				.isEqualTo(unit10FolderCount + unit11FolderCount + unit12FolderCount);
		assertThatCountOfFoldersVisibleBy(records.getEdouard_managerInB_userInC())
				.isEqualTo(unit11FolderCount + unit12FolderCount + unit30FolderCount);
		assertThatCountOfFoldersVisibleBy(records.getChuckNorris())
				.isEqualTo(unit10FolderCount + unit11FolderCount + unit12FolderCount + unit30FolderCount);
		assertThatCountOfFoldersVisibleBy(records.getGandalf_managerInABC())
				.isEqualTo(unit10FolderCount + unit11FolderCount + unit12FolderCount + unit30FolderCount);

		assertThat(records.getFolder_A01()).has(openDate(2000, 10, 4)).has(noCloseDate()).has(noPlanifiedTransferDate())
				.has(noPlanifiedDepositDate()).has(noPlanifiedDestructionDate());
	}

	private org.assertj.core.api.LongAssert assertThatCountOfFoldersVisibleBy(User user) {
		SearchServices searchServices = getModelLayerFactory().newSearchServices();
		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
		LogicalSearchCondition condition = from(rm.folderSchemaType()).returnAll();
		return assertThat(searchServices.getResultsCount(new LogicalSearchQuery(condition).filteredWithUserRead(user)));
	}

	@Test
	public void givenTestRecordsWithStorgeSpacesThenTheyHaveCorrectInfos()
			throws Exception {

		prepareSystem(
				withZeCollection().withConstellioRMModule().withAllTest(users).withRMTest(records)
						.withFoldersAndContainersOfEveryStatus()
		);

		assertThat(records.getStorageSpaceS01().getCode()).isEqualTo("S01");
		assertThat(records.getStorageSpaceS01().getTitle()).isEqualTo("Etagere 1");

		assertThat(records.getStorageSpaceS01_01().getCode()).isEqualTo("S01-01");
		assertThat(records.getStorageSpaceS01_01().getTitle()).isEqualTo("Tablette 1");
		assertThat(records.getStorageSpaceS01_01().getParentStorageSpace()).isEqualTo("S01");

		assertThat(records.getStorageSpaceS01_02().getCode()).isEqualTo("S01-02");
		assertThat(records.getStorageSpaceS01_02().getTitle()).isEqualTo("Tablette 2");
		assertThat(records.getStorageSpaceS01_02().getParentStorageSpace()).isEqualTo("S01");

		assertThat(records.getStorageSpaceS02().getCode()).isEqualTo("S02");
		assertThat(records.getStorageSpaceS02().getTitle()).isEqualTo("Etagere 2");

		assertThat(records.getStorageSpaceS02_01().getCode()).isEqualTo("S02-01");
		assertThat(records.getStorageSpaceS02_01().getTitle()).isEqualTo("Tablette 1");
		assertThat(records.getStorageSpaceS02_01().getParentStorageSpace()).isEqualTo("S02");

		assertThat(records.getStorageSpaceS02_02().getCode()).isEqualTo("S02-02");
		assertThat(records.getStorageSpaceS02_02().getTitle()).isEqualTo("Tablette 2");
		assertThat(records.getStorageSpaceS02_02().getParentStorageSpace()).isEqualTo("S02");
	}

	@Test
	public void givenTestRecordsWithListThenTheyHaveCorrectInfos()
			throws Exception {

		prepareSystem(
				withZeCollection().withConstellioRMModule().withAllTest(users).withRMTest(records)
						.withFoldersAndContainersOfEveryStatus()
		);

		DecommissioningList list01 = records.getList01();
		assertThat(list01.getId()).isEqualTo("list01");
		assertThat(list01.isUniform()).isFalse();
		assertThat(list01.getDecommissioningListType()).isEqualTo(DecommissioningListType.FOLDERS_TO_DESTROY);
		assertThat(list01.getFoldersMediaTypes()).containsExactly(ANALOG, HYBRID, UNKNOWN, ELECTRONIC, ANALOG, UNKNOWN);
		assertThat(list01.getStatus()).isEqualTo(DecomListStatus.GENERATED);
		assertThat(list01.getAdministrativeUnit()).isEqualTo(records.unitId_10a);
		assertThat(list01.getFolders()).containsOnlyOnce(records.folder_A42, records.folder_A43, records.folder_A44,
				records.folder_A45, records.folder_A46, records.folder_A47);
		assertThat(list01.getContainers()).containsOnlyOnce(records.containerId_bac18, records.containerId_bac19);

		assertThat(records.getList11().getContainers()).isEmpty();
		assertThat(records.getList11().getStatus()).isEqualTo(DecomListStatus.PROCESSED);

		assertThat(records.getList12().getContainers())
				.containsOnlyOnce(records.containerId_bac10, records.containerId_bac11, records.containerId_bac12);
		assertThat(records.getList12().getStatus()).isEqualTo(DecomListStatus.PROCESSED);

		assertThat(records.getList13().getContainers()).containsOnlyOnce(records.containerId_bac13);
		assertThat(records.getList13().getStatus()).isEqualTo(DecomListStatus.PROCESSED);

		assertThat(records.getList14().getContainers()).containsOnlyOnce(records.containerId_bac05);
		assertThat(records.getList14().getStatus()).isEqualTo(DecomListStatus.PROCESSED);

		DecommissioningList list15 = getListOrNull(records.list_15);
		assertThat(list15.getId()).isEqualTo("list15");
		assertThat(list15.isUniform()).isTrue();
		assertThat(list15.getDecommissioningListType()).isEqualTo(DecommissioningListType.FOLDERS_TO_DEPOSIT);
		assertThat(list15.getFoldersMediaTypes()).containsExactly(ELECTRONIC, ELECTRONIC, ELECTRONIC);
		assertThat(list15.getStatus()).isEqualTo(DecomListStatus.PROCESSED);
		assertThat(list15.getAdministrativeUnit()).isEqualTo(records.unitId_10a);
		assertThat(list15.getContainers()).containsOnlyOnce(records.containerId_bac04);

		assertThat(getListOrNull(records.list_30)).isNull();
		assertThat(getListOrNull(records.list_31)).isNull();
		assertThat(getListOrNull(records.list_32)).isNull();
		assertThat(getListOrNull(records.list_33)).isNull();
		assertThat(getListOrNull(records.list_34)).isNull();
		assertThat(getListOrNull(records.list_35)).isNull();
		assertThat(getListOrNull(records.list_36)).isNull();

	}

	private DecommissioningList getListOrNull(String listId) {
		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
		try {
			return rm.getDecommissioningList(listId);
		} catch (NoSuchRecordWithId e) {
			return null;
		}
	}

	@Test
	public void givenTestRecordsWithContainersThenTheyHaveCorrectInfos()
			throws Exception {

		prepareSystem(
				withZeCollection().withConstellioRMModule().withAllTest(users).withRMTest(records)
						.withFoldersAndContainersOfEveryStatus()
		);
		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
		assertThat(records.getContainerBac01().getId()).isEqualTo("bac01");
		assertThat(records.getContainerBac01().getTemporaryIdentifier()).isEqualTo("30_C_01");
		assertThat(records.getContainerBac01().getStorageSpace()).isEqualTo("S02-02");
		assertThat(records.getContainerBac01().getAdministrativeUnit()).isEqualTo(records.unitId_30c);
		assertThat(records.getContainerBac01().getRealTransferDate()).isEqualTo(localDate(2007, 10, 31));
		assertThat(records.getContainerBac01().getRealDepositDate()).isEqualTo(localDate(2011, 2, 13));
		assertThat(records.getContainerBac01().getDecommissioningType()).isEqualTo(DecommissioningType.DEPOSIT);
		assertThat(records.getContainerBac01().isFull()).isTrue();

		assertThat(records.getContainerBac02().getId()).isEqualTo("bac02");
		assertThat(records.getContainerBac02().getTemporaryIdentifier()).isEqualTo("12_B_01");
		assertThat(records.getContainerBac02().getStorageSpace()).isNull();
		assertThat(records.getContainerBac02().getAdministrativeUnit()).isEqualTo(records.unitId_12b);
		assertThat(records.getContainerBac02().getRealTransferDate()).isEqualTo(localDate(2007, 10, 31));
		assertThat(records.getContainerBac02().getRealDepositDate()).isEqualTo(localDate(2011, 2, 13));
		assertThat(records.getContainerBac02().getDecommissioningType()).isEqualTo(DecommissioningType.DEPOSIT);
		assertThat(records.getContainerBac02().isFull()).isFalse();

		assertThat(records.getContainerBac03().getId()).isEqualTo("bac03");
		assertThat(records.getContainerBac03().getTemporaryIdentifier()).isEqualTo("11_B_01");
		assertThat(records.getContainerBac03().getStorageSpace()).isEqualTo("S02-02");
		assertThat(records.getContainerBac03().getAdministrativeUnit()).isEqualTo(records.unitId_11b);
		assertThat(records.getContainerBac03().getRealTransferDate()).isEqualTo(localDate(2006, 10, 31));
		assertThat(records.getContainerBac03().getRealDepositDate()).isEqualTo(localDate(2009, 8, 17));
		assertThat(records.getContainerBac03().getDecommissioningType()).isEqualTo(DecommissioningType.DEPOSIT);
		assertThat(records.getContainerBac03().isFull()).isFalse();

		assertThat(records.getContainerBac04().getId()).isEqualTo("bac04");
		assertThat(records.getContainerBac04().getTemporaryIdentifier()).isEqualTo("10_A_01");
		assertThat(records.getContainerBac04().getStorageSpace()).isEqualTo("S01-02");
		assertThat(records.getContainerBac04().getAdministrativeUnit()).isEqualTo(records.unitId_10a);
		assertThat(records.getContainerBac04().getRealTransferDate()).isEqualTo(localDate(2007, 10, 31));
		assertThat(records.getContainerBac04().getRealDepositDate()).isEqualTo(localDate(2010, 8, 17));
		assertThat(records.getContainerBac04().getDecommissioningType()).isEqualTo(DecommissioningType.DEPOSIT);
		assertThat(records.getContainerBac04().isFull()).isFalse();

		assertThat(records.getContainerBac05().getId()).isEqualTo("bac05");
		assertThat(records.getContainerBac05().getTemporaryIdentifier()).isEqualTo("10_A_02");
		assertThat(records.getContainerBac05().getStorageSpace()).isEqualTo("S01-02");
		assertThat(records.getContainerBac05().getAdministrativeUnit()).isEqualTo(records.unitId_10a);
		assertThat(records.getContainerBac05().getRealTransferDate()).isEqualTo(localDate(2008, 10, 31));
		assertThat(records.getContainerBac05().getRealDepositDate()).isEqualTo(localDate(2012, 5, 15));
		assertThat(records.getContainerBac05().getDecommissioningType()).isEqualTo(DecommissioningType.DEPOSIT);
		assertThat(records.getContainerBac05().isFull()).isTrue();

		assertThat(records.getContainerBac06().getId()).isEqualTo("bac06");
		assertThat(records.getContainerBac06().getTemporaryIdentifier()).isEqualTo("30_C_02");
		assertThat(records.getContainerBac06().getStorageSpace()).isNull();
		assertThat(records.getContainerBac06().getAdministrativeUnit()).isEqualTo(records.unitId_30c);
		assertThat(records.getContainerBac06().getRealTransferDate()).isEqualTo(localDate(2006, 10, 31));
		assertThat(records.getContainerBac06().getRealDepositDate()).isNull();
		assertThat(records.getContainerBac06().getDecommissioningType()).isEqualTo(DecommissioningType.TRANSFERT_TO_SEMI_ACTIVE);
		assertThat(records.getContainerBac06().isFull()).isFalse();

		assertThat(records.getContainerBac07().getId()).isEqualTo("bac07");
		assertThat(records.getContainerBac07().getTemporaryIdentifier()).isEqualTo("30_C_03");
		assertThat(records.getContainerBac07().getStorageSpace()).isEqualTo("S02-01");
		assertThat(records.getContainerBac07().getAdministrativeUnit()).isEqualTo(records.unitId_30c);
		assertThat(records.getContainerBac07().getRealTransferDate()).isEqualTo(localDate(2007, 10, 31));
		assertThat(records.getContainerBac07().getRealDepositDate()).isNull();
		assertThat(records.getContainerBac07().getDecommissioningType()).isEqualTo(DecommissioningType.TRANSFERT_TO_SEMI_ACTIVE);
		assertThat(records.getContainerBac07().isFull()).isFalse();

		assertThat(records.getContainerBac08().getId()).isEqualTo("bac08");
		assertThat(records.getContainerBac08().getTemporaryIdentifier()).isEqualTo("12_B_02");
		assertThat(records.getContainerBac08().getStorageSpace()).isEqualTo("S02-01");
		assertThat(records.getContainerBac08().getAdministrativeUnit()).isEqualTo(records.unitId_12b);
		assertThat(records.getContainerBac08().getRealTransferDate()).isEqualTo(localDate(2007, 10, 31));
		assertThat(records.getContainerBac08().getRealDepositDate()).isNull();
		assertThat(records.getContainerBac08().getDecommissioningType()).isEqualTo(DecommissioningType.TRANSFERT_TO_SEMI_ACTIVE);
		assertThat(records.getContainerBac08().isFull()).isFalse();

		assertThat(records.getContainerBac09().getId()).isEqualTo("bac09");
		assertThat(records.getContainerBac09().getTemporaryIdentifier()).isEqualTo("11_B_02");
		assertThat(records.getContainerBac09().getStorageSpace()).isEqualTo("S02-01");
		assertThat(records.getContainerBac09().getAdministrativeUnit()).isEqualTo(records.unitId_11b);
		assertThat(records.getContainerBac09().getRealTransferDate()).isEqualTo(localDate(2006, 10, 31));
		assertThat(records.getContainerBac09().getRealDepositDate()).isNull();
		assertThat(records.getContainerBac09().getDecommissioningType()).isEqualTo(DecommissioningType.TRANSFERT_TO_SEMI_ACTIVE);
		assertThat(records.getContainerBac09().isFull()).isFalse();

		assertThat(records.getContainerBac10().getId()).isEqualTo("bac10");
		assertThat(records.getContainerBac10().getTemporaryIdentifier()).isEqualTo("10_A_03");
		assertThat(records.getContainerBac10().getStorageSpace()).isNull();
		assertThat(records.getContainerBac10().getAdministrativeUnit()).isEqualTo(records.unitId_10a);
		assertThat(records.getContainerBac10().getRealTransferDate()).isEqualTo(localDate(2007, 10, 31));
		assertThat(records.getContainerBac10().getRealDepositDate()).isNull();
		assertThat(records.getContainerBac10().getDecommissioningType()).isEqualTo(DecommissioningType.TRANSFERT_TO_SEMI_ACTIVE);
		assertThat(records.getContainerBac10().isFull()).isTrue();

		assertThat(records.getContainerBac11().getId()).isEqualTo("bac11");
		assertThat(records.getContainerBac11().getTemporaryIdentifier()).isEqualTo("10_A_04");
		assertThat(records.getContainerBac11().getStorageSpace()).isEqualTo("S01-01");
		assertThat(records.getContainerBac11().getAdministrativeUnit()).isEqualTo(records.unitId_10a);
		assertThat(records.getContainerBac11().getRealTransferDate()).isEqualTo(localDate(2005, 10, 31));
		assertThat(records.getContainerBac11().getRealDepositDate()).isNull();
		assertThat(records.getContainerBac11().getDecommissioningType()).isEqualTo(DecommissioningType.TRANSFERT_TO_SEMI_ACTIVE);
		assertThat(records.getContainerBac11().isFull()).isFalse();

		assertThat(records.getContainerBac12().getId()).isEqualTo("bac12");
		assertThat(records.getContainerBac12().getTemporaryIdentifier()).isEqualTo("10_A_05");
		assertThat(records.getContainerBac12().getStorageSpace()).isEqualTo("S01-01");
		assertThat(records.getContainerBac12().getAdministrativeUnit()).isEqualTo(records.unitId_10a);
		assertThat(records.getContainerBac12().getRealTransferDate()).isEqualTo(localDate(2006, 10, 31));
		assertThat(records.getContainerBac12().getRealDepositDate()).isNull();
		assertThat(records.getContainerBac12().getDecommissioningType()).isEqualTo(DecommissioningType.TRANSFERT_TO_SEMI_ACTIVE);
		assertThat(records.getContainerBac12().isFull()).isFalse();

		assertThat(records.getContainerBac13().getId()).isEqualTo("bac13");
		assertThat(records.getContainerBac13().getTemporaryIdentifier()).isEqualTo("10_A_06");
		assertThat(records.getContainerBac13().getStorageSpace()).isEqualTo("S01-01");
		assertThat(records.getContainerBac13().getAdministrativeUnit()).isEqualTo(records.unitId_10a);
		assertThat(records.getContainerBac13().getRealTransferDate()).isEqualTo(localDate(2008, 10, 31));
		assertThat(records.getContainerBac13().getRealDepositDate()).isNull();
		assertThat(records.getContainerBac13().getDecommissioningType()).isEqualTo(DecommissioningType.TRANSFERT_TO_SEMI_ACTIVE);
		assertThat(records.getContainerBac13().isFull()).isFalse();

	}

	@Test
	public void givenTestRecordsWithRetentionRulesThenTheyHaveCorrectInfos()
			throws Exception {
		prepareSystem(
				withZeCollection().withConstellioRMModule().withAllTest(users).withRMTest(records)
						.withFoldersAndContainersOfEveryStatus()
		);
		assertThat(records.getRule1().getCopyRulesComment())
				.isEqualTo(Arrays.asList("R1:comment1", "R2:comment2", "R3:comment3", "R4:comment4"));
		assertThat(records.getRule1().getDescription()).isEqualTo("Description Rule 1");
		assertThat(records.getRule1().getJuridicReference()).isEqualTo("Juridic reference Rule 1");
		assertThat(records.getRule1().getGeneralComment()).isEqualTo("General Comment Rule 1");
		assertThat(records.getRule1().getKeywords()).containsExactly("Rule #1");
		assertThat(records.getRule1().getCorpus()).isEqualTo("Corpus Rule 1");
	}

	@Test
	public void givenTestRecordsWithFoldersThenFoldersHaveCorrectInfos()
			throws Exception {
		prepareSystem(
				withZeCollection().withConstellioRMModule().withAllTest(users).withRMTest(records)
						.withFoldersAndContainersOfEveryStatus()
		);
		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
		SearchServices searchServices = getModelLayerFactory().newSearchServices();
		LogicalSearchCondition condition = from(rm.folderSchemaType()).returnAll();
		assertThat(searchServices.getResultsCount(condition)).isEqualTo(105);
		assertThat(records.getFolder_A01())
				.has(openDate(2000, 10, 4))
				.has(noCloseDate())
				.has(noPlanifiedTransferDate())
				.has(noPlanifiedDepositDate())
				.has(noPlanifiedDestructionDate());

		assertThat(records.getFolder_A02())
				.has(openDate(2000, 11, 4))
				.has(noCloseDate())
				.has(noPlanifiedTransferDate())
				.has(noPlanifiedDepositDate())
				.has(noPlanifiedDestructionDate());

		assertThat(records.getFolder_A03())
				.has(openDate(2000, 11, 5))
				.has(noCloseDate())
				.has(noPlanifiedTransferDate())
				.has(noPlanifiedDepositDate())
				.has(noPlanifiedDestructionDate());

		assertThat(records.getFolder_A04())
				.has(openDate(2000, 10, 4))
				.has(noCloseDate())
				.has(noPlanifiedTransferDate())
				.has(noPlanifiedDepositDate())
				.has(noPlanifiedDestructionDate());

		assertThat(records.getFolder_A05())
				.has(openDate(2000, 11, 4))
				.has(noCloseDate())
				.has(noPlanifiedTransferDate())
				.has(noPlanifiedDepositDate())
				.has(noPlanifiedDestructionDate());

		assertThat(records.getFolder_A06())
				.has(openDate(2000, 11, 5))
				.has(noCloseDate())
				.has(noPlanifiedTransferDate())
				.has(noPlanifiedDepositDate())
				.has(noPlanifiedDestructionDate());

		assertThat(records.getFolder_A07())
				.has(openDate(2000, 10, 4))
				.has(noCloseDate())
				.has(noPlanifiedTransferDate())
				.has(noPlanifiedDepositDate())
				.has(noPlanifiedDestructionDate());

		assertThat(records.getFolder_A08())
				.has(openDate(2000, 11, 4))
				.has(noCloseDate())
				.has(noPlanifiedTransferDate())
				.has(noPlanifiedDepositDate())
				.has(noPlanifiedDestructionDate());

		assertThat(records.getFolder_A09())
				.has(openDate(2000, 11, 5))
				.has(noCloseDate())
				.has(noPlanifiedTransferDate())
				.has(noPlanifiedDepositDate())
				.has(noPlanifiedDestructionDate());

		assertThat(records.getFolder_A10())
				.has(openDate(2000, 10, 4))
				.has(closeDate(2001, 10, 31))
				.has(planifiedTransferDate(2006, 10, 31))
				.has(planifiedDepositDate(2008, 10, 31))
				.has(planifiedDestructionDate(2008, 10, 31));

		assertThat(records.getFolder_A11())
				.has(openDate(2000, 11, 4))
				.has(closeDate(2001, 10, 31))
				.has(planifiedTransferDate(2006, 10, 31))
				.has(planifiedDepositDate(2008, 10, 31))
				.has(planifiedDestructionDate(2008, 10, 31));

		assertThat(records.getFolder_A12())
				.has(openDate(2000, 11, 5))
				.has(closeDate(2002, 10, 31))
				.has(planifiedTransferDate(2007, 10, 31))
				.has(planifiedDepositDate(2009, 10, 31))
				.has(planifiedDestructionDate(2009, 10, 31));

		assertThat(records.getFolder_A13())
				.has(openDate(2000, 10, 4))
				.has(closeDate(2001, 10, 31))
				.has(noPlanifiedTransferDate())
				.has(noPlanifiedDepositDate())
				.has(planifiedDestructionDate(2003, 10, 31));

		assertThat(records.getFolder_A14())
				.has(openDate(2000, 11, 4))
				.has(closeDate(2001, 10, 31))
				.has(noPlanifiedTransferDate())
				.has(noPlanifiedDepositDate())
				.has(planifiedDestructionDate(2003, 10, 31));

		assertThat(records.getFolder_A15())
				.has(openDate(2000, 11, 5))
				.has(closeDate(2002, 10, 31))
				.has(noPlanifiedTransferDate())
				.has(noPlanifiedDepositDate())
				.has(planifiedDestructionDate(2004, 10, 31));

		assertThat(records.getFolder_A16())
				.has(openDate(2000, 10, 4))
				.has(closeDate(2001, 10, 31))
				.has(planifiedTransferDate(2002, 10, 31))
				.has(planifiedDepositDate(2007, 10, 31))
				.has(noPlanifiedDestructionDate());

		assertThat(records.getFolder_A17())
				.has(openDate(2000, 11, 4))
				.has(closeDate(2001, 10, 31))
				.has(planifiedTransferDate(2002, 10, 31))
				.has(planifiedDepositDate(2007, 10, 31))
				.has(noPlanifiedDestructionDate());

		assertThat(records.getFolder_A18())
				.has(mediumTypes(records.PA, records.MD))
				.has(openDate(2000, 11, 5))
				.has(closeDate(2002, 10, 31))
				.has(planifiedTransferDate(2003, 10, 31))
				.has(planifiedDepositDate(2008, 10, 31))
				.has(noPlanifiedDestructionDate());

		assertThat(records.getFolder_A19())
				.has(mediumTypes(records.PA))
				.has(openDate(2000, 10, 4))
				.has(closeDate(2001, 10, 31))
				.has(planifiedTransferDate(2002, 10, 31))
				.has(planifiedDepositDate(2006, 10, 31))
				.has(planifiedDestructionDate(2006, 10, 31));

		assertThat(records.getFolder_A20())
				.has(noMediumTypes())
				.has(openDate(2000, 11, 4))
				.has(closeDate(2001, 10, 31))
				.has(planifiedTransferDate(2002, 10, 31))
				.has(planifiedDepositDate(2006, 10, 31))
				.has(planifiedDestructionDate(2006, 10, 31));

		assertThat(records.getFolder_A21())
				.has(mediumTypes(records.MD))
				.has(openDate(2000, 11, 5))
				.has(closeDate(2002, 10, 31))
				.has(planifiedTransferDate(2003, 10, 31))
				.has(planifiedDepositDate(2007, 10, 31))
				.has(planifiedDestructionDate(2007, 10, 31));

		assertThat(records.getFolder_A22())
				.has(openDate(2000, 5, 4))
				.has(closeDate(2002, 10, 31))
				.has(copyRule(copyBuilder.newPrincipal(asList(records.PA), "3-888-D")))
				.has(planifiedTransferDate(2005, 10, 31))
				.has(noPlanifiedDepositDate())
				.has(planifiedDestructionDate(2006, 10, 31));

		assertThat(records.getFolder_A23())
				.has(openDate(2000, 7, 4))
				.has(closeDate(2002, 10, 31))
				.has(copyRule(copyBuilder.newPrincipal(asList(records.PA), "3-888-D")))
				.has(planifiedTransferDate(2005, 10, 31))
				.has(noPlanifiedDepositDate())
				.has(planifiedDestructionDate(2006, 10, 31));

		assertThat(records.getFolder_A24())
				.has(openDate(2000, 7, 5))
				.has(closeDate(2003, 10, 31))
				.has(copyRule(copyBuilder.newPrincipal(asList(records.PA), "3-888-D")))
				.has(planifiedTransferDate(2006, 10, 31))
				.has(noPlanifiedDepositDate())
				.has(planifiedDestructionDate(2007, 10, 31));

		assertThat(records.getFolder_A25())
				.has(openDate(2000, 6, 4))
				.has(closeDate(2002, 10, 31))
				.has(copyRule(copyBuilder.newPrincipal(asList(records.MD), "3-888-C")))
				.has(planifiedTransferDate(2005, 10, 31))
				.has(planifiedDepositDate(2006, 10, 31))
				.has(noPlanifiedDestructionDate());

		assertThat(records.getFolder_A26())
				.has(openDate(2000, 7, 4))
				.has(closeDate(2002, 10, 31))
				.has(copyRule(copyBuilder.newPrincipal(asList(records.MD), "3-888-C")))
				.has(planifiedTransferDate(2005, 10, 31))
				.has(planifiedDepositDate(2006, 10, 31))
				.has(noPlanifiedDestructionDate());

		assertThat(records.getFolder_A27())
				.has(openDate(2000, 7, 5))
				.has(closeDate(2003, 10, 31))
				.has(copyRule(copyBuilder.newPrincipal(asList(records.MD), "3-888-C")))
				.has(planifiedTransferDate(2006, 10, 31))
				.has(planifiedDepositDate(2007, 10, 31))
				.has(noPlanifiedDestructionDate());

		assertThat(records.getFolder_A42())
				.has(mediumTypes(records.PA))
				.has(mediaType(ANALOG))
				.has(openDate(2000, 10, 4))
				.has(closeDate(2001, 10, 31))
				.has(actualTransferDate(2007, 10, 31))
				.has(planifiedDepositDate(2009, 10, 31))
				.has(planifiedDestructionDate(2009, 10, 31))
				.has(container("bac13"));

		assertThat(records.getFolder_A43())
				.has(mediumTypes(records.PA, records.MD))
				.has(mediaType(HYBRID))
				.has(openDate(2000, 11, 4))
				.has(closeDate(2001, 10, 31))
				.has(actualTransferDate(2007, 10, 31))
				.has(planifiedDepositDate(2009, 10, 31))
				.has(planifiedDestructionDate(2009, 10, 31))
				.has(container("bac13"));

		assertThat(records.getFolder_A44())
				.has(noMediumTypes())
				.has(mediaType(UNKNOWN))
				.has(openDate(2000, 11, 5))
				.has(closeDate(2002, 10, 31))
				.has(actualTransferDate(2008, 10, 31))
				.has(planifiedDepositDate(2010, 10, 31))
				.has(planifiedDestructionDate(2010, 10, 31))
				.has(container("bac13"));

		assertThat(records.getFolder_A45())
				.has(mediumTypes(records.MD))
				.has(mediaType(ELECTRONIC))
				.has(openDate(2000, 10, 4))
				.has(closeDate(2001, 10, 31))
				.has(noPlanifiedTransferDate())
				.has(noPlanifiedDepositDate())
				.has(planifiedDestructionDate(2003, 10, 31))
				.has(container("bac12"));

		assertThat(records.getFolder_A46())
				.has(mediumTypes(records.PA))
				.has(mediaType(ANALOG))
				.has(openDate(2000, 11, 4))
				.has(closeDate(2001, 10, 31))
				.has(noPlanifiedTransferDate())
				.has(noPlanifiedDepositDate())
				.has(planifiedDestructionDate(2003, 10, 31))
				.has(container("bac12"));

		assertThat(records.getFolder_A47())
				.has(noMediumTypes())
				.has(mediaType(UNKNOWN))
				.has(openDate(2000, 11, 5))
				.has(closeDate(2002, 10, 31))
				.has(noPlanifiedTransferDate())
				.has(noPlanifiedDepositDate())
				.has(planifiedDestructionDate(2004, 10, 31))
				.has(container("bac12"));

		assertThat(records.getFolder_A48())
				.has(mediumTypes(records.MD))
				.has(openDate(2000, 10, 4))
				.has(closeDate(2001, 10, 31))
				.has(actualTransferDate(2004, 10, 31))
				.has(planifiedDepositDate(2009, 10, 31))
				.has(noPlanifiedDestructionDate())
				.has(container(null));

		assertThat(records.getFolder_A49())
				.has(openDate(2000, 11, 4))
				.has(closeDate(2001, 10, 31))
				.has(actualTransferDate(2004, 10, 31))
				.has(planifiedDepositDate(2009, 10, 31))
				.has(noPlanifiedDestructionDate())
				.has(container("bac11"));

		assertThat(records.getFolder_A50())
				.has(openDate(2000, 11, 5))
				.has(closeDate(2002, 10, 31))
				.has(actualTransferDate(2005, 10, 31))
				.has(planifiedDepositDate(2010, 10, 31))
				.has(noPlanifiedDestructionDate())
				.has(container("bac11"));

		assertThat(records.getFolder_A51())
				.has(openDate(2000, 10, 4))
				.has(closeDate(2001, 10, 31))
				.has(actualTransferDate(2004, 10, 31))
				.has(planifiedDepositDate(2008, 10, 31))
				.has(planifiedDestructionDate(2008, 10, 31))
				.has(container("bac10"));

		assertThat(records.getFolder_A52())
				.has(openDate(2000, 11, 4))
				.has(closeDate(2001, 10, 31))
				.has(actualTransferDate(2004, 10, 31))
				.has(planifiedDepositDate(2008, 10, 31))
				.has(planifiedDestructionDate(2008, 10, 31))
				.has(container("bac10"));

		assertThat(records.getFolder_A53())
				.has(openDate(2000, 11, 5))
				.has(closeDate(2002, 10, 31))
				.has(actualTransferDate(2005, 10, 31))
				.has(planifiedDepositDate(2009, 10, 31))
				.has(planifiedDestructionDate(2009, 10, 31))
				.has(container("bac10"));

		assertThat(records.getFolder_A54())
				.has(openDate(2000, 5, 4))
				.has(closeDate(2002, 10, 31))
				.has(actualTransferDate(2006, 10, 31))
				.has(noPlanifiedDepositDate())
				.has(planifiedDestructionDate(2007, 10, 31))
				.has(container("bac10"));

		assertThat(records.getFolder_A55())
				.has(openDate(2000, 7, 4))
				.has(closeDate(2002, 10, 31))
				.has(actualTransferDate(2006, 10, 31))
				.has(noPlanifiedDepositDate())
				.has(planifiedDestructionDate(2007, 10, 31))
				.has(container("bac10"));

		assertThat(records.getFolder_A56())
				.has(openDate(2000, 7, 5))
				.has(closeDate(2003, 10, 31))
				.has(actualTransferDate(2007, 10, 31))
				.has(noPlanifiedDepositDate())
				.has(planifiedDestructionDate(2008, 10, 31))
				.has(container("bac10"));

		assertThat(records.getFolder_A57())
				.has(openDate(2000, 6, 4))
				.has(closeDate(2002, 10, 31))
				.has(actualTransferDate(2006, 10, 31))
				.has(planifiedDepositDate(2007, 10, 31))
				.has(noPlanifiedDestructionDate())
				.has(container("bac10"));

		assertThat(records.getFolder_A58())
				.has(openDate(2000, 7, 4))
				.has(closeDate(2002, 10, 31))
				.has(actualTransferDate(2006, 10, 31))
				.has(planifiedDepositDate(2007, 10, 31))
				.has(noPlanifiedDestructionDate())
				.has(container("bac10"));

		assertThat(records.getFolder_A59())
				.has(openDate(2000, 7, 5))
				.has(closeDate(2003, 10, 31))
				.has(actualTransferDate(2007, 10, 31))
				.has(planifiedDepositDate(2008, 10, 31))
				.has(noPlanifiedDestructionDate())
				.has(container("bac10"));

		assertThat(records.getFolder_A79())
				.has(openDate(2000, 10, 4))
				.has(closeDate(2001, 10, 31))
				.has(actualDepositDate(2011, 2, 13))
				.has(noPlanifiedDestructionDate())
				.has(container("bac05"));

		assertThat(records.getFolder_A80())
				.has(openDate(2000, 11, 4))
				.has(closeDate(2001, 10, 31))
				.has(noPlanifiedDepositDate())
				.has(actualDestructionDate(2011, 2, 13));

		assertThat(records.getFolder_A81())
				.has(openDate(2000, 11, 5))
				.has(closeDate(2002, 10, 31))
				.has(actualDepositDate(2012, 2, 13))
				.has(noPlanifiedDestructionDate())
				.has(container("bac05"));

		assertThat(records.getFolder_A82())
				.has(openDate(2000, 10, 4))
				.has(closeDate(2001, 10, 31))
				.has(noPlanifiedDepositDate())
				.has(actualDestructionDate(2007, 4, 14));

		assertThat(records.getFolder_A83())
				.has(openDate(2000, 11, 4))
				.has(closeDate(2001, 10, 31))
				.has(noPlanifiedDepositDate())
				.has(actualDestructionDate(2007, 4, 14));

		assertThat(records.getFolder_A84())
				.has(openDate(2000, 11, 5))
				.has(closeDate(2002, 10, 31))
				.has(noPlanifiedDepositDate())
				.has(actualDestructionDate(2008, 4, 14));

		assertThat(records.getFolder_A85())
				.has(openDate(2000, 10, 4))
				.has(closeDate(2001, 10, 31))
				.has(actualDepositDate(2011, 5, 15))
				.has(noPlanifiedDestructionDate())
				.has(container("bac05"));

		assertThat(records.getFolder_A86())
				.has(openDate(2000, 11, 4))
				.has(closeDate(2001, 10, 31))
				.has(actualDepositDate(2011, 5, 15))
				.has(noPlanifiedDestructionDate())
				.has(container("bac05"));

		assertThat(records.getFolder_A87())
				.has(openDate(2000, 11, 5))
				.has(closeDate(2002, 10, 31))
				.has(actualDepositDate(2012, 5, 15))
				.has(noPlanifiedDestructionDate())
				.has(container("bac05"));

		assertThat(records.getFolder_A88())
				.has(openDate(2000, 10, 4))
				.has(closeDate(2001, 10, 31))
				.has(noPlanifiedDepositDate())
				.has(actualDestructionDate(2011, 6, 16));

		assertThat(records.getFolder_A89())
				.has(openDate(2000, 11, 4))
				.has(closeDate(2001, 10, 31))
				.has(actualDepositDate(2011, 6, 16))
				.has(noPlanifiedDestructionDate())
				.has(container("bac05"));

		assertThat(records.getFolder_A90())
				.has(openDate(2000, 11, 5))
				.has(closeDate(2002, 10, 31))
				.has(noPlanifiedDepositDate())
				.has(actualDestructionDate(2012, 6, 16));

		assertThat(records.getFolder_A91())
				.has(openDate(2000, 5, 4))
				.has(closeDate(2002, 10, 31))
				.has(noPlanifiedDepositDate())
				.has(actualDestructionDate(2009, 7, 16));

		assertThat(records.getFolder_A92())
				.has(openDate(2000, 7, 4))
				.has(closeDate(2002, 10, 31))
				.has(noPlanifiedDepositDate())
				.has(actualDestructionDate(2009, 7, 16));

		assertThat(records.getFolder_A93())
				.has(openDate(2000, 7, 5))
				.has(closeDate(2003, 10, 31))
				.has(noPlanifiedDepositDate())
				.has(actualDestructionDate(2010, 7, 16));

		assertThat(records.getFolder_A94())
				.has(openDate(2000, 6, 4))
				.has(closeDate(2002, 10, 31))
				.has(actualDepositDate(2009, 8, 17))
				.has(noPlanifiedDestructionDate())
				.has(container("bac04"));

		assertThat(records.getFolder_A95())
				.has(openDate(2000, 7, 4))
				.has(closeDate(2002, 10, 31))
				.has(actualDepositDate(2009, 8, 17))
				.has(noPlanifiedDestructionDate())
				.has(container("bac04"));

		assertThat(records.getFolder_A96())
				.has(openDate(2000, 7, 5))
				.has(closeDate(2003, 10, 31))
				.has(actualDepositDate(2010, 8, 17))
				.has(noPlanifiedDestructionDate())
				.has(container("bac04"));

		assertThat(records.getFolder_B01())
				.has(openDate(2000, 10, 4))
				.has(noCloseDate())
				.has(noPlanifiedTransferDate())
				.has(noPlanifiedDepositDate())
				.has(noPlanifiedDestructionDate());

		assertThat(records.getFolder_B02())
				.has(openDate(2000, 10, 4))
				.has(noCloseDate())
				.has(noPlanifiedTransferDate())
				.has(noPlanifiedDepositDate())
				.has(noPlanifiedDestructionDate());

		assertThat(records.getFolder_B03())
				.has(openDate(2000, 10, 4))
				.has(noCloseDate())
				.has(noPlanifiedTransferDate())
				.has(noPlanifiedDepositDate())
				.has(noPlanifiedDestructionDate());

		assertThat(records.getFolder_B04())
				.has(openDate(2000, 10, 4))
				.has(closeDate(2001, 10, 31))
				.has(planifiedTransferDate(2006, 10, 31))
				.has(planifiedDepositDate(2008, 10, 31))
				.has(planifiedDestructionDate(2008, 10, 31));

		assertThat(records.getFolder_B05())
				.has(openDate(2000, 10, 4))
				.has(closeDate(2001, 10, 31))
				.has(noPlanifiedTransferDate())
				.has(noPlanifiedDepositDate())
				.has(planifiedDestructionDate(2003, 10, 31));

		assertThat(records.getFolder_B06())
				.has(openDate(2000, 10, 4))
				.has(closeDate(2001, 10, 31))
				.has(copyType(CopyType.SECONDARY))
				.has(noPlanifiedTransferDate())
				.has(noPlanifiedDepositDate())
				.has(planifiedDestructionDate(2002, 10, 31));

		assertThat(records.getFolder_B07())
				.has(openDate(2000, 10, 4))
				.has(closeDate(2001, 10, 31))
				.has(planifiedTransferDate(2002, 10, 31))
				.has(planifiedDepositDate(2006, 10, 31))
				.has(planifiedDestructionDate(2006, 10, 31));

		assertThat(records.getFolder_B08())
				.has(openDate(2000, 5, 4))
				.has(closeDate(2002, 10, 31))
				.has(planifiedTransferDate(2005, 10, 31))
				.has(noPlanifiedDepositDate())
				.has(planifiedDestructionDate(2006, 10, 31));

		assertThat(records.getFolder_B09())
				.has(openDate(2000, 6, 4))
				.has(closeDate(2002, 10, 31))
				.has(planifiedTransferDate(2005, 10, 31))
				.has(planifiedDepositDate(2006, 10, 31))
				.has(noPlanifiedDestructionDate());

		assertThat(records.getFolder_B30())
				.has(openDate(2000, 10, 4))
				.has(closeDate(2001, 10, 31))
				.has(actualTransferDate(2007, 10, 31))
				.has(planifiedDepositDate(2009, 10, 31))
				.has(planifiedDestructionDate(2009, 10, 31))
				.has(container("bac08"));

		assertThat(records.getFolder_B31())
				.has(openDate(2000, 10, 4))
				.has(closeDate(2001, 10, 31))
				.has(noPlanifiedTransferDate())
				.has(noPlanifiedDepositDate())
				.has(planifiedDestructionDate(2003, 10, 31))
				.has(container("bac09"));

		assertThat(records.getFolder_B32())
				.has(openDate(2000, 10, 4))
				.has(closeDate(2001, 10, 31))
				.has(noPlanifiedTransferDate())
				.has(noPlanifiedDepositDate())
				.has(planifiedDestructionDate(2002, 10, 31))
				.has(container("bac08"));

		assertThat(records.getFolder_B33())
				.has(openDate(2000, 10, 4))
				.has(closeDate(2001, 10, 31))
				.has(actualTransferDate(2004, 10, 31))
				.has(planifiedDepositDate(2008, 10, 31))
				.has(planifiedDestructionDate(2008, 10, 31))
				.has(container("bac09"));

		assertThat(records.getFolder_B34())
				.has(openDate(2000, 5, 4))
				.has(closeDate(2002, 10, 31))
				.has(actualTransferDate(2006, 10, 31))
				.has(noPlanifiedDepositDate())
				.has(planifiedDestructionDate(2007, 10, 31))
				.has(container("bac08"));

		assertThat(records.getFolder_B35())
				.has(openDate(2000, 6, 4))
				.has(closeDate(2002, 10, 31))
				.has(actualTransferDate(2006, 10, 31))
				.has(planifiedDepositDate(2007, 10, 31))
				.has(noPlanifiedDestructionDate());

		assertThat(records.getFolder_B50())
				.has(openDate(2000, 10, 4))
				.has(closeDate(2001, 10, 31))
				.has(actualDepositDate(2011, 2, 13))
				.has(noPlanifiedDestructionDate())
				.has(container("bac02"));

		assertThat(records.getFolder_B51())
				.has(openDate(2000, 10, 4))
				.has(closeDate(2001, 10, 31))
				.has(noPlanifiedDepositDate())
				.has(actualDestructionDate(2007, 4, 14));

		assertThat(records.getFolder_B52())
				.has(openDate(2000, 10, 4))
				.has(closeDate(2001, 10, 31))
				.has(noPlanifiedDepositDate())
				.has(actualDestructionDate(2006, 5, 15));

		assertThat(records.getFolder_B53())
				.has(openDate(2000, 10, 4))
				.has(closeDate(2001, 10, 31))
				.has(noPlanifiedDepositDate())
				.has(actualDestructionDate(2011, 6, 16));

		assertThat(records.getFolder_B54())
				.has(openDate(2000, 5, 4))
				.has(closeDate(2002, 10, 31))
				.has(noPlanifiedDepositDate())
				.has(actualDestructionDate(2009, 7, 16));

		assertThat(records.getFolder_B55())
				.has(openDate(2000, 6, 4))
				.has(closeDate(2002, 10, 31))
				.has(actualDepositDate(2009, 8, 17))
				.has(noPlanifiedDestructionDate())
				.has(container("bac03"));

		assertThat(records.getFolder_C01())
				.has(openDate(2000, 10, 4))
				.has(noCloseDate())
				.has(noPlanifiedTransferDate())
				.has(noPlanifiedDepositDate())
				.has(noPlanifiedDestructionDate());

		assertThat(records.getFolder_C02())
				.has(openDate(2000, 10, 4))
				.has(noCloseDate())
				.has(noPlanifiedTransferDate())
				.has(noPlanifiedDepositDate())
				.has(noPlanifiedDestructionDate());

		assertThat(records.getFolder_C03())
				.has(openDate(2000, 10, 4))
				.has(noCloseDate())
				.has(noPlanifiedTransferDate())
				.has(noPlanifiedDepositDate())
				.has(noPlanifiedDestructionDate());

		assertThat(records.getFolder_C04())
				.has(openDate(2000, 10, 4))
				.has(closeDate(2001, 10, 31))
				.has(planifiedTransferDate(2006, 10, 31))
				.has(planifiedDepositDate(2008, 10, 31))
				.has(planifiedDestructionDate(2008, 10, 31));

		assertThat(records.getFolder_C05())
				.has(openDate(2000, 10, 4))
				.has(closeDate(2001, 10, 31))
				.has(noPlanifiedTransferDate())
				.has(noPlanifiedDepositDate())
				.has(planifiedDestructionDate(2003, 10, 31));

		assertThat(records.getFolder_C06())
				.has(openDate(2000, 10, 4))
				.has(closeDate(2001, 10, 31))
				.has(noPlanifiedTransferDate())
				.has(noPlanifiedDepositDate())
				.has(planifiedDestructionDate(2002, 10, 31));

		assertThat(records.getFolder_C07())
				.has(openDate(2000, 10, 4))

				.has(closeDate(2001, 10, 31))
				.has(planifiedTransferDate(2002, 10, 31))
				.has(planifiedDepositDate(2006, 10, 31))
				.has(planifiedDestructionDate(2006, 10, 31));

		assertThat(records.getFolder_C08())
				.has(openDate(2000, 5, 4))
				.has(closeDate(2002, 10, 31))
				.has(planifiedTransferDate(2005, 10, 31))
				.has(noPlanifiedDepositDate())
				.has(planifiedDestructionDate(2006, 10, 31));

		assertThat(records.getFolder_C09())
				.has(openDate(2000, 6, 4))
				.has(closeDate(2002, 10, 31))
				.has(planifiedTransferDate(2005, 10, 31))
				.has(planifiedDepositDate(2006, 10, 31))
				.has(noPlanifiedDestructionDate());

		assertThat(records.getFolder_C30())
				.has(openDate(2000, 10, 4))
				.has(closeDate(2001, 10, 31))
				.has(actualTransferDate(2007, 10, 31))
				.has(planifiedDepositDate(2009, 10, 31))
				.has(planifiedDestructionDate(2009, 10, 31))
				.has(container("bac07"));

		assertThat(records.getFolder_C31())
				.has(openDate(2000, 10, 4))
				.has(closeDate(2001, 10, 31))
				.has(noPlanifiedTransferDate())
				.has(noPlanifiedDepositDate())
				.has(planifiedDestructionDate(2003, 10, 31))
				.has(container("bac07"));

		assertThat(records.getFolder_C32())
				.has(openDate(2000, 10, 4))
				.has(closeDate(2001, 10, 31))
				.has(noPlanifiedTransferDate())
				.has(noPlanifiedDepositDate())
				.has(planifiedDestructionDate(2002, 10, 31))
				.has(container("bac07"));

		assertThat(records.getFolder_C33())
				.has(openDate(2000, 10, 4))
				.has(closeDate(2001, 10, 31))
				.has(actualTransferDate(2004, 10, 31))
				.has(planifiedDepositDate(2008, 10, 31))
				.has(planifiedDestructionDate(2008, 10, 31))
				.has(container("bac07"));

		assertThat(records.getFolder_C34())
				.has(openDate(2000, 5, 4))
				.has(closeDate(2002, 10, 31))
				.has(actualTransferDate(2006, 10, 31))
				.has(noPlanifiedDepositDate())
				.has(planifiedDestructionDate(2007, 10, 31))
				.has(container("bac07"));

		assertThat(records.getFolder_C35())
				.has(openDate(2000, 6, 4))
				.has(closeDate(2002, 10, 31))
				.has(actualTransferDate(2006, 10, 31))
				.has(planifiedDepositDate(2007, 10, 31))
				.has(noPlanifiedDestructionDate())
				.has(container("bac06"));

		assertThat(records.getFolder_C50())
				.has(openDate(2000, 10, 4))
				.has(closeDate(2001, 10, 31))
				.has(actualDepositDate(2011, 2, 13))
				.has(noPlanifiedDestructionDate())
				.has(container("bac01"));

		assertThat(records.getFolder_C51())
				.has(openDate(2000, 10, 4))
				.has(closeDate(2001, 10, 31))
				.has(noPlanifiedDepositDate())
				.has(actualDestructionDate(2007, 4, 14));

		assertThat(records.getFolder_C52())
				.has(openDate(2000, 10, 4))
				.has(closeDate(2001, 10, 31))
				.has(noPlanifiedDepositDate())
				.has(actualDestructionDate(2006, 5, 15));

		assertThat(records.getFolder_C53())
				.has(openDate(2000, 10, 4))
				.has(closeDate(2001, 10, 31))
				.has(noPlanifiedDepositDate())
				.has(actualDestructionDate(2011, 6, 16));

		assertThat(records.getFolder_C54())
				.has(openDate(2000, 5, 4))
				.has(closeDate(2002, 10, 31))
				.has(noPlanifiedDepositDate())
				.has(actualDestructionDate(2009, 7, 16));

		assertThat(records.getFolder_C55())
				.has(openDate(2000, 6, 4))
				.has(closeDate(2002, 10, 31))
				.has(actualDepositDate(2009, 8, 17))
				.has(noPlanifiedDestructionDate())
				.has(container("bac01"));

	}

	@Test
	public void givenDocumentDecomListThenOK()
			throws Exception {

		prepareSystem(
				withZeCollection().withConstellioRMModule().withAllTest(users).withRMTest(records)
						.withFoldersAndContainersOfEveryStatus().withDocumentsDecommissioningList()
		);
		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());

		DecommissioningList list30 = rm.getDecommissioningList(records.list_30);
		DecommissioningList list31 = rm.getDecommissioningList(records.list_31);
		DecommissioningList list32 = rm.getDecommissioningList(records.list_32);
		DecommissioningList list33 = rm.getDecommissioningList(records.list_33);
		DecommissioningList list34 = rm.getDecommissioningList(records.list_34);
		DecommissioningList list35 = rm.getDecommissioningList(records.list_35);
		DecommissioningList list36 = rm.getDecommissioningList(records.list_36);

		assertThat(list30.getDecommissioningListType()).isEqualTo(DecommissioningListType.DOCUMENTS_TO_TRANSFER);
		assertThat(list30.getDocuments()).hasSize(20);

		assertThat(list31.getDecommissioningListType()).isEqualTo(DecommissioningListType.DOCUMENTS_TO_TRANSFER);
		assertThat(list31.getDocuments()).hasSize(12);

		assertThat(list32.getDecommissioningListType()).isEqualTo(DecommissioningListType.DOCUMENTS_TO_DEPOSIT);
		assertThat(list32.getDocuments()).hasSize(60);

		assertThat(list33.getDecommissioningListType()).isEqualTo(DecommissioningListType.DOCUMENTS_TO_DEPOSIT);
		assertThat(list33.getDocuments()).hasSize(12);

		assertThat(list34.getDecommissioningListType()).isEqualTo(DecommissioningListType.DOCUMENTS_TO_DEPOSIT);
		assertThat(list34.getDocuments()).hasSize(12);

		assertThat(list35.getDecommissioningListType()).isEqualTo(DecommissioningListType.DOCUMENTS_TO_DESTROY);
		assertThat(list35.getDocuments()).hasSize(12);

		assertThat(list36.getDecommissioningListType()).isEqualTo(DecommissioningListType.DOCUMENTS_TO_DESTROY);
		assertThat(list36.getDocuments()).hasSize(12);

	}

	private Condition<? super Folder> mediaType(final FolderMediaType type) {
		return new Condition<Folder>() {
			@Override
			public boolean matches(Folder value) {
				assertThat(value.getMediaType()).isEqualTo(type);
				return true;
			}
		};
	}

	private Condition<? super Folder> mediumTypes(final String... mediumTypes) {
		return new Condition<Folder>() {
			@Override
			public boolean matches(Folder value) {
				assertThat(value.getMediumTypes()).containsOnlyOnce(mediumTypes);
				return true;
			}
		};
	}

	private Condition<? super Folder> noMediumTypes() {
		return new Condition<Folder>() {
			@Override
			public boolean matches(Folder value) {
				assertThat(value.getMediumTypes()).isEmpty();
				return true;
			}
		};
	}

	private Condition<? super Folder> container(final String containerId) {
		return new Condition<Folder>() {
			@Override
			public boolean matches(Folder value) {
				assertThat(value.getContainer()).isEqualTo(containerId);
				return true;
			}
		};
	}

	private Condition<? super Folder> copyRule(final CopyRetentionRule rule) {
		return new Condition<Folder>() {
			@Override
			public boolean matches(Folder value) {
				assertThat(value.getMainCopyRule()).isEqualToIgnoringGivenFields(rule, "id", "dirty");
				return true;
			}
		};
	}

	private Condition<? super Folder> copyType(final CopyType type) {
		return new Condition<Folder>() {
			@Override
			public boolean matches(Folder value) {
				assertThat(value.getCopyStatus()).isEqualTo(type);
				return true;
			}
		};
	}

	private LocalDate localDate(int year, int month, int day) {
		return new LocalDate(year, month, day);
	}

	private Condition<? super Folder> status(final FolderStatus status) {
		return new Condition<Folder>() {
			@Override
			public boolean matches(Folder value) {
				assertThat(value.getArchivisticStatus()).isEqualTo(status);
				return true;
			}
		};
	}

	private Condition<? super Folder> openDate(int year, int month, int day) {
		final LocalDate date = new LocalDate(year, month, day);
		return new Condition<Folder>() {
			@Override
			public boolean matches(Folder value) {
				assertThat(value.getOpenDate()).isEqualTo(date);
				return true;
			}
		};
	}

	private Condition<? super Folder> noCloseDate() {
		return new Condition<Folder>() {
			@Override
			public boolean matches(Folder value) {
				assertThat(value.getCloseDate()).isNull();
				return true;
			}
		};
	}

	private Condition<? super Folder> closeDate(int year, int month, int day) {
		final LocalDate date = new LocalDate(year, month, day);
		return new Condition<Folder>() {
			@Override
			public boolean matches(Folder value) {
				assertThat(value.getCloseDate()).isEqualTo(date);
				return true;
			}
		};
	}

	private Condition<? super Folder> noPlanifiedTransferDate() {
		return new Condition<Folder>() {
			@Override
			public boolean matches(Folder value) {
				assertThat(value.getActualTransferDate()).isNull();
				assertThat(value.getExpectedTransferDate()).isNull();
				return true;
			}
		};
	}

	private Condition<? super Folder> noPlanifiedDestructionDate() {
		return new Condition<Folder>() {
			@Override
			public boolean matches(Folder value) {
				assertThat(value.getActualDestructionDate()).isNull();
				assertThat(value.getExpectedDestructionDate()).isNull();
				return true;
			}
		};
	}

	private Condition<? super Folder> noPlanifiedDepositDate() {
		return new Condition<Folder>() {
			@Override
			public boolean matches(Folder value) {
				assertThat(value.getActualDepositDate()).isNull();
				assertThat(value.getExpectedDepositDate()).isNull();
				return true;
			}
		};
	}

	private Condition<? super Folder> actualTransferDate(int year, int month, int day) {
		final LocalDate date = new LocalDate(year, month, day);
		return new Condition<Folder>() {
			@Override
			public boolean matches(Folder value) {
				assertThat(value.getActualTransferDate()).isEqualTo(date);
				return true;
			}
		};
	}

	private Condition<? super Folder> actualDestructionDate(int year, int month, int day) {
		final LocalDate date = new LocalDate(year, month, day);
		return new Condition<Folder>() {
			@Override
			public boolean matches(Folder value) {
				assertThat(value.getActualDestructionDate()).isEqualTo(date);
				return true;
			}
		};
	}

	private Condition<? super Folder> actualDepositDate(int year, int month, int day) {
		final LocalDate date = new LocalDate(year, month, day);
		return new Condition<Folder>() {
			@Override
			public boolean matches(Folder value) {
				assertThat(value.getActualDepositDate()).isEqualTo(date);
				return true;
			}
		};
	}

	private Condition<? super Folder> planifiedTransferDate(int year, int month, int day) {
		final LocalDate date = new LocalDate(year, month, day);
		return new Condition<Folder>() {
			@Override
			public boolean matches(Folder value) {
				assertThat(value.getActualTransferDate()).describedAs("actualTransferDate").isNull();
				assertThat(value.getExpectedTransferDate()).describedAs("planifiedTransferDate").isEqualTo(date);
				return true;
			}
		};
	}

	private Condition<? super Folder> planifiedDestructionDate(int year, int month, int day) {
		final LocalDate date = new LocalDate(year, month, day);
		return new Condition<Folder>() {
			@Override
			public boolean matches(Folder value) {
				assertThat(value.getActualDestructionDate()).describedAs("actualDestructionDate").isNull();
				assertThat(value.getExpectedDestructionDate()).describedAs("planifiedDestructionDate").isEqualTo(date);
				return true;
			}
		};
	}

	private Condition<? super Folder> planifiedDepositDate(int year, int month, int day) {
		final LocalDate date = new LocalDate(year, month, day);
		return new Condition<Folder>() {
			@Override
			public boolean matches(Folder value) {
				assertThat(value.getActualDepositDate()).describedAs("actualDepositDate").isNull();
				assertThat(value.getExpectedDepositDate()).describedAs("planifiedDepositDate").isEqualTo(date);
				return true;
			}
		};
	}
}
