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

import java.util.Arrays;

import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;

import com.constellio.app.modules.rm.RMConfigs;
import com.constellio.app.modules.rm.RMConfigs.DecommissioningPhase;
import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.model.enums.DecommissioningListType;
import com.constellio.app.modules.rm.model.enums.DecommissioningType;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.DecommissioningList;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.structures.DecomListContainerDetail;
import com.constellio.app.modules.rm.wrappers.structures.DecomListFolderDetail;
import com.constellio.app.modules.rm.wrappers.structures.FolderDetailWithType;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.configs.SystemConfigurationsManager;
import com.constellio.sdk.tests.ConstellioTest;

public class DecommissioningService_tawmas_AcceptTest extends ConstellioTest {
	DecommissioningService service;
	RMSchemasRecordsServices rm;
	RMTestRecords records = new RMTestRecords(zeCollection);

	@Before
	public void setUp()
			throws Exception {

		givenCollection(zeCollection).withConstellioRMModule().withAllTestUsers();
		records.setup(getModelLayerFactory()).withFoldersAndContainersOfEveryStatus().withDocumentsHavingContent();

		rm = new RMSchemasRecordsServices(zeCollection, getModelLayerFactory());
		service = new DecommissioningService(zeCollection, getModelLayerFactory());
	}

	@Test
	public void givenUnprocessedListAndManagerThenIsEditable() {
		assertThat(service.isEditable(records.getList01(), records.getGandalf_managerInABC())).isTrue();
		assertThat(service.isEditable(records.getList02(), records.getGandalf_managerInABC())).isTrue();
		assertThat(service.isEditable(records.getList03(), records.getGandalf_managerInABC())).isTrue();
		assertThat(service.isEditable(records.getList04(), records.getGandalf_managerInABC())).isTrue();
		assertThat(service.isEditable(records.getList05(), records.getGandalf_managerInABC())).isTrue();
		assertThat(service.isEditable(records.getList06(), records.getGandalf_managerInABC())).isTrue();
		assertThat(service.isEditable(records.getList07(), records.getGandalf_managerInABC())).isTrue();
		assertThat(service.isEditable(records.getList08(), records.getGandalf_managerInABC())).isTrue();
		assertThat(service.isEditable(records.getList09(), records.getGandalf_managerInABC())).isTrue();
		assertThat(service.isEditable(records.getList10(), records.getGandalf_managerInABC())).isTrue();
	}

	@Test
	public void givenProcessedListThenIsNotEditable() {
		assertThat(service.isEditable(records.getList11(), records.getGandalf_managerInABC())).isFalse();
		assertThat(service.isEditable(records.getList12(), records.getGandalf_managerInABC())).isFalse();
		assertThat(service.isEditable(records.getList13(), records.getGandalf_managerInABC())).isFalse();
		assertThat(service.isEditable(records.getList14(), records.getGandalf_managerInABC())).isFalse();
		assertThat(service.isEditable(records.getList15(), records.getGandalf_managerInABC())).isFalse();
	}

	@Test
	public void givenUnprocessedListAndManagerThenIsDeletable() {
		assertThat(service.isDeletable(records.getList01(), records.getGandalf_managerInABC())).isTrue();
		assertThat(service.isDeletable(records.getList02(), records.getGandalf_managerInABC())).isTrue();
		assertThat(service.isDeletable(records.getList03(), records.getGandalf_managerInABC())).isTrue();
		assertThat(service.isDeletable(records.getList04(), records.getGandalf_managerInABC())).isTrue();
		assertThat(service.isDeletable(records.getList05(), records.getGandalf_managerInABC())).isTrue();
		assertThat(service.isDeletable(records.getList06(), records.getGandalf_managerInABC())).isTrue();
		assertThat(service.isDeletable(records.getList07(), records.getGandalf_managerInABC())).isTrue();
		assertThat(service.isDeletable(records.getList08(), records.getGandalf_managerInABC())).isTrue();
		assertThat(service.isDeletable(records.getList09(), records.getGandalf_managerInABC())).isTrue();
		assertThat(service.isDeletable(records.getList10(), records.getGandalf_managerInABC())).isTrue();
	}

