package com.constellio.app.modules.rm.services.decommissioning;

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
import com.constellio.app.modules.rm.wrappers.structures.DecomListValidation;
import com.constellio.app.modules.rm.wrappers.structures.FolderDetailStatus;
import com.constellio.app.modules.rm.wrappers.structures.FolderDetailWithType;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.configs.SystemConfigurationsManager;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.records.RecordServicesRuntimeException;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.annotations.SlowTest;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static com.constellio.model.entities.enums.ParsingBehavior.SYNC_PARSING_FOR_ALL_CONTENTS;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

public class DecommissioningServiceFolderDecommissioningAcceptTest extends ConstellioTest {
	DecommissioningService service;
	RMSchemasRecordsServices rm;
	RMTestRecords records = new RMTestRecords(zeCollection);
	RecordServices recordServices;

	@Before
	public void setUp()
			throws Exception {

		givenBackgroundThreadsEnabled();
		prepareSystem(
				withZeCollection().withConstellioRMModule().withAllTestUsers().withRMTest(records)
						.withFoldersAndContainersOfEveryStatus().withDocumentsHavingContent()
		);
		rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
		service = new DecommissioningService(zeCollection, getAppLayerFactory());
		recordServices = getModelLayerFactory().newRecordServices();
	}

	@Test
	public void givenUnprocessedListAndRgdThenIsEditable() {
		assertThat(service.isEditable(records.getList01(), records.getChuckNorris())).isTrue();
		assertThat(service.isEditable(records.getList02(), records.getChuckNorris())).isTrue();
		assertThat(service.isEditable(records.getList03(), records.getChuckNorris())).isTrue();
		assertThat(service.isEditable(records.getList04(), records.getChuckNorris())).isTrue();
		assertThat(service.isEditable(records.getList05(), records.getChuckNorris())).isTrue();
		assertThat(service.isEditable(records.getList06(), records.getChuckNorris())).isTrue();
		assertThat(service.isEditable(records.getList07(), records.getChuckNorris())).isTrue();
		assertThat(service.isEditable(records.getList08(), records.getChuckNorris())).isTrue();
		assertThat(service.isEditable(records.getList09(), records.getChuckNorris())).isTrue();
		assertThat(service.isEditable(records.getList10(), records.getChuckNorris())).isTrue();
	}

	@Test
	public void givenProcessedListThenIsNotEditable() {
		assertThat(service.isEditable(records.getList11(), records.getChuckNorris())).isFalse();
		assertThat(service.isEditable(records.getList12(), records.getChuckNorris())).isFalse();
		assertThat(service.isEditable(records.getList13(), records.getChuckNorris())).isFalse();
		assertThat(service.isEditable(records.getList14(), records.getChuckNorris())).isFalse();
		assertThat(service.isEditable(records.getList15(), records.getChuckNorris())).isFalse();
	}

	@Test
	public void givenUnprocessedListAndRgdThenIsDeletable() {
		assertThat(service.isDeletable(records.getList01(), records.getChuckNorris())).isTrue();
		assertThat(service.isDeletable(records.getList02(), records.getChuckNorris())).isTrue();
		assertThat(service.isDeletable(records.getList03(), records.getChuckNorris())).isTrue();
		assertThat(service.isDeletable(records.getList04(), records.getChuckNorris())).isTrue();
		assertThat(service.isDeletable(records.getList05(), records.getChuckNorris())).isTrue();
		assertThat(service.isDeletable(records.getList06(), records.getChuckNorris())).isTrue();
		assertThat(service.isDeletable(records.getList07(), records.getChuckNorris())).isTrue();
		assertThat(service.isDeletable(records.getList08(), records.getChuckNorris())).isTrue();
		assertThat(service.isDeletable(records.getList09(), records.getChuckNorris())).isTrue();
		assertThat(service.isDeletable(records.getList10(), records.getChuckNorris())).isTrue();
	}

	@Test
	public void givenProcessedListThenIsNotDeletable() {
		assertThat(service.isDeletable(records.getList11(), records.getChuckNorris())).isFalse();
		assertThat(service.isDeletable(records.getList12(), records.getChuckNorris())).isFalse();
		assertThat(service.isDeletable(records.getList13(), records.getChuckNorris())).isFalse();
		assertThat(service.isDeletable(records.getList14(), records.getChuckNorris())).isFalse();
		assertThat(service.isDeletable(records.getList15(), records.getChuckNorris())).isFalse();
	}

	@Test
	public void givenUnprocessedListToCloseOrDestroyAndRdgThenIsProcessableIfApproved() {
		assertThat(service.isProcessable(records.getList02(), records.getChuckNorris())).isFalse();
		assertThat(service.isProcessable(approved(records.getList02()), records.getChuckNorris())).isTrue();

		assertThat(service.isProcessable(records.getList03(), records.getGandalf_managerInABC())).isFalse();
		assertThat(service.isProcessable(approved(records.getList03()), records.getChuckNorris())).isTrue();

		assertThat(service.isProcessable(records.getList07(), records.getGandalf_managerInABC())).isFalse();
		assertThat(service.isProcessable(approved(records.getList07()), records.getChuckNorris())).isTrue();
	}

