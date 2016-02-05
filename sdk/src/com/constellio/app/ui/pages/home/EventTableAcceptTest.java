package com.constellio.app.ui.pages.home;

import com.constellio.sdk.tests.ConstellioTest;

public class EventTableAcceptTest extends ConstellioTest {
	//
	//	SessionContext sessionContext;
	//	EventTable eventTable;
	//	RMTestRecords records = new RMTestRecords(zeCollection);
	//	LoggingServices loggingServices;
	//	RecordServices recordServices;
	//	LocalDateTime now = TimeProvider.getLocalDateTime();
	//
	//	@Before
	//	public void setUp()
	//			throws Exception {
	//
	//		prepareSystem(
	//				withZeCollection().withConstellioRMModule().withAllTestUsers().withRMTest(records)
	//						.withFoldersAndContainersOfEveryStatus().withEvents()
	//		);
	//
	//		inCollection(zeCollection).setCollectionTitleTo("Collection de test");
	//
	//		recordServices = getModelLayerFactory().newRecordServices();
	//		loggingServices = getModelLayerFactory().newLoggingServices();
	//		sessionContext = FakeSessionContext.adminInCollection(zeCollection);
	//		eventTable = new EventTable(getModelLayerFactory(), sessionContext, Folder.SCHEMA_TYPE, "view_folder");
	//	}
	//
	//	@Test
	//	public void givenFoldersConsultedWhenGetDataProviderSizeThenReturnNumberOfFoldersConsulted()
	//			throws Exception {
	//		givenFoldersConsultedByAdmin();
	//
	//		givenTimeIs(now);
	//		RecordVODataProvider dataProvider = eventTable.getDataProvider();
	//		assertThat(dataProvider.size()).isEqualTo(10);
	//		assertThat(dataProvider.getRecordVO(0).getId()).isEqualTo(records.folder_A14);
	//		assertThat(dataProvider.getRecordVO(1).getId()).isEqualTo(records.folder_A13);
	//		assertThat(dataProvider.getRecordVO(2).getId()).isEqualTo(records.folder_A12);
	//		assertThat(dataProvider.getRecordVO(3).getId()).isEqualTo(records.folder_A11);
	//		assertThat(dataProvider.getRecordVO(4).getId()).isEqualTo(records.folder_A10);
	//		assertThat(dataProvider.getRecordVO(5).getId()).isEqualTo(records.folder_A09);
	//		assertThat(dataProvider.getRecordVO(6).getId()).isEqualTo(records.folder_A08);
	//		assertThat(dataProvider.getRecordVO(7).getId()).isEqualTo(records.folder_A07);
	//		assertThat(dataProvider.getRecordVO(8).getId()).isEqualTo(records.folder_A06);
	//		assertThat(dataProvider.getRecordVO(9).getId()).isEqualTo(records.folder_A05);
	//
	//	}
	//
	//	private void givenFoldersConsultedByAdmin() {
	//		List<Record> foldersConsulted = new ArrayList<>();
	//		foldersConsulted
	//				.addAll(Arrays.asList(
	//						records.getFolder_A01().getWrappedRecord(),
	//						records.getFolder_A02().getWrappedRecord(),
	//						records.getFolder_A03().getWrappedRecord(),
	//						records.getFolder_A04().getWrappedRecord(),
	//						records.getFolder_A05().getWrappedRecord(),
	//						records.getFolder_A06().getWrappedRecord(),
	//						records.getFolder_A07().getWrappedRecord(),
	//						records.getFolder_A08().getWrappedRecord(),
	//						records.getFolder_A09().getWrappedRecord(),
	//						records.getFolder_A10().getWrappedRecord(),
	//						records.getFolder_A11().getWrappedRecord(),
	//						records.getFolder_A12().getWrappedRecord(),
	//						records.getFolder_A13().getWrappedRecord(),
	//						records.getFolder_A14().getWrappedRecord()
	//				));
	//		LocalDateTime pastDateTime = now.minusDays(foldersConsulted.size());
	//		int i = 0;
	//		for (Record record : foldersConsulted) {
	//			LocalDateTime incPastDateTime = pastDateTime.plusDays(i++);
	//			givenTimeIs(incPastDateTime);
	//			loggingServices.logRecordView(record, records.getAdmin());
	//		}
	//		recordServices.flush();
	//	}
}
