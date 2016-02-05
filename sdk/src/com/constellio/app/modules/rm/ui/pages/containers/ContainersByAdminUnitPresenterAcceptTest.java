package com.constellio.app.modules.rm.ui.pages.containers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.AdministrativeUnit;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.records.RecordServices;
import com.constellio.sdk.tests.ConstellioTest;

public class ContainersByAdminUnitPresenterAcceptTest extends ConstellioTest {

	RecordServices recordServices;
	RMTestRecords records = new RMTestRecords(zeCollection);
	@Mock ContainersByAdministrativeUnitsView view;
	@Mock SessionContext sessionContext;
	ContainersByAdministrativeUnitsPresenter presenter;
	RMSchemasRecordsServices rmSchemasRecordsServices;

	@Before
	public void setUp()
			throws Exception {

		prepareSystem(
				withZeCollection().withConstellioRMModule().withAllTestUsers().withRMTest(records)
						.withFoldersAndContainersOfEveryStatus().withEvents()
		);
		getDataLayerFactory().getDataLayerLogger().monitor("idx_rfc_00000000001");

		inCollection(zeCollection).setCollectionTitleTo("Collection de test");

		recordServices = getModelLayerFactory().newRecordServices();
		rmSchemasRecordsServices = new RMSchemasRecordsServices(zeCollection, getModelLayerFactory());

		when(view.getConstellioFactories()).thenReturn(getConstellioFactories());
		when(view.getCollection()).thenReturn(zeCollection);
		when(view.getSessionContext()).thenReturn(sessionContext);
		when(sessionContext.getCurrentCollection()).thenReturn(zeCollection);

		presenter = new ContainersByAdministrativeUnitsPresenter(view);
	}

	@Test
	public void givenTabDepositNoStorageWhenGettingDataProviderThenAllRootUnitsReturned()
			throws Exception {
		presenter.forParams(ContainersByAdministrativeUnitsPresenter.TAB_DEPOSIT_NO_STORAGE_SPACE);

		RecordVODataProvider dataProvider = presenter.getDataProvider();

		assertThat(dataProvider.getSchema().getCode()).isEqualTo(AdministrativeUnit.DEFAULT_SCHEMA);
		assertThat(getRecordIdsFromDataProvider(dataProvider))
				.containsOnly(records.unitId_10, records.unitId_20, records.unitId_30);
	}

	@Test
	public void givenTabDepositWithStorageWhenGettingDataProviderThenAllRootUnitsReturned()
			throws Exception {
		presenter.forParams(ContainersByAdministrativeUnitsPresenter.TAB_DEPOSIT_WITH_STORAGE_SPACE);

		RecordVODataProvider dataProvider = presenter.getDataProvider();

		assertThat(dataProvider.getSchema().getCode()).isEqualTo(AdministrativeUnit.DEFAULT_SCHEMA);
		assertThat(getRecordIdsFromDataProvider(dataProvider))
				.containsOnly(records.unitId_10, records.unitId_20, records.unitId_30);
	}

	@Test
	public void givenTabTransferNoStorageWhenGettingDataProviderThenAllRootUnitsReturned()
			throws Exception {
		presenter.forParams(ContainersByAdministrativeUnitsPresenter.TAB_TRANSFER_NO_STORAGE_SPACE);

		RecordVODataProvider dataProvider = presenter.getDataProvider();

		assertThat(dataProvider.getSchema().getCode()).isEqualTo(AdministrativeUnit.DEFAULT_SCHEMA);
		assertThat(getRecordIdsFromDataProvider(dataProvider))
				.containsOnly(records.unitId_10, records.unitId_20, records.unitId_30);
	}

	@Test
	public void givenTabTransferWithStorageWhenGettingDataProviderThenAllRootUnitsReturned()
			throws Exception {
		presenter.forParams(ContainersByAdministrativeUnitsPresenter.TAB_TRANSFER_WITH_STORAGE_SPACE);

		RecordVODataProvider dataProvider = presenter.getDataProvider();

		assertThat(dataProvider.getSchema().getCode()).isEqualTo(AdministrativeUnit.DEFAULT_SCHEMA);
		assertThat(getRecordIdsFromDataProvider(dataProvider))
				.containsOnly(records.unitId_10, records.unitId_20, records.unitId_30);
	}

	@Test
	public void givenDeletedAdministrativeUnitWhenGettingDataProviderThenNoDeletedAdministrativeUnitReturned()
			throws Exception {

		AdministrativeUnit administrativeUnit = rmSchemasRecordsServices.newAdministrativeUnitWithId("deletedAdministrativeUnit")
				.setCode("deletedAdministrativeUnit").setTitle("deletedAdministrativeUnit");
		recordServices.add(administrativeUnit);
		recordServices.logicallyDelete(administrativeUnit.getWrappedRecord(), User.GOD);
		assertThat(administrativeUnit.getWrappedRecord().get(Schemas.LOGICALLY_DELETED_STATUS)).isEqualTo(true);

		presenter.forParams(ContainersByAdministrativeUnitsPresenter.TAB_DEPOSIT_NO_STORAGE_SPACE);

		RecordVODataProvider dataProvider = presenter.getDataProvider();

		assertThat(dataProvider.getSchema().getCode()).isEqualTo(AdministrativeUnit.DEFAULT_SCHEMA);
		assertThat(getRecordIdsFromDataProvider(dataProvider))
				.containsOnly(records.unitId_10, records.unitId_20, records.unitId_30);
	}

	private List<String> getRecordIdsFromDataProvider(RecordVODataProvider dataProvider) {
		List<String> IDs = new ArrayList<>();
		for (RecordVO recordVO : dataProvider.listRecordVOs(0, dataProvider.size())) {
			IDs.add(recordVO.getId());
		}
		return IDs;
	}
}