	@Test
	public void givenUnprocessedListToTransferThenIsProcessableIfNoFolderDetailStatusSelected() {
		getConfigurationManager().setValue(RMConfigs.DECOMMISSIONING_LIST_WITH_SELECTED_FOLDERS, true);
		getConfigurationManager().setValue(RMConfigs.REQUIRE_APPROVAL_FOR_TRANSFER, false);

		ContainerRecord container = rm.newContainerRecord()
				.setDecommissioningType(DecommissioningType.TRANSFERT_TO_SEMI_ACTIVE)
				.setIdentifier("ZeContainer").setTemporaryIdentifier("ZeContainer")
				.setType(records.containerTypeId_boite22x22)
				.setAdministrativeUnits(asList(records.unitId_10a));

		DecommissioningListParams params = new DecommissioningListParams();
		params.setTitle("Ze title");
		params.setDescription("Ze description");
		params.setAdministrativeUnit(records.unitId_10a);
		params.setSearchType(SearchType.transfer);
		params.setSelectedRecordIds(Arrays.asList(records.folder_A10, records.folder_A11, records.folder_A12));
		params.setFolderDetailStatus(FolderDetailStatus.SELECTED);

		User chuckNorris = records.getChuckNorris();
		DecommissioningList list = packed(service.createDecommissioningList(params, chuckNorris), container.getId());

		assertThat(service.isProcessable(list, chuckNorris)).isFalse();

		final boolean[] included = {false};
		list.getFolderDetails().forEach(fd -> {
			fd.setFolderDetailStatus(included[0] ? FolderDetailStatus.INCLUDED : FolderDetailStatus.EXCLUDED);
			included[0] = !included[0];
		});
		assertThat(service.isProcessable(list, chuckNorris)).isTrue();

		list.getFolderDetail(records.folder_A10).setFolderDetailStatus(FolderDetailStatus.SELECTED);
		assertThat(service.isProcessable(list, chuckNorris)).isFalse();
	}

	@Test
	public void givenUnprocessedListToCloseThenIsProcessableWithoutApprovalIfApprovalNotRequired() {
		getConfigurationManager().setValue(RMConfigs.REQUIRE_APPROVAL_FOR_CLOSING, false);
		assertThat(service.isProcessable(records.getList03(), records.getChuckNorris())).isTrue();
	}

	@Test
	public void givenUnprocessedListToCloseAndApprovalNotRequiredButApprovalRequestedThenIsProcessableIfApproved() {
		getConfigurationManager().setValue(RMConfigs.REQUIRE_APPROVAL_FOR_CLOSING, false);
		assertThat(service.isProcessable(requestApproval(records.getList03()), records.getChuckNorris())).isFalse();
		assertThat(service.isProcessable(approved(records.getList03()), records.getChuckNorris())).isTrue();
	}

	@Test
	public void givenUnprocessedListToCloseAndApprovalNotRequiredButValidationRequestedThenIsProcessableIfValidated() {
		getConfigurationManager().setValue(RMConfigs.REQUIRE_APPROVAL_FOR_CLOSING, false);
		assertThat(service.isProcessable(requestValidation(records.getList03()), records.getChuckNorris())).isFalse();
		assertThat(service.isProcessable(validated(records.getList03()), records.getChuckNorris())).isTrue();
	}

	@Test
	public void givenUnprocessedListWithAllElectronicAndRdgThenIsProcessableIfApproved() {
		assertThat(service.isProcessable(records.getList06(), records.getChuckNorris())).isFalse();
		assertThat(service.isProcessable(approved(records.getList06()), records.getChuckNorris())).isTrue();

		assertThat(service.isProcessable(records.getList09(), records.getChuckNorris())).isFalse();
		assertThat(service.isProcessable(approved(records.getList09()), records.getChuckNorris())).isTrue();
	}

	@Test
	public void givenUnprocessedActiveToDepositListThenIsProcessableWithoutApprovalIfApprovalNotRequired() {
		DecommissioningList list = packed(records.getList20(), records.containerId_bac01);
		assertThat(service.isProcessable(list, records.getChuckNorris())).isFalse();

		getConfigurationManager().setValue(RMConfigs.REQUIRE_APPROVAL_FOR_DEPOSIT_OF_ACTIVE, false);
		assertThat(service.isProcessable(list, records.getChuckNorris())).isTrue();
	}

	@Test
	public void givenUnprocessedActiveToDepositListAndApprovalNotRequiredButApprovalRequestedThenIsProcessableIfApproved() {
		getConfigurationManager().setValue(RMConfigs.REQUIRE_APPROVAL_FOR_DEPOSIT_OF_ACTIVE, false);
		DecommissioningList list = packed(records.getList20(), records.containerId_bac01);
		assertThat(service.isProcessable(requestApproval(list), records.getChuckNorris())).isFalse();
		assertThat(service.isProcessable(approved(records.getList20()), records.getChuckNorris())).isTrue();
	}

	@Test
	public void givenUnprocessedActiveToDepositListAndApprovalNotRequiredButValidationRequestedThenIsProcessableIfValidated() {
		getConfigurationManager().setValue(RMConfigs.REQUIRE_APPROVAL_FOR_DEPOSIT_OF_ACTIVE, false);
		DecommissioningList list = packed(records.getList20(), records.containerId_bac01);
		assertThat(service.isProcessable(requestValidation(list), records.getChuckNorris())).isFalse();
		assertThat(service.isProcessable(validated(records.getList20()), records.getChuckNorris())).isTrue();
	}

	@Test
	public void givenUnprocessedSemiActiveToDepositListThenIsProcessableWithoutApprovalIfApprovalNotRequired() {
		assertThat(service.isProcessable(records.getList09(), records.getChuckNorris())).isFalse();

		getConfigurationManager().setValue(RMConfigs.REQUIRE_APPROVAL_FOR_DEPOSIT_OF_SEMIACTIVE, false);
		assertThat(service.isProcessable(records.getList09(), records.getChuckNorris())).isTrue();
	}

	@Test
	public void givenUnprocessedSemiActiveToDepositListAndApprovalNotRequiredButApprovalRequestedThenIsProcessableIfApproved() {
		getConfigurationManager().setValue(RMConfigs.REQUIRE_APPROVAL_FOR_DEPOSIT_OF_SEMIACTIVE, false);
		assertThat(service.isProcessable(requestApproval(records.getList09()), records.getChuckNorris())).isFalse();
		assertThat(service.isProcessable(approved(records.getList09()), records.getChuckNorris())).isTrue();
	}

