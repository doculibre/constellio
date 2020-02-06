package com.constellio.model.services.logging;

import com.constellio.data.dao.dto.records.RecordsFlushing;
import com.constellio.data.utils.ImpossibleRuntimeException;
import com.constellio.data.utils.TimeProvider;
import com.constellio.data.utils.dev.Toggle;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.RecordUpdateOptions;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.Authorization;
import com.constellio.model.entities.records.wrappers.Event;
import com.constellio.model.entities.records.wrappers.EventType;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.schemas.SchemaUtils;
import com.constellio.model.services.search.SearchServices;
import org.joda.time.LocalDateTime;

import static com.constellio.model.entities.records.wrappers.EventType.MODIFY_PERMISSION;

public class LoggingServices {

	EventFactory eventFactory;

	ModelLayerFactory modelLayerFactory;

	RecordServices recordServices;

	MetadataSchemasManager metadataSchemasManager;

	SearchServices searchServices;

	public LoggingServices(ModelLayerFactory modelLayerFactory) {
		this.modelLayerFactory = modelLayerFactory;
		this.recordServices = modelLayerFactory.newRecordServices();
		this.searchServices = modelLayerFactory.newSearchServices();
		this.metadataSchemasManager = modelLayerFactory.getMetadataSchemasManager();
		this.eventFactory = new EventFactory(modelLayerFactory);
	}

	public void logBorrowRecord(Record record, User currentUser) {
		executeTransaction(eventFactory.newRecordEvent(record, currentUser, EventType.BORROW));
	}

	public void logReturnRecord(Record record, User currentUser) {
		executeTransaction(eventFactory.newRecordEvent(record, currentUser, EventType.RETURN));
	}

	public void logTransaction(Transaction transaction) {
		if (transaction.getUser() == null) {
			return;
		}
		RecordUpdateOptions recordUpdateOptions = transaction.getRecordUpdateOptions();
		if(recordUpdateOptions != null && !recordUpdateOptions.isOverwriteModificationDateAndUser()) {
			return;
		}

		Transaction eventsTransaction = new Transaction().setRecordFlushing(RecordsFlushing.WITHIN_SECONDS(30));
		for (Record record : transaction.getRecords()) {
				Event event = eventFactory.logAddUpdateRecord(record, transaction.getUser());
				if (event != null) {
					eventsTransaction.addUpdate(event.getWrappedRecord());
				}
		}

		try {
			recordServices.execute(eventsTransaction);
		} catch (RecordServicesException e) {
			throw new ImpossibleRuntimeException("Only adding records, so this exception is impossible");
		}
	}

	public void grantPermission(Authorization authorization, User user) {
		Event event = eventFactory
				.eventPermission(authorization, null, user, authorization.getTarget(), EventType.GRANT_PERMISSION);
		executeTransaction(event);
	}

	public void modifyPermission(Authorization authorization, Authorization authorizationBefore, Record record,
								 User user) {
		String recordId = record == null ? null : record.getId();
		Event event = eventFactory.eventPermission(authorization, authorizationBefore, user, recordId, MODIFY_PERMISSION);
		executeTransaction(event);
	}

	public void deletePermission(Authorization authorization, User user) {
		Event event = eventFactory.eventPermission(authorization, null, user, null, EventType.DELETE_PERMISSION);
		executeTransaction(event);
	}

	public void logRecordView(Record record, User currentUser) {
		executeTransaction(eventFactory.newRecordEvent(record, currentUser, EventType.VIEW));
	}

	public void logRecordView(Record record, User currentUser, LocalDateTime dateTime) {
		executeTransaction(eventFactory.newRecordEvent(record, currentUser, EventType.VIEW, null, dateTime));
	}

	public void borrowExtensionOnRecord(Record record, User currentUser, LocalDateTime dateTime,
										LocalDateTime oldReturnDate,
										LocalDateTime newReturnDate) {
		executeTransaction(eventFactory.newRecordEvent(record, currentUser, EventType.BORROWING_TIME_EXTENSIONS, null, dateTime)
				.setDelta(eventFactory.getBorrowDurationDelta(oldReturnDate, newReturnDate)));
	}

	public void reactivateRecord(Record record, User currentUser, LocalDateTime dateTime) {
		executeTransaction(eventFactory.newRecordEvent(record, currentUser, EventType.REACTIVATING_FOLDER, null, dateTime));
	}

	public void borrowRecord(Record record, User currentUser, LocalDateTime dateTime) {
		executeTransaction(eventFactory.newRecordEvent(record, currentUser, EventType.BORROW, null, dateTime));
	}

	public void borrowRecord(Record record, User currentUser) {
		executeTransaction(
				eventFactory.newRecordEvent(record, currentUser, EventType.BORROW, null, TimeProvider.getLocalDateTime()));
	}

	public void openDocument(Record record, User currentUser) {
		executeTransaction(
				eventFactory.newRecordEvent(record, currentUser, EventType.OPEN, null, TimeProvider.getLocalDateTime()));
	}

