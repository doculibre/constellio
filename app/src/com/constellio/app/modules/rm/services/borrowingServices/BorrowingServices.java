package com.constellio.app.modules.rm.services.borrowingServices;

import com.constellio.app.modules.rm.RMEmailTemplateConstants;
import com.constellio.app.modules.rm.model.enums.FolderStatus;
import com.constellio.app.modules.rm.navigation.RMNavigationConfiguration;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.services.borrowingServices.BorrowingServicesRunTimeException.*;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.RMTask;
import com.constellio.data.utils.TimeProvider;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.EmailToSend;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.structures.EmailAddress;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.logging.LoggingServices;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.users.UserServices;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;

public class BorrowingServices {

    private static Logger LOGGER = LoggerFactory.getLogger(BorrowingServices.class);
    public static final String RGD = "RGD";
    private final RecordServices recordServices;
    private final UserServices userServices;
    private final LoggingServices loggingServices;
    private final RMSchemasRecordsServices rm;
    ConstellioEIMConfigs eimConfigs;
    MetadataSchemasManager metadataSchemasManager;
    String collection;

    public BorrowingServices(String collection, ModelLayerFactory modelLayerFactory) {
        this.recordServices = modelLayerFactory.newRecordServices();
        this.userServices = modelLayerFactory.newUserServices();
        this.loggingServices = modelLayerFactory.newLoggingServices();
        this.rm = new RMSchemasRecordsServices(collection, modelLayerFactory);
        this.eimConfigs = new ConstellioEIMConfigs(modelLayerFactory.getSystemConfigurationsManager());
        this.metadataSchemasManager = modelLayerFactory.getMetadataSchemasManager();
        this.collection = collection;
    }

    public void borrowRecordsFromTask(String taskId, LocalDate borrowingDate, LocalDate previewReturnDate, User currentUser,
                                      User borrowerEntered, BorrowingType borrowingType)
            throws RecordServicesException {

        Record taskRecord = recordServices.getDocumentById(taskId);
        RMTask task = rm.wrapRMTask(taskRecord);
        String schemaType = "";
        if (task.getLinkedFolders() != null) {
            schemaType = Folder.SCHEMA_TYPE;
            for (String folderId : task.getLinkedFolders()) {
                borrowFolder(folderId, borrowingDate, previewReturnDate, currentUser, borrowerEntered, borrowingType, false);
            }
        }
        if (task.getLinkedDocuments() != null) {
            schemaType = Document.SCHEMA_TYPE;
        }
        //TODO gérer schéma ContainerRecord
        alertUsersWhenBorrowing(schemaType, taskRecord, borrowingDate, previewReturnDate, currentUser, borrowerEntered, borrowingType);
    }

    public void returnRecordsFromTask(String taskId, User currentUser, LocalDate returnDate)
            throws RecordServicesException {

        Record taskRecord = recordServices.getDocumentById(taskId);
        RMTask task = rm.wrapRMTask(taskRecord);
        String schemaType = "";
        if (task.getLinkedFolders() != null) {
            schemaType = Folder.SCHEMA_TYPE;
            for (String folderId : task.getLinkedFolders()) {
                returnFolder(folderId, currentUser, returnDate, false);
            }
        }
        if (task.getLinkedDocuments() != null) {
            schemaType = Document.SCHEMA_TYPE;
        }
        //TODO gérer schéma ContainerRecord
        alertUsersWhenReturning(schemaType, taskRecord, currentUser, returnDate);
    }

    public void borrowFolder(String folderId, LocalDate borrowingDate, LocalDate previewReturnDate, User currentUser,
                             User borrowerEntered, BorrowingType borrowingType, boolean isCreateEvent)
            throws RecordServicesException {

        Record folderRecord = recordServices.getDocumentById(folderId);
        Folder folder = rm.wrapFolder(folderRecord);
        validateCanBorrow(currentUser, folder, borrowingDate);
        setBorrowedMetadatasToFolder(folder, borrowingDate.toDateTimeAtStartOfDay().toLocalDateTime(),
                previewReturnDate,
                currentUser.getId(), borrowerEntered.getId(),
                borrowingType);
        recordServices.update(folder);
        if (borrowingType == BorrowingType.BORROW) {
            loggingServices.borrowRecord(folderRecord, borrowerEntered, borrowingDate.toDateTimeAtStartOfDay().toLocalDateTime());
        } else {
            loggingServices
                    .consultingRecord(folderRecord, borrowerEntered, borrowingDate.toDateTimeAtStartOfDay().toLocalDateTime());
        }
    }

    public void returnFolder(String folderId, User currentUser, LocalDate returnDate, boolean isCreateEvent)
            throws RecordServicesException {

        Record folderRecord = recordServices.getDocumentById(folderId);
        Folder folder = rm.wrapFolder(folderRecord);
        validateCanReturnFolder(currentUser, folder);
        BorrowingType borrowingType = folder.getBorrowType();
        setReturnedMetadatasToFolder(folder);
        recordServices.update(folder);
        if (borrowingType == BorrowingType.BORROW) {
            loggingServices.returnRecord(folderRecord, currentUser, returnDate.toDateTimeAtStartOfDay().toLocalDateTime());
        }
    }