	@Test
	public void givenUnprocessedSemiActiveToDepositListAndApprovalNotRequiredButValidationRequestedThenIsProcessableIfValidated() {
		getConfigurationManager().setValue(RMConfigs.REQUIRE_APPROVAL_FOR_DEPOSIT_OF_SEMIACTIVE, false);
		assertThat(service.isProcessable(requestValidation(records.getList09()), records.getChuckNorris())).isFalse();
		assertThat(service.isProcessable(validated(records.getList09()), records.getChuckNorris())).isTrue();
	}

	@Test
	public void givenUnprocessedListWithNonElectronicAndManagerWhenFoldersAlreadyInContainersThenIsProcessableIfApproved() {
		assertThat(service.isProcessable(records.getList08(), records.getChuckNorris())).isFalse();
		assertThat(service.isProcessable(approved(records.getList08()), records.getChuckNorris())).isTrue();

		assertThat(service.isProcessable(records.getList10(), records.getChuckNorris())).isFalse();
		assertThat(service.isProcessable(approved(records.getList10()), records.getChuckNorris())).isTrue();

		assertThat(service.isProcessable(records.getList16(), records.getChuckNorris())).isFalse();
		assertThat(service.isProcessable(approved(records.getList16()), records.getChuckNorris())).isTrue();
	}

	@Test
	public void giveUnprocessedTransferListThenIsProcessableWithoutApprovalIfApprovalNotRequired() {
		assertThat(service.isProcessable(records.getList16(), records.getChuckNorris())).isFalse();

		getConfigurationManager().setValue(RMConfigs.REQUIRE_APPROVAL_FOR_TRANSFER, false);
		assertThat(service.isProcessable(records.getList16(), records.getChuckNorris())).isTrue();
	}

	@Test
	public void givenUnprocessedTransferListAndApprovalNotRequiredButApprovalRequestedThenIsProcessableIfApproved() {
		getConfigurationManager().setValue(RMConfigs.REQUIRE_APPROVAL_FOR_TRANSFER, false);
		assertThat(service.isProcessable(requestApproval(records.getList16()), records.getChuckNorris())).isFalse();
		assertThat(service.isProcessable(approved(records.getList16()), records.getChuckNorris())).isTrue();
	}

	@Test
	public void givenUnprocessedTransferListAndApprovalNotRequiredButValidationRequestedThenIsProcessableIfValidated() {
		getConfigurationManager().setValue(RMConfigs.REQUIRE_APPROVAL_FOR_TRANSFER, false);
		assertThat(service.isProcessable(requestValidation(records.getList16()), records.getChuckNorris())).isFalse();
		assertThat(service.isProcessable(validated(records.getList16()), records.getChuckNorris())).isTrue();
	}

	@Test
	public void givenUnprocessedListWithNonElectronicAndRdgWhenFoldersNotInContainersThenIsNotProcessable() {
		assertThat(service.isProcessable(records.getList04(), records.getChuckNorris())).isFalse();
		assertThat(service.isProcessable(approved(records.getList04()), records.getChuckNorris())).isFalse();
		assertThat(service.isProcessable(records.getList05(), records.getChuckNorris())).isFalse();
		assertThat(service.isProcessable(approved(records.getList05()), records.getChuckNorris())).isFalse();
	}

	@Test
	public void givenUnprocessedActiveToDestroyListThenIsProcessableIfApproved() {
		assertThat(service.isProcessable(records.getList21(), records.getChuckNorris())).isFalse();
		assertThat(service.isProcessable(approved(records.getList21()), records.getChuckNorris())).isTrue();
	}

	@Test
	public void givenUnprocessedActiveToDestroyListThenIsProcessableWithoutApprovalIfApprovalNotRequired() {
		getConfigurationManager().setValue(RMConfigs.REQUIRE_APPROVAL_FOR_DESTRUCTION_OF_ACTIVE, false);
		assertThat(service.isProcessable(records.getList21(), records.getChuckNorris())).isTrue();
	}

	@Test
	public void givenUnprocessedActiveToDestroyListAndApprovalNotRequiredButApprovalRequestedThenIsProcessableIfApproved() {
		getConfigurationManager().setValue(RMConfigs.REQUIRE_APPROVAL_FOR_DESTRUCTION_OF_ACTIVE, false);
		assertThat(service.isProcessable(requestApproval(records.getList21()), records.getChuckNorris())).isFalse();
		assertThat(service.isProcessable(approved(records.getList21()), records.getChuckNorris())).isTrue();
	}

	@Test
	public void givenUnprocessedActiveToDestroyListAndApprovalNotRequiredButValidationRequestedThenIsProcessableIfValidated() {
		getConfigurationManager().setValue(RMConfigs.REQUIRE_APPROVAL_FOR_DESTRUCTION_OF_ACTIVE, false);
		assertThat(service.isProcessable(requestValidation(records.getList21()), records.getChuckNorris())).isFalse();
		assertThat(service.isProcessable(validated(records.getList21()), records.getChuckNorris())).isTrue();
	}

	@Test
	public void givenUnprocessedSemiActiveToDestroyListThenIsProcessableIfApproved() {
		assertThat(service.isProcessable(records.getList01(), records.getChuckNorris())).isFalse();
		assertThat(service.isProcessable(approved(records.getList01()), records.getChuckNorris())).isTrue();
	}

	@Test
	public void givenUnprocessedSemiActiveToDestroyListThenIsProcessableWithoutApprovalIfApprovalNotRequired() {
		getConfigurationManager().setValue(RMConfigs.REQUIRE_APPROVAL_FOR_DESTRUCTION_OF_SEMIACTIVE, false);
		assertThat(service.isProcessable(records.getList01(), records.getChuckNorris())).isTrue();
	}

	@Test
	public void givenUnprocessedSemiActiveToDestroyListAndApprovalNotRequiredButApprovalRequestedThenIsProcessableIfApproved() {
		getConfigurationManager().setValue(RMConfigs.REQUIRE_APPROVAL_FOR_DESTRUCTION_OF_SEMIACTIVE, false);
		assertThat(service.isProcessable(requestApproval(records.getList01()), records.getChuckNorris())).isFalse();
		assertThat(service.isProcessable(approved(records.getList01()), records.getChuckNorris())).isTrue();
	}

