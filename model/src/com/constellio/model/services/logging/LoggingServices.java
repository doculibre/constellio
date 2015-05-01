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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;

import com.constellio.data.dao.dto.records.RecordsFlushing;
import com.constellio.data.utils.ImpossibleRuntimeException;
import com.constellio.data.utils.LangUtils;
import com.constellio.data.utils.LangUtils.ListComparisonResults;
import com.constellio.data.utils.TimeProvider;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.Event;
import com.constellio.model.entities.records.wrappers.EventType;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.schemas.entries.DataEntryType;
import com.constellio.model.entities.security.Authorization;
import com.constellio.model.entities.security.AuthorizationDetails;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.notifications.NotificationsServices;
import com.constellio.model.services.records.RecordImpl;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.schemas.SchemaUtils;
import com.constellio.model.services.search.SearchServices;

public class LoggingServices {

	ModelLayerFactory modelLayerFactory;

	RecordServices recordServices;

	MetadataSchemasManager metadataSchemasManager;

	NotificationsServices notificationsServices;

	SearchServices searchServices;

	public LoggingServices(ModelLayerFactory modelLayerFactory) {
		this.modelLayerFactory = modelLayerFactory;
		this.recordServices = modelLayerFactory.newRecordServices();
		this.notificationsServices = modelLayerFactory.newNotificationsServices();
		this.searchServices = modelLayerFactory.newSearchServices();
		this.metadataSchemasManager = modelLayerFactory.getMetadataSchemasManager();
	}

	public void logBorrowRecord(Record record, User currentUser) {
		logRecordEvent(record, currentUser, EventType.BORROW);
	}

	public void logReturnRecord(Record record, User currentUser) {
		logRecordEvent(record, currentUser, EventType.RETURN);
	}

	public void logTransaction(Transaction transaction) {
		if (transaction.getUser() == null) {
			return;
		}

		Transaction eventsTransaction = new Transaction().setRecordFlushing(RecordsFlushing.WITHIN_SECONDS(30));
		for (Record record : transaction.getRecords()) {
			Record event = logAddUpdateRecord(record, transaction.getUser());
			if (event != null) {
				eventsTransaction.addUpdate(event);
				notificationsServices.createNotifications(event.getId(), record.getFollowers());
			}
		}

		try {
			recordServices.execute(eventsTransaction);
		} catch (RecordServicesException e) {
			throw new ImpossibleRuntimeException("Only adding records, so this exception is impossible");
		}
	}

	public void grantPermission(Authorization authorization, User user) {
		List<Record> records = eventPermission(authorization, null, user, EventType.GRANT_PERMISSION);
		executeTransaction(records);
	}

	public void modifyPermission(Authorization authorization, Authorization authorizationBefore, User user) {
		List<Record> records = eventPermission(authorization, authorizationBefore, user, EventType.MODIFY_PERMISSION);
		executeTransaction(records);
	}

	public void deletePermission(Authorization authorization, User user) {
		List<Record> records = eventPermission(authorization, null, user, EventType.DELETE_PERMISSION);
		executeTransaction(records);
	}

	public void logRecordView(Record record, User currentUser) {
		logRecordEvent(record, currentUser, EventType.VIEW);
	}

	public void borrowRecord(Record record, User currentUser) {
		logRecordEvent(record, currentUser, EventType.BORROW);
	}

	public void returnRecord(Record record, User currentUser) {
		logRecordEvent(record, currentUser, EventType.RETURN);
	}

	public void login(User user) {
		SchemasRecordsServices schemasRecords = new SchemasRecordsServices(user.getCollection(), modelLayerFactory);
		Event event = schemasRecords.newEvent();
		setDefaultMetadata(event, user);
		event.setType(EventType.OPEN_SESSION);
		executeTransaction(event.getWrappedRecord());
	}

	public void logout(User user) {
		SchemasRecordsServices schemasRecords = new SchemasRecordsServices(user.getCollection(), modelLayerFactory);
		Event event = schemasRecords.newEvent();
		setDefaultMetadata(event, user);
		event.setType(EventType.CLOSE_SESSION);
		executeTransaction(event.getWrappedRecord());
	}

	public void addUser(Record record, User user) {
		logRecordEvent(record, user, EventType.CREATE);
	}

	public void removeUser(Record record, User user) {
		logRecordEvent(record, user, EventType.DELETE);
	}

	public void addGroup(Record record, User user) {
		logRecordEvent(record, user, EventType.CREATE);
	}

	public void removeGroup(Record record, User user) {
		logRecordEvent(record, user, EventType.DELETE);
	}

