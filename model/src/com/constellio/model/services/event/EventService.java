package com.constellio.model.services.event;

import com.constellio.data.dao.dto.records.RecordsFlushing;
import com.constellio.data.dao.dto.records.TransactionDTO;
import com.constellio.data.dao.services.bigVault.RecordDaoException;
import com.constellio.data.dao.services.bigVault.SearchResponseIterator;
import com.constellio.data.dao.services.records.RecordDao;
import com.constellio.data.io.services.facades.IOServices;
import com.constellio.data.io.services.zip.ZipService;
import com.constellio.data.io.services.zip.ZipServiceException;
import com.constellio.data.utils.TimeProvider;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.Event;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.model.services.search.query.logical.criteria.CriteriaUtils;
import com.sun.xml.txw2.output.IndentingXMLStreamWriter;
import org.apache.commons.collections.CollectionUtils;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.jetbrains.annotations.NotNull;
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.fromEveryTypesOfEveryCollection;

public class EventService implements Runnable {
    ModelLayerFactory modelayerFactory;

    public static final String DATE_TIME_FORMAT = "dd/MM/yyyy HH:mm:ss";
    public static final String ENCODING = "UTF-8";
    public static final String FOLDER = "eventsBackup";
    public static final String IO_STREAM_NAME_BACKUP_EVENTS_IN_VAULT = "com.constellio.model.services.event.EventService#backupEventsInVault";
    public static final String IO_STREAM_NAME_CLOSE = "com.constellio.model.services.event.EventService#close";
    public static final LocalDateTime MIN_LOCAL_DATE_TIME  = new LocalDateTime( 0000, 1, 1, 0, 0, 0 );

    private IOServices ioServices;
    private ZipService zipService;
    private MetadataSchemasManager metadataSchemasManager;

    public EventService(ModelLayerFactory modelayerFactory) {
        this.modelayerFactory = modelayerFactory;
        this.ioServices = modelayerFactory.getIOServicesFactory().newIOServices();
        this.zipService = modelayerFactory.getIOServicesFactory().newZipService();
        metadataSchemasManager = modelayerFactory.getMetadataSchemasManager();
    }

    public LocalDateTime getDeletetionDateCutOff() {
        Integer periodInMonth = modelayerFactory.getSystemConfigurationsManager().getValue(ConstellioEIMConfigs.KEEP_EVENTS_FOR_X_MONTH);
        LocalDateTime nowLocalDateTime = TimeProvider.getLocalDateTime();
        nowLocalDateTime = nowLocalDateTime.withSecondOfMinute(0).withHourOfDay(0).withMillisOfSecond(0).withMinuteOfHour(0);
        LocalDateTime cutoffLocalDateTime;


        cutoffLocalDateTime = nowLocalDateTime.minusMonths(periodInMonth);

        return cutoffLocalDateTime;

    }

    public LocalDateTime getLastDayTimeArchived() {
        String dateTimeAsString = modelayerFactory.getSystemConfigurationsManager().getValue(ConstellioEIMConfigs.LAST_BACKUP_DAY);
        LocalDateTime dateTime = null;

        if(dateTimeAsString != null) {
            DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern(DATE_TIME_FORMAT);
            dateTime = dateTimeFormatter.parseLocalDateTime(dateTimeAsString);
        } else {
            dateTime = MIN_LOCAL_DATE_TIME;
        }

        return dateTime;
    }
    public void setLastArchivedDayTime(LocalDateTime lastDayTime) {
        modelayerFactory
                .getSystemConfigurationsManager().setValue(ConstellioEIMConfigs.LAST_BACKUP_DAY,
                lastDayTime.toString(DATE_TIME_FORMAT));
    }

    public void backupAndRemove() {
        backupEventsInVault();
        removeOldEventFromSolr();
    }

    private void closeFile(File file, IndentingXMLStreamWriter indentingXMLStreamWriter, LocalDateTime localDateTime, String fileName, OutputStream fileStreamToClose) {

        File zipFile = null;
        InputStream zipFileInputStream = null;
        boolean isindextingXMLStreamClose = false;
        try {
            if(indentingXMLStreamWriter != null) {
                if(localDateTime != null) {
                    setLastArchivedDayTime(localDateTime.withTime(0, 0,0,0));
                }
                indentingXMLStreamWriter.writeEndElement();
                indentingXMLStreamWriter.writeEndDocument();
                indentingXMLStreamWriter.flush();

                indentingXMLStreamWriter.close();
                isindextingXMLStreamClose = true;
                zipFile = createNewFile(fileName + ".zip");
                zipService.zip(zipFile, Arrays.asList(file));

                zipFileInputStream = ioServices.newFileInputStream(zipFile, IO_STREAM_NAME_CLOSE);
                modelayerFactory.getContentManager()
                        .getContentDao().add(FOLDER + "/" + fileName + ".zip", zipFileInputStream);
            }
        } catch (XMLStreamException e) {
            throw new RuntimeException("Error while closing the Event writer outputStream. File : " + fileName, e);
        } catch (ZipServiceException e) {
            throw new RuntimeException("Error while zipping the file : " + fileName, e);
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Error while zipping the file : " + fileName, e);
        } finally {
            if(indentingXMLStreamWriter != null && !isindextingXMLStreamClose) {
                try {
                    indentingXMLStreamWriter.close();
                } catch (XMLStreamException e) {
                    throw new RuntimeException("Error while closing the IndentingXMLStreamWriter stream : with file name :" + fileName, e);
                }
            }
            ioServices.closeQuietly(fileStreamToClose);
            ioServices.closeQuietly(zipFileInputStream);
            ioServices.deleteQuietly(zipFile);
            ioServices.deleteQuietly(file);
        }
    }