	@Test
	public void givenUnprocessedSemiActiveToDestroyListAndApprovalNotRequiredButValidationRequestedThenIsProcessableIfValidated() {
		getConfigurationManager().setValue(RMConfigs.REQUIRE_APPROVAL_FOR_DESTRUCTION_OF_SEMIACTIVE, false);
		assertThat(service.isProcessable(requestValidation(records.getList01()), records.getChuckNorris())).isFalse();
		assertThat(service.isProcessable(validated(records.getList01()), records.getChuckNorris())).isTrue();
	}

	@Test
	public void givenProcessedListThenIsNotProcessable() {
		assertThat(service.isProcessable(records.getList11(), records.getChuckNorris())).isFalse();
		assertThat(service.isProcessable(records.getList12(), records.getChuckNorris())).isFalse();
		assertThat(service.isProcessable(records.getList13(), records.getChuckNorris())).isFalse();
		assertThat(service.isProcessable(records.getList14(), records.getChuckNorris())).isFalse();
		assertThat(service.isProcessable(records.getList15(), records.getChuckNorris())).isFalse();
	}

	@Test
	public void givenUnprocessedSortableListToDestroyThenCanEditContainers() {
		assertThat(service.canEditContainers(records.getList01(), records.getChuckNorris())).isTrue();
	}

	@Test
	public void givenUnprocessedListToCloseOrDestroyThenCannotEditContainers() {
		assertThat(service.canEditContainers(records.getList02(), records.getChuckNorris())).isFalse();
		assertThat(service.canEditContainers(records.getList03(), records.getChuckNorris())).isFalse();
		assertThat(service.canEditContainers(records.getList07(), records.getChuckNorris())).isFalse();
	}

	@Test
	public void givenProcessedListThenCannotEditContainers() {
		assertThat(service.canEditContainers(records.getList11(), records.getChuckNorris())).isFalse();
		assertThat(service.canEditContainers(records.getList12(), records.getChuckNorris())).isFalse();
		assertThat(service.canEditContainers(records.getList13(), records.getChuckNorris())).isFalse();
		assertThat(service.canEditContainers(records.getList14(), records.getChuckNorris())).isFalse();
		assertThat(service.canEditContainers(records.getList15(), records.getChuckNorris())).isFalse();
	}

	@Test
	public void givenRegularUserOrAdminThenIsNotEditableDeletableProcessableContainerizable() {
		assertThat(service.isEditable(records.getList01(), records.getAlice())).isFalse();
		assertThat(service.isDeletable(records.getList01(), records.getAlice())).isFalse();
		assertThat(service.isProcessable(records.getList01(), records.getAlice())).isFalse();
		assertThat(service.canEditContainers(records.getList01(), records.getAlice())).isFalse();
		assertThat(service.isEditable(records.getList01(), records.getGandalf_managerInABC())).isFalse();
		assertThat(service.isDeletable(records.getList01(), records.getGandalf_managerInABC())).isFalse();
		assertThat(service.isProcessable(records.getList01(), records.getGandalf_managerInABC())).isFalse();
		assertThat(service.canEditContainers(records.getList01(), records.getGandalf_managerInABC())).isFalse();
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
		params.setAdministrativeUnit(records.unitId_10a);
		params.setSearchType(SearchType.fixedPeriod);
		params.setSelectedRecordIds(Arrays.asList(records.folder_A01));
		params.setFolderDetailStatus(FolderDetailStatus.INCLUDED);

		DecommissioningList decommissioningList = service.createDecommissioningList(params, records.getChuckNorris());
		assertThat(decommissioningList.getTitle()).isEqualTo("Ze title");
		assertThat(decommissioningList.getDescription()).isEqualTo("Ze description");
		assertThat(decommissioningList.getAdministrativeUnit()).isEqualTo(records.unitId_10a);
		assertThat(decommissioningList.getDecommissioningListType()).isEqualTo(DecommissioningListType.FOLDERS_TO_CLOSE);
		assertThat(decommissioningList.getFolderDetails()).containsOnly(new DecomListFolderDetail(records.getFolder_A01(), FolderDetailStatus.INCLUDED));
		assertThat(decommissioningList.getContainerDetails()).isEmpty();
	}

	@Test
	public void givenCode888SearchThenCreateListToClose() {
		DecommissioningListParams params = new DecommissioningListParams();
		params.setTitle("Ze title");
		params.setDescription("Ze description");
		params.setAdministrativeUnit(records.unitId_10a);
		params.setSearchType(SearchType.code888);
		params.setSelectedRecordIds(Arrays.asList(records.folder_A04));
		params.setFolderDetailStatus(FolderDetailStatus.INCLUDED);

		DecommissioningList decommissioningList = service.createDecommissioningList(params, records.getChuckNorris());
		assertThat(decommissioningList.getTitle()).isEqualTo("Ze title");
		assertThat(decommissioningList.getDescription()).isEqualTo("Ze description");
		assertThat(decommissioningList.getAdministrativeUnit()).isEqualTo(records.unitId_10a);
		assertThat(decommissioningList.getDecommissioningListType()).isEqualTo(DecommissioningListType.FOLDERS_TO_CLOSE);
		assertThat(decommissioningList.getFolderDetails()).containsOnly(new DecomListFolderDetail(records.getFolder_A04(), FolderDetailStatus.INCLUDED));
		assertThat(decommissioningList.getContainerDetails()).isEmpty();
	}

