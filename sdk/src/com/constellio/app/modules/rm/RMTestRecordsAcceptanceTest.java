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
package com.constellio.app.modules.rm;

import static com.constellio.app.modules.rm.model.CopyRetentionRule.newPrincipal;
import static com.constellio.app.modules.rm.model.enums.FolderMediaType.ANALOG;
import static com.constellio.app.modules.rm.model.enums.FolderMediaType.ELECTRONIC;
import static com.constellio.app.modules.rm.model.enums.FolderMediaType.HYBRID;
import static com.constellio.app.modules.rm.model.enums.FolderMediaType.UNKNOWN;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;

import org.assertj.core.api.Condition;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;

import com.constellio.app.modules.rm.model.CopyRetentionRule;
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
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.sdk.tests.ConstellioTest;

public class RMTestRecordsAcceptanceTest extends ConstellioTest {

	RMSchemasRecordsServices schemas;
	RMTestRecords rm = new RMTestRecords(zeCollection);

	@Before
	public void setUp()
			throws Exception {
		schemas = new RMSchemasRecordsServices(zeCollection, getModelLayerFactory());

	}

	@Test
	public void givenTestRecordsWithoutFoldersThenNoFolders()
			throws Exception {
		givenCollection(zeCollection).withConstellioRMModule().withAllTestUsers();
		rm.setup(getModelLayerFactory());

		SearchServices searchServices = getModelLayerFactory().newSearchServices();
		LogicalSearchCondition condition = from(schemas.folderSchemaType()).returnAll();
		assertThat(searchServices.getResultsCount(condition)).isEqualTo(0);

	}

	@Test
	public void givenTestRecordsThenChuchAndAdminHasContentVersionDeletePermissions()
			throws Exception {
		givenCollection(zeCollection).withConstellioRMModule().withAllTestUsers();
		rm.setup(getModelLayerFactory());

		assertThat(rm.getAdmin().has(CorePermissions.DELETE_CONTENT_VERSION).globally()).isTrue();
		assertThat(rm.getBob_userInAC().has(CorePermissions.DELETE_CONTENT_VERSION).globally()).isFalse();
		assertThat(rm.getCharles_userInA().has(CorePermissions.DELETE_CONTENT_VERSION).globally()).isFalse();
		assertThat(rm.getDakota_managerInA_userInB().has(CorePermissions.DELETE_CONTENT_VERSION).globally()).isFalse();
		assertThat(rm.getEdouard_managerInB_userInC().has(CorePermissions.DELETE_CONTENT_VERSION).globally()).isFalse();
		assertThat(rm.getGandalf_managerInABC().has(CorePermissions.DELETE_CONTENT_VERSION).globally()).isFalse();
		assertThat(rm.getChuckNorris().has(CorePermissions.DELETE_CONTENT_VERSION).globally()).isTrue();

	}

	@Test
	public void givenTestRecordsWithFoldersThenUsersHaveAuthorizationToTheirFilingSpacesFolders()
			throws Exception {

		givenCollection(zeCollection).withConstellioRMModule().withAllTestUsers();
		rm.setup(getModelLayerFactory()).withFoldersAndContainersOfEveryStatus();

		SearchServices searchServices = getModelLayerFactory().newSearchServices();
		long unit10FolderCount = searchServices.getResultsCount(from(schemas.folderSchemaType())
				.where(schemas.folderAdministrativeUnit()).isEqualTo(rm.unitId_10));

		long unit11FolderCount = searchServices.getResultsCount(from(schemas.folderSchemaType())
				.where(schemas.folderAdministrativeUnit()).isEqualTo(rm.unitId_11));

		long unit12FolderCount = searchServices.getResultsCount(from(schemas.folderSchemaType())
				.where(schemas.folderAdministrativeUnit()).isEqualTo(rm.unitId_12));

		long unit20FolderCount = searchServices.getResultsCount(from(schemas.folderSchemaType())
				.where(schemas.folderAdministrativeUnit()).isEqualTo(rm.unitId_20));

		long unit30FolderCount = searchServices.getResultsCount(from(schemas.folderSchemaType())
				.where(schemas.folderAdministrativeUnit()).isEqualTo(rm.unitId_30));

		assertThatCountOfFoldersVisibleBy(rm.getBob_userInAC())
				.isEqualTo(unit10FolderCount + unit11FolderCount + unit12FolderCount + unit30FolderCount);
		assertThatCountOfFoldersVisibleBy(rm.getCharles_userInA())
				.isEqualTo(unit10FolderCount + unit11FolderCount + unit12FolderCount);
		assertThatCountOfFoldersVisibleBy(rm.getDakota_managerInA_userInB())
				.isEqualTo(unit10FolderCount + unit11FolderCount + unit12FolderCount);
		assertThatCountOfFoldersVisibleBy(rm.getEdouard_managerInB_userInC())
				.isEqualTo(unit11FolderCount + unit12FolderCount + unit30FolderCount);
		assertThatCountOfFoldersVisibleBy(rm.getChuckNorris())
				.isEqualTo(unit10FolderCount + unit11FolderCount + unit12FolderCount + unit30FolderCount);
		assertThatCountOfFoldersVisibleBy(rm.getGandalf_managerInABC())
				.isEqualTo(unit10FolderCount + unit11FolderCount + unit12FolderCount + unit30FolderCount);

		assertThat(rm.getFolder_A01()).has(openDate(2000, 10, 4)).has(noCloseDate()).has(noPlanifiedTransferDate())
				.has(noPlanifiedDepositDate()).has(noPlanifiedDestructionDate());
	}

	private org.assertj.core.api.LongAssert assertThatCountOfFoldersVisibleBy(User user) {
		SearchServices searchServices = getModelLayerFactory().newSearchServices();
		LogicalSearchCondition condition = from(schemas.folderSchemaType()).returnAll();
		return assertThat(searchServices.getResultsCount(new LogicalSearchQuery(condition).filteredWithUser(user)));
	}

	@Test
	public void givenTestRecordsWithStorgeSpacesThenTheyHaveCorrectInfos()
			throws Exception {

		givenCollection(zeCollection).withConstellioRMModule().withAllTestUsers();
		rm.setup(getModelLayerFactory()).withFoldersAndContainersOfEveryStatus();

		assertThat(rm.getStorageSpaceS01().getCode()).isEqualTo("S01");
		assertThat(rm.getStorageSpaceS01().getTitle()).isEqualTo("Etagere 1");

		assertThat(rm.getStorageSpaceS01_01().getCode()).isEqualTo("S01-01");
		assertThat(rm.getStorageSpaceS01_01().getTitle()).isEqualTo("Tablette 1");
		assertThat(rm.getStorageSpaceS01_01().getParentStorageSpace()).isEqualTo("S01");

		assertThat(rm.getStorageSpaceS01_02().getCode()).isEqualTo("S01-02");
		assertThat(rm.getStorageSpaceS01_02().getTitle()).isEqualTo("Tablette 2");
		assertThat(rm.getStorageSpaceS01_02().getParentStorageSpace()).isEqualTo("S01");

		assertThat(rm.getStorageSpaceS02().getCode()).isEqualTo("S02");
		assertThat(rm.getStorageSpaceS02().getTitle()).isEqualTo("Etagere 2");

		assertThat(rm.getStorageSpaceS02_01().getCode()).isEqualTo("S02-01");
		assertThat(rm.getStorageSpaceS02_01().getTitle()).isEqualTo("Tablette 1");
		assertThat(rm.getStorageSpaceS02_01().getParentStorageSpace()).isEqualTo("S02");

		assertThat(rm.getStorageSpaceS02_02().getCode()).isEqualTo("S02-02");
		assertThat(rm.getStorageSpaceS02_02().getTitle()).isEqualTo("Tablette 2");
		assertThat(rm.getStorageSpaceS02_02().getParentStorageSpace()).isEqualTo("S02");
	}