	@Test
	public void givenProcessedListThenIsNotDeletable() {
		assertThat(service.isDeletable(records.getList11(), records.getGandalf_managerInABC())).isFalse();
		assertThat(service.isDeletable(records.getList12(), records.getGandalf_managerInABC())).isFalse();
		assertThat(service.isDeletable(records.getList13(), records.getGandalf_managerInABC())).isFalse();
		assertThat(service.isDeletable(records.getList14(), records.getGandalf_managerInABC())).isFalse();
		assertThat(service.isDeletable(records.getList15(), records.getGandalf_managerInABC())).isFalse();
	}

	@Test
	public void givenUnprocessedListToCloseOrDestroyAndManagerThenIsProcessable() {
		assertThat(service.isProcessable(records.getList02(), records.getGandalf_managerInABC())).isTrue();
		assertThat(service.isProcessable(records.getList03(), records.getGandalf_managerInABC())).isTrue();
		assertThat(service.isProcessable(records.getList07(), records.getGandalf_managerInABC())).isTrue();
	}

	@Test
	public void givenUnprocessedListWithAllElectronicAndManagerThenIsProcessable() {
		assertThat(service.isProcessable(records.getList06(), records.getGandalf_managerInABC())).isTrue();
		assertThat(service.isProcessable(records.getList09(), records.getGandalf_managerInABC())).isTrue();
	}

	@Test
	public void givenUnprocessedListWithNonElectronicAndManagerWhenFoldersAlreadyInContainersThenIsProcessable() {
		assertThat(service.isProcessable(records.getList08(), records.getGandalf_managerInABC())).isTrue();
		assertThat(service.isProcessable(records.getList10(), records.getGandalf_managerInABC())).isTrue();
		assertThat(service.isProcessable(records.getList16(), records.getGandalf_managerInABC())).isTrue();
	}

	@Test
	public void givenUnprocessedListWithNonElectronicAndManagerWhenFoldersNotInContainersThenIsNotProcessable() {
		assertThat(service.isProcessable(records.getList04(), records.getGandalf_managerInABC())).isFalse();
		assertThat(service.isProcessable(records.getList05(), records.getGandalf_managerInABC())).isFalse();
	}

	@Test
	public void givenProcessedListThenIsNotProcessable() {
		assertThat(service.isProcessable(records.getList11(), records.getGandalf_managerInABC())).isFalse();
		assertThat(service.isProcessable(records.getList12(), records.getGandalf_managerInABC())).isFalse();
		assertThat(service.isProcessable(records.getList13(), records.getGandalf_managerInABC())).isFalse();
		assertThat(service.isProcessable(records.getList14(), records.getGandalf_managerInABC())).isFalse();
		assertThat(service.isProcessable(records.getList15(), records.getGandalf_managerInABC())).isFalse();
	}

	@Test
	public void givenUnprocessedListWhenNotClosureOrDestroyalThenCanEditContainers() {
		assertThat(service.canEditContainers(records.getList04(), records.getGandalf_managerInABC())).isTrue();
		assertThat(service.canEditContainers(records.getList05(), records.getGandalf_managerInABC())).isTrue();
		assertThat(service.canEditContainers(records.getList06(), records.getGandalf_managerInABC())).isTrue();
		assertThat(service.canEditContainers(records.getList08(), records.getGandalf_managerInABC())).isTrue();
		assertThat(service.canEditContainers(records.getList09(), records.getGandalf_managerInABC())).isTrue();
		assertThat(service.canEditContainers(records.getList10(), records.getGandalf_managerInABC())).isTrue();
		assertThat(service.canEditContainers(records.getList16(), records.getGandalf_managerInABC())).isTrue();
	}

	@Test
	public void givenUnprocessedSortableListToDestroyThenCanEditContainers() {
		assertThat(service.canEditContainers(records.getList01(), records.getGandalf_managerInABC())).isTrue();
	}

	@Test
	public void givenUnprocessedListToCloseOrDestroyThenCannotEditContainers() {
		assertThat(service.canEditContainers(records.getList02(), records.getGandalf_managerInABC())).isFalse();
		assertThat(service.canEditContainers(records.getList03(), records.getGandalf_managerInABC())).isFalse();
		assertThat(service.canEditContainers(records.getList07(), records.getGandalf_managerInABC())).isFalse();
	}

