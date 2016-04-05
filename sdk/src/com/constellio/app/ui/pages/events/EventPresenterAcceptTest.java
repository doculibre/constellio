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
import com.constellio.app.modules.rm.services.borrowingServices.BorrowingType;
import com.constellio.app.modules.rm.services.events.RMEventsSearchServices;
import com.constellio.app.ui.application.CoreViews;
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
	@Mock CoreViews navigator;
	RMTestRecords records = new RMTestRecords(zeCollection);
	SearchServices searchServices;
	EventPresenter presenter;
	SessionContext sessionContext;
	RMEventsSearchServices rmEventsSearchServices;
	RMSchemasRecordsServices rm;
	BorrowingServices borrowingServices;
	LocalDateTime now = TimeProvider.getLocalDateTime();
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

		givenTimeIs(now);
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
		borrowingServices
				.borrowFolder("C30", now.toLocalDate(), now.plusDays(1).toLocalDate(), records.getAdmin(),
						records.getBob_userInAC(),
						BorrowingType.BORROW);

		Map<String, String> params = new HashMap<>();
		params.put("id", records.getBob_userInAC().getId());
		params.put("startDate", now.toString());
		params.put("endDate", now.plusDays(1).toString());
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
