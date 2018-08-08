package com.constellio.app.modules.rm.ui.pages.containers;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.wrappers.AdministrativeUnit;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.UserVO;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.records.RecordServices;
import com.constellio.sdk.tests.ConstellioTest;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class ContainersInAdministrativeUnitPresenterAcceptTest extends ConstellioTest {

	RecordServices recordServices;
	RMTestRecords records = new RMTestRecords(zeCollection);
	@Mock ContainersInAdministrativeUnitView view;
	@Mock SessionContext sessionContext;
	@Mock UserVO currentUser;
	ContainersInAdministrativeUnitPresenter presenter;

	@Before
	public void setUp()
			throws Exception {
		prepareSystem(
				withZeCollection().withConstellioRMModule().withAllTestUsers().withRMTest(records)
						.withFoldersAndContainersOfEveryStatus().withEvents()
		);

		inCollection(zeCollection).setCollectionTitleTo("Collection de test");

		recordServices = getModelLayerFactory().newRecordServices();

		when(view.getConstellioFactories()).thenReturn(getConstellioFactories());
		when(view.getCollection()).thenReturn(zeCollection);
		when(view.getSessionContext()).thenReturn(sessionContext);
		when(sessionContext.getCurrentCollection()).thenReturn(zeCollection);
		when(sessionContext.getCurrentUser()).thenReturn(currentUser);
		when(sessionContext.getCurrentLocale()).thenReturn(Locale.FRENCH);
		when(currentUser.getUsername()).thenReturn(chuckNorris);
		when(currentUser.getId()).thenReturn(records.getChuckNorris().getId());

		presenter = new ContainersInAdministrativeUnitPresenter(view);
	}

	@Test
	public void givenAdminUnit10InTransferNoStorageSpaceWhenGettingDataProvidersThenContainsRightData()
			throws Exception {
		presenter.forParams(ContainersByAdministrativeUnitsPresenter.TAB_TRANSFER_NO_STORAGE_SPACE + "/" + records.unitId_10);

		RecordVODataProvider childAdminUnitsProvider = presenter.getChildrenAdminUnitsDataProvider();

		assertThat(childAdminUnitsProvider.getSchema().getCode()).isEqualTo(AdministrativeUnit.DEFAULT_SCHEMA);
		assertThat(recordIdsFrom(childAdminUnitsProvider)).containsOnly(records.unitId_11, records.unitId_12, records.unitId_10a);
	}

	@Test
	public void givenAdminUnit10InTransferNoStorageSpaceWhenGettingAdminUnitThenAdminUnit10Returned()
			throws Exception {
		presenter.forParams(ContainersByAdministrativeUnitsPresenter.TAB_TRANSFER_NO_STORAGE_SPACE + "/" + records.unitId_10);

		RecordVO adminUnit = presenter.getAdministrativeUnit();

		assertThat(adminUnit.getId()).isEqualTo(records.unitId_10);
		assertThat(adminUnit.<String>get(AdministrativeUnit.CODE)).isEqualTo(records.getUnit10().getCode());
		assertThat(adminUnit.<String>get(Schemas.TITLE.getLocalCode())).isEqualTo(records.getUnit10().getTitle());
		assertThat(adminUnit.<LocalDateTime>get(Schemas.CREATED_ON.getLocalCode())).isEqualTo(records.getUnit10().getCreatedOn());
	}

	@Test
	public void givenAdminUnit10InTransferWithStorageSpaceWhenGettingDataProvidersThenContainsRightData()
			throws Exception {
		presenter.forParams(ContainersByAdministrativeUnitsPresenter.TAB_TRANSFER_WITH_STORAGE_SPACE + "/" + records.unitId_10);

		RecordVODataProvider childAdminUnitsProvider = presenter.getChildrenAdminUnitsDataProvider();

		assertThat(childAdminUnitsProvider.getSchema().getCode()).isEqualTo(AdministrativeUnit.DEFAULT_SCHEMA);
		assertThat(recordIdsFrom(childAdminUnitsProvider)).containsOnly(records.unitId_11, records.unitId_12, records.unitId_10a);
	}

	@Test
	public void givenAdminUnit10InDepositNoStorageSpaceWhenGettingDataProvidersThenContainsRightData()
			throws Exception {
		presenter.forParams(ContainersByAdministrativeUnitsPresenter.TAB_DEPOSIT_NO_STORAGE_SPACE + "/" + records.unitId_10);

		RecordVODataProvider childAdminUnitsProvider = presenter.getChildrenAdminUnitsDataProvider();

		assertThat(childAdminUnitsProvider.getSchema().getCode()).isEqualTo(AdministrativeUnit.DEFAULT_SCHEMA);
		assertThat(recordIdsFrom(childAdminUnitsProvider)).containsOnly(records.unitId_11, records.unitId_12, records.unitId_10a);
	}

	@Test
	public void givenAdminUnit10InDepositWithStorageSpaceWhenGettingDataProvidersThenContainsRightData()
			throws Exception {
		presenter.forParams(ContainersByAdministrativeUnitsPresenter.TAB_DEPOSIT_WITH_STORAGE_SPACE + "/" + records.unitId_10);

		RecordVODataProvider childAdminUnitsProvider = presenter.getChildrenAdminUnitsDataProvider();

		assertThat(childAdminUnitsProvider.getSchema().getCode()).isEqualTo(AdministrativeUnit.DEFAULT_SCHEMA);
		assertThat(recordIdsFrom(childAdminUnitsProvider)).containsOnly(records.unitId_11, records.unitId_12, records.unitId_10a);

	}

	@Test
	public void givenAdminUnit10AInTransferNoStorageSpaceWhenGettingContainerDataProvidersThenContainsRightData()
			throws Exception {
		presenter.forParams(ContainersByAdministrativeUnitsPresenter.TAB_TRANSFER_NO_STORAGE_SPACE + "/" + records.unitId_10a);

		RecordVODataProvider containersProvider = presenter.getContainersDataProvider();

		assertThat(containersProvider.getSchema().getCode()).isEqualTo(ContainerRecord.DEFAULT_SCHEMA);
		assertThat(recordIdsFrom(containersProvider))
				.containsOnly(records.containerId_bac10, records.containerId_bac14, records.containerId_bac15);
	}

	@Test
	public void givenAdminUnit10InTransferWithStorageSpaceWhenGettingContainerDataProvidersThenContainsRightData()
			throws Exception {
		presenter.forParams(ContainersByAdministrativeUnitsPresenter.TAB_TRANSFER_WITH_STORAGE_SPACE + "/" + records.unitId_10a);

		RecordVODataProvider containersProvider = presenter.getContainersDataProvider();

		assertThat(containersProvider.getSchema().getCode()).isEqualTo(ContainerRecord.DEFAULT_SCHEMA);
		assertThat(recordIdsFrom(containersProvider))
				.containsOnly(records.containerId_bac11, records.containerId_bac12, records.containerId_bac13);
	}

	@Test
	public void givenAdminUnit10InDepositNoStorageSpaceWhenGettingContainerDataProvidersThenContainsRightData()
			throws Exception {
		presenter.forParams(ContainersByAdministrativeUnitsPresenter.TAB_DEPOSIT_NO_STORAGE_SPACE + "/" + records.unitId_10a);

		RecordVODataProvider containersProvider = presenter.getContainersDataProvider();

		assertThat(containersProvider.getSchema().getCode()).isEqualTo(ContainerRecord.DEFAULT_SCHEMA);
		assertThat(recordIdsFrom(containersProvider))
				.containsOnly(records.containerId_bac16, records.containerId_bac17);
	}

	@Test
	public void givenAdminUnit10InDepositWithStorageSpaceWhenGettingContainerDataProvidersThenContainsRightData()
			throws Exception {
		presenter.forParams(ContainersByAdministrativeUnitsPresenter.TAB_DEPOSIT_WITH_STORAGE_SPACE + "/" + records.unitId_10a);

		RecordVODataProvider containersProvider = presenter.getContainersDataProvider();

		assertThat(containersProvider.getSchema().getCode()).isEqualTo(ContainerRecord.DEFAULT_SCHEMA);
		assertThat(recordIdsFrom(containersProvider))
				.containsOnly(records.containerId_bac04, records.containerId_bac05);
	}

	private List<String> recordIdsFrom(RecordVODataProvider dataProvider) {
		List<String> IDs = new ArrayList<>();
		for (RecordVO recordVO : dataProvider.listRecordVOs(0, dataProvider.size())) {
			IDs.add(recordVO.getId());
		}
		return IDs;
	}
}
