package com.constellio.model.services.event;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.data.dao.services.contents.ContentDaoException;
import com.constellio.data.io.services.facades.IOServices;
import com.constellio.data.io.services.zip.ZipService;
import com.constellio.data.io.services.zip.ZipServiceException;
import com.constellio.model.entities.records.wrappers.Event;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.constellio.model.services.search.SPEQueryResponse;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.setups.Users;
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.Before;
import org.junit.Test;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.fromEveryTypesOfEveryCollection;
import static org.assertj.core.api.Assertions.assertThat;

public class EventServiceAcceptanceTest extends ConstellioTest {
	RMTestRecords records = new RMTestRecords(zeCollection);
	Users users = new Users();
	EventService eventService;
	SchemasRecordsServices schemasRecordsServices;
	RecordServices recordServices;
	SearchServices searchServices;

	private IOServices ioServices;
	private ZipService zipService;

	public static final String DATE_1 = "10/01/2000 12:00:00";
	public static final String CUT_OFF_DATE_1 = "10/10/0000 00:00:00";
	public static final String CUT_OFF_DATE_2 = "10/01/1999 00:00:00";

	public static final String FOLDER_NAME = "eventsBackup";
	public static final String FILE_NAME_1 = "eventsBackup/" + EventService.EVENTS_ZIP_WITH_EXTENTION;

	public static final String ZIP_TEMP_FILE_1 = "zipTempFile.zip";
	public static final String TEMP_FILE_1 = "tempFile1";
	public static final String TEMP_FILE_2 = "tempFile2";

	private LocalDateTime getLocalDateTimeFromString(String dateTimeAsString) {
		DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern(EventService.DATE_TIME_FORMAT);
		return dateTimeFormatter.parseLocalDateTime(dateTimeAsString);
	}

	@Before
	public void setUp() {
		prepareSystem(withZeCollection().withConstellioRMModule().withRMTest(records).withAllTest(users).withAllTestUsers());
		eventService = new EventService(getModelLayerFactory());
		schemasRecordsServices = new SchemasRecordsServices(zeCollection, getModelLayerFactory());
		recordServices = getAppLayerFactory().getModelLayerFactory().newRecordServices();
		ioServices = getAppLayerFactory().getModelLayerFactory().getIOServicesFactory().newIOServices();
		zipService = getAppLayerFactory().getModelLayerFactory().getIOServicesFactory().newZipService();
		searchServices = getAppLayerFactory().getModelLayerFactory().newSearchServices();
	}

	@Test
	public void getLastArchivedTimeThenSetThenGetThenOk() {
		LocalDateTime dateTime;

		assertThat(eventService.getLastDayTimeArchived()).isEqualTo(EventService.MIN_LOCAL_DATE_TIME);

		dateTime = getLocalDateTimeFromString(DATE_1);
		eventService.setLastArchivedDayTime(dateTime);

		assertThat(eventService.getLastDayTimeArchived()).isEqualTo(dateTime);
	}

	@Test
	public void getCutOffDateValueThenOk() {
		givenTimeIs(getLocalDateTimeFromString(DATE_1));

		// Default cutoff is 60 months
		LocalDateTime cutOffDate = eventService.getDeletetionDateCutOff();

		assertThat(cutOffDate).isEqualTo(getLocalDateTimeFromString(CUT_OFF_DATE_1).minusYears(6334));

		getModelLayerFactory().getSystemConfigurationsManager().setValue(ConstellioEIMConfigs.KEEP_EVENTS_FOR_X_MONTH, 12);

		assertThat(eventService.getDeletetionDateCutOff()).isEqualTo(getLocalDateTimeFromString(CUT_OFF_DATE_2));
	}