	public void downloadDocument(Record record, User currentUser) {
		executeTransaction(
				eventFactory.newRecordEvent(record, currentUser, EventType.DOWNLOAD, null, TimeProvider.getLocalDateTime()));
	}

	public void uploadDocument(Record record, User currentUser) {
		executeTransaction(
				eventFactory.newRecordEvent(record, currentUser, EventType.UPLOAD, null, TimeProvider.getLocalDateTime()));
	}

	public void shareDocument(Record record, User currentUser) {
		executeTransaction(
				eventFactory.newRecordEvent(record, currentUser, EventType.SHARE, null, TimeProvider.getLocalDateTime()));
	}

	public void finalizeDocument(Record record, User currentUser) {
		executeTransaction(
				eventFactory.newRecordEvent(record, currentUser, EventType.FINALIZE, null, TimeProvider.getLocalDateTime()));
	}

	public void consultingRecord(Record record, User currentUser, LocalDateTime dateTime) {
		executeTransaction(eventFactory.newRecordEvent(record, currentUser, EventType.CONSULTATION, null, dateTime));
	}

	public void returnRecord(Record record, User currentUser, LocalDateTime returnDateTime) {
		executeTransaction(eventFactory.newRecordEvent(record, currentUser, EventType.RETURN, null, returnDateTime));
	}

	public void returnRecord(Record record, User currentUser) {
		executeTransaction(
				eventFactory.newRecordEvent(record, currentUser, EventType.RETURN, null, TimeProvider.getLocalDateTime()));
	}

	public void login(User user) {
		executeTransaction(eventFactory.newLoginEvent(user));
	}

	public void failingLogin(String attemptedUsername, String ip) {
		executeTransaction(eventFactory.newFailedLoginEvent(attemptedUsername, ip));
	}

	public void logout(User user) {
		executeTransaction(eventFactory.newLogoutEvent(user));
	}

	public void addUser(Record record, User user) {
		executeTransaction(eventFactory.newRecordEvent(record, user, EventType.CREATE));
	}

	public void addUserOrGroup(Record record, User user, String collection) {
		executeTransaction(eventFactory.newRecordEvent(record, user, EventType.CREATE, null, null, collection));
	}

	public void removeUser(Record record, User user) {
		executeTransaction(eventFactory.newRecordEvent(record, user, EventType.DELETE));
	}

	public void addGroup(Record record, User user) {
		executeTransaction(eventFactory.newRecordEvent(record, user, EventType.CREATE));
	}

	public void removeGroup(Record record, User user) {
		executeTransaction(eventFactory.newRecordEvent(record, user, EventType.DELETE));
	}

	private void executeTransaction(Event event) {
		if (event != null) {
			executeTransaction(event.getWrappedRecord());
		}
	}

	private void executeTransaction(Record record) {
		if (Toggle.AUDIT_EVENTS.isEnabled()) {
			Transaction transaction = new Transaction();
			transaction.setRecordFlushing(RecordsFlushing.ADD_LATER());
			transaction.add(record);
			try {
				modelLayerFactory.newRecordServices().execute(transaction);
			} catch (RecordServicesException e) {
				//TODO
				throw new RuntimeException(e.getMessage());
			}
		}
	}

	public void logDeleteRecordWithJustification(Record record, User user, String reason) {
		SchemaUtils schemaUtils = new SchemaUtils();
		executeTransaction(eventFactory.newRecordEvent(record, user, EventType.DELETE, reason));
	}

	public void completeBorrowRequestTask(Record record, String taskId, boolean isAccepted, User applicant,
										  User respondant,
										  String reason, String delta) {
		executeTransaction(eventFactory.newRecordEvent(record, applicant, EventType.BORROW_REQUEST, reason, LocalDateTime.now())
				.setTask(taskId).setReceiver(respondant).setDelta(delta).setAccepted(isAccepted));
	}

	public void completeReturnRequestTask(Record record, String taskId, boolean isAccepted, User applicant,
										  User respondant,
										  String reason) {
		executeTransaction(eventFactory.newRecordEvent(record, applicant, EventType.RETURN_REQUEST, reason, LocalDateTime.now())
				.setTask(taskId).setReceiver(respondant).setAccepted(isAccepted));
	}

	public void completeReactivationRequestTask(Record record, String taskId, boolean isAccepted, User applicant,
												User respondant,
												String reason, String delta) {
		executeTransaction(
				eventFactory.newRecordEvent(record, applicant, EventType.REACTIVATION_REQUEST, reason, LocalDateTime.now())
						.setTask(taskId).setReceiver(respondant).setDelta(delta).setAccepted(isAccepted));
	}

	public void completeBorrowExtensionRequestTask(Record record, String taskId, boolean isAccepted, User applicant,
												   User respondant, String reason, String delta) {
		executeTransaction(
				eventFactory.newRecordEvent(record, applicant, EventType.BORROW_EXTENSION_REQUEST, reason, LocalDateTime.now())
						.setTask(taskId).setReceiver(respondant).setDelta(delta).setAccepted(isAccepted));
	}
}
