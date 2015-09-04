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
package com.constellio.model.services.emails;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.mail.Session;

import org.joda.time.Duration;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.data.dao.managers.StatefulService;
import com.constellio.data.threads.BackgroundThreadConfiguration;
import com.constellio.data.threads.BackgroundThreadExceptionHandling;
import com.constellio.data.threads.BackgroundThreadsManager;
import com.constellio.data.utils.TimeProvider;
import com.constellio.model.conf.email.EmailConfigurationsManager;
import com.constellio.model.conf.email.EmailServerConfiguration;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.EmailToSend;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.services.collections.CollectionsListManager;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import java.util.*;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

public class EmailQueueManager implements StatefulService {
    private static final Logger LOGGER = LoggerFactory.getLogger(EmailQueueManager.class);
    private static final long TWENTY_SECONDS = 20 * 1000l;
    public static final int MAX_TRY_SEND = 3;
    static int SEND_EMAIL_BATCH = 20;
    private static final Duration DURATION_BETWEEN_EXECUTION = new Duration(TWENTY_SECONDS);

    private EmailConfigurationsManager emailConfigurationManager;
    private SearchServices searchServices;
    private CollectionsListManager collectionManager;
    private RecordServices recordServices;
    private EmailServices emailServices;
    BackgroundThreadsManager backgroundThreadsManager;
    MetadataSchemasManager metadataSchemasManager;
    private ModelLayerFactory modelLayerFactory;

    public EmailQueueManager(ModelLayerFactory modelLayerFactory, EmailServices emailServices) {
        this.modelLayerFactory = modelLayerFactory;
        this.emailConfigurationManager = modelLayerFactory.getEmailConfigurationsManager();
        this.backgroundThreadsManager = modelLayerFactory.getDataLayerFactory().getBackgroundThreadsManager();
        this.searchServices = modelLayerFactory.newSearchServices();
        this.collectionManager = modelLayerFactory.getCollectionsListManager();
        this.recordServices = modelLayerFactory.newRecordServices();
        this.metadataSchemasManager = modelLayerFactory.getMetadataSchemasManager();
        this.emailServices = emailServices;
    }

    @Override
    public void initialize() {
        configureBackgroundThread();
    }

    void configureBackgroundThread() {

        Runnable sendEmailsAction = new Runnable() {
            @Override
            public void run() {
                sendEmails();
            }
        };

        backgroundThreadsManager.configure(BackgroundThreadConfiguration
                .repeatingAction("EmailQueueManager", sendEmailsAction)
                .handlingExceptionWith(BackgroundThreadExceptionHandling.CONTINUE)
                .executedEvery(DURATION_BETWEEN_EXECUTION));
    }

    public void sendEmails() {
        //LOGGER.info("EmailQueueManager started");
        List<LogicalSearchQuery> searchConditions = prepareSearchQueries(TimeProvider.getLocalDateTime());
        while(!searchConditions.isEmpty()){
            for (Iterator<LogicalSearchQuery> iterator = searchConditions.iterator(); iterator.hasNext();) {
                LogicalSearchQuery query = iterator.next();
                query.setNumberOfRows(SEND_EMAIL_BATCH);
                List<Record> records = searchServices.search(query);
                if (records.isEmpty()) {
                    iterator.remove();
                }else{
                    try {
                        trySendCollectionEmails(query, records);
                    } catch (EmailServicesException.EmailServerException e) {
                        LocalDateTime tomorrow = TimeProvider.getLocalDateTime().plusDays(1);
                        postponeRemainingEmailToDateTime(query, tomorrow, "server exception");
                        iterator.remove();
                    }
                }
            }
        }
    }

    private void trySendCollectionEmails(LogicalSearchQuery query, List<Record> records) throws EmailServicesException.EmailServerException {
        LocalDateTime tomorrow = TimeProvider.getLocalDateTime().plusDays(1);
        EmailBuilder emailBuilder = new EmailBuilder(modelLayerFactory.getEmailTemplatesManager(), modelLayerFactory.getSystemConfigurationsManager());
        String collection = query.getCondition().getCollection();
        SchemasRecordsServices schemas = new SchemasRecordsServices(collection, modelLayerFactory);
        Metadata numberOfTryMetadata = getNumberOfTryMetadata(collection);
        Metadata errorMetadata = getEmailToSendErrorMetadata(collection);
        Metadata sendDateMetadata = getSendDateMetadata(collection);
        EmailServerConfiguration emailConfiguration = getEmailConfiguration(collection);
        Session session = emailServices.openSession(emailConfiguration);
        String defaultEmail = emailConfiguration.getDefaultSenderEmail();
        List<Record> recordsToUpdate = new ArrayList<>();
        for (Record record : records) {
            try {
                Message email = buildEmail(session, emailBuilder, schemas.wrapEmailToSend(record), defaultEmail);
                emailServices.sendEmail(email);
                deleteEmail(record, "email sent correctly");
            } catch (EmailServicesException.EmailTempException e) {
                LOGGER.warn("Email not sent correctly", e);
                Record updatedRecord = postponeEmailToDateTime(record, tomorrow, e.getMessage(), numberOfTryMetadata, errorMetadata, sendDateMetadata);
                if (updatedRecord != null) {
                    recordsToUpdate.add(updatedRecord);
                }
            } catch (EmailServicesException.EmailPermanentException e) {
                LOGGER.warn("Email not sent correctly", e);
                deleteEmail(record, e.getMessage());
            } catch (Exception e) {
                LOGGER.warn("Email not sent correctly", e);
                Record updatedRecord = postponeEmailToDateTime(record, tomorrow, e.getMessage(), numberOfTryMetadata, errorMetadata, sendDateMetadata);
                if (updatedRecord != null) {
                    recordsToUpdate.add(updatedRecord);
                }
            }
        }
        emailServices.closeSession(session);
        if (!recordsToUpdate.isEmpty()) {
            updateRecords(recordsToUpdate);
        }
    }