	private List<Record> eventPermission(Authorization authorization, Authorization authorizationBefore, User user,
			String eventPermissionType) {
		SchemasRecordsServices schemasRecords = new SchemasRecordsServices(user.getCollection(), modelLayerFactory);
		List<Record> returnRecords = new ArrayList<>();
		List<String> recordsIds = authorization.getGrantedOnRecords();
		String deltaString = compareAuthorizations(authorizationBefore, authorization);

		SchemaUtils schemaUtils = new SchemaUtils();
		String authorizationRolesString = StringUtils.join(authorization.getDetail().getRoles(), "; ");
		String authorizationPrincipalsString = getAuthorizationPrincipals(authorization);
		String dateRangeString = getAuthorizationDateRange(authorization);
		for (String recordId : recordsIds) {
			Event event = schemasRecords.newEvent();
			setDefaultMetadata(event, user);
			event.setPermissionUsers(authorizationPrincipalsString);
			event.setPermissionDateRange(dateRangeString);
			event.setPermissionRoles(authorizationRolesString);
			event.setDelta(deltaString);
			Record currentRecord = recordServices.getDocumentById(recordId);
			String recordSchema = currentRecord.getSchemaCode();

			String recordSchemaType = schemaUtils.getSchemaTypeCode(recordSchema);
			setRecordMetadata(event, currentRecord);
			event.setType(eventPermissionType + "_" + recordSchemaType);
			returnRecords.add(event.getWrappedRecord());
		}
		return returnRecords;
	}

	private String compareAuthorizations(Authorization authorizationBefore, Authorization authorization) {
		StringBuilder deltaStringBuilder = new StringBuilder("");
		if (authorizationBefore != null) {
			String recordsDelta = getAuthorizationsRecordsDelta(authorizationBefore, authorization);
			if (StringUtils.isNotBlank(recordsDelta)) {
				deltaStringBuilder.append(recordsDelta);
			}
			String principalsDelta = getAuthorizationsPrincipalsDelta(authorizationBefore, authorization);
			if (StringUtils.isNotBlank(principalsDelta)) {
				deltaStringBuilder.append("\n" + principalsDelta);
			}
			String datesDelta = getAuthorizationsDatesDelta(authorizationBefore, authorization);
			if (StringUtils.isNotBlank(datesDelta)) {
				deltaStringBuilder.append("\n" + datesDelta);
			}
		}
		return deltaStringBuilder.toString();
	}

	private String getAuthorizationsDatesDelta(Authorization authorizationBefore, Authorization authorization) {
		String datesBefore = getAuthorizationDateRange(authorizationBefore);
		String datesAfter = getAuthorizationDateRange(authorization);
		if (datesAfter.equals(datesBefore)) {
			return "";
		} else {
			//FIXME
			return "Dates avant :" + datesBefore;
		}
	}

	private String getAuthorizationsPrincipalsDelta(Authorization authorizationBefore, Authorization authorization) {
		ListComparisonResults<String> principalsComparisonResults = new LangUtils()
				.compare(authorizationBefore.getGrantedToPrincipals(), authorization.getGrantedToPrincipals());
		if (principalsComparisonResults.getRemovedItems().size() == 0 && principalsComparisonResults.getNewItems().size() == 0) {
			return "";
		}

		List<String> addPrincipals = new ArrayList<>();
		for (String principalId : principalsComparisonResults.getNewItems()) {
			Record currentPrincipal = recordServices.getDocumentById(principalId);
			addPrincipals.add((String) currentPrincipal.get(Schemas.TITLE));
		}
		List<String> removedPrincipals = new ArrayList<>();
		for (String principalId : principalsComparisonResults.getRemovedItems()) {
			Record currentPrincipal = recordServices.getDocumentById(principalId);
			removedPrincipals.add((String) currentPrincipal.get(Schemas.TITLE));
		}
		//FIXME
		StringBuilder principalsDelta = new StringBuilder(("Utilisateurs :\n-["));
		principalsDelta.append(StringUtils.join(removedPrincipals, "; "));
		principalsDelta.append("]\n+[");
		principalsDelta.append(StringUtils.join(addPrincipals, "; "));
		principalsDelta.append("]\n");
		return principalsDelta.toString();
	}

	private String getAuthorizationsRecordsDelta(Authorization authorizationBefore, Authorization authorization) {
		ListComparisonResults<String> recordsComparisonResults = new LangUtils()
				.compare(authorizationBefore.getGrantedOnRecords(), authorization.getGrantedOnRecords());
		if (recordsComparisonResults.getRemovedItems().size() == 0 && recordsComparisonResults.getNewItems().size() == 0) {
			return "";
		}
		//FIXME
		StringBuilder recordsDelta = new StringBuilder(("Enregistrements :\n -["));
		recordsDelta.append(StringUtils.join(recordsComparisonResults.getRemovedItems(), "; "));
		recordsDelta.append("]\n+[");
		recordsDelta.append(StringUtils.join(recordsComparisonResults.getNewItems(), "; "));
		recordsDelta.append("]\n");
		return recordsDelta.toString();
	}

	private String getAuthorizationDateRange(Authorization authorization) {
		AuthorizationDetails detail = authorization.getDetail();
		StringBuilder dateRangeStringBuilder = new StringBuilder("[");
		if (detail.getStartDate() != null) {
			dateRangeStringBuilder.append(detail.getStartDate());
		}
		dateRangeStringBuilder.append(", ");
		if (detail.getEndDate() != null) {
			dateRangeStringBuilder.append(detail.getEndDate());
		}
		dateRangeStringBuilder.append("]");
		return dateRangeStringBuilder.toString();
	}