	@Test
	public void givenTestRecordsWithListThenTheyHaveCorrectInfos()
			throws Exception {

		givenCollection(zeCollection).withConstellioRMModule().withAllTestUsers();
		rm.setup(getModelLayerFactory()).withFoldersAndContainersOfEveryStatus();

		DecommissioningList list01 = rm.getList01();
		assertThat(list01.getId()).isEqualTo("list01");
		assertThat(list01.isUniform()).isFalse();
		assertThat(list01.getDecommissioningListType()).isEqualTo(DecommissioningListType.FOLDERS_TO_DESTROY);
		assertThat(list01.getFoldersMediaTypes()).containsExactly(ANALOG, HYBRID, UNKNOWN, ELECTRONIC, ANALOG, UNKNOWN);
		assertThat(list01.getStatus()).isEqualTo(DecomListStatus.GENERATED);
		assertThat(list01.getFilingSpace()).isEqualTo(rm.filingId_A);
		assertThat(list01.getAdministrativeUnit()).isEqualTo(rm.unitId_10);
		assertThat(list01.getFolders()).containsOnlyOnce(rm.folder_A(42, 47));
		assertThat(list01.getContainers()).containsOnlyOnce(rm.containerId_bac18, rm.containerId_bac19);

		assertThat(rm.getList11().getContainers()).isEmpty();
		assertThat(rm.getList11().getStatus()).isEqualTo(DecomListStatus.PROCESSED);

		assertThat(rm.getList12().getContainers())
				.containsOnlyOnce(rm.containerId_bac10, rm.containerId_bac11, rm.containerId_bac12);
		assertThat(rm.getList12().getStatus()).isEqualTo(DecomListStatus.PROCESSED);

		assertThat(rm.getList13().getContainers()).containsOnlyOnce(rm.containerId_bac13);
		assertThat(rm.getList13().getStatus()).isEqualTo(DecomListStatus.PROCESSED);

		assertThat(rm.getList14().getContainers()).containsOnlyOnce(rm.containerId_bac05);
		assertThat(rm.getList14().getStatus()).isEqualTo(DecomListStatus.PROCESSED);

		DecommissioningList list15 = rm.getList15();
		assertThat(list15.getId()).isEqualTo("list15");
		assertThat(list15.isUniform()).isTrue();
		assertThat(list15.getDecommissioningListType()).isEqualTo(DecommissioningListType.FOLDERS_TO_DEPOSIT);
		assertThat(list15.getFoldersMediaTypes()).containsExactly(ELECTRONIC, ELECTRONIC, ELECTRONIC);
		assertThat(list15.getStatus()).isEqualTo(DecomListStatus.PROCESSED);
		assertThat(list15.getFilingSpace()).isEqualTo(rm.filingId_A);
		assertThat(list15.getAdministrativeUnit()).isEqualTo(rm.unitId_10);
		assertThat(list15.getFolders()).containsOnlyOnce(rm.folder_A(94, 96));
		assertThat(list15.getContainers()).containsOnlyOnce(rm.containerId_bac04);
	}