	@Test
	public void checkSolrStateBackupAndDeleteEventThanCheckSolrStateThenOk()
			throws Exception {
		getModelLayerFactory().getSystemConfigurationsManager().setValue(ConstellioEIMConfigs.KEEP_EVENTS_FOR_X_MONTH, 0);

		givenTimeIs(LocalDateTime.now().plusDays(2));

		LocalDateTime event1LocalDateTime = getLocalDateTimeFromString(DATE_1);
		Event event1 = createEvent(event1LocalDateTime.minusSeconds(6));
		Event event2 = createEvent(event1LocalDateTime.minusSeconds(5));

		recordServices.add(event1);
		recordServices.add(event2);

		LogicalSearchQuery logicalSearchQuery = new LogicalSearchQuery(fromEveryTypesOfEveryCollection().returnAll());
		logicalSearchQuery.setNumberOfRows(0);
		logicalSearchQuery.sortAsc(Schemas.CREATED_ON);
		SPEQueryResponse allRecord1 = searchServices.query(logicalSearchQuery);

		File backupZipFile = eventService.backupAndRemove();

		ioServices.deleteQuietly(backupZipFile);

		SPEQueryResponse allRecord2 = searchServices.query(logicalSearchQuery);

		//DELETE HAS BEEN DISABLED FOR THE MOMENT assertThat(allRecord1.getNumFound()).isEqualTo(allRecord2.getNumFound() + 2);
	}

	@Test
	public void backupEventInVaultAndRemoveFromSolrThenOk()
			throws Exception {

		LocalDateTime localDateTime = getLocalDateTimeFromString(DATE_1);

		LocalDateTime event1LocalDateTime = localDateTime;
		Event event1 = createEvent(event1LocalDateTime.minusSeconds(6));
		Event event2 = createEvent(event1LocalDateTime.minusSeconds(5));
		Event event3 = createEvent(event1LocalDateTime.minusSeconds(4));
		recordServices.add(event1);
		recordServices.add(event2);
		recordServices.add(event3);

		getModelLayerFactory().getSystemConfigurationsManager().setValue(ConstellioEIMConfigs.KEEP_EVENTS_FOR_X_MONTH, 12);

		LocalDateTime event2LocalDateTime = eventService.getDeletetionDateCutOff().minusHours(1);
		Event event4 = createEvent(event2LocalDateTime.minusSeconds(3));
		Event event5 = createEvent(event2LocalDateTime.minusSeconds(2));
		Event event6 = createEvent(event2LocalDateTime.minusSeconds(1));
		recordServices.add(event4);
		recordServices.add(event5);
		recordServices.add(event6);

		Event event7 = createEvent(eventService.getDeletetionDateCutOff());
		recordServices.add(event7);

		Event event8 = createEvent(eventService.getArchivedUntilLocalDate().plusDays(1));
		recordServices.add(event8);

		Event event9 = createEvent(eventService.getArchivedUntilLocalDate().plusDays(2));
		recordServices.add(event9);

		File backup1 = eventService.backupAndRemove();

		String vaultPathToZip1 = EventService.addNumberToContentIdForEventZip(EventService.getContentIdForEventZip(), ".zip", 1);
		findZipFileAndAssertXml(Arrays.asList(event1, event2, event3),
				vaultPathToZip1,
				3, AutoSplitByDayEventsExecutor.getPathFromDateTime(
				(LocalDateTime) event1.get(Schemas.CREATED_ON)));
		findZipFileAndAssertXml(Arrays.asList(event4, event5, event6),
				vaultPathToZip1
				, 3,
				AutoSplitByDayEventsExecutor.getPathFromDateTime(
				(LocalDateTime) event4.get(Schemas.CREATED_ON)));

		findZipFileAndAssertXml(Arrays.asList(event7),
				vaultPathToZip1, 1,
				AutoSplitByDayEventsExecutor.getPathFromDateTime(
						(LocalDateTime) event7.get(Schemas.CREATED_ON)));


		assertThat(findNumberOfFileInZip(backup1)).isEqualTo(3);


		assertThat(getAppLayerFactory().getModelLayerFactory().getContentManager()
				.getContentDao().getFolderContents(FOLDER_NAME).size()).isEqualTo(1);

		// Verify solar state.
		assertThat(searchServices.search(eventService.getEventAfterLastArchivedDayAndBeforeLastDayToArchiveLogicalSearchQuery())
				.size()).isEqualTo(0);
		LogicalSearchQuery logicalSearchQuery = new LogicalSearchQuery(
				fromEveryTypesOfEveryCollection().where(Schemas.SCHEMA).isStartingWithText("event_"));
		// Check for remaning event in solr

		//DELETE HAS BEEN DISABLED FOR THE MOMENT assertThat(searchServices.search(logicalSearchQuery).size()).isEqualTo(3);

		eventService.getLastDayTimeArchived().toString(EventService.DATE_TIME_FORMAT)
				.equals(eventService.getDeletetionDateCutOff().toString(EventService.DATE_TIME_FORMAT));

		givenTimeIs(LocalDateTime.now().plusDays(2));

		File backup2 = eventService.backupAndRemove();


		findZipFileAndAssertXml(Arrays.asList(event8),
				EventService.addNumberToContentIdForEventZip(EventService.getContentIdForEventZip(), ".zip", 2), 1,
				AutoSplitByDayEventsExecutor.getPathFromDateTime(
						(LocalDateTime) event8.get(Schemas.CREATED_ON)));

		assertThat(getAppLayerFactory().getModelLayerFactory().getContentManager()
				.getContentDao().getFolderContents(FOLDER_NAME).size()).isEqualTo(2);

		ioServices.deleteQuietly(backup2);
		ioServices.deleteQuietly(backup1);


		// Event 7 is not deleted
		//DELETE HAS BEEN DISABLED FOR THE MOMENT assertThat(searchServices.search(logicalSearchQuery).size()).isEqualTo(2);

	}