	@Test
	public void givenProcessedListThenCannotEditContainers() {
		assertThat(service.canEditContainers(records.getList11(), records.getGandalf_managerInABC())).isFalse();
		assertThat(service.canEditContainers(records.getList12(), records.getGandalf_managerInABC())).isFalse();
		assertThat(service.canEditContainers(records.getList13(), records.getGandalf_managerInABC())).isFalse();
		assertThat(service.canEditContainers(records.getList14(), records.getGandalf_managerInABC())).isFalse();
		assertThat(service.canEditContainers(records.getList15(), records.getGandalf_managerInABC())).isFalse();
	}

	@Test
	public void givenRegularUserThenIsNotEditableDeletableProcessableContainerizable() {
		assertThat(service.isEditable(records.getList01(), records.getAlice())).isFalse();
		assertThat(service.isDeletable(records.getList01(), records.getAlice())).isFalse();
		assertThat(service.isProcessable(records.getList01(), records.getAlice())).isFalse();
		assertThat(service.canEditContainers(records.getList01(), records.getAlice())).isFalse();
	}

	@Test
	public void givenUnprocessedListToCloseOrDestroyThenAllFoldersAreProcessable() {
		verifyFolderProcessabilityForAllFoldersIn(records.getList01(), true);
		verifyFolderProcessabilityForAllFoldersIn(records.getList02(), true);
		verifyFolderProcessabilityForAllFoldersIn(records.getList03(), true);
		verifyFolderProcessabilityForAllFoldersIn(records.getList07(), true);
	}

	@Test
	public void givenUnprocessedListWithAllElectronicThenAllFoldersAreProcessable() {
		verifyFolderProcessabilityForAllFoldersIn(records.getList06(), true);
		verifyFolderProcessabilityForAllFoldersIn(records.getList09(), true);
	}

	@Test
	public void givenUnprocessedListWithNonElectronicWhenFoldersAlreadyInContainersThenAllFoldersAreProcessable() {
		verifyFolderProcessabilityForAllFoldersIn(records.getList09(), true);
	}

	@Test
	public void givenUnprocessedListWithNonElectronicWhenFoldersNotInContainersThenNoFoldersAreProcessable() {
		verifyFolderProcessabilityForAllFoldersIn(records.getList04(), false);
	}

	@Test
	public void givenListToDepositOrDestroyWithSortableFoldersThenIsSortable() {
		assertThat(service.isSortable(records.getList01())).isTrue();
		assertThat(service.isSortable(records.getList10())).isTrue();
		assertThat(service.isSortable(records.getList18())).isTrue();
		assertThat(service.isSortable(records.getList19())).isTrue();
		// Already treated
		assertThat(service.isSortable(records.getList14())).isTrue();
	}

	@Test
	public void givenListToDepositOrDestroyWithoutSortableFoldersThenIsNotSortable() {
		assertThat(service.isSortable(records.getList02())).isFalse();
		assertThat(service.isSortable(records.getList07())).isFalse();
		assertThat(service.isSortable(records.getList09())).isFalse();
		// Already treated
		assertThat(service.isSortable(records.getList15())).isFalse();
	}

	@Test
	public void givenListToCloseOrTransferThenIsNotSortable() {
		assertThat(service.isSortable(records.getList03())).isFalse();
		assertThat(service.isSortable(records.getList04())).isFalse();
		assertThat(service.isSortable(records.getList05())).isFalse();
		assertThat(service.isSortable(records.getList06())).isFalse();
		assertThat(service.isSortable(records.getList16())).isFalse();
		assertThat(service.isSortable(records.getList17())).isFalse();
		// Already treated
		assertThat(service.isSortable(records.getList11())).isFalse();
		assertThat(service.isSortable(records.getList12())).isFalse();
		assertThat(service.isSortable(records.getList13())).isFalse();
	}

