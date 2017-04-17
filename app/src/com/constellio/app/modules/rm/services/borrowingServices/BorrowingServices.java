package com.constellio.app.modules.rm.services.borrowingServices;

import com.constellio.app.modules.rm.RMEmailTemplateConstants;
import com.constellio.app.modules.rm.model.enums.FolderStatus;
import com.constellio.app.modules.rm.navigation.RMNavigationConfiguration;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.services.borrowingServices.BorrowingServicesRunTimeException.*;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
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

    public void borrowRecordsFromTask(String taskId, LocalDate borrowingDate, LocalDate returnDate, User respondant,
									  User applicant, BorrowingType borrowingType, boolean isAccepted)
            throws RecordServicesException {

        String schemaType = "";
		Record taskRecord = recordServices.getDocumentById(taskId);
		RMTask task = rm.wrapRMTask(taskRecord);
		if(task.getLinkedFolders() != null) {
            schemaType = Folder.SCHEMA_TYPE;
			Transaction t = new Transaction();
			for(String folderId: task.getLinkedFolders()) {
				if(isAccepted) {
					borrowFolder(folderId, borrowingDate, returnDate, applicant, applicant, borrowingType, false);
				}
				loggingServices.completeBorrowRequestTask(recordServices.getDocumentById(folderId), task.getId(), isAccepted, applicant, respondant, task.getReason(), returnDate.toString());
				Folder folder = rm.getFolder(folderId);
				Record event = rm.newEvent()
						.setUsername(applicant.getUsername())
						.setRecordId(folderId)
						.setTitle(folder.getTitle())
						.setReceiver(respondant)
						.setReason(task.getReason())
						.setTask(taskId)
						.setType(EventType.BORROW_FOLDER)
						.setIp(applicant.getLastIPAddress())
						.setCreatedOn(TimeProvider.getLocalDateTime())
						.getWrappedRecord();
				t.add(event);
				alertUsers(RMEmailTemplateConstants.ALERT_BORROWED, schemaType, taskRecord, folder.getWrappedRecord(),
						borrowingDate, returnDate, null, respondant, applicant, borrowingType, isAccepted);
			}
			recordServices.execute(t);
		}
        if (task.getLinkedContainers() != null) {
			schemaType = ContainerRecord.SCHEMA_TYPE;
			Transaction t = new Transaction();
			for(String containerId: task.getLinkedContainers()) {
				if(isAccepted) {
					borrowContainer(containerId, borrowingDate, returnDate, applicant, applicant, borrowingType, false);
				}
				loggingServices.completeBorrowRequestTask(recordServices.getDocumentById(containerId), task.getId(), isAccepted, applicant, respondant, task.getReason(), returnDate.toString());
				ContainerRecord containerRecord = rm.getContainerRecord(containerId);
				Record event = rm.newEvent()
						.setUsername(applicant.getUsername())
						.setRecordId(containerId)
						.setTitle(containerRecord.getTitle())
						.setReceiver(respondant)
						.setReason(task.getReason())
						.setTask(taskId)
						.setType(EventType.BORROW_CONTAINER)
						.setIp(applicant.getLastIPAddress())
						.setCreatedOn(TimeProvider.getLocalDateTime())
						.getWrappedRecord();
				t.add(event);
				alertUsers(RMEmailTemplateConstants.ALERT_BORROWED, schemaType, taskRecord, containerRecord.getWrappedRecord(),
						borrowingDate, returnDate, null, respondant, applicant, borrowingType, isAccepted);
			}
			recordServices.execute(t);
        }
	}

    public void returnRecordsFromTask(String taskId, LocalDate returnDate, User respondant, User applicant, boolean isAccepted)
            throws RecordServicesException {

		Record taskRecord = recordServices.getDocumentById(taskId);
		RMTask task = rm.wrapRMTask(taskRecord);
        String schemaType = "";
		if(task.getLinkedFolders() != null) {
            schemaType = Folder.SCHEMA_TYPE;
			Transaction t = new Transaction();
			for(String folderId: task.getLinkedFolders()) {
				if(isAccepted) {
					returnFolder(folderId, applicant, returnDate, false);
				}
				loggingServices.completeReturnRequestTask(recordServices.getDocumentById(folderId), task.getId(), isAccepted, applicant, respondant, task.getReason());
				Folder folder = rm.getFolder(folderId);
				Record event = rm.newEvent()
						.setUsername(applicant.getUsername())
						.setRecordId(folderId)
						.setTitle(folder.getTitle())
						.setReceiver(respondant)
						.setReason(task.getReason())
						.setTask(taskId)
						.setType(EventType.RETURN_FOLDER)
						.setIp(applicant.getLastIPAddress())
						.setCreatedOn(TimeProvider.getLocalDateTime())
						.getWrappedRecord();
				t.add(event);
				alertUsers(RMEmailTemplateConstants.ALERT_RETURNED, schemaType, taskRecord, folder.getWrappedRecord(), null,
						returnDate, null, respondant, applicant, null, isAccepted);
			}
			recordServices.execute(t);
		}
		if (task.getLinkedContainers() != null) {
			schemaType = ContainerRecord.SCHEMA_TYPE;
			Transaction t = new Transaction();
			for(String containerId: task.getLinkedContainers()) {
				if(isAccepted) {
					returnContainer(containerId, applicant, returnDate, false);
				}
				loggingServices.completeReturnRequestTask(recordServices.getDocumentById(containerId), task.getId(), isAccepted, applicant, respondant, task.getReason());
				ContainerRecord containerRecord = rm.getContainerRecord(containerId);
				Record event = rm.newEvent()
						.setUsername(applicant.getUsername())
						.setRecordId(containerId)
						.setTitle(containerRecord.getTitle())
						.setReceiver(respondant)
						.setReason(task.getReason())
						.setTask(taskId)
						.setType(EventType.RETURN_CONTAINER)
						.setIp(applicant.getLastIPAddress())
						.setCreatedOn(TimeProvider.getLocalDateTime())
						.getWrappedRecord();
				t.add(event);
				alertUsers(RMEmailTemplateConstants.ALERT_RETURNED, schemaType, taskRecord, containerRecord.getWrappedRecord(),
						null, returnDate, null, respondant, applicant, null, isAccepted);
			}
			recordServices.execute(t);
		}
	}

    public void extendRecordsBorrowingPeriodFromTask(String taskId, LocalDate returnDate, User respondant, User applicant, boolean isAccepted)
            throws RecordServicesException {
        Record taskRecord = recordServices.getDocumentById(taskId);
        RMTask task = rm.wrapRMTask(taskRecord);
        String schemaType = "";
        if (task.getLinkedFolders() != null) {
            schemaType = Folder.SCHEMA_TYPE;
			Transaction t = new Transaction();
			for(String folderId: task.getLinkedFolders()) {
				if(isAccepted) {
					extendBorrowDateForFolder(folderId, returnDate, applicant, false);
				}
				Record record = recordServices.getDocumentById(folderId);
				loggingServices.completeBorrowExtensionRequestTask(record, task.getId(), isAccepted, applicant, respondant, task.getReason(), returnDate.toString());
				alertUsers(RMEmailTemplateConstants.ALERT_BORROWING_EXTENTED, schemaType, taskRecord, record, null, returnDate,
						null, respondant, applicant, null, isAccepted);
			}
			recordServices.execute(t);
        }
        if (task.getLinkedContainers() != null) {
            schemaType = ContainerRecord.SCHEMA_TYPE;
			Transaction t = new Transaction();
			for(String containerId: task.getLinkedContainers()) {
				if(isAccepted) {
					extendBorrowDateForContainer(containerId, returnDate, applicant, false);
				}
				Record record = recordServices.getDocumentById(containerId);
				loggingServices.completeBorrowExtensionRequestTask(record, task.getId(), isAccepted, applicant, respondant, task.getReason(), returnDate.toString());
				alertUsers(RMEmailTemplateConstants.ALERT_BORROWING_EXTENTED, schemaType, taskRecord, record, null, returnDate,
						null, respondant, applicant, null, isAccepted);
			}
			recordServices.execute(t);
        }
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

		if(isCreateEvent) {
			if (borrowingType == BorrowingType.BORROW) {
				loggingServices.borrowRecord(folderRecord, borrowerEntered, borrowingDate.toDateTimeAtStartOfDay().toLocalDateTime());
			} else {
				loggingServices
						.consultingRecord(folderRecord, borrowerEntered, borrowingDate.toDateTimeAtStartOfDay().toLocalDateTime());
			}
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
		if(isCreateEvent) {
			if (borrowingType == BorrowingType.BORROW) {
				loggingServices.borrowRecord(record, borrowerEntered, borrowingDate.toDateTimeAtStartOfDay().toLocalDateTime());
			} else {
				loggingServices
						.consultingRecord(record, borrowerEntered, borrowingDate.toDateTimeAtStartOfDay().toLocalDateTime());
			}
		}
	}

	public void extendBorrowDateForFolder(String folderId, LocalDate previewReturnDate, User currentUser, boolean isCreateEvent)
			throws RecordServicesException {

		Record folderRecord = recordServices.getDocumentById(folderId);
		Folder folder = rm.wrapFolder(folderRecord);
		boolean equals = Boolean.TRUE.equals(folder.getBorrowed());
		boolean equals1 = folder.getBorrowUser().equals(currentUser.getId());
		if(equals && equals1) {
			recordServices.update(folder.setBorrowPreviewReturnDate(previewReturnDate));
		}
	}

	public void extendBorrowDateForContainer(String containerId, LocalDate previewReturnDate, User currentUser, boolean isCreateEvent)
			throws RecordServicesException {

		Record record = recordServices.getDocumentById(containerId);
		ContainerRecord containerRecord = rm.wrapContainerRecord(record);
		if(Boolean.TRUE.equals(containerRecord.getBorrowed()) && containerRecord.getBorrower().equals(currentUser.getId())) {
			recordServices.update(containerRecord.setPlanifiedReturnDate(previewReturnDate));
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

		if(isCreateEvent) {
			if (borrowingType == BorrowingType.BORROW) {
				loggingServices.returnRecord(folderRecord, currentUser, returnDate.toDateTimeAtStartOfDay().toLocalDateTime());
			}
		}
	}

	public void returnContainer(String containerId, User currentUser, LocalDate returnDate, boolean isCreateEvent)
			throws RecordServicesException {

		Record record = recordServices.getDocumentById(containerId);
		ContainerRecord containerRecord = rm.wrapContainerRecord(record);
		validateCanReturnContainer(currentUser, containerRecord);
		setReturnedMetadatasToContainer(containerRecord);
		recordServices.update(containerRecord);

		if(isCreateEvent) {
			loggingServices.returnRecord(record, currentUser, returnDate.toDateTimeAtStartOfDay().toLocalDateTime());
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
			} else if(isInDecommissioningList(folder)) {
				throw new BorrowingServicesRunTimeException_FolderIsInDecommissioningList(folder.getId());
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

	private boolean isInDecommissioningList(Folder folder) {
		boolean isInDecommissioningList = searchServices.getResultsCount(LogicalSearchQueryOperators.from(rm.decommissioningList.schemaType())
				.where(rm.decommissioningList.folders()).isEqualTo(folder.getId()).andWhere(rm.decommissioningList.processingDate()).isNull()) > 0;
		return isInDecommissioningList;
	}

    private void alertUsers(String template, String schemaType, Record task, Record record, LocalDate borrowingDate, LocalDate returnDate, LocalDate reactivationDate, User currentUser,
                            User borrowerEntered, BorrowingType borrowingType, boolean isAccepted) {

        try {
            String displayURL = schemaType.equals(Folder.SCHEMA_TYPE) ? RMNavigationConfiguration.DISPLAY_FOLDER : RMNavigationConfiguration.DISPLAY_CONTAINER;
            String subject = "";
            List<String> parameters = new ArrayList<>();
            Transaction transaction = new Transaction();
            EmailToSend emailToSend = newEmailToSend();
            EmailAddress toAddress = new EmailAddress();
			subject = task.getTitle();

            if (template.equals(RMEmailTemplateConstants.ALERT_BORROWED)) {
                toAddress = new EmailAddress(borrowerEntered.getTitle(), borrowerEntered.getEmail());
                parameters.add("borrowingType" + EmailToSend.PARAMETER_SEPARATOR + borrowingType);
                parameters.add("borrowerEntered" + EmailToSend.PARAMETER_SEPARATOR + borrowerEntered);
                parameters.add("borrowingDate" + EmailToSend.PARAMETER_SEPARATOR + formatDateToParameter(borrowingDate));
                parameters.add("returnDate" + EmailToSend.PARAMETER_SEPARATOR + formatDateToParameter(returnDate));
            } else if (template.equals(RMEmailTemplateConstants.ALERT_REACTIVATED)) {
                toAddress = new EmailAddress(borrowerEntered.getTitle(), borrowerEntered.getEmail());
                parameters.add("reactivationDate" + EmailToSend.PARAMETER_SEPARATOR + formatDateToParameter(reactivationDate));
            } else if (template.equals(RMEmailTemplateConstants.ALERT_RETURNED)) {
                toAddress = new EmailAddress(borrowerEntered.getTitle(), borrowerEntered.getEmail());
                parameters.add("returnDate" + EmailToSend.PARAMETER_SEPARATOR + formatDateToParameter(returnDate));
            } else if (template.equals(RMEmailTemplateConstants.ALERT_BORROWING_EXTENTED)) {
                toAddress = new EmailAddress(borrowerEntered.getTitle(), borrowerEntered.getEmail());
                parameters.add("extensionDate" + EmailToSend.PARAMETER_SEPARATOR + formatDateToParameter(LocalDate.now()));
                parameters.add("returnDate" + EmailToSend.PARAMETER_SEPARATOR + formatDateToParameter(returnDate));
            }

            LocalDateTime sendDate = TimeProvider.getLocalDateTime();
            emailToSend.setTo(toAddress);
            emailToSend.setSendOn(sendDate);
            emailToSend.setSubject(subject);
			String fullTemplate = isAccepted? template+RMEmailTemplateConstants.ACCEPTED: template+RMEmailTemplateConstants.DENIED;
            emailToSend.setTemplate(fullTemplate);
            parameters.add("subject" + EmailToSend.PARAMETER_SEPARATOR + subject);
            String recordTitle = record.getTitle();
            parameters.add("title" + EmailToSend.PARAMETER_SEPARATOR + recordTitle);
            parameters.add("currentUser" + EmailToSend.PARAMETER_SEPARATOR + currentUser);
            String constellioUrl = eimConfigs.getConstellioUrl();
            parameters.add("constellioURL" + EmailToSend.PARAMETER_SEPARATOR + constellioUrl);
            parameters.add("recordURL" + EmailToSend.PARAMETER_SEPARATOR + constellioUrl + "#!" + displayURL + "/" + record.getId());
            parameters.add("recordType" + EmailToSend.PARAMETER_SEPARATOR + $(schemaType).toLowerCase());
			parameters.add("isAccepted" + EmailToSend.PARAMETER_SEPARATOR + $(String.valueOf(isAccepted)));
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