    public String dateAsFileName(LocalDateTime localDateTime) {

        if(localDateTime == null) {
            return "null";
        }

        return localDateTime.toString("yyyy-MM-dd");
    }

    public LocalDateTime getArchivedUntilLocalDate() {
        return TimeProvider.getLocalDateTime().withTime(0,0,0,0);
    }

    private File createNewFile(String fileName) {
        return ioServices.newTemporaryFileWithoutGuid(fileName);
    }

    public static final String EVENTS_XML_TAG = "Events";
    public static final String EVENT_XML_TAG = "Event";

    public List<Event> backupEventsInVault() {
        XMLOutputFactory factory = XMLOutputFactory.newInstance();
        List<Event> eventList = new ArrayList<>();
        LocalDateTime lastDayTimeArchived = getLastDayTimeArchived();
        File currentFile = null;
        if(lastDayTimeArchived == null || lastDayTimeArchived.compareTo(getArchivedUntilLocalDate()) < 0) {
            SearchServices searchServices = modelayerFactory.newSearchServices();
            LogicalSearchQuery logicalSearchQuery = getEventAfterLastArchivedDayAndBeforeLastDayToArchiveLogicalSearchQuery();

            SearchResponseIterator<Record> searchResponseIterator = searchServices.recordsIteratorKeepingOrder(logicalSearchQuery, 25000);

            int dayOfTheMonth = -1;

            OutputStream fileOutputStream = null;
            XMLStreamWriter xmlStreamWriter;
            IndentingXMLStreamWriter writer = null;
            LocalDateTime oldLocalDateTime;
            LocalDateTime localDateTime = null;
            String fileName = null;
            try
            {
                while (searchResponseIterator.hasNext()) {
                    Record record = searchResponseIterator.next();

                    oldLocalDateTime = localDateTime;
                    localDateTime = record.get(Schemas.CREATED_ON);

                    try {
                        if (dayOfTheMonth != localDateTime.getDayOfMonth()) {
                            dayOfTheMonth = localDateTime.getDayOfMonth();
                            closeFile(currentFile, writer, oldLocalDateTime, fileName, fileOutputStream);
                            fileName = dateAsFileName(localDateTime);
                            currentFile = createNewFile(fileName + ".xml");
                            fileOutputStream = ioServices.newFileOutputStream(currentFile, IO_STREAM_NAME_BACKUP_EVENTS_IN_VAULT);
                            xmlStreamWriter = factory.createXMLStreamWriter(fileOutputStream, ENCODING);
                            writer = new IndentingXMLStreamWriter(xmlStreamWriter);
                            writer.setIndentStep("  ");
                            writer.writeStartDocument(ENCODING, "1.0");
                            writer.writeStartElement(EVENTS_XML_TAG);

                        }
                        writer.writeStartElement(EVENT_XML_TAG);

                        MetadataSchemaTypes metadataSchemaTypes = metadataSchemasManager.getSchemaTypes(record.getCollection());
                        MetadataSchema metadataSchema = metadataSchemaTypes.getSchema(record.getSchemaCode());
                        for (Metadata metadata : metadataSchema.getMetadatas()) {
                            Object value = record.get(metadata);

                            boolean write;
                            if (value != null) {
                                write = true;
                                if (value instanceof java.util.Collection) {
                                    if (CollectionUtils.isNotEmpty((java.util.Collection) value)) {
                                        write = true;
                                    } else {
                                        write = false;
                                    }
                                }

                                if (write) {
                                    writer.writeAttribute(metadata.getLocalCode(), record.get(metadata).toString());
                                }
                            }
                        }

                        writer.writeEndElement();

                    } catch (Exception e) {
                        throw new RuntimeException("File not found for Event writing", e);
                    }
                }
            }
            finally {
                closeFile(currentFile, writer, localDateTime, fileName, fileOutputStream);
            }


        }

        return eventList;
    }

    @NotNull
    public LogicalSearchQuery getEventAfterLastArchivedDayAndBeforeLastDayToArchiveLogicalSearchQuery() {
        LogicalSearchQuery logicalSearchQuery = new LogicalSearchQuery();
        LogicalSearchCondition logicalSearchCondition = fromEveryTypesOfEveryCollection().where(Schemas.SCHEMA).isStartingWithText("event_")
                .andWhere(Schemas.CREATED_ON).isGreaterThan(getLastDayTimeArchived()).andWhere(Schemas.CREATED_ON).isLessThan(getArchivedUntilLocalDate());
        logicalSearchQuery.setCondition(logicalSearchCondition);
        logicalSearchQuery.sortAsc(Schemas.CREATED_ON);
        return logicalSearchQuery;
    }

    public void removeOldEventFromSolr() {
        deleteArchivedEvents();
    }

    public void deleteArchivedEvents() {
        RecordDao recordDao = modelayerFactory.getDataLayerFactory().newRecordDao();


        TransactionDTO transaction = new TransactionDTO(RecordsFlushing.NOW());
        ModifiableSolrParams modifiableSolrParams = new ModifiableSolrParams();
        String code  = "createdOn_dt";

        String between = code + ":{* TO " + CriteriaUtils.toSolrStringValue(getDeletetionDateCutOff(), null) + "}";

        modifiableSolrParams.set("q", between);
        modifiableSolrParams.add("fq", "schema_s:event_* ");

        transaction = transaction.withDeletedByQueries(modifiableSolrParams);
        try {
            recordDao.execute(transaction);
        } catch (RecordDaoException.OptimisticLocking optimisticLocking) {
            throw new RuntimeException("Error while deleting archived eventRecord", optimisticLocking);
        }
    }

    @Override
    public void run() {
        backupAndRemove();
    }
}
