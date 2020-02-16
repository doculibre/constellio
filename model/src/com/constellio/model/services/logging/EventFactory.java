package com.constellio.model.services.logging;

import com.constellio.data.utils.LangUtils;
import com.constellio.data.utils.LangUtils.ListComparisonResults;
import com.constellio.data.utils.TimeProvider;
import com.constellio.model.entities.batchprocess.BatchProcess;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.ContentVersion;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.Authorization;
import com.constellio.model.entities.records.wrappers.Collection;
import com.constellio.model.entities.records.wrappers.Event;
import com.constellio.model.entities.records.wrappers.EventType;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.schemas.entries.DataEntryType;
import com.constellio.model.services.contents.ContentFactory;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordImpl;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.schemas.SchemaUtils;
import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDateTime;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class EventFactory {

	ModelLayerFactory modelLayerFactory;
	MetadataSchemasManager metadataSchemasManager;
	RecordServices recordServices;

	public EventFactory(ModelLayerFactory modelLayerFactory) {
		this.modelLayerFactory = modelLayerFactory;
		this.metadataSchemasManager = modelLayerFactory.getMetadataSchemasManager();
		this.recordServices = modelLayerFactory.newRecordServices();
	}

	public Event newLoginEvent(User user) {
		SchemasRecordsServices schemasRecords = new SchemasRecordsServices(user.getCollection(), modelLayerFactory);
		Event event = schemasRecords.newEvent();
		setDefaultMetadata(event, user);
		event.setIp(user.getLastIPAddress());
		event.setType(EventType.OPEN_SESSION);
		return event;
	}

	public Event newFailedLoginEvent(String username, String ip) {
		SchemasRecordsServices schemasRecords = new SchemasRecordsServices(Collection.SYSTEM_COLLECTION, modelLayerFactory);
		Event event = schemasRecords.newEvent();
		event.setCreatedOn(TimeProvider.getLocalDateTime());
		event.setUsername(username);
		event.setIp(ip);
		event.setType(EventType.ATTEMPTED_OPEN_SESSION);
		return event;
	}

	public Event newLogoutEvent(User user) {
		SchemasRecordsServices schemasRecords = new SchemasRecordsServices(user.getCollection(), modelLayerFactory);
		Event event = schemasRecords.newEvent();
		setDefaultMetadata(event, user);
		event.setType(EventType.CLOSE_SESSION);
		return event;
	}

	public Event newBatchProcessEvent(BatchProcess process, int totalModifiedRecords, String eventType) {
		User user = modelLayerFactory.newUserServices().getUserInCollection(process.getUsername(), process.getCollection());
		SchemasRecordsServices schemasRecords = new SchemasRecordsServices(user.getCollection(), modelLayerFactory);
		Event event = schemasRecords.newEvent();
		setDefaultMetadata(event, user);
		event.setBatchProcessId(process.getId());
		event.setTotalModifiedRecord(totalModifiedRecords);
		event.setType(eventType);
		return event;
	}

	public Event newRecordEvent(Record record, User currentUser, String eventType) {
		return newRecordEvent(record, currentUser, eventType, null);
	}

	public Event newRecordEvent(Record record, User currentUser, String eventType, String reason) {
		return newRecordEvent(record, currentUser, eventType, reason, null);
	}

	public Event newRecordEvent(Record record, User currentUser, String eventType, String reason,
								LocalDateTime eventDateTime) {
		if (currentUser != User.GOD && record.getCollection().endsWith(currentUser.getCollection())) {
			return createRecordEvent(record, currentUser, eventType, reason, eventDateTime, currentUser.getCollection());
		} else {
			return null;
		}
	}

	public Event newRecordEvent(Record record, User currentUser, String eventType, String reason,
								LocalDateTime eventDateTime,
								String collection) {
		return createRecordEvent(record, currentUser, eventType, reason, eventDateTime, collection);
	}

	private Event createRecordEvent(Record record, User currentUser, String eventType, String reason,
									LocalDateTime eventDateTime,
									String collection) {
		SchemasRecordsServices schemasRecords = new SchemasRecordsServices(collection, modelLayerFactory);
		Event event = schemasRecords.newEvent();
		setDefaultMetadata(event, currentUser);
		setRecordMetadata(event, record);
		if (reason != null) {
			event.setReason(reason);
		}

		if (eventDateTime != null) {
			event.setCreatedOn(eventDateTime);
			event.setModifiedOn(eventDateTime);
		}

		String recordSchema = record.getSchemaCode();
		SchemaUtils schemaUtils = new SchemaUtils();
		String recordSchemaType = schemaUtils.getSchemaTypeCode(recordSchema);
		event.setType(eventType + "_" + recordSchemaType);

		return event;
	}

	private void setRecordMetadata(Event event, Record record) {
		event.setRecordId(record.getId());
		String principalPath = record.get(metadataSchemasManager.getSchemaOf(record).get(Schemas.PRINCIPAL_PATH.getLocalCode()));

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

	public Event logAddUpdateRecord(Record record, User user) {
		SchemasRecordsServices schemasRecords = new SchemasRecordsServices(user.getCollection(), modelLayerFactory);
		Event event = schemasRecords.newEvent();
		setDefaultMetadata(event, user);
		setRecordMetadata(event, record);

		String recordSchema = record.getSchemaCode();
		SchemaUtils schemaUtils = new SchemaUtils();
		String recordSchemaType = schemaUtils.getSchemaTypeCode(recordSchema);

		if (record.isSaved()) {
			if (record.isModified(Schemas.LOGICALLY_DELETED_STATUS)
				|| record.isModified(Schemas.LOGICALLY_DELETED_ON)) {
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

		return event;
	}

	public String getBorrowDurationDelta(LocalDateTime oldValue, LocalDateTime newValue) {
		StringBuilder delta = new StringBuilder();
		if (!oldValue.toString().equals(newValue.toString())) {
			delta.append("\tAvant : " + limitContentLength(oldValue.toString()) + "\n");
			delta.append("\tAprès : " + limitContentLength(newValue.toString()));
		}
		return delta.toString();
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
				MetadataSchema schema = metadataSchemasManager.getSchemaTypes(record.getCollection())
						.getSchema(record.getSchemaCode());
				if (schema.hasMetadataWithCode(metadataLocalCode)) {
					Metadata metadata = schema.getMetadata(metadataLocalCode);
					if (notAccepted(metadata)) {
						continue;
					}
					Object newValue = modifiedValueEntry.getValue();
					Object oldValue = recordImpl.getRecordDTO().getFields().get(metadataDatastoreCode);
					if (metadata.getType() == MetadataValueType.CONTENT) {
						newValue = newValue == null ? null : formatContentForDeltaMetadata(newValue);
						oldValue = oldValue == null ? null : formatContentForDeltaMetadata(oldValue);
					}

					if (newValue == null) {
						if (oldValue != null) {
							delta.append("-[" + metadata.getCode() + " : " + oldValue.toString() + "]\n");
						}
					} else {
						if (oldValue == null) {
							delta.append("+[" + metadata.getCode() + " : " + newValue.toString() + "]\n");
						} else {
							if (!oldValue.toString().equals(newValue.toString())) {
								delta.append("[ " + metadata.getCode() + " :\n");
								delta.append("\tAvant : " + limitContentLength(oldValue.toString()) + "\n");
								delta.append("\tAprès : " + limitContentLength(newValue.toString()) + "]\n");
							}
						}
					}
				}
			}
		}
		event.setDelta(delta.toString());
	}

	private Object formatContentForDeltaMetadata(Object content) {
		try {
			if (content instanceof Content && ((Content) content).getCurrentVersion() != null) {
				StringBuilder printableValue = new StringBuilder();
				ContentVersion currentVersion = ((Content) content).getCurrentVersion();
				printableValue.append(currentVersion.getFilename());
				printableValue.append(" (v" + currentVersion.getVersion() + ")");
				printableValue.append(" - " + (currentVersion.getLength() / 1024) + " KB");
				return printableValue.toString();
			} else if (content instanceof String) {
				return formatContentForDeltaMetadata(new ContentFactory().build((String) content, true));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return content;
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

	public Event eventPermission(Authorization authorization, Authorization authorizationBefore,
								 User user,
								 String recordId, String eventPermissionType) {
		SchemasRecordsServices schemasRecords = new SchemasRecordsServices(user.getCollection(), modelLayerFactory);

		if (recordId == null) {
			recordId = authorization.getTarget();
		}
		String deltaString = compareAuthorizations(authorizationBefore, authorization);

		SchemaUtils schemaUtils = new SchemaUtils();
		String authorizationRolesString = StringUtils.join(authorization.getRoles(), "; ");
		Boolean negative = authorization.isNegative() ? true : null;
		String authorizationPrincipalsString = getAuthorizationPrincipals(authorization);
		String dateRangeString = getAuthorizationDateRange(authorization);
		Event event = schemasRecords.newEvent();
		setDefaultMetadata(event, user);
		event.setPermissionUsers(authorizationPrincipalsString);
		event.setPermissionDateRange(dateRangeString);
		event.setPermissionRoles(authorizationRolesString);
		event.setDelta(deltaString);
		event.setNegative(negative);
		Record currentRecord = recordServices.getDocumentById(recordId);
		String recordSchema = currentRecord.getSchemaCode();

		String recordSchemaType = schemaUtils.getSchemaTypeCode(recordSchema);
		setRecordMetadata(event, currentRecord);
		event.setType(eventPermissionType + "_" + recordSchemaType);
		return event;
	}

	private String compareAuthorizations(Authorization authorizationBefore,
										 Authorization authorization) {
		StringBuilder deltaStringBuilder = new StringBuilder("");
		if (authorizationBefore != null) {
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

	private String getAuthorizationsDatesDelta(Authorization authorizationBefore,
											   Authorization authorization) {
		String datesBefore = getAuthorizationDateRange(authorizationBefore);
		String datesAfter = getAuthorizationDateRange(authorization);
		if (datesAfter.equals(datesBefore)) {
			return "";
		} else {
			//FIXME
			return "Dates avant :" + datesBefore;
		}
	}

	private String getAuthorizationsPrincipalsDelta(Authorization authorizationBefore,
													Authorization authorization) {
		ListComparisonResults<String> principalsComparisonResults = new LangUtils()
				.compare(authorizationBefore.getPrincipals(), authorization.getPrincipals());
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

	private String getAuthorizationDateRange(Authorization authorization) {
		Authorization detail = authorization;
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
		for (String userId : authorization.getPrincipals()) {
			Record userRecord = recordServices.getDocumentById(userId);
			usersNames.add((String) userRecord.get(Schemas.TITLE));
		}
		return StringUtils.join(usersNames, "; ");
	}

}