	@Test
	public void givenFixedPeriodSearchThenCreateListToClose() {
		DecommissioningListParams params = new DecommissioningListParams();
		params.setTitle("Ze title");
		params.setDescription("Ze description");
		params.setAdministrativeUnit(records.unitId_10);
		params.setFilingSpace(records.filingId_A);
		params.setSearchType(SearchType.fixedPeriod);
		params.setSelectedFolderIds(Arrays.asList(records.folder_A01));

		DecommissioningList decommissioningList = service.createDecommissioningList(params, records.getGandalf_managerInABC());
		assertThat(decommissioningList.getTitle()).isEqualTo("Ze title");
		assertThat(decommissioningList.getDescription()).isEqualTo("Ze description");
		assertThat(decommissioningList.getAdministrativeUnit()).isEqualTo(records.unitId_10);
		assertThat(decommissioningList.getFilingSpace()).isEqualTo(records.filingId_A);
		assertThat(decommissioningList.getDecommissioningListType()).isEqualTo(DecommissioningListType.FOLDERS_TO_CLOSE);
		assertThat(decommissioningList.getFolderDetails()).containsOnly(new DecomListFolderDetail(records.folder_A01));
		assertThat(decommissioningList.getContainerDetails()).isEmpty();
	}

	@Test
	public void givenCode888SearchThenCreateListToClose() {
		DecommissioningListParams params = new DecommissioningListParams();
		params.setTitle("Ze title");
		params.setDescription("Ze description");
		params.setAdministrativeUnit(records.unitId_10);
		params.setFilingSpace(records.filingId_A);
		params.setSearchType(SearchType.code888);
		params.setSelectedFolderIds(Arrays.asList(records.folder_A04));

		DecommissioningList decommissioningList = service.createDecommissioningList(params, records.getGandalf_managerInABC());
		assertThat(decommissioningList.getTitle()).isEqualTo("Ze title");
		assertThat(decommissioningList.getDescription()).isEqualTo("Ze description");
		assertThat(decommissioningList.getAdministrativeUnit()).isEqualTo(records.unitId_10);
		assertThat(decommissioningList.getFilingSpace()).isEqualTo(records.filingId_A);
		assertThat(decommissioningList.getDecommissioningListType()).isEqualTo(DecommissioningListType.FOLDERS_TO_CLOSE);
		assertThat(decommissioningList.getFolderDetails()).containsOnly(new DecomListFolderDetail(records.folder_A04));
		assertThat(decommissioningList.getContainerDetails()).isEmpty();
	}

	@Test
	public void givenCode999SearchThenCreateListToClose() {
		DecommissioningListParams params = new DecommissioningListParams();
		params.setTitle("Ze title");
		params.setDescription("Ze description");
		params.setAdministrativeUnit(records.unitId_10);
		params.setFilingSpace(records.filingId_A);
		params.setSearchType(SearchType.code999);
		params.setSelectedFolderIds(Arrays.asList(records.folder_A07));

		DecommissioningList decommissioningList = service.createDecommissioningList(params, records.getGandalf_managerInABC());
		assertThat(decommissioningList.getTitle()).isEqualTo("Ze title");
		assertThat(decommissioningList.getDescription()).isEqualTo("Ze description");
		assertThat(decommissioningList.getAdministrativeUnit()).isEqualTo(records.unitId_10);
		assertThat(decommissioningList.getFilingSpace()).isEqualTo(records.filingId_A);
		assertThat(decommissioningList.getDecommissioningListType()).isEqualTo(DecommissioningListType.FOLDERS_TO_CLOSE);
		assertThat(decommissioningList.getFolderDetails()).containsOnly(new DecomListFolderDetail(records.folder_A07));
		assertThat(decommissioningList.getContainerDetails()).isEmpty();
	}

	@Test
	public void givenTransferSearchThenCreateListToTransfer() {
		DecommissioningListParams params = new DecommissioningListParams();
		params.setTitle("Ze title");
		params.setDescription("Ze description");
		params.setAdministrativeUnit(records.unitId_10);
		params.setFilingSpace(records.filingId_A);
		params.setSearchType(SearchType.transfer);
		params.setSelectedFolderIds(Arrays.asList(records.folder_A10));

		DecommissioningList decommissioningList = service.createDecommissioningList(params, records.getGandalf_managerInABC());
		assertThat(decommissioningList.getTitle()).isEqualTo("Ze title");
		assertThat(decommissioningList.getDescription()).isEqualTo("Ze description");
		assertThat(decommissioningList.getAdministrativeUnit()).isEqualTo(records.unitId_10);
		assertThat(decommissioningList.getFilingSpace()).isEqualTo(records.filingId_A);
		assertThat(decommissioningList.getDecommissioningListType()).isEqualTo(DecommissioningListType.FOLDERS_TO_TRANSFER);
		assertThat(decommissioningList.getFolderDetails()).containsOnly(new DecomListFolderDetail(records.folder_A10));
		assertThat(decommissioningList.getContainerDetails()).isEmpty();
	}