    public void validateCanReturnFolder(User currentUser, Folder folder) {
        if (currentUser.hasReadAccess().on(folder)) {
            if (folder.getBorrowed() == null || !folder.getBorrowed()) {
                throw new BorrowingServicesRunTimeException_FolderIsNotBorrowed(folder.getId());
            } else if (!currentUser.getUserRoles().contains(RGD) && !currentUser.getId()
                    .equals(folder.getBorrowUserEntered())) {
                throw new BorrowingServicesRunTimeException_UserNotAllowedToReturnFolder(currentUser.getUsername());
            }
        } else {
            throw new BorrowingServicesRunTimeException_UserWithoutReadAccessToFolder(currentUser.getUsername(), folder.getId());
        }
    }

    public void validateCanBorrow(User currentUser, Folder folder, LocalDate borrowingDate) {

        if (currentUser.hasReadAccess().on(folder)) {
            if (FolderStatus.ACTIVE == folder.getArchivisticStatus()) {
                throw new BorrowingServicesRunTimeException_CannotBorrowActiveFolder(folder.getId());
            } else if (folder.getBorrowed() != null && folder.getBorrowed()) {
                throw new BorrowingServicesRunTimeException_FolderIsAlreadyBorrowed(folder.getId());
            } else if (borrowingDate != null && borrowingDate.isAfter(TimeProvider.getLocalDate())) {
                throw new BorrowingServicesRunTimeException_InvalidBorrowingDate(borrowingDate);
            }
        } else {
            throw new BorrowingServicesRunTimeException_UserWithoutReadAccessToFolder(currentUser.getUsername(), folder.getId());
        }
    }

    private void setBorrowedMetadatasToFolder(Folder folder, LocalDateTime borrowingDate, LocalDate previewReturnDate,
                                              String userId, String borrowerEnteredId, BorrowingType borrowingType) {
        folder.setBorrowed(true);
        folder.setBorrowDate(borrowingDate != null ? borrowingDate : TimeProvider.getLocalDateTime());
        folder.setBorrowPreviewReturnDate(previewReturnDate);
        folder.setBorrowUser(userId);
        folder.setBorrowUserEntered(borrowerEnteredId);
        folder.setBorrowType(borrowingType);
        folder.setAlertUsersWhenAvailable(new ArrayList<String>());
    }

    private void setReturnedMetadatasToFolder(Folder folder) {
        folder.setBorrowed(null);
        folder.setBorrowDate(null);
        folder.setBorrowPreviewReturnDate(null);
        folder.setBorrowUser(null);
        folder.setBorrowUserEntered(null);
        folder.setBorrowType(null);
    }

    public String validateBorrowingInfos(String userId, LocalDate borrowingDate, LocalDate previewReturnDate,
                                         BorrowingType borrowingType, LocalDate returnDate) {
        String errorMessage = null;
        if (borrowingDate == null) {
            borrowingDate = TimeProvider.getLocalDate();
        } else {
            if (borrowingDate.isAfter(TimeProvider.getLocalDate())) {
                errorMessage = "BorrowingServices.invalidBorrowingDate";
            }
        }
        if (borrowingType == null) {
            errorMessage = "BorrowingServices.invalidBorrowingType";
            return errorMessage;
        }
        if (StringUtils.isBlank(userId) || userId == null) {
            errorMessage = "BorrowingServices.invalidBorrower";
            return errorMessage;
        }
        if (previewReturnDate != null) {
            if (previewReturnDate.isBefore(borrowingDate)) {
                errorMessage = "BorrowingServices.invalidPreviewReturnDate";
                return errorMessage;
            }
        } else {
            errorMessage = "BorrowingServices.invalidPreviewReturnDate";
            return errorMessage;
        }
        if (returnDate != null) {
            return validateReturnDate(returnDate, borrowingDate);
        }
        return errorMessage;
    }

    public String validateReturnDate(LocalDate returnDate, LocalDate borrowingDate) {
        String errorMessage = null;
        if (returnDate == null) {
            errorMessage = "BorrowingServices.invalidReturnDate";
        } else if (borrowingDate != null && (returnDate.isAfter(TimeProvider.getLocalDate()) || returnDate.isBefore(borrowingDate))) {
            errorMessage = "BorrowingServices.invalidReturnDate";
        }
        return errorMessage;
    }