	private int findNumberOfFileInZip(File zip)
			throws ZipServiceException {
		File tmpFile2 = ioServices.newTemporaryFolder(TEMP_FILE_2);
		zipService.unzip(zip, tmpFile2);
		return findNumberOfFile(tmpFile2);
	}

	private int findNumberOfFile(File rootFile) {
		int numberOfFile = 0;

		for(File file : rootFile.listFiles()) {
			if(file.isFile()) {
				numberOfFile++;
			} else {
				numberOfFile += findNumberOfFile(file);
			}
		}
		return numberOfFile;

	}

	private void findZipFileAndAssertXml(List<Event> eventList, String vaultPathToZip, int numberOfEventToBeExpected, String pathToFileInZip)
			throws ContentDaoException.ContentDaoException_NoSuchContent, IOException, ZipServiceException, XMLStreamException {
		InputStream zipInputStream = getAppLayerFactory().getModelLayerFactory().getContentManager()
				.getContentDao().getContentInputStream(vaultPathToZip, SDK_STREAM);

		File zipTEmpFile1 = ioServices.newTemporaryFile(ZIP_TEMP_FILE_1);

		XMLInputFactory factory = XMLInputFactory.newInstance();

		java.nio.file.Files.copy(
				zipInputStream,
				zipTEmpFile1.toPath(),
				StandardCopyOption.REPLACE_EXISTING);

		ioServices.closeQuietly(zipInputStream);

		File tmpFile1 = ioServices.newTemporaryFolder(TEMP_FILE_1);

		zipService.unzip(zipTEmpFile1, tmpFile1);

		File zipFilefolder = new File(tmpFile1, pathToFileInZip);
		InputStream tmpInputStream1 = ioServices.newFileInputStream(zipFilefolder, SDK_STREAM);
		XMLStreamReader xmlReader = factory.createXMLStreamReader(tmpInputStream1);

		assertEvent(xmlReader, eventList, numberOfEventToBeExpected);

		xmlReader.close();
		tmpInputStream1.close();
		ioServices.deleteQuietly(zipTEmpFile1);
		ioServices.deleteQuietly(zipFilefolder);
	}

	private void assertEvent(XMLStreamReader xmlStreamReader, List<Event> event, int numberOfEventToBeExpected)
			throws XMLStreamException {
		int eventCounter = 0;
		while (xmlStreamReader.hasNext()) {
			int elementType = xmlStreamReader.next();
			if (elementType == XMLStreamConstants.START_ELEMENT && xmlStreamReader.getLocalName().equals("Event")) {
				;
				for (int i = 0; i < xmlStreamReader.getAttributeCount(); i++) {
					String key = xmlStreamReader.getAttributeLocalName(i);
					assertThat(event.get(eventCounter).get(key).toString()).isEqualTo(xmlStreamReader.getAttributeValue(i));
				}
				eventCounter++;
			}
		}
		assertThat(eventCounter).isEqualTo(numberOfEventToBeExpected);
	}

	private Event createEvent(LocalDateTime localDateTime) {
		Event event = schemasRecordsServices.newEvent();
		event.setTitle("Event1").setCreatedOn(localDateTime).setCreatedBy(users.adminIn(zeCollection).getId());
		event.setType("Type1");

		return event;

	}
}