	@Test
	public void givenActiveToDepositSearchThenCreateListToDeposit() {
		DecommissioningListParams params = new DecommissioningListParams();
		params.setTitle("Ze title");
		params.setDescription("Ze description");
		params.setAdministrativeUnit(records.unitId_10);
		params.setFilingSpace(records.filingId_A);
		params.setSearchType(SearchType.activeToDeposit);
		params.setSelectedFolderIds(Arrays.asList(records.folder_A10));

		DecommissioningList decommissioningList = service.createDecommissioningList(params, records.getGandalf_managerInABC());
		assertThat(decommissioningList.getTitle()).isEqualTo("Ze title");
		assertThat(decommissioningList.getDescription()).isEqualTo("Ze description");
		assertThat(decommissioningList.getAdministrativeUnit()).isEqualTo(records.unitId_10);
		assertThat(decommissioningList.getFilingSpace()).isEqualTo(records.filingId_A);
		assertThat(decommissioningList.getDecommissioningListType()).isEqualTo(DecommissioningListType.FOLDERS_TO_DEPOSIT);
		assertThat(decommissioningList.getFolderDetails()).containsOnly(new DecomListFolderDetail(records.folder_A10));
		assertThat(decommissioningList.getContainerDetails()).isEmpty();
	}

	@Test
	public void givenActiveToDestroySearchThenCreateListToDestroy() {
		DecommissioningListParams params = new DecommissioningListParams();
		params.setTitle("Ze title");
		params.setDescription("Ze description");
		params.setAdministrativeUnit(records.unitId_10);
		params.setFilingSpace(records.filingId_A);
		params.setSearchType(SearchType.activeToDestroy);
		params.setSelectedFolderIds(Arrays.asList(records.folder_A10));

		DecommissioningList decommissioningList = service.createDecommissioningList(params, records.getGandalf_managerInABC());
		assertThat(decommissioningList.getTitle()).isEqualTo("Ze title");
		assertThat(decommissioningList.getDescription()).isEqualTo("Ze description");
		assertThat(decommissioningList.getAdministrativeUnit()).isEqualTo(records.unitId_10);
		assertThat(decommissioningList.getFilingSpace()).isEqualTo(records.filingId_A);
		assertThat(decommissioningList.getDecommissioningListType()).isEqualTo(DecommissioningListType.FOLDERS_TO_DESTROY);
		assertThat(decommissioningList.getFolderDetails()).containsOnly(new DecomListFolderDetail(records.folder_A10));
		assertThat(decommissioningList.getContainerDetails()).isEmpty();
	}

	@Test
	public void givenSemiActiveToDepositSearchThenCreateListToDepositWithContainerDetailsAndFoldersFromSameContainers() {
		DecommissioningListParams params = new DecommissioningListParams();
		params.setTitle("Ze title");
		params.setDescription("Ze description");
		params.setAdministrativeUnit(records.unitId_10);
		params.setFilingSpace(records.filingId_A);
		params.setSearchType(SearchType.semiActiveToDeposit);
		params.setSelectedFolderIds(Arrays.asList(records.folder_A42));

		DecommissioningList decommissioningList = service.createDecommissioningList(params, records.getGandalf_managerInABC());
		assertThat(decommissioningList.getTitle()).isEqualTo("Ze title");
		assertThat(decommissioningList.getDescription()).isEqualTo("Ze description");
		assertThat(decommissioningList.getAdministrativeUnit()).isEqualTo(records.unitId_10);
		assertThat(decommissioningList.getFilingSpace()).isEqualTo(records.filingId_A);
		assertThat(decommissioningList.getDecommissioningListType()).isEqualTo(DecommissioningListType.FOLDERS_TO_DEPOSIT);
		assertThat(decommissioningList.getFolderDetails()).containsOnly(
				new DecomListFolderDetail(records.folder_A42).setContainerRecordId(records.containerId_bac13),
				new DecomListFolderDetail(records.folder_A43).setContainerRecordId(records.containerId_bac13),
				new DecomListFolderDetail(records.folder_A44).setContainerRecordId(records.containerId_bac13));
		assertThat(decommissioningList.getContainerDetails()).containsOnly(
				new DecomListContainerDetail(records.containerId_bac13));
	}