	@Test
	public void givenCode999SearchThenCreateListToClose() {
		DecommissioningListParams params = new DecommissioningListParams();
		params.setTitle("Ze title");
		params.setDescription("Ze description");
		params.setAdministrativeUnit(records.unitId_10a);
		params.setSearchType(SearchType.code999);
		params.setSelectedRecordIds(Arrays.asList(records.folder_A07));
		params.setFolderDetailStatus(FolderDetailStatus.INCLUDED);

		DecommissioningList decommissioningList = service.createDecommissioningList(params, records.getChuckNorris());
		assertThat(decommissioningList.getTitle()).isEqualTo("Ze title");
		assertThat(decommissioningList.getDescription()).isEqualTo("Ze description");
		assertThat(decommissioningList.getAdministrativeUnit()).isEqualTo(records.unitId_10a);
		assertThat(decommissioningList.getDecommissioningListType()).isEqualTo(DecommissioningListType.FOLDERS_TO_CLOSE);
		assertThat(decommissioningList.getFolderDetails()).containsOnly(new DecomListFolderDetail(records.getFolder_A07(), FolderDetailStatus.INCLUDED));
		assertThat(decommissioningList.getContainerDetails()).isEmpty();
	}

	@Test
	public void givenTransferSearchThenCreateListToTransfer() {
		DecommissioningListParams params = new DecommissioningListParams();
		params.setTitle("Ze title");
		params.setDescription("Ze description");
		params.setAdministrativeUnit(records.unitId_10a);
		params.setSearchType(SearchType.transfer);
		params.setSelectedRecordIds(Arrays.asList(records.folder_A10));
		params.setFolderDetailStatus(FolderDetailStatus.INCLUDED);

		DecommissioningList decommissioningList = service.createDecommissioningList(params, records.getChuckNorris());
		assertThat(decommissioningList.getTitle()).isEqualTo("Ze title");
		assertThat(decommissioningList.getDescription()).isEqualTo("Ze description");
		assertThat(decommissioningList.getAdministrativeUnit()).isEqualTo(records.unitId_10a);
		assertThat(decommissioningList.getDecommissioningListType()).isEqualTo(DecommissioningListType.FOLDERS_TO_TRANSFER);
		assertThat(decommissioningList.getFolderDetails()).containsOnly(new DecomListFolderDetail(records.getFolder_A10(), FolderDetailStatus.INCLUDED));
		assertThat(decommissioningList.getContainerDetails()).isEmpty();
	}

	@Test
	public void givenActiveToDepositSearchThenCreateListToDeposit() {
		DecommissioningListParams params = new DecommissioningListParams();
		params.setTitle("Ze title");
		params.setDescription("Ze description");
		params.setAdministrativeUnit(records.unitId_10a);
		params.setSearchType(SearchType.activeToDeposit);
		params.setSelectedRecordIds(Arrays.asList(records.folder_A10));
		params.setFolderDetailStatus(FolderDetailStatus.INCLUDED);

		DecommissioningList decommissioningList = service.createDecommissioningList(params, records.getChuckNorris());
		assertThat(decommissioningList.getTitle()).isEqualTo("Ze title");
		assertThat(decommissioningList.getDescription()).isEqualTo("Ze description");
		assertThat(decommissioningList.getAdministrativeUnit()).isEqualTo(records.unitId_10a);
		assertThat(decommissioningList.getDecommissioningListType()).isEqualTo(DecommissioningListType.FOLDERS_TO_DEPOSIT);
		assertThat(decommissioningList.getFolderDetails()).containsOnly(new DecomListFolderDetail(records.getFolder_A10(), FolderDetailStatus.INCLUDED));
		assertThat(decommissioningList.getContainerDetails()).isEmpty();
	}

	@Test
	public void givenActiveToDestroySearchThenCreateListToDestroy() {
		DecommissioningListParams params = new DecommissioningListParams();
		params.setTitle("Ze title");
		params.setDescription("Ze description");
		params.setAdministrativeUnit(records.unitId_10a);
		params.setSearchType(SearchType.activeToDestroy);
		params.setSelectedRecordIds(Arrays.asList(records.folder_A10));
		params.setFolderDetailStatus(FolderDetailStatus.INCLUDED);

		DecommissioningList decommissioningList = service.createDecommissioningList(params, records.getChuckNorris());
		assertThat(decommissioningList.getTitle()).isEqualTo("Ze title");
		assertThat(decommissioningList.getDescription()).isEqualTo("Ze description");
		assertThat(decommissioningList.getAdministrativeUnit()).isEqualTo(records.unitId_10a);
		assertThat(decommissioningList.getDecommissioningListType()).isEqualTo(DecommissioningListType.FOLDERS_TO_DESTROY);
		assertThat(decommissioningList.getFolderDetails()).containsOnly(new DecomListFolderDetail(records.getFolder_A10(), FolderDetailStatus.INCLUDED));
		assertThat(decommissioningList.getContainerDetails()).isEmpty();
	}

	@Test
	public void givenSemiActiveToDepositSearchThenCreateListToDepositWithContainerDetailsAndFoldersFromSameContainers() {
		DecommissioningListParams params = new DecommissioningListParams();
		params.setTitle("Ze title");
		params.setDescription("Ze description");
		params.setAdministrativeUnit(records.unitId_10a);
		params.setSearchType(SearchType.semiActiveToDeposit);
		params.setSelectedRecordIds(Arrays.asList(records.folder_A42));
		params.setFolderDetailStatus(FolderDetailStatus.INCLUDED);

		DecommissioningList decommissioningList = service.createDecommissioningList(params, records.getChuckNorris());
		assertThat(decommissioningList.getTitle()).isEqualTo("Ze title");
		assertThat(decommissioningList.getDescription()).isEqualTo("Ze description");
		assertThat(decommissioningList.getAdministrativeUnit()).isEqualTo(records.unitId_10a);
		assertThat(decommissioningList.getDecommissioningListType()).isEqualTo(DecommissioningListType.FOLDERS_TO_DEPOSIT);
		assertThat(decommissioningList.getFolderDetails()).containsOnly(
				new DecomListFolderDetail(records.getFolder_A42(), FolderDetailStatus.INCLUDED).setContainerRecordId(records.containerId_bac13),
				new DecomListFolderDetail(records.getFolder_A43(), FolderDetailStatus.INCLUDED).setContainerRecordId(records.containerId_bac13),
				new DecomListFolderDetail(records.getFolder_A44(), FolderDetailStatus.INCLUDED).setContainerRecordId(records.containerId_bac13));
		assertThat(decommissioningList.getContainerDetails()).containsOnly(
				new DecomListContainerDetail(records.containerId_bac13));
	}

