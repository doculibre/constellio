package com.constellio.app.modules.rm.services.borrowingServices;

import com.constellio.app.modules.rm.RMEmailTemplateConstants;
import com.constellio.app.modules.rm.model.enums.FolderStatus;
import com.constellio.app.modules.rm.navigation.RMNavigationConfiguration;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.services.borrowingServices.BorrowingServicesRunTimeException.*;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.RMTask;
import com.constellio.data.utils.TimeProvider;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.EmailToSend;
import com.constellio.model.entities.records.wrappers.EventType;
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
    public static final String SEPARATOR = " : ";
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

    public void borrowRecordsFromTask(String taskId, LocalDate borrowingDate, LocalDate returnDate, User currentUser,
                                      User borrowerEntered, BorrowingType borrowingType)
            throws RecordServicesException {

        String schemaType = "";
		Record taskRecord = recordServices.getDocumentById(taskId);
		RMTask task = rm.wrapRMTask(taskRecord);
		if(task.getLinkedFolders() != null) {
            schemaType = Folder.SCHEMA_TYPE;
			Transaction t = new Transaction();
			for(String folderId: task.getLinkedFolders()) {
				borrowFolder(folderId, borrowingDate, returnDate, currentUser, borrowerEntered, borrowingType, false);
				Record event = rm.newEvent()
						.setUsername(currentUser.getUsername())
						.setTask(taskId)
						.setType(EventType.BORROW_FOLDER)
						.setIp(currentUser.getLastIPAddress())
						.setCreatedOn(TimeProvider.getLocalDateTime())
						.getWrappedRecord();
				t.add(event);
			}
			recordServices.execute(t);
		}
        if (task.getLinkedContainers() != null) {
			schemaType = ContainerRecord.SCHEMA_TYPE;
			Transaction t = new Transaction();
			for(String containerId: task.getLinkedContainers()) {
				borrowContainer(containerId, borrowingDate, returnDate, currentUser, borrowerEntered, borrowingType, false);
				Record event = rm.newEvent()
						.setUsername(currentUser.getUsername())
						.setTask(taskId)
						.setType(EventType.BORROW_FOLDER)
						.setIp(currentUser.getLastIPAddress())
						.setCreatedOn(TimeProvider.getLocalDateTime())
						.getWrappedRecord();
				t.add(event);
			}
			recordServices.execute(t);
        }
        alertUsers(RMEmailTemplateConstants.ALERT_BORROWED, schemaType, taskRecord, borrowingDate, returnDate, null, currentUser, borrowerEntered, borrowingType);
	}

    public void returnRecordsFromTask(String taskId, LocalDate returnDate, User currentUser)
            throws RecordServicesException {

		Record taskRecord = recordServices.getDocumentById(taskId);
		RMTask task = rm.wrapRMTask(taskRecord);
        String schemaType = "";
		if(task.getLinkedFolders() != null) {
            schemaType = Folder.SCHEMA_TYPE;
			Transaction t = new Transaction();
			for(String folderId: task.getLinkedFolders()) {
				returnFolder(folderId, currentUser, returnDate, false);
				Record event = rm.newEvent()
						.setUsername(currentUser.getUsername())
						.setTask(taskId)
						.setType(EventType.RETURN_FOLDER)
						.setIp(currentUser.getLastIPAddress())
						.setCreatedOn(TimeProvider.getLocalDateTime())
						.getWrappedRecord();
				t.add(event);
			}
			recordServices.execute(t);
		}
		if (task.getLinkedContainers() != null) {
			schemaType = ContainerRecord.SCHEMA_TYPE;
			Transaction t = new Transaction();
			for(String containerId: task.getLinkedContainers()) {
				returnContainer(containerId, currentUser, returnDate, false);
				Record event = rm.newEvent()
						.setUsername(currentUser.getUsername())
						.setTask(taskId)
						.setType(EventType.BORROW_FOLDER)
						.setIp(currentUser.getLastIPAddress())
						.setCreatedOn(TimeProvider.getLocalDateTime())
						.getWrappedRecord();
				t.add(event);
			}
			recordServices.execute(t);
		}
        alertUsers(RMEmailTemplateConstants.ALERT_RETURNED, schemaType, taskRecord, null, returnDate, null, currentUser, null, null);
	}

    public void reactivateRecordsFromTask(String taskId, LocalDate reactivationDate, User currentUser)
            throws RecordServicesException {

        Record taskRecord = recordServices.getDocumentById(taskId);
        RMTask task = rm.wrapRMTask(taskRecord);
        String schemaType = "";
        if (task.getLinkedFolders() != null) {
            schemaType = Folder.SCHEMA_TYPE;
        }
        if (task.getLinkedDocuments() != null) {
            schemaType = Document.SCHEMA_TYPE;
        }
        alertUsers(RMEmailTemplateConstants.ALERT_REACTIVATED, schemaType, taskRecord, null, null, reactivationDate, currentUser, null, null);
    }

    public void extendRecordsBorrowingPeriodFromTask(String taskId, LocalDate borrowingDate, LocalDate returnDate, User currentUser,
                                                     User borrowerEntered, BorrowingType borrowingType)
            throws RecordServicesException {
        Record taskRecord = recordServices.getDocumentById(taskId);
        RMTask task = rm.wrapRMTask(taskRecord);
        String schemaType = "";
        if (task.getLinkedFolders() != null) {
            schemaType = Folder.SCHEMA_TYPE;
        }
        if (task.getLinkedDocuments() != null) {
            schemaType = Document.SCHEMA_TYPE;
        }
        alertUsers(RMEmailTemplateConstants.ALERT_BORROWING_EXTENTED, schemaType, taskRecord, borrowingDate, returnDate, null, currentUser, borrowerEntered, borrowingType);
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

	public void borrowContainer(String containerId, LocalDate borrowingDate, LocalDate previewReturnDate, User currentUser,
							 User borrowerEntered, BorrowingType borrowingType, boolean isCreateEvent)
			throws RecordServicesException {

		Record record = recordServices.getDocumentById(containerId);
		ContainerRecord containerRecord = rm.wrapContainerRecord(record);
		validateCanBorrow(currentUser, containerRecord, borrowingDate);
		setBorrowedMetadatasToContainer(containerRecord, borrowingDate.toDateTimeAtStartOfDay().toLocalDateTime(),
				previewReturnDate,
				currentUser.getId());
		recordServices.update(containerRecord);
		if (borrowingType == BorrowingType.BORROW) {
			loggingServices.borrowRecord(record, borrowerEntered, borrowingDate.toDateTimeAtStartOfDay().toLocalDateTime());
		} else {
			loggingServices
					.consultingRecord(record, borrowerEntered, borrowingDate.toDateTimeAtStartOfDay().toLocalDateTime());
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

	public void returnContainer(String containerId, User currentUser, LocalDate returnDate, boolean isCreateEvent)
			throws RecordServicesException {

		Record record = recordServices.getDocumentById(containerId);
		ContainerRecord containerRecord = rm.wrapContainerRecord(record);
		validateCanReturnContainer(currentUser, containerRecord);
		setReturnedMetadatasToContainer(containerRecord);
		recordServices.update(containerRecord);
		loggingServices.returnRecord(record, currentUser, returnDate.toDateTimeAtStartOfDay().toLocalDateTime());
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

	public void validateCanReturnContainer(User currentUser, ContainerRecord containerRecord) {
		if (currentUser.hasReadAccess().on(containerRecord)) {
			if (containerRecord.getBorrowed() == null || !containerRecord.getBorrowed()) {
				throw new BorrowingServicesRunTimeException_ContainerIsNotBorrowed(containerRecord.getId());
			} else if (!currentUser.getUserRoles().contains(RGD) && !currentUser.getId()
					.equals(containerRecord.getBorrower())) {
				throw new BorrowingServicesRunTimeException_UserNotAllowedToReturnContainer(currentUser.getUsername());
			}
		} else {
			throw new BorrowingServicesRunTimeException_UserWithoutReadAccessToContainer(currentUser.getUsername(), containerRecord.getId());
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

	public void validateCanBorrow(User currentUser, ContainerRecord containerRecord, LocalDate borrowingDate) {

		if (currentUser.hasReadAccess().on(containerRecord)) {
			if (containerRecord.getBorrowed() != null && containerRecord.getBorrowed()) {
				throw new BorrowingServicesRunTimeException_ContainerIsAlreadyBorrowed(containerRecord.getId());
			} else if (borrowingDate != null && borrowingDate.isAfter(TimeProvider.getLocalDate())) {
				throw new BorrowingServicesRunTimeException_InvalidBorrowingDate(borrowingDate);
			}
		} else {
			throw new BorrowingServicesRunTimeException_UserWithoutReadAccessToContainer(currentUser.getUsername(), containerRecord.getId());
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

	private void setBorrowedMetadatasToContainer(ContainerRecord containerRecord, LocalDateTime borrowingDate, LocalDate previewReturnDate,
											  String userId) {
		containerRecord.setBorrowed(true);
		containerRecord.setBorrowDate(borrowingDate != null ? borrowingDate.toLocalDate() : TimeProvider.getLocalDate());
		containerRecord.setPlanifiedReturnDate(previewReturnDate);
		containerRecord.setBorrower(userId);
	}

	private void setReturnedMetadatasToFolder(Folder folder) {
		folder.setBorrowed(null);
		folder.setBorrowDate(null);
		folder.setBorrowPreviewReturnDate(null);
		folder.setBorrowUser(null);
		folder.setBorrowUserEntered(null);
		folder.setBorrowType(null);
	}

	private void setReturnedMetadatasToContainer(ContainerRecord containerRecord) {
		containerRecord.setBorrowed(null);
		containerRecord.setBorrowDate(null);
		containerRecord.setPlanifiedReturnDate(null);
		containerRecord.setBorrower((String) null);
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

    private void alertUsers(String template, String schemaType, Record record, LocalDate borrowingDate, LocalDate returnDate, LocalDate reactivationDate, User currentUser,
                            User borrowerEntered, BorrowingType borrowingType) {

        try {
            String displayURL = schemaType.equals("folder") ? RMNavigationConfiguration.DISPLAY_FOLDER : RMNavigationConfiguration.DISPLAY_DOCUMENT;
            String subject = "";
            List<String> parameters = new ArrayList<>();
            Transaction transaction = new Transaction();
            EmailToSend emailToSend = newEmailToSend();
            EmailAddress toAddress = new EmailAddress();

            if (template.equals(RMEmailTemplateConstants.ALERT_BORROWED)) {
                toAddress = new EmailAddress(borrowerEntered.getTitle(), borrowerEntered.getEmail());
                subject = schemaType.equals("folder") ?
                        $("BorrowingServices.alertWhenFolderBorrowedSubject") + SEPARATOR + record.getTitle() :
                        $("BorrowingServices.alertWhenDocumentBorrowedSubject") + SEPARATOR + record.getTitle();
                parameters.add("borrowingType" + EmailToSend.PARAMETER_SEPARATOR + borrowingType);
                parameters.add("borrowerEntered" + EmailToSend.PARAMETER_SEPARATOR + borrowerEntered);
                parameters.add("borrowingDate" + EmailToSend.PARAMETER_SEPARATOR + formatDateToParameter(borrowingDate));
                parameters.add("returnDate" + EmailToSend.PARAMETER_SEPARATOR + formatDateToParameter(returnDate));
            }
            if (template.equals(RMEmailTemplateConstants.ALERT_REACTIVATED)) {
                toAddress = new EmailAddress(currentUser.getTitle(), currentUser.getEmail());
                subject = schemaType.equals("folder") ?
                        $("BorrowingServices.alertWhenFolderReactivatedSubject") + SEPARATOR + record.getTitle() :
                        $("BorrowingServices.alertWhenDocumentReactivatedSubject") + SEPARATOR + record.getTitle();
                parameters.add("reactivationDate" + EmailToSend.PARAMETER_SEPARATOR + formatDateToParameter(reactivationDate));
            }
            if (template.equals(RMEmailTemplateConstants.ALERT_RETURNED)) {
                toAddress = new EmailAddress(currentUser.getTitle(), currentUser.getEmail());
                subject = schemaType.equals("folder") ?
                        $("BorrowingServices.alertWhenFolderReturnedSubject") + SEPARATOR + record.getTitle() :
                        $("BorrowingServices.alertWhenDocumentReturnedSubject") + SEPARATOR + record.getTitle();
                parameters.add("returnDate" + EmailToSend.PARAMETER_SEPARATOR + formatDateToParameter(returnDate));
            }
            if (template.equals(RMEmailTemplateConstants.ALERT_BORROWING_EXTENTED)) {
                toAddress = new EmailAddress(borrowerEntered.getTitle(), borrowerEntered.getEmail());
                subject = schemaType.equals("folder") ?
                        $("BorrowingServices.alertWhenFolderBorrowingExtendedSubject") + SEPARATOR + record.getTitle() :
                        $("BorrowingServices.alertWhenDocumentBorrowingExtendedSubject") + SEPARATOR + record.getTitle();
                parameters.add("extensionDate" + EmailToSend.PARAMETER_SEPARATOR + formatDateToParameter(borrowingDate));
                parameters.add("returnDate" + EmailToSend.PARAMETER_SEPARATOR + formatDateToParameter(returnDate));
            }

            LocalDateTime sendDate = TimeProvider.getLocalDateTime();
            emailToSend.setTo(toAddress);
            emailToSend.setSendOn(sendDate);
            emailToSend.setSubject(subject);
            emailToSend.setTemplate(template);
            parameters.add("subject" + EmailToSend.PARAMETER_SEPARATOR + subject);
            String recordTitle = record.getTitle();
            parameters.add("title" + EmailToSend.PARAMETER_SEPARATOR + recordTitle);
            parameters.add("currentUser" + EmailToSend.PARAMETER_SEPARATOR + currentUser);
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