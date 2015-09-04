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
package com.constellio.model.services.logging;

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
		List<Record> records = eventFactory.eventPermission(authorization, null, user, EventType.GRANT_PERMISSION);
		executeTransaction(records);
	}

	public void modifyPermission(Authorization authorization, Authorization authorizationBefore, User user) {
		List<Record> records = eventFactory
				.eventPermission(authorization, authorizationBefore, user, EventType.MODIFY_PERMISSION);
		executeTransaction(records);
	}

	public void deletePermission(Authorization authorization, User user) {
		List<Record> records = eventFactory.eventPermission(authorization, null, user, EventType.DELETE_PERMISSION);
		executeTransaction(records);
	}

	public void logRecordView(Record record, User currentUser) {
		executeTransaction(eventFactory.newRecordEvent(record, currentUser, EventType.VIEW));
	}

	public void borrowRecord(Record record, User currentUser, LocalDateTime dateTime) {
		executeTransaction(eventFactory.newRecordEvent(record, currentUser, EventType.BORROW, null, dateTime));
	}

	public void borrowRecord(Record record, User currentUser) {
		executeTransaction(
				eventFactory.newRecordEvent(record, currentUser, EventType.BORROW, null, TimeProvider.getLocalDateTime()));
	}

	public void consultingRecord(Record record, User currentUser, LocalDateTime dateTime) {
		executeTransaction(eventFactory.newRecordEvent(record, currentUser, EventType.CONSULTATION, null, dateTime));
	}

	public void consultingRecord(Record record, User currentUser) {
		executeTransaction(
				eventFactory.newRecordEvent(record, currentUser, EventType.CONSULTATION, null, TimeProvider.getLocalDateTime()));
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