	@Test
	public void givenSemiActiveToDepositSearchWhenMixedContainersAllowedThenCreateListWithOnlyGivenFolders() {
		getConfigurationManager().setValue(RMConfigs.MIXED_CONTAINERS_ALLOWED, true);
		DecommissioningListParams params = new DecommissioningListParams();
		params.setTitle("Ze title");
		params.setDescription("Ze description");
		params.setAdministrativeUnit(records.unitId_10a);
		params.setSearchType(SearchType.semiActiveToDeposit);
		params.setSelectedRecordIds(Arrays.asList(records.folder_A42));
		params.setFolderDetailStatus(FolderDetailStatus.INCLUDED);

		DecommissioningList decommissioningList = service.createDecommissioningList(params, records.getChuckNorris());
		assertThat(decommissioningList.getTitle()).isEqualTo("Ze title");
		assertThat(decommissioningList.getDescription()).isEqualTo("Ze description");
		assertThat(decommissioningList.getAdministrativeUnit()).isEqualTo(records.unitId_10a);
		assertThat(decommissioningList.getDecommissioningListType()).isEqualTo(DecommissioningListType.FOLDERS_TO_DEPOSIT);
		assertThat(decommissioningList.getFolderDetails()).containsOnly(
				new DecomListFolderDetail(records.getFolder_A42(), FolderDetailStatus.INCLUDED).setContainerRecordId(records.containerId_bac13));
		assertThat(decommissioningList.getContainerDetails()).containsOnly(
				new DecomListContainerDetail(records.containerId_bac13));
	}

	@Test
	public void givenSemiActiveToDestroySearchThenCreateListToDepositWithContainerDetailsAndFoldersFromSameContainers() {
		DecommissioningListParams params = new DecommissioningListParams();
		params.setTitle("Ze title");
		params.setDescription("Ze description");
		params.setAdministrativeUnit(records.unitId_10a);
		params.setSearchType(SearchType.semiActiveToDestroy);
		params.setSelectedRecordIds(Arrays.asList(records.folder_A42));
		params.setFolderDetailStatus(FolderDetailStatus.INCLUDED);

		DecommissioningList decommissioningList = service.createDecommissioningList(params, records.getChuckNorris());
		assertThat(decommissioningList.getTitle()).isEqualTo("Ze title");
		assertThat(decommissioningList.getDescription()).isEqualTo("Ze description");
		assertThat(decommissioningList.getAdministrativeUnit()).isEqualTo(records.unitId_10a);
		assertThat(decommissioningList.getDecommissioningListType()).isEqualTo(DecommissioningListType.FOLDERS_TO_DESTROY);
		assertThat(decommissioningList.getFolderDetails()).containsOnly(
				new DecomListFolderDetail(records.getFolder_A42(), FolderDetailStatus.INCLUDED).setContainerRecordId(records.containerId_bac13),
				new DecomListFolderDetail(records.getFolder_A43(), FolderDetailStatus.INCLUDED).setContainerRecordId(records.containerId_bac13),
				new DecomListFolderDetail(records.getFolder_A44(), FolderDetailStatus.INCLUDED).setContainerRecordId(records.containerId_bac13));
		assertThat(decommissioningList.getContainerDetails()).containsOnly(
				new DecomListContainerDetail(records.containerId_bac13));
	}

	@Test
	public void givenContainerIsRecycledThenEmptyTheContainer() {
		User user = records.getGandalf_managerInABC();
		service.recycleContainer(records.getContainerBac13(), user);
		assertThat(records.getFolder_A42().getContainer()).isNull();
		assertThat(records.getFolder_A43().getContainer()).isNull();
		assertThat(records.getFolder_A44().getContainer()).isNull();
	}

	@Test
	public void givenContainerIsRecycledThenResetTheDecommissioningDatesFullnessAndSize() {
		User user = records.getGandalf_managerInABC();
		service.recycleContainer(records.getContainerBac13().setFull(true).setFillRatioEntered(100.0), user);
		assertThat(records.getContainerBac13().getRealTransferDate()).isNull();
		assertThat(records.getContainerBac13().getRealDepositDate()).isNull();
		assertThat(records.getContainerBac13().isFull()).isFalse();
		assertThat(records.getContainerBac13().getFillRatioEntered()).isEqualTo(0.0);
	}

	@Test
	public void givenListToCloseThenAllFoldersAreClosed() throws Exception {
		User processingUser = records.getChuckNorris();
		LocalDate processingDate = new LocalDate();
		givenTimeIs(processingDate);

		service.decommission(approved(records.getList03()), processingUser);
		waitForBatchProcess();

		verifyProcessed(processingDate, processingUser, records.getList03());
		verifyFoldersClosed(processingDate, records.getFolder_A01(), records.getFolder_A02(),
				records.getFolder_A03());
	}

	@Test
	public void givenListToTransferThenAllFoldersAreTransferred() throws Exception {
		User processingUser = records.getChuckNorris();
		LocalDate processingDate = new LocalDate();
		givenTimeIs(processingDate);

		service.decommission(approved(records.getList16()), processingUser);
		waitForBatchProcess();

		verifyProcessed(processingDate, processingUser, records.getList16());
		verifyFoldersTransferred(processingDate, records.containerId_bac14,
				records.getFolder_A22(), records.getFolder_A23(), records.getFolder_A24());
		verifyContainersTransferred(processingDate, records.getContainerBac14());
	}