	@Test
	public void givenSemiActiveToDestroySearchThenCreateListToDepositWithContainerDetailsAndFoldersFromSameContainers() {
		DecommissioningListParams params = new DecommissioningListParams();
		params.setTitle("Ze title");
		params.setDescription("Ze description");
		params.setAdministrativeUnit(records.unitId_10);
		params.setFilingSpace(records.filingId_A);
		params.setSearchType(SearchType.semiActiveToDestroy);
		params.setSelectedFolderIds(Arrays.asList(records.folder_A42));

		DecommissioningList decommissioningList = service.createDecommissioningList(params, records.getGandalf_managerInABC());
		assertThat(decommissioningList.getTitle()).isEqualTo("Ze title");
		assertThat(decommissioningList.getDescription()).isEqualTo("Ze description");
		assertThat(decommissioningList.getAdministrativeUnit()).isEqualTo(records.unitId_10);
		assertThat(decommissioningList.getFilingSpace()).isEqualTo(records.filingId_A);
		assertThat(decommissioningList.getDecommissioningListType()).isEqualTo(DecommissioningListType.FOLDERS_TO_DESTROY);
		assertThat(decommissioningList.getFolderDetails()).containsOnly(
				new DecomListFolderDetail(records.folder_A42).setContainerRecordId(records.containerId_bac13),
				new DecomListFolderDetail(records.folder_A43).setContainerRecordId(records.containerId_bac13),
				new DecomListFolderDetail(records.folder_A44).setContainerRecordId(records.containerId_bac13));
		assertThat(decommissioningList.getContainerDetails()).containsOnly(
				new DecomListContainerDetail(records.containerId_bac13));
	}

	@Test
	public void givenListToCloseThenAllFoldersAreClosed() {
		User processingUser = records.getGandalf_managerInABC();
		LocalDate processingDate = new LocalDate();
		givenTimeIs(processingDate);

		service.decommission(records.getList03(), processingUser);
		verifyProcessed(processingDate, processingUser, records.getList03());
		verifyFoldersClosed(processingUser, processingDate, records.getFolder_A01(), records.getFolder_A02(),
				records.getFolder_A03());
	}

	@Test
	public void givenListToTransferThenAllFoldersAreTransferred() {
		User processingUser = records.getGandalf_managerInABC();
		LocalDate processingDate = new LocalDate();
		givenTimeIs(processingDate);

		service.decommission(records.getList16(), processingUser);
		verifyProcessed(processingDate, processingUser, records.getList16());
		verifyFoldersTransferred(processingDate, processingUser, records.containerId_bac14,
				records.getFolder_A22(), records.getFolder_A23(), records.getFolder_A24());
		verifyContainersTransferred(processingDate, processingUser, records.getContainerBac14());
	}

	@Test
	public void givenListToTransferWhenPurgeMinorVersionOnTransferThenMinorVersionsArePurged() {
		getConfigurationManager().setValue(RMConfigs.MINOR_VERSIONS_PURGED_ON, DecommissioningPhase.ON_TRANSFER_OR_DEPOSIT);

		service.decommission(packed(records.getList05(), records.containerId_bac15), records.getGandalf_managerInABC());
		assertThat(records.getDocumentWithContent_A19().getContent().getHistoryVersions()).isEmpty();
	}

	@Test
	public void givenListToTransferWhenCreatePDFaOnTransferThenPDFaCreated() {
		getConfigurationManager().setValue(RMConfigs.PDFA_CREATED_ON, DecommissioningPhase.ON_TRANSFER_OR_DEPOSIT);

		service.decommission(packed(records.getList05(), records.containerId_bac15), records.getGandalf_managerInABC());
		assertThat(records.getDocumentWithContent_A19().getContent().getCurrentVersion().getMimetype())
				.isEqualTo("application/pdf");
	}

