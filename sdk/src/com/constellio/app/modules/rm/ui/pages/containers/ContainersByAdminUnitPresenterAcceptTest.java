package com.constellio.app.modules.rm.ui.pages.containers;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.ui.builders.UserToVOBuilder;
import com.constellio.app.modules.rm.wrappers.AdministrativeUnit;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.entities.UserVO;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.records.RecordServices;
import com.constellio.sdk.tests.ConstellioTest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class ContainersByAdminUnitPresenterAcceptTest extends ConstellioTest {

	RecordServices recordServices;
	RMTestRecords records = new RMTestRecords(zeCollection);
	@Mock ContainersByAdministrativeUnitsView view;
	@Mock SessionContext sessionContext;
	ContainersByAdministrativeUnitsPresenter presenter;
	RMSchemasRecordsServices rmSchemasRecordsServices;
	User admin;

	@Before
	public void setUp()
			throws Exception {

		prepareSystem(
				withZeCollection().withConstellioRMModule().withAllTestUsers().withRMTest(records)
						.withFoldersAndContainersOfEveryStatus().withEvents()
		);

		inCollection(zeCollection).setCollectionTitleTo("Collection de test");

		recordServices = getModelLayerFactory().newRecordServices();
		rmSchemasRecordsServices = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
		admin = records.getAdmin();

		when(view.getConstellioFactories()).thenReturn(getConstellioFactories());
		when(view.getCollection()).thenReturn(zeCollection);
		when(view.getSessionContext()).thenReturn(sessionContext);
		when(sessionContext.getCurrentLocale()).thenReturn(Locale.FRENCH);
		when(sessionContext.getCurrentCollection()).thenReturn(zeCollection);

		UserVO userVO = new UserToVOBuilder().build(admin.getWrappedRecord(), VIEW_MODE.DISPLAY, sessionContext);

		when(sessionContext.getCurrentUser()).thenReturn(userVO);


		presenter = new ContainersByAdministrativeUnitsPresenter(view) {
			@Override
			protected User getCurrentUser() {
				return admin;
			}
		};
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
		assertThat(administrativeUnit.getWrappedRecord().<Boolean>get(Schemas.LOGICALLY_DELETED_STATUS)).isEqualTo(true);

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