	@Test
	public void givenListToTransferWhenPurgeMinorVersionOnTransferThenMinorVersionsArePurged() throws Exception {
		getConfigurationManager().setValue(RMConfigs.MINOR_VERSIONS_PURGED_ON, DecommissioningPhase.ON_TRANSFER_OR_DEPOSIT);
		givenDisabledAfterTestValidations();
		service.decommission(approved(packed(records.getList05(), records.containerId_bac15)), records.getChuckNorris());
		waitForBatchProcess();
		assertThat(records.getDocumentWithContent_A19().getContent().getHistoryVersions()).isEmpty();
	}

	@Test
	@SlowTest
	public void givenListToTransferWhenCreatePDFaOnTransferThenPDFaCreated() throws Exception {
		getConfigurationManager().setValue(ConstellioEIMConfigs.DEFAULT_PARSING_BEHAVIOR, SYNC_PARSING_FOR_ALL_CONTENTS);
		getConfigurationManager().setValue(RMConfigs.PDFA_CREATED_ON, DecommissioningPhase.ON_TRANSFER_OR_DEPOSIT);
		givenDisabledAfterTestValidations();
		service.decommission(approved(packed(records.getList05(), records.containerId_bac15)), records.getChuckNorris());
		waitForBatchProcess();
		assertThat(records.getDocumentWithContent_A19().getContent().getCurrentVersion().getMimetype())
				.isEqualTo("application/pdf");
	}

	@Test
	public void givenListToDepositThenAllFoldersAreDeposited() throws Exception {
		User processingUser = records.getChuckNorris();
		LocalDate processingDate = new LocalDate();
		givenTimeIs(processingDate);

		service.decommission(approved(records.getList17()), processingUser);
		waitForBatchProcess();

		verifyProcessed(processingDate, processingUser, records.getList17());
		verifyFoldersDeposited(processingDate, records.containerId_bac11,
				records.getFolder_A49(), records.getFolder_A50());
		// Electronic only: not in container
		verifyFoldersDeposited(processingDate, null, records.getFolder_A48());
		verifyContainersDeposited(processingDate, records.getContainerBac11());
	}

	@Test
	public void givenListToDepositWhenPurgeMinorVersionOnTransferThenMinorVersionsArePurged() throws Exception {
		getConfigurationManager().setValue(RMConfigs.MINOR_VERSIONS_PURGED_ON, DecommissioningPhase.ON_TRANSFER_OR_DEPOSIT);
		givenDisabledAfterTestValidations();
		service.decommission(approved(packed(records.getList20(), records.containerId_bac16)), records.getChuckNorris());
		waitForBatchProcess();
		assertThat(records.getDocumentWithContent_A19().getContent().getHistoryVersions()).isEmpty();
	}

	@Test
	public void givenListToDepositWhenPurgeMinorVersionsOnDepositThenMinorVersionsArePurged() throws Exception {
		getConfigurationManager().setValue(RMConfigs.MINOR_VERSIONS_PURGED_ON, DecommissioningPhase.ON_DEPOSIT);
		givenDisabledAfterTestValidations();
		service.decommission(approved(records.getList17()), records.getChuckNorris());
		waitForBatchProcess();
		assertThat(records.getDocumentWithContent_A49().getContent().getHistoryVersions()).isEmpty();
	}

	@Test
	@SlowTest
	public void givenListToDepositWhenCreatePDFaOnTransferThenPDFaCreated() throws Exception {
		getConfigurationManager().setValue(ConstellioEIMConfigs.DEFAULT_PARSING_BEHAVIOR, SYNC_PARSING_FOR_ALL_CONTENTS);
		getConfigurationManager().setValue(RMConfigs.PDFA_CREATED_ON, DecommissioningPhase.ON_TRANSFER_OR_DEPOSIT);
		givenDisabledAfterTestValidations();
		service.decommission(approved(packed(records.getList20(), records.containerId_bac16)), records.getChuckNorris());
		waitForBatchProcess();
		assertThat(records.getDocumentWithContent_A19().getContent().getCurrentVersion().getMimetype())
				.isEqualTo("application/pdf");
	}

	@Test
	@SlowTest
	public void givenListToDepositWhenCreatePDFaOnDepositThenPDFaCreated() throws Exception {
		getConfigurationManager().setValue(ConstellioEIMConfigs.DEFAULT_PARSING_BEHAVIOR, SYNC_PARSING_FOR_ALL_CONTENTS);
		getConfigurationManager().setValue(RMConfigs.PDFA_CREATED_ON, DecommissioningPhase.ON_DEPOSIT);
		givenDisabledAfterTestValidations();
		service.decommission(approved(records.getList17()), records.getChuckNorris());
		waitForBatchProcess();
		assertThat(records.getDocumentWithContent_A49().getContent().getCurrentVersion().getMimetype())
				.isEqualTo("application/pdf");
	}

	@Test
	public void givenListToDestroyThenAllFoldersAreDestroyed() throws Exception {
		User processingUser = records.getChuckNorris();
		LocalDate processingDate = new LocalDate();
		givenTimeIs(processingDate);

		service.decommission(approved(records.getList02()), processingUser);
		waitForBatchProcess();

		verifyProcessed(processingDate, processingUser, records.getList02());
		verifyFoldersDestroyed(processingDate,
				records.getFolder_A54(), records.getFolder_A55(), records.getFolder_A56());
		assertThat(records.getContainerBac10().isFull()).isFalse();
	}

	@Test
	public void givenListToDestroyThenDocumentsContentsAreDestroyed() throws Exception {
		givenDisabledAfterTestValidations();
		service.decommission(approved(records.getList21()), records.getChuckNorris());
		waitForBatchProcess();
		assertThat(records.getDocumentWithContent_A19().getContent()).isNull();
	}

	@Test
	public void givenListToDestroyWhenDocumentDeletionIsEnabledThenDocumentsAreDeleted() throws Exception {
		getConfigurationManager().setValue(RMConfigs.DELETE_DOCUMENT_RECORDS_WITH_DESTRUCTION, true);
		givenDisabledAfterTestValidations();
		service.decommission(approved(records.getList21()), records.getChuckNorris());
		waitForBatchProcess();

		assertThatRecordDoesNotExist(records.document_A19);

	}

