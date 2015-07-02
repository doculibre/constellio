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
package com.constellio.app.ui.pages.events;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.services.borrowingServices.BorrowingServices;
import com.constellio.app.modules.rm.services.events.RMEventsSearchServices;
import com.constellio.app.ui.application.ConstellioNavigator;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.MetadataValueVO;
import com.constellio.app.ui.framework.builders.MetadataToVOBuilder;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.data.utils.TimeProvider;
import com.constellio.model.entities.records.wrappers.EventType;
import com.constellio.model.services.search.SearchServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.FakeSessionContext;

public class EventPresenterAcceptTest extends ConstellioTest {
	@Mock EventView view;
	@Mock ConstellioNavigator navigator;
	RMTestRecords records = new RMTestRecords(zeCollection);
	SearchServices searchServices;
	EventPresenter presenter;
	SessionContext sessionContext;
	RMEventsSearchServices rmEventsSearchServices;
	RMSchemasRecordsServices rm;
	BorrowingServices borrowingServices;
	LocalDateTime nowDateTime = TimeProvider.getLocalDateTime();
	MetadataToVOBuilder metadataToVOBuilder = new MetadataToVOBuilder();

	@Before
	public void setUp()
			throws Exception {
		prepareSystem(
				withZeCollection().withConstellioRMModule().withAllTestUsers().withRMTest(records)
						.withFoldersAndContainersOfEveryStatus().withEvents()
		);
		inCollection(zeCollection).setCollectionTitleTo("Collection de test");

		rmEventsSearchServices = new RMEventsSearchServices(getModelLayerFactory(), zeCollection);
		rm = new RMSchemasRecordsServices(zeCollection, getModelLayerFactory());

		sessionContext = FakeSessionContext.adminInCollection(zeCollection);
		sessionContext.setCurrentLocale(Locale.FRENCH);
		searchServices = getModelLayerFactory().newSearchServices();
		borrowingServices = new BorrowingServices(zeCollection, getModelLayerFactory());

		when(view.getSessionContext()).thenReturn(sessionContext);
		when(view.getCollection()).thenReturn(zeCollection);
		when(view.getConstellioFactories()).thenReturn(getConstellioFactories());
		when(view.navigateTo()).thenReturn(navigator);

		presenter = spy(new EventPresenter(view));

		givenTimeIs(nowDateTime);
	}

	@Test
	public void givenFolderIdMetadataWhenIsRecordIdMetadataThenReturnTrue()
			throws Exception {

		MetadataValueVO metadataValueVO = getMetadataValueVO("folder_default_id");

		assertThat(presenter.isRecordIdMetadata(metadataValueVO)).isTrue();
	}

	@Test
	public void givenBorrowedFolderByBobWhenGetDataProviderForCurrentlyBorrowedFoldersByUserThenOk()
			throws Exception {
		borrowingServices.borrowFolder("C30", nowDateTime.plusDays(1).toDate(), records.getAdmin(), records.getBob_userInAC());

		Map<String, String> params = new HashMap<>();
		params.put("id", records.getBob_userInAC().getId());
		params.put("startDate", nowDateTime.toString());
		params.put("endDate", nowDateTime.plusDays(1).toString());
		params.put("eventType", EventType.CURRENTLY_BORROWED_FOLDERS);
		params.put("eventCategory", EventCategory.EVENTS_BY_USER.name());
		when(view.getParameters()).thenReturn(params);

		RecordVODataProvider recordVODataProvider = presenter.getDataProvider();

		assertThat(recordVODataProvider.size()).isEqualTo(1);
		assertThat(recordVODataProvider.getRecordVO(0).getId()).isEqualTo("C30");
		assertThat(recordVODataProvider.getRecordVO(0).getTitle()).isEqualTo("Haricot");
	}

	private MetadataValueVO getMetadataValueVO(String localCode) {
		MetadataVO metadataVO = metadataToVOBuilder
				.build(rm.defaultFolderSchema().getMetadata(localCode), sessionContext);
		return new MetadataValueVO(metadataVO);
	}
}