	@Test
	public void givenTestRecordsWithContainersThenTheyHaveCorrectInfos()
			throws Exception {

		givenCollection(zeCollection).withConstellioRMModule().withAllTestUsers();
		rm.setup(getModelLayerFactory()).withFoldersAndContainersOfEveryStatus();

		assertThat(rm.getContainerBac01().getId()).isEqualTo("bac01");
		assertThat(rm.getContainerBac01().getTemporaryIdentifier()).isEqualTo("30_C_01");
		assertThat(rm.getContainerBac01().getStorageSpace()).isEqualTo("S02-02");
		assertThat(rm.getContainerBac01().getAdministrativeUnit()).isEqualTo("unitId_30");
		assertThat(rm.getContainerBac01().getFilingSpace()).isEqualTo("filingId_C");
		assertThat(rm.getContainerBac01().getRealTransferDate()).isEqualTo(localDate(2007, 10, 31));
		assertThat(rm.getContainerBac01().getRealDepositDate()).isEqualTo(localDate(2011, 2, 13));
		assertThat(rm.getContainerBac01().getDecommissioningType()).isEqualTo(DecommissioningType.DEPOSIT);
		assertThat(rm.getContainerBac01().isFull()).isTrue();

		assertThat(rm.getContainerBac02().getId()).isEqualTo("bac02");
		assertThat(rm.getContainerBac02().getTemporaryIdentifier()).isEqualTo("12_B_01");
		assertThat(rm.getContainerBac02().getStorageSpace()).isNull();
		assertThat(rm.getContainerBac02().getAdministrativeUnit()).isEqualTo("unitId_12");
		assertThat(rm.getContainerBac02().getFilingSpace()).isEqualTo("filingId_B");
		assertThat(rm.getContainerBac02().getRealTransferDate()).isEqualTo(localDate(2007, 10, 31));
		assertThat(rm.getContainerBac02().getRealDepositDate()).isEqualTo(localDate(2011, 2, 13));
		assertThat(rm.getContainerBac02().getDecommissioningType()).isEqualTo(DecommissioningType.DEPOSIT);
		assertThat(rm.getContainerBac02().isFull()).isFalse();

		assertThat(rm.getContainerBac03().getId()).isEqualTo("bac03");
		assertThat(rm.getContainerBac03().getTemporaryIdentifier()).isEqualTo("11_B_01");
		assertThat(rm.getContainerBac03().getStorageSpace()).isEqualTo("S02-02");
		assertThat(rm.getContainerBac03().getAdministrativeUnit()).isEqualTo("unitId_11");
		assertThat(rm.getContainerBac03().getFilingSpace()).isEqualTo("filingId_B");
		assertThat(rm.getContainerBac03().getRealTransferDate()).isEqualTo(localDate(2006, 10, 31));
		assertThat(rm.getContainerBac03().getRealDepositDate()).isEqualTo(localDate(2009, 8, 17));
		assertThat(rm.getContainerBac03().getDecommissioningType()).isEqualTo(DecommissioningType.DEPOSIT);
		assertThat(rm.getContainerBac03().isFull()).isFalse();

		assertThat(rm.getContainerBac04().getId()).isEqualTo("bac04");
		assertThat(rm.getContainerBac04().getTemporaryIdentifier()).isEqualTo("10_A_01");
		assertThat(rm.getContainerBac04().getStorageSpace()).isEqualTo("S01-02");
		assertThat(rm.getContainerBac04().getAdministrativeUnit()).isEqualTo("unitId_10");
		assertThat(rm.getContainerBac04().getFilingSpace()).isEqualTo("filingId_A");
		assertThat(rm.getContainerBac04().getRealTransferDate()).isEqualTo(localDate(2007, 10, 31));
		assertThat(rm.getContainerBac04().getRealDepositDate()).isEqualTo(localDate(2010, 8, 17));
		assertThat(rm.getContainerBac04().getDecommissioningType()).isEqualTo(DecommissioningType.DEPOSIT);
		assertThat(rm.getContainerBac04().isFull()).isFalse();

		assertThat(rm.getContainerBac05().getId()).isEqualTo("bac05");
		assertThat(rm.getContainerBac05().getTemporaryIdentifier()).isEqualTo("10_A_02");
		assertThat(rm.getContainerBac05().getStorageSpace()).isEqualTo("S01-02");
		assertThat(rm.getContainerBac05().getAdministrativeUnit()).isEqualTo("unitId_10");
		assertThat(rm.getContainerBac05().getFilingSpace()).isEqualTo("filingId_A");
		assertThat(rm.getContainerBac05().getRealTransferDate()).isEqualTo(localDate(2008, 10, 31));
		assertThat(rm.getContainerBac05().getRealDepositDate()).isEqualTo(localDate(2012, 5, 15));
		assertThat(rm.getContainerBac05().getDecommissioningType()).isEqualTo(DecommissioningType.DEPOSIT);
		assertThat(rm.getContainerBac05().isFull()).isTrue();

		assertThat(rm.getContainerBac06().getId()).isEqualTo("bac06");
		assertThat(rm.getContainerBac06().getTemporaryIdentifier()).isEqualTo("30_C_02");
		assertThat(rm.getContainerBac06().getStorageSpace()).isNull();
		assertThat(rm.getContainerBac06().getAdministrativeUnit()).isEqualTo("unitId_30");
		assertThat(rm.getContainerBac06().getFilingSpace()).isEqualTo("filingId_C");
		assertThat(rm.getContainerBac06().getRealTransferDate()).isEqualTo(localDate(2006, 10, 31));
		assertThat(rm.getContainerBac06().getRealDepositDate()).isNull();
		assertThat(rm.getContainerBac06().getDecommissioningType()).isEqualTo(DecommissioningType.TRANSFERT_TO_SEMI_ACTIVE);
		assertThat(rm.getContainerBac06().isFull()).isFalse();

		assertThat(rm.getContainerBac07().getId()).isEqualTo("bac07");
		assertThat(rm.getContainerBac07().getTemporaryIdentifier()).isEqualTo("30_C_03");
		assertThat(rm.getContainerBac07().getStorageSpace()).isEqualTo("S02-01");
		assertThat(rm.getContainerBac07().getAdministrativeUnit()).isEqualTo("unitId_30");
		assertThat(rm.getContainerBac07().getFilingSpace()).isEqualTo("filingId_C");
		assertThat(rm.getContainerBac07().getRealTransferDate()).isEqualTo(localDate(2007, 10, 31));
		assertThat(rm.getContainerBac07().getRealDepositDate()).isNull();
		assertThat(rm.getContainerBac07().getDecommissioningType()).isEqualTo(DecommissioningType.TRANSFERT_TO_SEMI_ACTIVE);
		assertThat(rm.getContainerBac07().isFull()).isFalse();

		assertThat(rm.getContainerBac08().getId()).isEqualTo("bac08");
		assertThat(rm.getContainerBac08().getTemporaryIdentifier()).isEqualTo("12_B_02");
		assertThat(rm.getContainerBac08().getStorageSpace()).isEqualTo("S02-01");
		assertThat(rm.getContainerBac08().getAdministrativeUnit()).isEqualTo("unitId_12");
		assertThat(rm.getContainerBac08().getFilingSpace()).isEqualTo("filingId_B");
		assertThat(rm.getContainerBac08().getRealTransferDate()).isEqualTo(localDate(2007, 10, 31));
		assertThat(rm.getContainerBac08().getRealDepositDate()).isNull();
		assertThat(rm.getContainerBac08().getDecommissioningType()).isEqualTo(DecommissioningType.TRANSFERT_TO_SEMI_ACTIVE);
		assertThat(rm.getContainerBac08().isFull()).isFalse();

		assertThat(rm.getContainerBac09().getId()).isEqualTo("bac09");
		assertThat(rm.getContainerBac09().getTemporaryIdentifier()).isEqualTo("11_B_02");
		assertThat(rm.getContainerBac09().getStorageSpace()).isEqualTo("S02-01");
		assertThat(rm.getContainerBac09().getAdministrativeUnit()).isEqualTo("unitId_11");
		assertThat(rm.getContainerBac09().getFilingSpace()).isEqualTo("filingId_B");
		assertThat(rm.getContainerBac09().getRealTransferDate()).isEqualTo(localDate(2006, 10, 31));
		assertThat(rm.getContainerBac09().getRealDepositDate()).isNull();
		assertThat(rm.getContainerBac09().getDecommissioningType()).isEqualTo(DecommissioningType.TRANSFERT_TO_SEMI_ACTIVE);
		assertThat(rm.getContainerBac09().isFull()).isFalse();

		assertThat(rm.getContainerBac10().getId()).isEqualTo("bac10");
		assertThat(rm.getContainerBac10().getTemporaryIdentifier()).isEqualTo("10_A_03");
		assertThat(rm.getContainerBac10().getStorageSpace()).isNull();
		assertThat(rm.getContainerBac10().getAdministrativeUnit()).isEqualTo("unitId_10");
		assertThat(rm.getContainerBac10().getFilingSpace()).isEqualTo("filingId_A");
		assertThat(rm.getContainerBac10().getRealTransferDate()).isEqualTo(localDate(2007, 10, 31));
		assertThat(rm.getContainerBac10().getRealDepositDate()).isNull();
		assertThat(rm.getContainerBac10().getDecommissioningType()).isEqualTo(DecommissioningType.TRANSFERT_TO_SEMI_ACTIVE);
		assertThat(rm.getContainerBac10().isFull()).isTrue();

		assertThat(rm.getContainerBac11().getId()).isEqualTo("bac11");
		assertThat(rm.getContainerBac11().getTemporaryIdentifier()).isEqualTo("10_A_04");
		assertThat(rm.getContainerBac11().getStorageSpace()).isEqualTo("S01-01");
		assertThat(rm.getContainerBac11().getAdministrativeUnit()).isEqualTo("unitId_10");
		assertThat(rm.getContainerBac11().getFilingSpace()).isEqualTo("filingId_A");
		assertThat(rm.getContainerBac11().getRealTransferDate()).isEqualTo(localDate(2005, 10, 31));
		assertThat(rm.getContainerBac11().getRealDepositDate()).isNull();
		assertThat(rm.getContainerBac11().getDecommissioningType()).isEqualTo(DecommissioningType.TRANSFERT_TO_SEMI_ACTIVE);
		assertThat(rm.getContainerBac11().isFull()).isFalse();

		assertThat(rm.getContainerBac12().getId()).isEqualTo("bac12");
		assertThat(rm.getContainerBac12().getTemporaryIdentifier()).isEqualTo("10_A_05");
		assertThat(rm.getContainerBac12().getStorageSpace()).isEqualTo("S01-01");
		assertThat(rm.getContainerBac12().getAdministrativeUnit()).isEqualTo("unitId_10");
		assertThat(rm.getContainerBac12().getFilingSpace()).isEqualTo("filingId_A");
		assertThat(rm.getContainerBac12().getRealTransferDate()).isEqualTo(localDate(2006, 10, 31));
		assertThat(rm.getContainerBac12().getRealDepositDate()).isNull();
		assertThat(rm.getContainerBac12().getDecommissioningType()).isEqualTo(DecommissioningType.TRANSFERT_TO_SEMI_ACTIVE);
		assertThat(rm.getContainerBac12().isFull()).isFalse();

		assertThat(rm.getContainerBac13().getId()).isEqualTo("bac13");
		assertThat(rm.getContainerBac13().getTemporaryIdentifier()).isEqualTo("10_A_06");
		assertThat(rm.getContainerBac13().getStorageSpace()).isEqualTo("S01-01");
		assertThat(rm.getContainerBac13().getAdministrativeUnit()).isEqualTo("unitId_10");
		assertThat(rm.getContainerBac13().getFilingSpace()).isEqualTo("filingId_A");
		assertThat(rm.getContainerBac13().getRealTransferDate()).isEqualTo(localDate(2008, 10, 31));
		assertThat(rm.getContainerBac13().getRealDepositDate()).isNull();
		assertThat(rm.getContainerBac13().getDecommissioningType()).isEqualTo(DecommissioningType.TRANSFERT_TO_SEMI_ACTIVE);
		assertThat(rm.getContainerBac13().isFull()).isFalse();

	}

