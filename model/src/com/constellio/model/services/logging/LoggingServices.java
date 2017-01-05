package com.constellio.model.services.logging;

import static com.constellio.model.entities.records.wrappers.EventType.MODIFY_PERMISSION;

import java.util.List;

import org.joda.time.LocalDateTime;

import com.constellio.data.dao.dto.records.RecordsFlushing;
import com.constellio.data.utils.ImpossibleRuntimeException;
import com.constellio.data.utils.TimeProvider;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.Event;
import com.constellio.model.entities.records.wrappers.EventType;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.security.Authorization;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.schemas.SchemaUtils;
import com.constellio.model.services.search.SearchServices;

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
		Event event = eventFactory.eventPermission(authorization, null, user, null, EventType.GRANT_PERMISSION);
		executeTransaction(event);
	}

	public void modifyPermission(Authorization authorization, Authorization authorizationBefore, Record record, User user) {
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

	public void borrowRecord(Record record, User currentUser, LocalDateTime dateTime) {
		executeTransaction(eventFactory.newRecordEvent(record, currentUser, EventType.BORROW, null, dateTime));
	}

	public void borrowRecord(Record record, User currentUser) {
		executeTransaction(
				eventFactory.newRecordEvent(record, currentUser, EventType.BORROW, null, TimeProvider.getLocalDateTime()));
	}

	public void consultingRecord(Record record, User currentUser, LocalDateTime dateTime) {
		logRecordView(record, currentUser, dateTime);
		//executeTransaction(eventFactory.newRecordEvent(record, currentUser, EventType.CONSULTATION, null, dateTime));
	}

	public void consultingRecord(Record record, User currentUser) {
		logRecordView(record, currentUser);
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
		Transaction transaction = new Transaction();
		transaction.setRecordFlushing(RecordsFlushing.WITHIN_SECONDS(1));
		transaction.add(record);
		try {
			modelLayerFactory.newRecordServices().execute(transaction);
		} catch (RecordServicesException e) {
			//TODO
			throw new RuntimeException(e.getMessage());
		}
	}

	private void executeTransaction(List<Record> records) {
		Transaction transaction = new Transaction();
		transaction.setRecordFlushing(RecordsFlushing.WITHIN_SECONDS(10));
		for (Record record : records) {
			transaction.add(record);
		}
		try {
			modelLayerFactory.newRecordServices().execute(transaction);
		} catch (RecordServicesException e) {
			//TODO
			throw new RuntimeException(e.getMessage());
		}
	}

	public void logDeleteRecordWithJustification(Record record, User user, String reason) {
		SchemaUtils schemaUtils = new SchemaUtils();
		executeTransaction(eventFactory.newRecordEvent(record, user, EventType.DELETE, reason));
	}

}
