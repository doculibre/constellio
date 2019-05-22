package com.constellio.model.services.event;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.data.io.services.facades.IOServices;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.Event;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.setups.Users;
import org.joda.time.LocalDateTime;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Arrays;

public class FileEventXMLWriterAcceptanceTest extends ConstellioTest {

	public static final String DATE_1 = "10/01/2000 12:00:00";
	public static final String TEMP_FILE = "tempFile";

	SchemasRecordsServices schemasRecordsServices;
	Users users = new Users();
	RMTestRecords records = new RMTestRecords(zeCollection);
	RecordServices recordServices;
	IOServices ioServices;


	@Before
	public void setUp() {
		prepareSystem(withZeCollection().withAllTest(users).withAllTestUsers());
		schemasRecordsServices = new SchemasRecordsServices(zeCollection, getModelLayerFactory());
		recordServices = getModelLayerFactory().newRecordServices();
		ioServices = getModelLayerFactory().getIOServicesFactory().newIOServices();
	}

	@Test(expected=RuntimeException.class)
	public void givenFolderRecordToWriteThenThrow() {
		Record userRecord = users.aliceIn(zeCollection).getWrappedRecord();
		File file = null;
		try {
			file = ioServices.newTemporaryFile(TEMP_FILE, "xml");

			FileEventXMLWriter fileEventXMLWriter = new FileEventXMLWriter(file, getModelLayerFactory());
			fileEventXMLWriter.write(userRecord);
			Assert.fail("Runtime exception should be thrown.");
		} finally {
			ioServices.deleteQuietly(file);
		}
	}

	@Test
	public void writeEventThenVerifyContent()
			throws RecordServicesException, FileNotFoundException, XMLStreamException {
		LocalDateTime localDateTime = EventTestUtil.getLocalDateTimeFromString(DATE_1);

		LocalDateTime event1LocalDateTime = localDateTime;
		Event event1 = createEvent(event1LocalDateTime.minusSeconds(6));
		Event event2 = createEvent(event1LocalDateTime.minusSeconds(5));
		Event event3 = createEvent(event1LocalDateTime.minusSeconds(4));
		recordServices.add(event1);
		recordServices.add(event2);
		recordServices.add(event3);

		File file = ioServices.newTemporaryFile(TEMP_FILE, "xml");

		FileEventXMLWriter fileEventXMLWriter = new FileEventXMLWriter(file, getModelLayerFactory());

		fileEventXMLWriter.write(event1.getWrappedRecord());
		fileEventXMLWriter.write(event2.getWrappedRecord());
		fileEventXMLWriter.write(event3.getWrappedRecord());

		fileEventXMLWriter.closeXMLFile();

		XMLInputFactory factory = XMLInputFactory.newInstance();
		InputStream tmpInputStream1 = ioServices.newFileInputStream(file, SDK_STREAM);
		XMLStreamReader xmlReader = factory.createXMLStreamReader(tmpInputStream1);
		EventTestUtil.assertEvent(xmlReader, Arrays.asList(event1, event2, event3), 3);

		ioServices.deleteQuietly(file);
	}

	private Event createEvent(LocalDateTime localDateTime) {
		Event event = schemasRecordsServices.newEvent();
		event.setTitle("Event1").setCreatedOn(localDateTime).setCreatedBy(users.adminIn(zeCollection).getId());
		event.setType("Type1");

		return event;
	}
}