	@Test
	public void givenListToDepositThenAllFoldersAreDeposited() {
		User processingUser = records.getGandalf_managerInABC();
		LocalDate processingDate = new LocalDate();
		givenTimeIs(processingDate);

		service.decommission(records.getList17(), processingUser);
		verifyProcessed(processingDate, processingUser, records.getList17());
		verifyFoldersDeposited(processingDate, processingUser, records.containerId_bac11,
				records.getFolder_A49(), records.getFolder_A50());
		// Electronic only: not in container
		verifyFoldersDeposited(processingDate, processingUser, null, records.getFolder_A48());
		verifyContainersDeposited(processingDate, processingUser, records.getContainerBac11());
	}

	@Test
	public void givenListToDepositWhenPurgeMinorVersionOnTransferThenMinorVersionsArePurged() {
		getConfigurationManager().setValue(RMConfigs.MINOR_VERSIONS_PURGED_ON, DecommissioningPhase.ON_TRANSFER_OR_DEPOSIT);

		service.decommission(packed(records.getList20(), records.containerId_bac16), records.getGandalf_managerInABC());
		assertThat(records.getDocumentWithContent_A19().getContent().getHistoryVersions()).isEmpty();
	}

	@Test
	public void givenListToDepositWhenPurgeMinorVersionsOnDepositThenMinorVersionsArePurged() {
		getConfigurationManager().setValue(RMConfigs.MINOR_VERSIONS_PURGED_ON, DecommissioningPhase.ON_DEPOSIT);

		service.decommission(records.getList17(), records.getGandalf_managerInABC());
		assertThat(records.getDocumentWithContent_A49().getContent().getHistoryVersions()).isEmpty();
	}

	@Test
	public void givenListToDepositWhenCreatePDFaOnTransferThenPDFaCreated() {
		getConfigurationManager().setValue(RMConfigs.PDFA_CREATED_ON, DecommissioningPhase.ON_TRANSFER_OR_DEPOSIT);

		service.decommission(packed(records.getList20(), records.containerId_bac16), records.getGandalf_managerInABC());
		assertThat(records.getDocumentWithContent_A19().getContent().getCurrentVersion().getMimetype())
				.isEqualTo("application/pdf");
	}

	@Test
	public void givenListToDepositWhenCreatePDFaOnDepositThenPDFaCreated() {
		getConfigurationManager().setValue(RMConfigs.PDFA_CREATED_ON, DecommissioningPhase.ON_DEPOSIT);

		service.decommission(records.getList17(), records.getGandalf_managerInABC());
		assertThat(records.getDocumentWithContent_A49().getContent().getCurrentVersion().getMimetype())
				.isEqualTo("application/pdf");
	}

	@Test
	public void givenListToDestroyThenAllFoldersAreDestroyed() {
		User processingUser = records.getGandalf_managerInABC();
		LocalDate processingDate = new LocalDate();
		givenTimeIs(processingDate);

		service.decommission(records.getList02(), processingUser);
		verifyProcessed(processingDate, processingUser, records.getList02());
		verifyFoldersDestroyed(processingDate, processingUser,
				records.getFolder_A54(), records.getFolder_A55(), records.getFolder_A56());
		assertThat(records.getContainerBac10().isFull()).isFalse();
	}

	@Test
	public void givenListToDestroyThenDocumentsAreDestroyed() {
		service.decommission(records.getList21(), records.getGandalf_managerInABC());
		assertThat(records.getDocumentWithContent_A19().getContent()).isNull();
	}

	@Test
	public void givenListToDepositWithSortThenRegularFoldersAreDepositedAndReversedFoldersAreDestroyed() {
		User processingUser = records.getGandalf_managerInABC();
		LocalDate processingDate = new LocalDate();
		givenTimeIs(processingDate);

		service.decommission(records.getList18(), processingUser);
		verifyProcessed(processingDate, processingUser, records.getList18());
		verifyFoldersDeposited(processingDate, processingUser, records.containerId_bac08, records.getFolder_B30());
		verifyFoldersDestroyed(processingDate, processingUser, records.getFolder_B33());
		verifyContainersDeposited(processingDate, processingUser, records.getContainerBac08(), records.getContainerBac09());
	}

