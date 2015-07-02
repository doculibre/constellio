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
package com.constellio.app.modules.rm.ui.pages.containers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.wrappers.AdministrativeUnit;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.services.records.RecordServices;
import com.constellio.sdk.tests.ConstellioTest;

public class ContainersByAdminUnitPresenterAcceptTest extends ConstellioTest {

	RecordServices recordServices;
	RMTestRecords records = new RMTestRecords(zeCollection);
	@Mock ContainersByAdministrativeUnitsView view;
	@Mock SessionContext sessionContext;
	ContainersByAdministrativeUnitsPresenter presenter;

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

	private List<String> getRecordIdsFromDataProvider(RecordVODataProvider dataProvider) {
		List<String> IDs = new ArrayList<>();
		for (RecordVO recordVO : dataProvider.listRecordVOs(0, dataProvider.size())) {
			IDs.add(recordVO.getId());
		}
		return IDs;
	}
}