    private Message buildEmail(Session session, EmailBuilder emailBuilder, EmailToSend emailToSend, String defaultEmail) throws Exception {
        try {
            return emailBuilder.build(emailToSend, session, defaultEmail);
        } catch (MessagingException e) {
            Exception exception = new EmailServices().throwAppropriateException(e);
            throw exception;
        } catch (EmailBuilder.InvalidBlankEmail invalidBlankEmail) {
            throw new EmailServicesException.EmailPermanentException(invalidBlankEmail);
        }
    }

    private void updateRecords(List<Record> recordsToUpdate) {
        try {
            LOGGER.warn("Update records start");
            recordServices.update(recordsToUpdate, null);
            LOGGER.warn("Update records succeeded");
        } catch (RecordServicesException e) {
            e.printStackTrace();
            LOGGER.warn("Update records failed");
        }
    }

    EmailServerConfiguration getEmailConfiguration(String collection) {
        return emailConfigurationManager.getEmailConfiguration(collection);
    }

    private List<LogicalSearchQuery> prepareSearchQueries(LocalDateTime dateTime) {
        List<LogicalSearchQuery> returnList = new ArrayList<>();
        List<String> collections = this.collectionManager.getCollections();
        for(String collection : collections){
            if(modelLayerFactory.getEmailConfigurationsManager().getEmailConfiguration(collection)!= null){
                returnList.add(prepareRemainingEmailsToSend(collection, dateTime));
            }
        }
        return returnList;
    }

    private LogicalSearchQuery prepareRemainingEmailsToSend(String collection, LocalDateTime dateTime) {
        Metadata sendDateMetadata = getSendDateMetadata(collection);
        MetadataSchema schema = getEmailToSendSchema(collection);

        LogicalSearchCondition condition = from(schema).where(sendDateMetadata).isLessOrEqualThan(dateTime);
        return new LogicalSearchQuery(condition).sortAsc(sendDateMetadata);
    }

    private void postponeRemainingEmailToDateTime(LogicalSearchQuery query, LocalDateTime dateTime, String error) {
        do {
            List<Record> records = searchServices.search(query);
            if(records.isEmpty()){
                break;
            }
            List<Record> recordsToUpdate = new ArrayList<>();
            String collection = query.getCondition().getCollection();
            Metadata numberOfTryMetadata = getNumberOfTryMetadata(collection);
            Metadata errorMetadata = getEmailToSendErrorMetadata(collection);
            Metadata sendDateMetadata = getSendDateMetadata(collection);
            for (Record record : records) {
                Record updatedRecord = postponeEmailToDateTime(record, dateTime, error, numberOfTryMetadata, errorMetadata, sendDateMetadata);
                if(updatedRecord != null){
                    recordsToUpdate.add(updatedRecord);
                }
            }
            updateRecords(recordsToUpdate);
        } while (true);
    }

    private Record postponeEmailToDateTime(Record record, LocalDateTime dateTime, String error, Metadata numberOfTryMetadata, Metadata errorMetadata, Metadata sendDateMetadata) {
        int numberOfTry = ((Double)record.get(numberOfTryMetadata)).intValue();
        if (numberOfTry >= MAX_TRY_SEND) {
            deleteEmail(record, "Exceeded number of try send " + numberOfTry);
            return null;
        } else {
            record.set(numberOfTryMetadata, numberOfTry + 1);
            record.set(errorMetadata, error);
            record.set(sendDateMetadata, dateTime);
            LOGGER.warn("Record (" + record.getId() + ") postponed because of :" + error);
            return record;
        }
    }

    private void deleteEmail(Record record, String error) {
        LOGGER.warn("Record (" + record.getId() + ") deleted because of: " + error);
        recordServices.logicallyDelete(record, null);
        recordServices.physicallyDelete(record, null);
    }

    private Metadata getSendDateMetadata(String collection) {
        return getEmailToSendSchema(collection).getMetadata(EmailToSend.SEND_ON);
    }

    private Metadata getEmailToSendErrorMetadata(String collection) {
        return getEmailToSendSchema(collection).getMetadata(EmailToSend.ERROR);
    }

    private Metadata getNumberOfTryMetadata(String collection) {
        return getEmailToSendSchema(collection).getMetadata(EmailToSend.TRYING_COUNT);
    }

    private MetadataSchema getEmailToSendSchema(String collection) {
        SchemasRecordsServices schemasRecords = new SchemasRecordsServices(collection, modelLayerFactory);
        return schemasRecords.schema(EmailToSend.DEFAULT_SCHEMA);
    }

    @Override
    public void close() {

    }
}