	@Test
	public void givenListToDestroyWithSortThenRegularFoldersAreDestroyedAndReversedFoldersAreDeposited() {
		User processingUser = records.getGandalf_managerInABC();
		LocalDate processingDate = new LocalDate();
		givenTimeIs(processingDate);

		service.decommission(records.getList19(), processingUser);
		verifyProcessed(processingDate, processingUser, records.getList19());
		verifyFoldersDeposited(processingDate, processingUser, records.containerId_bac09, records.getFolder_B33());
		verifyFoldersDestroyed(processingDate, processingUser, records.getFolder_B30());
		verifyContainersDeposited(processingDate, processingUser, records.getContainerBac08(), records.getContainerBac09());
	}

	private void verifyFolderProcessabilityForAllFoldersIn(DecommissioningList list, boolean expected) {
		for (FolderDetailWithType folder : list.getFolderDetailsWithType()) {
			assertThat(service.isFolderProcessable(list, folder)).isEqualTo(expected);
		}
	}

	private void verifyProcessed(LocalDate processingDate, User processingUser, DecommissioningList decommissioningList) {
		assertThat(decommissioningList.getProcessingDate()).isEqualTo(processingDate);
		assertThat(decommissioningList.getProcessingUser()).isEqualTo(processingUser.getId());
	}

	private void verifyFoldersClosed(User processingUser, LocalDate processingDate, Folder... folders) {
		for (Folder folder : folders) {
			assertThat(folder.getCloseDateEntered()).isEqualTo(processingDate);
			assertThat(folder.getModifiedBy()).isEqualTo(processingUser.getId());
		}
	}

	private void verifyFoldersTransferred(
			LocalDate processingDate, User processingUser, String containerId, Folder... folders) {
		for (Folder folder : folders) {
			assertThat(folder.getActualTransferDate()).isEqualTo(processingDate);
			assertThat(folder.getModifiedBy()).isEqualTo(processingUser.getId());
			assertThat(folder.getContainer()).isEqualTo(containerId);
		}
	}

	private void verifyContainersTransferred(LocalDate processingDate, User processingUser, ContainerRecord... containers) {
		for (ContainerRecord container : containers) {
			assertThat(container.getRealTransferDate()).isEqualTo(processingDate);
			assertThat(container.getModifiedBy()).isEqualTo(processingUser.getId());
			assertThat(container.getDecommissioningType()).isEqualTo(DecommissioningType.TRANSFERT_TO_SEMI_ACTIVE);
		}
	}

	private void verifyFoldersDeposited(LocalDate processingDate, User processingUser, String containerId, Folder... folders) {
		for (Folder folder : folders) {
			assertThat(folder.getActualDepositDate()).isEqualTo(processingDate);
			assertThat(folder.getModifiedBy()).isEqualTo(processingUser.getId());
			assertThat(folder.getContainer()).isEqualTo(containerId);
		}
	}

	private void verifyContainersDeposited(LocalDate processingDate, User processingUser, ContainerRecord... containers) {
		for (ContainerRecord container : containers) {
			assertThat(container.getRealDepositDate()).isEqualTo(processingDate);
			assertThat(container.getModifiedBy()).isEqualTo(processingUser.getId());
			assertThat(container.getDecommissioningType()).isEqualTo(DecommissioningType.DEPOSIT);
		}
	}

	private void verifyFoldersDestroyed(LocalDate processingDate, User processingUser, Folder... folders) {
		for (Folder folder : folders) {
			assertThat(folder.getActualDestructionDate()).isEqualTo(processingDate);
			assertThat(folder.getModifiedBy()).isEqualTo(processingUser.getId());
			assertThat(folder.getContainer()).isNull();
		}
	}

	private DecommissioningList packed(DecommissioningList list, String container) {
		for (FolderDetailWithType folder : list.getFolderDetailsWithType()) {
			if (folder.getType().potentiallyHasAnalogMedium()) {
				folder.getDetail().setContainerRecordId(container);
			}
		}
		return list.setContainerDetailsFor(container);
	}

	private SystemConfigurationsManager getConfigurationManager() {
		return getModelLayerFactory().getSystemConfigurationsManager();
	}
}