    private void alertUsersWhenBorrowing(String schemaType, Record record, LocalDate borrowingDate, LocalDate previewReturnDate, User currentUser,
                                         User borrowerEntered, BorrowingType borrowingType) {
        try {
            String displayURL = RMNavigationConfiguration.DISPLAY_FOLDER;

            Transaction transaction = new Transaction();

            EmailToSend emailToSend = newEmailToSend();
            EmailAddress toAddress = new EmailAddress(borrowerEntered.getTitle(), borrowerEntered.getEmail());

            LocalDateTime sendDate = TimeProvider.getLocalDateTime();
            emailToSend.setTo(toAddress);
            emailToSend.setSendOn(sendDate);
            final String subject = schemaType.equals("folder") ?
                    $("BorrowingServices.alertWhenFolderBorrowedSubject") + " : " + record.getTitle() :
                    $("BorrowingServices.alertWhenDocumentBorrowedSubject") + " : " + record.getTitle();

            emailToSend.setSubject(subject);
            emailToSend.setTemplate(RMEmailTemplateConstants.ALERT_BORROWED);
            List<String> parameters = new ArrayList<>();
            parameters.add("subject" + EmailToSend.PARAMETER_SEPARATOR + subject);
            parameters.add("borrowingDate" + EmailToSend.PARAMETER_SEPARATOR + formatDateToParameter(borrowingDate));
            parameters.add("returnDate" + EmailToSend.PARAMETER_SEPARATOR + formatDateToParameter(previewReturnDate));
            parameters.add("currentUser" + EmailToSend.PARAMETER_SEPARATOR + currentUser);
            parameters.add("borrowingType" + EmailToSend.PARAMETER_SEPARATOR + borrowingType);
            parameters.add("borrowerEntered" + EmailToSend.PARAMETER_SEPARATOR + borrowerEntered);
            String recordTitle = record.getTitle();
            parameters.add("title" + EmailToSend.PARAMETER_SEPARATOR + recordTitle);
            String constellioUrl = eimConfigs.getConstellioUrl();
            parameters.add("constellioURL" + EmailToSend.PARAMETER_SEPARATOR + constellioUrl);
            parameters.add("recordURL" + EmailToSend.PARAMETER_SEPARATOR + constellioUrl + "#!" + displayURL + "/" + record.getId());
            parameters.add("recordType" + EmailToSend.PARAMETER_SEPARATOR + $("BorrowingServices.classifiedObject." + schemaType).toLowerCase());
            emailToSend.setParameters(parameters);
            transaction.add(emailToSend);

            recordServices.execute(transaction);

        } catch (RecordServicesException e) {
            LOGGER.error("Cannot alert user", e);
        }
    }

    private void alertUsersWhenReturning(String schemaType, Record record, User currentUser, LocalDate returnDate) {
        try {
            String displayURL = RMNavigationConfiguration.DISPLAY_FOLDER;

            Transaction transaction = new Transaction();

            EmailToSend emailToSend = newEmailToSend();
            EmailAddress toAddress = new EmailAddress(currentUser.getTitle(), currentUser.getEmail());

            LocalDateTime sendDate = TimeProvider.getLocalDateTime();
            emailToSend.setTo(toAddress);
            emailToSend.setSendOn(sendDate);
            final String subject = schemaType.equals("folder") ?
                    $("BorrowingServices.alertWhenFolderBorrowedSubject") + " : " + record.getTitle() :
                    $("BorrowingServices.alertWhenDocumentBorrowedSubject") + " : " + record.getTitle();

            emailToSend.setSubject(subject);
            emailToSend.setTemplate(RMEmailTemplateConstants.ALERT_RETURNED);
            List<String> parameters = new ArrayList<>();
            parameters.add("subject" + EmailToSend.PARAMETER_SEPARATOR + subject);
            parameters.add("currentUser" + EmailToSend.PARAMETER_SEPARATOR + currentUser);
            parameters.add("returnDate" + EmailToSend.PARAMETER_SEPARATOR + formatDateToParameter(returnDate));
            String recordTitle = record.getTitle();
            parameters.add("title" + EmailToSend.PARAMETER_SEPARATOR + recordTitle);
            String constellioUrl = eimConfigs.getConstellioUrl();
            parameters.add("constellioURL" + EmailToSend.PARAMETER_SEPARATOR + constellioUrl);
            parameters.add("recordURL" + EmailToSend.PARAMETER_SEPARATOR + constellioUrl + "#!" + displayURL + "/" + record.getId());
            parameters.add("recordType" + EmailToSend.PARAMETER_SEPARATOR + $("BorrowingServices.classifiedObject." + schemaType).toLowerCase());
            emailToSend.setParameters(parameters);
            transaction.add(emailToSend);

            recordServices.execute(transaction);

        } catch (RecordServicesException e) {
            LOGGER.error("Cannot alert user", e);
        }
    }

    private String formatDateToParameter(LocalDate date) {
        if (date == null) {
            return "";
        }
        return date.toString("yyyy-MM-dd");
    }

    private EmailToSend newEmailToSend() {
        MetadataSchemaTypes types = metadataSchemasManager.getSchemaTypes(collection);
        MetadataSchema schema = types.getSchemaType(EmailToSend.SCHEMA_TYPE).getDefaultSchema();
        Record emailToSendRecord = recordServices.newRecordWithSchema(schema);
        return new EmailToSend(emailToSendRecord, types);
    }
}