	@Test
	public void givenListToDestroyWhenFolderDeletionIsEnabledThenFoldersAreDeleted()
			throws Exception {
		User processingUser = records.getChuckNorris();
		LocalDate processingDate = new LocalDate();
		givenTimeIs(processingDate);

		getConfigurationManager().setValue(RMConfigs.DELETE_FOLDER_RECORDS_WITH_DESTRUCTION, true);

		service.decommission(approved(records.getList02()), records.getChuckNorris());
		waitForBatchProcess();
		verifyProcessed(processingDate, processingUser, records.getList02());

		assertThatRecordDoesNotExist(records.folder_A54);
		assertThatRecordDoesNotExist(records.folder_A55);
		assertThatRecordDoesNotExist(records.folder_A56);
	}

	@Test
	public void givenListToDepositWithSortThenRegularFoldersAreDepositedAndReversedFoldersAreDestroyed()
			throws Exception {
		User processingUser = records.getChuckNorris();
		LocalDate processingDate = new LocalDate();
		givenTimeIs(processingDate);

		service.decommission(approved(records.getList18()), processingUser);
		waitForBatchProcess();

		verifyProcessed(processingDate, processingUser, records.getList18());
		verifyFoldersDeposited(processingDate, records.containerId_bac08, records.getFolder_B30());
		verifyFoldersDestroyed(processingDate, records.getFolder_B33());
		verifyContainersDeposited(processingDate, records.getContainerBac08(), records.getContainerBac09());
	}

	@Test
	public void givenListToDestroyWithSortThenRegularFoldersAreDestroyedAndReversedFoldersAreDeposited()
			throws Exception {
		User processingUser = records.getChuckNorris();
		LocalDate processingDate = new LocalDate();
		givenTimeIs(processingDate);

		service.decommission(approved(records.getList19()), processingUser);
		waitForBatchProcess();

		verifyProcessed(processingDate, processingUser, records.getList19());
		verifyFoldersDeposited(processingDate, records.containerId_bac09, records.getFolder_B33());
		verifyFoldersDestroyed(processingDate, records.getFolder_B30());
		verifyContainersDeposited(processingDate, records.getContainerBac08(), records.getContainerBac09());
	}

	private void verifyFolderProcessabilityForAllFoldersIn(DecommissioningList list, boolean expected) {
		for (FolderDetailWithType folder : list.getFolderDetailsWithType()) {
			assertThat(service.isFolderProcessable(list, folder)).isEqualTo(expected);
		}
	}

	private void verifyProcessed(LocalDate processingDate, User processingUser,
								 DecommissioningList decommissioningList) {
		assertThat(decommissioningList.getProcessingDate()).isEqualTo(processingDate);
		assertThat(decommissioningList.getProcessingUser()).isEqualTo(processingUser.getId());
	}

	private void verifyFoldersClosed(LocalDate processingDate, Folder... folders) {
		for (Folder folder : folders) {
			assertThat(folder.getCloseDateEntered()).isEqualTo(processingDate);
		}
	}

	private void verifyFoldersTransferred(
			LocalDate processingDate, String containerId, Folder... folders) {
		for (Folder folder : folders) {
			assertThat(folder.getActualTransferDate()).isEqualTo(processingDate);
			assertThat(folder.getContainer()).isEqualTo(containerId);
		}
	}

	private void verifyContainersTransferred(LocalDate processingDate, ContainerRecord... containers) {
		for (ContainerRecord container : containers) {
			assertThat(container.getRealTransferDate()).isEqualTo(processingDate);
			assertThat(container.getDecommissioningType()).isEqualTo(DecommissioningType.TRANSFERT_TO_SEMI_ACTIVE);
		}
	}

	private void verifyFoldersDeposited(LocalDate processingDate, String containerId, Folder... folders) {
		for (Folder folder : folders) {
			assertThat(folder.getActualDepositDate()).isEqualTo(processingDate);
			assertThat(folder.getContainer()).isEqualTo(containerId);
		}
	}

	private void verifyContainersDeposited(LocalDate processingDate, ContainerRecord... containers) {
		for (ContainerRecord container : containers) {
			assertThat(container.getRealDepositDate()).isEqualTo(processingDate);
			assertThat(container.getDecommissioningType()).isEqualTo(DecommissioningType.DEPOSIT);
		}
	}

	private void verifyFoldersDestroyed(LocalDate processingDate, Folder... folders) {
		for (Folder folder : folders) {
			assertThat(folder.getActualDestructionDate()).isEqualTo(processingDate);
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

	private DecommissioningList requestValidation(DecommissioningList list) {
		return savedAndRefreshed(list.addValidationRequest(records.getAlice(), new LocalDate()));
	}

	private DecommissioningList validated(DecommissioningList list) {
		for (DecomListValidation validation : list.getValidations()) {
			validation.setValidationDate(new LocalDate());
		}
		return savedAndRefreshed(list);
	}

	private DecommissioningList requestApproval(DecommissioningList list) {
		return savedAndRefreshed(list.setApprovalRequestDate(new LocalDate()).setApprovalRequester(records.getChuckNorris()));
	}

	private void assertThatRecordDoesNotExist(String id) {
		try {
			recordServices.getDocumentById(id);
			fail("Record should not exist : " + id);
		} catch (RecordServicesRuntimeException.NoSuchRecordWithId e) {
			//OK
		}
	}

	private DecommissioningList approved(DecommissioningList list) {
		return savedAndRefreshed(list.setApprovalDate(new LocalDate()).setApprovalUser(records.getGandalf_managerInABC()));
	}

	private DecommissioningList savedAndRefreshed(DecommissioningList list) {
		try {
			recordServices.update(list);
		} catch (RecordServicesException e) {
			throw new RuntimeException(e);
		}
		recordServices.refresh(list);
		return list;
	}

	private SystemConfigurationsManager getConfigurationManager() {
		return getModelLayerFactory().getSystemConfigurationsManager();
	}
}