	private String getAuthorizationPrincipals(Authorization authorization) {
		List<String> usersNames = new ArrayList<>();
		for (String userId : authorization.getGrantedToPrincipals()) {
			Record userRecord = recordServices.getDocumentById(userId);
			usersNames.add((String) userRecord.get(Schemas.TITLE));
		}
		return StringUtils.join(usersNames, "; ");
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
		logRecordEvent(record, user, EventType.DELETE, reason);
	}

	private Record logAddUpdateRecord(Record record, User user) {
		SchemasRecordsServices schemasRecords = new SchemasRecordsServices(user.getCollection(), modelLayerFactory);
		Event event = schemasRecords.newEvent();
		setDefaultMetadata(event, user);
		setRecordMetadata(event, record);

		String recordSchema = record.getSchemaCode();
		SchemaUtils schemaUtils = new SchemaUtils();
		String recordSchemaType = schemaUtils.getSchemaTypeCode(recordSchema);

		if (record.isSaved()) {
			if (record.isModified(Schemas.LOGICALLY_DELETED_STATUS)) {
				// event.setType(EventType.DELETE + "_" + recordSchemaType);
				// Deletions are logged separately
				return null;
			} else {
				event.setType(EventType.MODIFY + "_" + recordSchemaType);
				setDeltaMetadata(event, record);
			}
		} else {
			event.setType(EventType.CREATE + "_" + recordSchemaType);
		}

		return event.getWrappedRecord();
	}

	private void setDeltaMetadata(Event event, Record record) {
		//FIXME
		StringBuilder delta = new StringBuilder();

		RecordImpl recordImpl = (RecordImpl) record;

		Map<String, Object> modifiedValues = recordImpl.getModifiedValues();
		if (record.isSaved()) {

			for (Entry<String, Object> modifiedValueEntry : modifiedValues.entrySet()) {
				String metadataDatastoreCode = modifiedValueEntry.getKey();
				String metadataLocalCode = new SchemaUtils().getLocalCodeFromDataStoreCode(metadataDatastoreCode);
				Metadata metadata = metadataSchemasManager.getSchemaTypes(record.getCollection())
						.getSchema(record.getSchemaCode()).getMetadata(metadataLocalCode);
				if (notAccepted(metadata)) {
					continue;
				}
				Object newValue = modifiedValueEntry.getValue();
				Object oldValue = recordImpl.getRecordDTO().getFields().get(metadataDatastoreCode);
				if (newValue == null) {
					if (oldValue != null) {
						delta.append("-[" + metadata.getLabel() + " : " + oldValue.toString() + "]\n");
					}
				} else {
					if (oldValue == null) {
						delta.append("+[" + metadata.getLabel() + " : " + newValue.toString() + "]\n");
					} else {
						if (!oldValue.toString().equals(newValue.toString())) {
							delta.append("[ " + metadata.getLabel() + " :\n");
							delta.append("\tAvant : " + limitContentLength(oldValue.toString()) + "\n");
							delta.append("\tAprès : " + limitContentLength(newValue.toString()) + "]\n");
						}
					}
				}
			}
		}
		event.setDelta(delta.toString());
	}

	private String limitContentLength(String text) {
		return StringUtils.abbreviate(text, 100);
	}

	private boolean notAccepted(Metadata metadata) {
		if (metadata.isSystemReserved()) {
			return true;
		}
		if (!metadata.getDataEntry().getType().name().equals(DataEntryType.MANUAL.name())) {
			return true;
		}
		return false;
	}

	private void setRecordMetadata(Event event, Record record) {
		event.setRecordId(record.getId());
		String principalPath = record.get(Schemas.PRINCIPAL_PATH);
		event.setEventPrincipalPath(principalPath);
		Object title = record.get(Schemas.TITLE);
		if (title != null) {
			event.setTitle((String) title);
		}
	}

	private void setDefaultMetadata(Event event, User user) {
		event.setUsername(user.getUsername());
		List<String> roles = user.getAllRoles();
		event.setUserRoles(StringUtils.join(roles.toArray(), "; "));
		event.setCreatedOn(TimeProvider.getLocalDateTime());
		String ipAddress = user.getLastIPAddress();
		event.setIp(ipAddress);
	}

	private void logRecordEvent(Record record, User currentUser, String eventType) {
		logRecordEvent(record, currentUser, eventType, null);
	}

	private void logRecordEvent(Record record, User currentUser, String eventType, String reason) {
		if (record.getCollection().endsWith(currentUser.getCollection())) {
			SchemasRecordsServices schemasRecords = new SchemasRecordsServices(currentUser.getCollection(), modelLayerFactory);
			Event event = schemasRecords.newEvent();
			setDefaultMetadata(event, currentUser);
			setRecordMetadata(event, record);
			if (reason != null) {
				event.setReason(reason);
			}

			String recordSchema = record.getSchemaCode();
			SchemaUtils schemaUtils = new SchemaUtils();
			String recordSchemaType = schemaUtils.getSchemaTypeCode(recordSchema);
			event.setType(eventType + "_" + recordSchemaType);

			executeTransaction(event.getWrappedRecord());
		}
	}

}
