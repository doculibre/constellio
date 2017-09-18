package com.constellio.app.services.event;

import com.constellio.app.modules.rm.RMConfigs;
import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.tasks.services.BetaWorkflowServices;
import com.constellio.data.io.services.facades.IOServices;
import com.constellio.data.io.services.zip.ZipService;
import com.constellio.model.entities.records.wrappers.Event;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.setups.Users;
import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.Before;
import org.junit.Test;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.StandardCopyOption;

import static com.constellio.app.services.event.EventService.FOLDER;
import static org.assertj.core.api.Assertions.assertThat;

public class EventServiceAcceptanceTest extends ConstellioTest
{
    RMTestRecords records = new RMTestRecords(zeCollection);
    Users users = new Users();
    EventService eventService;
    SchemasRecordsServices schemasRecordsServices;
    RecordServices recordServices;

    private IOServices ioServices;
    private ZipService zipService;

    public static final String DATE_1 = "10/01/2000 12:00:00";
    public static final String CUT_OFF_DATE_1 = "10/01/1995 00:00:00";
    public static final String CUT_OFF_DATE_2 = "10/01/1999 00:00:00";

    public static final String FILE_NAME_1 = "eventsBackup/2000-1-10.zip";
    public static final String FILE_NAME_2 = "eventsBackup/2000-1-11.zip";

    public static final String ZIP_TEMP_FILE_1 = "zipTempFile.zip";
    public static final String TEMP_FILE_1 = "tempFile1";

    private LocalDateTime getLocalDateTimeFromString(String dateTimeAsString) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern(EventService.DATE_TIME_FORMAT);
        return dateTimeFormatter.parseLocalDateTime(dateTimeAsString);
    }

    @Before
    public void setUp() {
        prepareSystem(withZeCollection().withConstellioRMModule().withRMTest(records).withAllTest(users));
        eventService = new EventService(getAppLayerFactory());
        schemasRecordsServices = new SchemasRecordsServices(zeCollection, getModelLayerFactory());
        recordServices = getAppLayerFactory().getModelLayerFactory().newRecordServices();
        ioServices = getAppLayerFactory().getModelLayerFactory().getIOServicesFactory().newIOServices();
        zipService = getAppLayerFactory().getModelLayerFactory().getIOServicesFactory().newZipService();
    }

    @Test
    public void getLastDayTimeDeletedThenSetThenGetThenOk() {
        LocalDateTime dateTime;

        assertThat(eventService.getLastDayTimeDeleted()).isNull();

        dateTime = getLocalDateTimeFromString(DATE_1);
        eventService.setLastDayTimeDelete(dateTime);

        assertThat(eventService.getLastDayTimeDeleted()).isEqualTo(dateTime);
    }

    @Test
    public void getCutOffDateThenOk() {
        givenTimeIs(getLocalDateTimeFromString(DATE_1));

        // Default cutoff is 60 months
        LocalDateTime cutOffDate = eventService.getCurrentCutOff();

        assertThat(cutOffDate).isEqualTo(getLocalDateTimeFromString(CUT_OFF_DATE_1));

        getModelLayerFactory().getSystemConfigurationsManager().setValue(RMConfigs.KEEP_EVENTS_FOR_X_MONTH, 12);

        assertThat(eventService.getCurrentCutOff()).isEqualTo(getLocalDateTimeFromString(CUT_OFF_DATE_2));
    }

    @Test
    public void backupEventInVaultThenOk() throws Exception {

        LocalDateTime localDateTime = getLocalDateTimeFromString(DATE_1);

        recordServices.add(createEvent(localDateTime));
        recordServices.add(createEvent(localDateTime));
        recordServices.add(createEvent(localDateTime));

        recordServices.add(createEvent(localDateTime.plusDays(1)));
        recordServices.add(createEvent(localDateTime.plusDays(1)));
        recordServices.add(createEvent(localDateTime.plusDays(1)));

        eventService.backupEventsInVault();

//         InputStream zipInputStream = getAppLayerFactory().getModelLayerFactory().getContentManager()
//                .getContentDao().getContentInputStream(FILE_NAME_1, SDK_STREAM);
//
//        File zipTEmpFile1 = ioServices.newTemporaryFile(ZIP_TEMP_FILE_1);
//
//        XMLInputFactory factory = XMLInputFactory.newInstance();
//
//        java.nio.file.Files.copy(
//                zipInputStream,
//                zipTEmpFile1.toPath(),
//                StandardCopyOption.REPLACE_EXISTING);
//
//        File tmpFile1 = ioServices.newTemporaryFile(TEMP_FILE_1);
//
//        zipService.unzip(zipTEmpFile1, tmpFile1);
//        InputStream tmpInputStream1 = new FileInputStream(tmpFile1);
//
//        XMLStreamReader xmlReader = factory.createXMLStreamReader(tmpInputStream1);
//
//        xmlReader.next();


    }

    private Event createEvent(LocalDateTime localDateTime) {
        Event event = schemasRecordsServices.newEvent();
        event.setTitle("Event1").setCreatedOn(localDateTime).setCreatedBy(users.adminIn(zeCollection).getId());
        event.setType("Type1");

        return event;

    }
}