	@Test
	public void givenTestRecordsWithRetentionRulesThenTheyHaveCorrectInfos()
			throws Exception {
		givenCollection(zeCollection).withConstellioRMModule().withAllTestUsers();
		rm.setup(getModelLayerFactory()).withFoldersAndContainersOfEveryStatus();

		assertThat(rm.getRule1().getCopyRulesComment())
				.isEqualTo(Arrays.asList("R1:comment1", "R2:comment2", "R3:comment3", "R4:comment4"));
		assertThat(rm.getRule1().getDescription()).isEqualTo("Description Rule 1");
		assertThat(rm.getRule1().getJuridicReference()).isEqualTo("Juridic reference Rule 1");
		assertThat(rm.getRule1().getGeneralComment()).isEqualTo("General Comment Rule 1");
		assertThat(rm.getRule1().getKeywords()).containsExactly("Rule #1");
		assertThat(rm.getRule1().getCorpus()).isEqualTo("Corpus Rule 1");
	}

	@Test
	public void givenTestRecordsWithFoldersThenFoldersHaveCorrectInfos()
			throws Exception {
		givenCollection(zeCollection).withConstellioRMModule().withAllTestUsers();
		rm.setup(getModelLayerFactory()).withFoldersAndContainersOfEveryStatus();

		SearchServices searchServices = getModelLayerFactory().newSearchServices();
		LogicalSearchCondition condition = from(schemas.folderSchemaType()).returnAll();
		assertThat(searchServices.getResultsCount(condition)).isEqualTo(105);
		assertThat(rm.getFolder_A01())
				.has(openDate(2000, 10, 4))
				.has(noCloseDate())
				.has(noPlanifiedTransferDate())
				.has(noPlanifiedDepositDate())
				.has(noPlanifiedDestructionDate());

		assertThat(rm.getFolder_A02())
				.has(openDate(2000, 11, 4))
				.has(noCloseDate())
				.has(noPlanifiedTransferDate())
				.has(noPlanifiedDepositDate())
				.has(noPlanifiedDestructionDate());

		assertThat(rm.getFolder_A03())
				.has(openDate(2000, 11, 5))
				.has(noCloseDate())
				.has(noPlanifiedTransferDate())
				.has(noPlanifiedDepositDate())
				.has(noPlanifiedDestructionDate());

		assertThat(rm.getFolder_A04())
				.has(openDate(2000, 10, 4))
				.has(noCloseDate())
				.has(noPlanifiedTransferDate())
				.has(noPlanifiedDepositDate())
				.has(noPlanifiedDestructionDate());

		assertThat(rm.getFolder_A05())
				.has(openDate(2000, 11, 4))
				.has(noCloseDate())
				.has(noPlanifiedTransferDate())
				.has(noPlanifiedDepositDate())
				.has(noPlanifiedDestructionDate());

		assertThat(rm.getFolder_A06())
				.has(openDate(2000, 11, 5))
				.has(noCloseDate())
				.has(noPlanifiedTransferDate())
				.has(noPlanifiedDepositDate())
				.has(noPlanifiedDestructionDate());

		assertThat(rm.getFolder_A07())
				.has(openDate(2000, 10, 4))
				.has(noCloseDate())
				.has(noPlanifiedTransferDate())
				.has(noPlanifiedDepositDate())
				.has(noPlanifiedDestructionDate());

		assertThat(rm.getFolder_A08())
				.has(openDate(2000, 11, 4))
				.has(noCloseDate())
				.has(noPlanifiedTransferDate())
				.has(noPlanifiedDepositDate())
				.has(noPlanifiedDestructionDate());

		assertThat(rm.getFolder_A09())
				.has(openDate(2000, 11, 5))
				.has(noCloseDate())
				.has(noPlanifiedTransferDate())
				.has(noPlanifiedDepositDate())
				.has(noPlanifiedDestructionDate());

		assertThat(rm.getFolder_A10())
				.has(openDate(2000, 10, 4))
				.has(closeDate(2001, 10, 31))
				.has(planifiedTransferDate(2006, 10, 31))
				.has(planifiedDepositDate(2008, 10, 31))
				.has(planifiedDestructionDate(2008, 10, 31));

		assertThat(rm.getFolder_A11())
				.has(openDate(2000, 11, 4))
				.has(closeDate(2001, 10, 31))
				.has(planifiedTransferDate(2006, 10, 31))
				.has(planifiedDepositDate(2008, 10, 31))
				.has(planifiedDestructionDate(2008, 10, 31));

		assertThat(rm.getFolder_A12())
				.has(openDate(2000, 11, 5))
				.has(closeDate(2002, 10, 31))
				.has(planifiedTransferDate(2007, 10, 31))
				.has(planifiedDepositDate(2009, 10, 31))
				.has(planifiedDestructionDate(2009, 10, 31));

		assertThat(rm.getFolder_A13())
				.has(openDate(2000, 10, 4))
				.has(closeDate(2001, 10, 31))
				.has(planifiedTransferDate(2003, 10, 31))
				.has(noPlanifiedDepositDate())
				.has(planifiedDestructionDate(2003, 10, 31));

		assertThat(rm.getFolder_A14())
				.has(openDate(2000, 11, 4))
				.has(closeDate(2001, 10, 31))
				.has(planifiedTransferDate(2003, 10, 31))
				.has(noPlanifiedDepositDate())
				.has(planifiedDestructionDate(2003, 10, 31));

		assertThat(rm.getFolder_A15())
				.has(openDate(2000, 11, 5))
				.has(closeDate(2002, 10, 31))
				.has(planifiedTransferDate(2004, 10, 31))
				.has(noPlanifiedDepositDate())
				.has(planifiedDestructionDate(2004, 10, 31));

		assertThat(rm.getFolder_A16())
				.has(openDate(2000, 10, 4))
				.has(closeDate(2001, 10, 31))
				.has(planifiedTransferDate(2002, 10, 31))
				.has(planifiedDepositDate(2007, 10, 31))
				.has(noPlanifiedDestructionDate());

		assertThat(rm.getFolder_A17())
				.has(openDate(2000, 11, 4))
				.has(closeDate(2001, 10, 31))
				.has(planifiedTransferDate(2002, 10, 31))
				.has(planifiedDepositDate(2007, 10, 31))
				.has(noPlanifiedDestructionDate());

		assertThat(rm.getFolder_A18())
				.has(mediumTypes(rm.PA, rm.MD))
				.has(openDate(2000, 11, 5))
				.has(closeDate(2002, 10, 31))
				.has(planifiedTransferDate(2003, 10, 31))
				.has(planifiedDepositDate(2008, 10, 31))
				.has(noPlanifiedDestructionDate());

		assertThat(rm.getFolder_A19())
				.has(mediumTypes(rm.PA))
				.has(openDate(2000, 10, 4))
				.has(closeDate(2001, 10, 31))
				.has(planifiedTransferDate(2002, 10, 31))
				.has(planifiedDepositDate(2006, 10, 31))
				.has(planifiedDestructionDate(2006, 10, 31));

		assertThat(rm.getFolder_A20())
				.has(noMediumTypes())
				.has(openDate(2000, 11, 4))
				.has(closeDate(2001, 10, 31))
				.has(planifiedTransferDate(2002, 10, 31))
				.has(planifiedDepositDate(2006, 10, 31))
				.has(planifiedDestructionDate(2006, 10, 31));

		assertThat(rm.getFolder_A21())
				.has(mediumTypes(rm.MD))
				.has(openDate(2000, 11, 5))
				.has(closeDate(2002, 10, 31))
				.has(planifiedTransferDate(2003, 10, 31))
				.has(planifiedDepositDate(2007, 10, 31))
				.has(planifiedDestructionDate(2007, 10, 31));

		assertThat(rm.getFolder_A22())
				.has(openDate(2000, 5, 4))
				.has(closeDate(2002, 10, 31))
				.has(copyRule(newPrincipal(asList(rm.PA), "3-888-D")))
				.has(planifiedTransferDate(2005, 10, 31))
				.has(noPlanifiedDepositDate())
				.has(planifiedDestructionDate(2006, 10, 31));

		assertThat(rm.getFolder_A23())
				.has(openDate(2000, 7, 4))
				.has(closeDate(2002, 10, 31))
				.has(copyRule(newPrincipal(asList(rm.PA), "3-888-D")))
				.has(planifiedTransferDate(2005, 10, 31))
				.has(noPlanifiedDepositDate())
				.has(planifiedDestructionDate(2006, 10, 31));

		assertThat(rm.getFolder_A24())
				.has(openDate(2000, 7, 5))
				.has(closeDate(2003, 10, 31))
				.has(copyRule(newPrincipal(asList(rm.PA), "3-888-D")))
				.has(planifiedTransferDate(2006, 10, 31))
				.has(noPlanifiedDepositDate())
				.has(planifiedDestructionDate(2007, 10, 31));

		assertThat(rm.getFolder_A25())
				.has(openDate(2000, 6, 4))
				.has(closeDate(2002, 10, 31))
				.has(copyRule(newPrincipal(asList(rm.MD), "3-888-C")))
				.has(planifiedTransferDate(2005, 10, 31))
				.has(planifiedDepositDate(2006, 10, 31))
				.has(noPlanifiedDestructionDate());

		assertThat(rm.getFolder_A26())
				.has(openDate(2000, 7, 4))
				.has(closeDate(2002, 10, 31))
				.has(copyRule(newPrincipal(asList(rm.MD), "3-888-C")))
				.has(planifiedTransferDate(2005, 10, 31))
				.has(planifiedDepositDate(2006, 10, 31))
				.has(noPlanifiedDestructionDate());

		assertThat(rm.getFolder_A27())
				.has(openDate(2000, 7, 5))
				.has(closeDate(2003, 10, 31))
				.has(copyRule(newPrincipal(asList(rm.MD), "3-888-C")))
				.has(planifiedTransferDate(2006, 10, 31))
				.has(planifiedDepositDate(2007, 10, 31))
				.has(noPlanifiedDestructionDate());

		assertThat(rm.getFolder_A42())
				.has(mediumTypes(rm.PA))
				.has(mediaType(ANALOG))
				.has(openDate(2000, 10, 4))
				.has(closeDate(2001, 10, 31))
				.has(actualTransferDate(2007, 10, 31))
				.has(planifiedDepositDate(2009, 10, 31))
				.has(planifiedDestructionDate(2009, 10, 31))
				.has(container("bac13"));

		assertThat(rm.getFolder_A43())
				.has(mediumTypes(rm.PA, rm.MD))
				.has(mediaType(HYBRID))
				.has(openDate(2000, 11, 4))
				.has(closeDate(2001, 10, 31))
				.has(actualTransferDate(2007, 10, 31))
				.has(planifiedDepositDate(2009, 10, 31))
				.has(planifiedDestructionDate(2009, 10, 31))
				.has(container("bac13"));

		assertThat(rm.getFolder_A44())
				.has(noMediumTypes())
				.has(mediaType(UNKNOWN))
				.has(openDate(2000, 11, 5))
				.has(closeDate(2002, 10, 31))
				.has(actualTransferDate(2008, 10, 31))
				.has(planifiedDepositDate(2010, 10, 31))
				.has(planifiedDestructionDate(2010, 10, 31))
				.has(container("bac13"));

		assertThat(rm.getFolder_A45())
				.has(mediumTypes(rm.MD))
				.has(mediaType(ELECTRONIC))
				.has(openDate(2000, 10, 4))
				.has(closeDate(2001, 10, 31))
				.has(actualTransferDate(2005, 10, 31))
				.has(noPlanifiedDepositDate())
				.has(planifiedDestructionDate(2005, 10, 31))
				.has(container("bac12"));

		assertThat(rm.getFolder_A46())
				.has(mediumTypes(rm.PA))
				.has(mediaType(ANALOG))
				.has(openDate(2000, 11, 4))
				.has(closeDate(2001, 10, 31))
				.has(actualTransferDate(2005, 10, 31))
				.has(noPlanifiedDepositDate())
				.has(planifiedDestructionDate(2005, 10, 31))
				.has(container("bac12"));

		assertThat(rm.getFolder_A47())
				.has(noMediumTypes())
				.has(mediaType(UNKNOWN))
				.has(openDate(2000, 11, 5))
				.has(closeDate(2002, 10, 31))
				.has(actualTransferDate(2006, 10, 31))
				.has(noPlanifiedDepositDate())
				.has(planifiedDestructionDate(2006, 10, 31))
				.has(container("bac12"));

		assertThat(rm.getFolder_A48())
				.has(mediumTypes(rm.MD))
				.has(openDate(2000, 10, 4))
				.has(closeDate(2001, 10, 31))
				.has(actualTransferDate(2004, 10, 31))
				.has(planifiedDepositDate(2009, 10, 31))
				.has(noPlanifiedDestructionDate())
				.has(container(null));

		assertThat(rm.getFolder_A49())
				.has(openDate(2000, 11, 4))
				.has(closeDate(2001, 10, 31))
				.has(actualTransferDate(2004, 10, 31))
				.has(planifiedDepositDate(2009, 10, 31))
				.has(noPlanifiedDestructionDate())
				.has(container("bac11"));

		assertThat(rm.getFolder_A50())
				.has(openDate(2000, 11, 5))
				.has(closeDate(2002, 10, 31))
				.has(actualTransferDate(2005, 10, 31))
				.has(planifiedDepositDate(2010, 10, 31))
				.has(noPlanifiedDestructionDate())
				.has(container("bac11"));

		assertThat(rm.getFolder_A51())
				.has(openDate(2000, 10, 4))
				.has(closeDate(2001, 10, 31))
				.has(actualTransferDate(2004, 10, 31))
				.has(planifiedDepositDate(2008, 10, 31))
				.has(planifiedDestructionDate(2008, 10, 31))
				.has(container("bac10"));

		assertThat(rm.getFolder_A52())
				.has(openDate(2000, 11, 4))
				.has(closeDate(2001, 10, 31))
				.has(actualTransferDate(2004, 10, 31))
				.has(planifiedDepositDate(2008, 10, 31))
				.has(planifiedDestructionDate(2008, 10, 31))
				.has(container("bac10"));

		assertThat(rm.getFolder_A53())
				.has(openDate(2000, 11, 5))
				.has(closeDate(2002, 10, 31))
				.has(actualTransferDate(2005, 10, 31))
				.has(planifiedDepositDate(2009, 10, 31))
				.has(planifiedDestructionDate(2009, 10, 31))
				.has(container("bac10"));

		assertThat(rm.getFolder_A54())
				.has(openDate(2000, 5, 4))
				.has(closeDate(2002, 10, 31))
				.has(actualTransferDate(2006, 10, 31))
				.has(noPlanifiedDepositDate())
				.has(planifiedDestructionDate(2007, 10, 31))
				.has(container("bac10"));

		assertThat(rm.getFolder_A55())
				.has(openDate(2000, 7, 4))
				.has(closeDate(2002, 10, 31))
				.has(actualTransferDate(2006, 10, 31))
				.has(noPlanifiedDepositDate())
				.has(planifiedDestructionDate(2007, 10, 31))
				.has(container("bac10"));

		assertThat(rm.getFolder_A56())
				.has(openDate(2000, 7, 5))
				.has(closeDate(2003, 10, 31))
				.has(actualTransferDate(2007, 10, 31))
				.has(noPlanifiedDepositDate())
				.has(planifiedDestructionDate(2008, 10, 31))
				.has(container("bac10"));

		assertThat(rm.getFolder_A57())
				.has(openDate(2000, 6, 4))
				.has(closeDate(2002, 10, 31))
				.has(actualTransferDate(2006, 10, 31))
				.has(planifiedDepositDate(2007, 10, 31))
				.has(noPlanifiedDestructionDate())
				.has(container("bac10"));

		assertThat(rm.getFolder_A58())
				.has(openDate(2000, 7, 4))
				.has(closeDate(2002, 10, 31))
				.has(actualTransferDate(2006, 10, 31))
				.has(planifiedDepositDate(2007, 10, 31))
				.has(noPlanifiedDestructionDate())
				.has(container("bac10"));

		assertThat(rm.getFolder_A59())
				.has(openDate(2000, 7, 5))
				.has(closeDate(2003, 10, 31))
				.has(actualTransferDate(2007, 10, 31))
				.has(planifiedDepositDate(2008, 10, 31))
				.has(noPlanifiedDestructionDate())
				.has(container("bac10"));

		assertThat(rm.getFolder_A79())
				.has(openDate(2000, 10, 4))
				.has(closeDate(2001, 10, 31))
				.has(actualDepositDate(2011, 2, 13))
				.has(noPlanifiedDestructionDate())
				.has(container("bac05"));

		assertThat(rm.getFolder_A80())
				.has(openDate(2000, 11, 4))
				.has(closeDate(2001, 10, 31))
				.has(noPlanifiedDepositDate())
				.has(actualDestructionDate(2011, 2, 13));

		assertThat(rm.getFolder_A81())
				.has(openDate(2000, 11, 5))
				.has(closeDate(2002, 10, 31))
				.has(actualDepositDate(2012, 2, 13))
				.has(noPlanifiedDestructionDate())
				.has(container("bac05"));

		assertThat(rm.getFolder_A82())
				.has(openDate(2000, 10, 4))
				.has(closeDate(2001, 10, 31))
				.has(noPlanifiedDepositDate())
				.has(actualDestructionDate(2007, 4, 14));

		assertThat(rm.getFolder_A83())
				.has(openDate(2000, 11, 4))
				.has(closeDate(2001, 10, 31))
				.has(noPlanifiedDepositDate())
				.has(actualDestructionDate(2007, 4, 14));

		assertThat(rm.getFolder_A84())
				.has(openDate(2000, 11, 5))
				.has(closeDate(2002, 10, 31))
				.has(noPlanifiedDepositDate())
				.has(actualDestructionDate(2008, 4, 14));

		assertThat(rm.getFolder_A85())
				.has(openDate(2000, 10, 4))
				.has(closeDate(2001, 10, 31))
				.has(actualDepositDate(2011, 5, 15))
				.has(noPlanifiedDestructionDate())
				.has(container("bac05"));

		assertThat(rm.getFolder_A86())
				.has(openDate(2000, 11, 4))
				.has(closeDate(2001, 10, 31))
				.has(actualDepositDate(2011, 5, 15))
				.has(noPlanifiedDestructionDate())
				.has(container("bac05"));

		assertThat(rm.getFolder_A87())
				.has(openDate(2000, 11, 5))
				.has(closeDate(2002, 10, 31))
				.has(actualDepositDate(2012, 5, 15))
				.has(noPlanifiedDestructionDate())
				.has(container("bac05"));

		assertThat(rm.getFolder_A88())
				.has(openDate(2000, 10, 4))
				.has(closeDate(2001, 10, 31))
				.has(noPlanifiedDepositDate())
				.has(actualDestructionDate(2011, 6, 16));

		assertThat(rm.getFolder_A89())
				.has(openDate(2000, 11, 4))
				.has(closeDate(2001, 10, 31))
				.has(actualDepositDate(2011, 6, 16))
				.has(noPlanifiedDestructionDate())
				.has(container("bac05"));

		assertThat(rm.getFolder_A90())
				.has(openDate(2000, 11, 5))
				.has(closeDate(2002, 10, 31))
				.has(noPlanifiedDepositDate())
				.has(actualDestructionDate(2012, 6, 16));

		assertThat(rm.getFolder_A91())
				.has(openDate(2000, 5, 4))
				.has(closeDate(2002, 10, 31))
				.has(noPlanifiedDepositDate())
				.has(actualDestructionDate(2009, 7, 16));

		assertThat(rm.getFolder_A92())
				.has(openDate(2000, 7, 4))
				.has(closeDate(2002, 10, 31))
				.has(noPlanifiedDepositDate())
				.has(actualDestructionDate(2009, 7, 16));

		assertThat(rm.getFolder_A93())
				.has(openDate(2000, 7, 5))
				.has(closeDate(2003, 10, 31))
				.has(noPlanifiedDepositDate())
				.has(actualDestructionDate(2010, 7, 16));

		assertThat(rm.getFolder_A94())
				.has(openDate(2000, 6, 4))
				.has(closeDate(2002, 10, 31))
				.has(actualDepositDate(2009, 8, 17))
				.has(noPlanifiedDestructionDate())
				.has(container("bac04"));

		assertThat(rm.getFolder_A95())
				.has(openDate(2000, 7, 4))
				.has(closeDate(2002, 10, 31))
				.has(actualDepositDate(2009, 8, 17))
				.has(noPlanifiedDestructionDate())
				.has(container("bac04"));

		assertThat(rm.getFolder_A96())
				.has(openDate(2000, 7, 5))
				.has(closeDate(2003, 10, 31))
				.has(actualDepositDate(2010, 8, 17))
				.has(noPlanifiedDestructionDate())
				.has(container("bac04"));

		assertThat(rm.getFolder_B01())
				.has(openDate(2000, 10, 4))
				.has(noCloseDate())
				.has(noPlanifiedTransferDate())
				.has(noPlanifiedDepositDate())
				.has(noPlanifiedDestructionDate());

		assertThat(rm.getFolder_B02())
				.has(openDate(2000, 10, 4))
				.has(noCloseDate())
				.has(noPlanifiedTransferDate())
				.has(noPlanifiedDepositDate())
				.has(noPlanifiedDestructionDate());

		assertThat(rm.getFolder_B03())
				.has(openDate(2000, 10, 4))
				.has(noCloseDate())
				.has(noPlanifiedTransferDate())
				.has(noPlanifiedDepositDate())
				.has(noPlanifiedDestructionDate());

		assertThat(rm.getFolder_B04())
				.has(openDate(2000, 10, 4))
				.has(closeDate(2001, 10, 31))
				.has(planifiedTransferDate(2006, 10, 31))
				.has(planifiedDepositDate(2008, 10, 31))
				.has(planifiedDestructionDate(2008, 10, 31));

		assertThat(rm.getFolder_B05())
				.has(openDate(2000, 10, 4))
				.has(closeDate(2001, 10, 31))
				.has(planifiedTransferDate(2003, 10, 31))
				.has(noPlanifiedDepositDate())
				.has(planifiedDestructionDate(2003, 10, 31));

		assertThat(rm.getFolder_B06())
				.has(openDate(2000, 10, 4))
				.has(closeDate(2001, 10, 31))
				.has(planifiedTransferDate(2002, 10, 31))
				.has(noPlanifiedDepositDate())
				.has(planifiedDestructionDate(2002, 10, 31));

		assertThat(rm.getFolder_B07())
				.has(openDate(2000, 10, 4))
				.has(closeDate(2001, 10, 31))
				.has(planifiedTransferDate(2002, 10, 31))
				.has(planifiedDepositDate(2006, 10, 31))
				.has(planifiedDestructionDate(2006, 10, 31));

		assertThat(rm.getFolder_B08())
				.has(openDate(2000, 5, 4))
				.has(closeDate(2002, 10, 31))
				.has(planifiedTransferDate(2005, 10, 31))
				.has(noPlanifiedDepositDate())
				.has(planifiedDestructionDate(2006, 10, 31));

		assertThat(rm.getFolder_B09())
				.has(openDate(2000, 6, 4))
				.has(closeDate(2002, 10, 31))
				.has(planifiedTransferDate(2005, 10, 31))
				.has(planifiedDepositDate(2006, 10, 31))
				.has(noPlanifiedDestructionDate());

		assertThat(rm.getFolder_B30())
				.has(openDate(2000, 10, 4))
				.has(closeDate(2001, 10, 31))
				.has(actualTransferDate(2007, 10, 31))
				.has(planifiedDepositDate(2009, 10, 31))
				.has(planifiedDestructionDate(2009, 10, 31))
				.has(container("bac08"));

		assertThat(rm.getFolder_B31())
				.has(openDate(2000, 10, 4))
				.has(closeDate(2001, 10, 31))
				.has(actualTransferDate(2005, 10, 31))
				.has(noPlanifiedDepositDate())
				.has(planifiedDestructionDate(2005, 10, 31))
				.has(container("bac09"));

		assertThat(rm.getFolder_B32())
				.has(openDate(2000, 10, 4))
				.has(closeDate(2001, 10, 31))
				.has(actualTransferDate(2004, 10, 31))
				.has(noPlanifiedDepositDate())
				.has(planifiedDestructionDate(2004, 10, 31))
				.has(container("bac08"));

		assertThat(rm.getFolder_B33())
				.has(openDate(2000, 10, 4))
				.has(closeDate(2001, 10, 31))
				.has(actualTransferDate(2004, 10, 31))
				.has(planifiedDepositDate(2008, 10, 31))
				.has(planifiedDestructionDate(2008, 10, 31))
				.has(container("bac09"));

		assertThat(rm.getFolder_B34())
				.has(openDate(2000, 5, 4))
				.has(closeDate(2002, 10, 31))
				.has(actualTransferDate(2006, 10, 31))
				.has(noPlanifiedDepositDate())
				.has(planifiedDestructionDate(2007, 10, 31))
				.has(container("bac08"));

		assertThat(rm.getFolder_B35())
				.has(openDate(2000, 6, 4))
				.has(closeDate(2002, 10, 31))
				.has(actualTransferDate(2006, 10, 31))
				.has(planifiedDepositDate(2007, 10, 31))
				.has(noPlanifiedDestructionDate());

		assertThat(rm.getFolder_B50())
				.has(openDate(2000, 10, 4))
				.has(closeDate(2001, 10, 31))
				.has(actualDepositDate(2011, 2, 13))
				.has(noPlanifiedDestructionDate())
				.has(container("bac02"));

		assertThat(rm.getFolder_B51())
				.has(openDate(2000, 10, 4))
				.has(closeDate(2001, 10, 31))
				.has(noPlanifiedDepositDate())
				.has(actualDestructionDate(2007, 4, 14));

		assertThat(rm.getFolder_B52())
				.has(openDate(2000, 10, 4))
				.has(closeDate(2001, 10, 31))
				.has(noPlanifiedDepositDate())
				.has(actualDestructionDate(2006, 5, 15));

		assertThat(rm.getFolder_B53())
				.has(openDate(2000, 10, 4))
				.has(closeDate(2001, 10, 31))
				.has(noPlanifiedDepositDate())
				.has(actualDestructionDate(2011, 6, 16));

		assertThat(rm.getFolder_B54())
				.has(openDate(2000, 5, 4))
				.has(closeDate(2002, 10, 31))
				.has(noPlanifiedDepositDate())
				.has(actualDestructionDate(2009, 7, 16));

		assertThat(rm.getFolder_B55())
				.has(openDate(2000, 6, 4))
				.has(closeDate(2002, 10, 31))
				.has(actualDepositDate(2009, 8, 17))
				.has(noPlanifiedDestructionDate())
				.has(container("bac03"));

		assertThat(rm.getFolder_C01())
				.has(openDate(2000, 10, 4))
				.has(noCloseDate())
				.has(noPlanifiedTransferDate())
				.has(noPlanifiedDepositDate())
				.has(noPlanifiedDestructionDate());

		assertThat(rm.getFolder_C02())
				.has(openDate(2000, 10, 4))
				.has(noCloseDate())
				.has(noPlanifiedTransferDate())
				.has(noPlanifiedDepositDate())
				.has(noPlanifiedDestructionDate());

		assertThat(rm.getFolder_C03())
				.has(openDate(2000, 10, 4))
				.has(noCloseDate())
				.has(noPlanifiedTransferDate())
				.has(noPlanifiedDepositDate())
				.has(noPlanifiedDestructionDate());

		assertThat(rm.getFolder_C04())
				.has(openDate(2000, 10, 4))
				.has(closeDate(2001, 10, 31))
				.has(planifiedTransferDate(2006, 10, 31))
				.has(planifiedDepositDate(2008, 10, 31))
				.has(planifiedDestructionDate(2008, 10, 31));

		assertThat(rm.getFolder_C05())
				.has(openDate(2000, 10, 4))
				.has(closeDate(2001, 10, 31))
				.has(planifiedTransferDate(2003, 10, 31))
				.has(noPlanifiedDepositDate())
				.has(planifiedDestructionDate(2003, 10, 31));

		assertThat(rm.getFolder_C06())
				.has(openDate(2000, 10, 4))
				.has(closeDate(2001, 10, 31))
				.has(planifiedTransferDate(2002, 10, 31))
				.has(noPlanifiedDepositDate())
				.has(planifiedDestructionDate(2002, 10, 31));

		assertThat(rm.getFolder_C07())
				.has(openDate(2000, 10, 4))
				.has(closeDate(2001, 10, 31))
				.has(planifiedTransferDate(2002, 10, 31))
				.has(planifiedDepositDate(2006, 10, 31))
				.has(planifiedDestructionDate(2006, 10, 31));

		assertThat(rm.getFolder_C08())
				.has(openDate(2000, 5, 4))
				.has(closeDate(2002, 10, 31))
				.has(planifiedTransferDate(2005, 10, 31))
				.has(noPlanifiedDepositDate())
				.has(planifiedDestructionDate(2006, 10, 31));

		assertThat(rm.getFolder_C09())
				.has(openDate(2000, 6, 4))
				.has(closeDate(2002, 10, 31))
				.has(planifiedTransferDate(2005, 10, 31))
				.has(planifiedDepositDate(2006, 10, 31))
				.has(noPlanifiedDestructionDate());

		assertThat(rm.getFolder_C30())
				.has(openDate(2000, 10, 4))
				.has(closeDate(2001, 10, 31))
				.has(actualTransferDate(2007, 10, 31))
				.has(planifiedDepositDate(2009, 10, 31))
				.has(planifiedDestructionDate(2009, 10, 31))
				.has(container("bac07"));

		assertThat(rm.getFolder_C31())
				.has(openDate(2000, 10, 4))
				.has(closeDate(2001, 10, 31))
				.has(actualTransferDate(2005, 10, 31))
				.has(noPlanifiedDepositDate())
				.has(planifiedDestructionDate(2005, 10, 31))
				.has(container("bac07"));

		assertThat(rm.getFolder_C32())
				.has(openDate(2000, 10, 4))
				.has(closeDate(2001, 10, 31))
				.has(actualTransferDate(2004, 10, 31))
				.has(noPlanifiedDepositDate())
				.has(planifiedDestructionDate(2004, 10, 31))
				.has(container("bac07"));

		assertThat(rm.getFolder_C33())
				.has(openDate(2000, 10, 4))
				.has(closeDate(2001, 10, 31))
				.has(actualTransferDate(2004, 10, 31))
				.has(planifiedDepositDate(2008, 10, 31))
				.has(planifiedDestructionDate(2008, 10, 31))
				.has(container("bac07"));

		assertThat(rm.getFolder_C34())
				.has(openDate(2000, 5, 4))
				.has(closeDate(2002, 10, 31))
				.has(actualTransferDate(2006, 10, 31))
				.has(noPlanifiedDepositDate())
				.has(planifiedDestructionDate(2007, 10, 31))
				.has(container("bac07"));

		assertThat(rm.getFolder_C35())
				.has(openDate(2000, 6, 4))
				.has(closeDate(2002, 10, 31))
				.has(actualTransferDate(2006, 10, 31))
				.has(planifiedDepositDate(2007, 10, 31))
				.has(noPlanifiedDestructionDate())
				.has(container("bac06"));

		assertThat(rm.getFolder_C50())
				.has(openDate(2000, 10, 4))
				.has(closeDate(2001, 10, 31))
				.has(actualDepositDate(2011, 2, 13))
				.has(noPlanifiedDestructionDate())
				.has(container("bac01"));

		assertThat(rm.getFolder_C51())
				.has(openDate(2000, 10, 4))
				.has(closeDate(2001, 10, 31))
				.has(noPlanifiedDepositDate())
				.has(actualDestructionDate(2007, 4, 14));

		assertThat(rm.getFolder_C52())
				.has(openDate(2000, 10, 4))
				.has(closeDate(2001, 10, 31))
				.has(noPlanifiedDepositDate())
				.has(actualDestructionDate(2006, 5, 15));

		assertThat(rm.getFolder_C53())
				.has(openDate(2000, 10, 4))
				.has(closeDate(2001, 10, 31))
				.has(noPlanifiedDepositDate())
				.has(actualDestructionDate(2011, 6, 16));

		assertThat(rm.getFolder_C54())
				.has(openDate(2000, 5, 4))
				.has(closeDate(2002, 10, 31))
				.has(noPlanifiedDepositDate())
				.has(actualDestructionDate(2009, 7, 16));

		assertThat(rm.getFolder_C55())
				.has(openDate(2000, 6, 4))
				.has(closeDate(2002, 10, 31))
				.has(actualDepositDate(2009, 8, 17))
				.has(noPlanifiedDestructionDate())
				.has(container("bac01"));

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
				assertThat(value.getMainCopyRule()).isEqualTo(rule);
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
				return true;
			}
		};
	}

	private Condition<? super Folder> noPlanifiedDestructionDate() {
		return new Condition<Folder>() {
			@Override
			public boolean matches(Folder value) {
				assertThat(value.getActualDestructionDate()).isNull();
				return true;
			}
		};
	}

	private Condition<? super Folder> noPlanifiedDepositDate() {
		return new Condition<Folder>() {
			@Override
			public boolean matches(Folder value) {
				assertThat(value.getActualDepositDate()).isNull();
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
				assertThat(value.getActualTransferDate()).isNull();
				assertThat(value.getExpectedTransferDate()).isEqualTo(date);
				return true;
			}
		};
	}

	private Condition<? super Folder> planifiedDestructionDate(int year, int month, int day) {
		final LocalDate date = new LocalDate(year, month, day);
		return new Condition<Folder>() {
			@Override
			public boolean matches(Folder value) {
				assertThat(value.getActualDestructionDate()).isNull();
				assertThat(value.getExpectedDestructionDate()).isEqualTo(date);
				return true;
			}
		};
	}

	private Condition<? super Folder> planifiedDepositDate(int year, int month, int day) {
		final LocalDate date = new LocalDate(year, month, day);
		return new Condition<Folder>() {
			@Override
			public boolean matches(Folder value) {
				assertThat(value.getActualDepositDate()).isNull();
				assertThat(value.getExpectedDepositDate()).isEqualTo(date);
				return true;
			}
		};
	}
}
