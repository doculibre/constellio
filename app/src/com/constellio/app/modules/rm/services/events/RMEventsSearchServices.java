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
package com.constellio.app.modules.rm.services.events;

import static com.constellio.model.services.contents.ContentFactory.checkedOut;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.containingText;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.endingWithText;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.where;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDateTime;

import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.data.utils.TimeProvider;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.Event;
import com.constellio.model.entities.records.wrappers.EventType;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;

public class RMEventsSearchServices {

	private ModelLayerFactory modelLayerFactory;

	private RMSchemasRecordsServices schemas;

	public RMEventsSearchServices(ModelLayerFactory modelLayerFactory, String collection) {
		this.modelLayerFactory = modelLayerFactory;
		this.schemas = new RMSchemasRecordsServices(collection, modelLayerFactory);
	}

	public List<Event> findLoggedUsers(User currentUser) {
		return findNotCanceledEventsPerUser(currentUser, EventType.OPEN_SESSION, EventType.CLOSE_SESSION);
	}

	public LogicalSearchQuery newFindOpenedSessionsByDateRangeQuery(User currentUser, LocalDateTime startDate,
			LocalDateTime endDate) {
		return newFindEventByDateRangeQuery(currentUser, EventType.OPEN_SESSION, startDate, endDate);
	}

	//by range date
	public LogicalSearchQuery newFindCreatedDocumentsByDateRangeQuery(User currentUser, LocalDateTime startDate,
			LocalDateTime endDate) {
		return newFindEventByDateRangeQuery(currentUser, EventType.CREATE_DOCUMENT, startDate, endDate);
	}

	public LogicalSearchQuery newFindDeletedDocumentsByDateRangeQuery(User currentUser, LocalDateTime startDate,
			LocalDateTime endDate) {
		return newFindEventByDateRangeQuery(currentUser, EventType.DELETE_DOCUMENT, startDate, endDate);
	}

	public LogicalSearchQuery newFindModifiedDocumentsByDateRangeQuery(User currentUser, LocalDateTime startDate,
			LocalDateTime endDate) {
		return newFindEventByDateRangeQuery(currentUser, EventType.MODIFY_DOCUMENT, startDate, endDate);
	}

	public LogicalSearchQuery newFindBorrowedDocumentsByDateRangeQuery(User currentUser, LocalDateTime startDate,
			LocalDateTime endDate) {
		return newFindEventByDateRangeQuery(currentUser, EventType.BORROW_DOCUMENT, startDate, endDate);
	}

	public LogicalSearchQuery newFindReturnedDocumentsByDateRangeQuery(User currentUser, LocalDateTime startDate,
			LocalDateTime endDate) {
		return newFindEventByDateRangeQuery(currentUser, EventType.RETURN_DOCUMENT, startDate, endDate);
	}

	public List<Event> findCurrentlyBorrowedDocuments(User currentUser) {
		return findNotCanceledEventsPerUser(currentUser, EventType.BORROW_DOCUMENT, EventType.RETURN_DOCUMENT);
	}

	public LogicalSearchQuery newFindCurrentlyBorrowedDocumentsQuery(User currentUser) {
		return new LogicalSearchQuery()
				.filteredWithUser(currentUser)
				.setCondition(
						from(schemas.documentSchemaType())
								.where(schemas.documentContent()).is(checkedOut()));
	}

	public LogicalSearchQuery newFindCurrentlyBorrowedFoldersQuery(User currentUser) {
		return new LogicalSearchQuery()
				.filteredWithUser(currentUser)
				.setCondition(
						from(schemas.folderSchemaType())
								.where(schemas.folderBorrowed()).isTrue());
	}

	public LogicalSearchQuery newFindCurrentlyBorrowedFoldersByUserAndDateRangeQuery(User currentUser, String userId) {
		return new LogicalSearchQuery()
				.filteredWithUser(currentUser)
				.setCondition(
						from(schemas.folderSchemaType())
								.where(schemas.folderBorrowed()).isTrue().andWhere(schemas.folderBorrowedUserEntered())
								.is(userId));
	}

	public LogicalSearchQuery newFindLateBorrowedFoldersByUserAndDateRangeQuery(User currentUser, String userId) {
		return new LogicalSearchQuery()
				.filteredWithUser(currentUser)
				.setCondition(
						from(schemas.folderSchemaType())
								.where(schemas.folderBorrowed()).isTrue().andWhere(schemas.folderBorrowedUserEntered())
								.is(userId).andWhere(schemas.folderBorrowPreviewReturnDate())
								.isLessThan(TimeProvider.getLocalDateTime()));
	}

	public LogicalSearchQuery newFindCreatedFoldersByDateRangeQuery(User currentUser, LocalDateTime startDate,
			LocalDateTime endDate) {
		return newFindEventByDateRangeQuery(currentUser, EventType.CREATE_FOLDER, startDate, endDate);
	}

	public LogicalSearchQuery newFindModifiedFoldersByDateRangeQuery(User currentUser, LocalDateTime startDate,
			LocalDateTime endDate) {
		return newFindEventByDateRangeQuery(currentUser, EventType.MODIFY_FOLDER, startDate, endDate);
	}

	public LogicalSearchQuery newFindDeletedFoldersByDateRangeQuery(User currentUser, LocalDateTime startDate,
			LocalDateTime endDate) {
		return newFindEventByDateRangeQuery(currentUser, EventType.DELETE_FOLDER, startDate, endDate);
	}

	public LogicalSearchQuery newFindBorrowedFoldersByDateRangeQuery(User currentUser, LocalDateTime startDate,
			LocalDateTime endDate) {
		return newFindEventByDateRangeQuery(currentUser, EventType.BORROW_FOLDER, startDate, endDate);
	}

	public LogicalSearchQuery newFindReturnedFoldersByDateRangeQuery(User currentUser, LocalDateTime startDate,
			LocalDateTime endDate) {
		return newFindEventByDateRangeQuery(currentUser, EventType.RETURN_FOLDER, startDate, endDate);
	}

	public List<Event> findCurrentlyBorrowedFolders(User currentUser) {
		return findNotCanceledEventsPerUser(currentUser, EventType.BORROW_FOLDER, EventType.RETURN_FOLDER);
	}

	public LogicalSearchQuery newFindBorrowedContainersByDateRangeQuery(User currentUser, LocalDateTime startDate,
			LocalDateTime endDate) {
		return newFindEventByDateRangeQuery(currentUser, EventType.BORROW_CONTAINER, startDate, endDate);
	}

	public LogicalSearchQuery newFindReturnedContainersByDateRangeQuery(User currentUser, LocalDateTime startDate,
			LocalDateTime endDate) {
		return newFindEventByDateRangeQuery(currentUser, EventType.RETURN_CONTAINER, startDate, endDate);
	}

	public LogicalSearchQuery newFindRelocatedFoldersByDateRangeQuery(User currentUser, LocalDateTime startDate,
			LocalDateTime endDate) {
		return newFindEventByDateRangeQuery(currentUser, EventType.FOLDER_RELOCATION, startDate, endDate);
	}

	public LogicalSearchQuery newFindFolderDepositByDateRangeQuery(User currentUser, LocalDateTime startDate,
			LocalDateTime endDate) {
		return newFindEventByDateRangeQuery(currentUser, EventType.FOLDER_DEPOSIT, startDate, endDate);
	}

	public LogicalSearchQuery newFindDestructedFoldersByDateRangeQuery(User currentUser, LocalDateTime startDate,
			LocalDateTime endDate) {
		return newFindEventByDateRangeQuery(currentUser, EventType.FOLDER_DESTRUCTION, startDate, endDate);
	}

	public LogicalSearchQuery newFindPdfAGenerationEventsByDateRangeQuery(User currentUser, LocalDateTime startDate,
			LocalDateTime endDate) {
		return newFindEventByDateRangeQuery(currentUser, EventType.PDF_A_GENERATION, startDate, endDate);
	}

	public LogicalSearchQuery newFindReceivedFoldersByDateRangeQuery(User currentUser, LocalDateTime startDate,
			LocalDateTime endDate) {
		return newFindEventByDateRangeQuery(currentUser, EventType.RECEIVE_FOLDER, startDate, endDate);
	}

	public LogicalSearchQuery newFindReceivedContainersByDateRangeQuery(User currentUser, LocalDateTime startDate,
			LocalDateTime endDate) {
		return newFindEventByDateRangeQuery(currentUser, EventType.RECEIVE_CONTAINER, startDate, endDate);
	}

	//By Filing space and date range
	public LogicalSearchQuery newFindCreatedFoldersByDateRangeAndByFilingSpaceQuery(User currentUser, LocalDateTime startDate,
			LocalDateTime endDate, String principalPath) {
		return newFindEventByDateRangeAndByAdministrativeUnitQuery(currentUser, EventType.CREATE_FOLDER, startDate, endDate,
				principalPath);
	}

	/*public LogicalSearchQuery newFindModifiedFoldersByDateRangeAndByFilingSpaceQuery(User currentUser, LocalDateTime startDate,
			LocalDateTime endDate, String principalPath) {
		return newFindEventByDateRangeAndByAdministrativeUnitQuery(currentUser, EventType.MODIFY_FOLDER, startDate, endDate,
				principalPath);
	}

	public LogicalSearchQuery newFindDeletedFoldersByDateRangeAndByFilingSpaceQuery(User currentUser, LocalDateTime startDate,
			LocalDateTime endDate, String principalPath) {
		return newFindEventByDateRangeAndByAdministrativeUnitQuery(currentUser, EventType.DELETE_FOLDER, startDate, endDate,
				principalPath);
	}

	public LogicalSearchQuery newFindCreatedDocumentsByDateRangeAndByFilingSpaceQuery(User currentUser, LocalDateTime startDate,
			LocalDateTime endDate, String principalPath) {
		return newFindEventByDateRangeAndByAdministrativeUnitQuery(currentUser, EventType.CREATE_DOCUMENT, startDate, endDate,
				principalPath);
	}

	public LogicalSearchQuery newFindModifiedDocumentsByDateRangeAndByFilingSpaceQuery(User currentUser, LocalDateTime startDate,
			LocalDateTime endDate, String principalPath) {
		return newFindEventByDateRangeAndByAdministrativeUnitQuery(currentUser, EventType.MODIFY_DOCUMENT, startDate, endDate,
				principalPath);
	}

	public LogicalSearchQuery newFindDeletedDocumentsByDateRangeAndByFilingSpaceQuery(User currentUser, LocalDateTime startDate,
			LocalDateTime endDate, String principalPath) {
		return newFindEventByDateRangeAndByAdministrativeUnitQuery(currentUser, EventType.DELETE_DOCUMENT, startDate, endDate,
				principalPath);
	}

	public LogicalSearchQuery newFindCreatedUsersByDateRangeAndByFilingSpaceQuery(User currentUser, LocalDateTime startDate,
			LocalDateTime endDate, String principalPath) {
		return newFindEventByDateRangeAndByAdministrativeUnitQuery(currentUser, EventType.CREATE_USER, startDate, endDate,
				principalPath);
	}

	public LogicalSearchQuery newFindModifiedUsersByDateRangeAndByFilingSpaceQuery(User currentUser, LocalDateTime startDate,
			LocalDateTime endDate, String principalPath) {
		return newFindEventByDateRangeAndByAdministrativeUnitQuery(currentUser, EventType.MODIFY_USER, startDate, endDate,
				principalPath);
	}

	public LogicalSearchQuery newFindDeletedUsersByDateRangeAndByFilingSpaceQuery(User currentUser, LocalDateTime startDate,
			LocalDateTime endDate, String principalPath) {
		return newFindEventByDateRangeAndByAdministrativeUnitQuery(currentUser, EventType.DELETE_USER, startDate, endDate,
				principalPath);
	}

	public LogicalSearchQuery newFindGrantedPermissionsByDateRangeAndByFilingSpaceQuery(User currentUser, LocalDateTime startDate,
			LocalDateTime endDate, String principalPath) {
		return newFindEventByDateRangeAndByAdministrativeUnitQuery(currentUser, EventType.GRANT_PERMISSION, startDate, endDate,
				principalPath);
	}

	public LogicalSearchQuery newFindModifiedPermissionsByDateRangeAndByFilingSpaceQuery(User currentUser,
			LocalDateTime startDate, LocalDateTime endDate, String principalPath) {
		return newFindEventByDateRangeAndByAdministrativeUnitQuery(currentUser, EventType.MODIFY_PERMISSION, startDate, endDate,
				principalPath);
	}

	public LogicalSearchQuery newFindDeletedPermissionsByDateRangeAndByFilingSpaceQuery(User currentUser, LocalDateTime startDate,
			LocalDateTime endDate, String principalPath) {
		return newFindEventByDateRangeAndByAdministrativeUnitQuery(currentUser, EventType.DELETE_PERMISSION, startDate, endDate,
				principalPath);
	}

	//By user and date range
	public LogicalSearchQuery newFindCreatedFoldersByDateRangeAndByUserQuery(LocalDateTime startDate, LocalDateTime endDate,
			String username,
			User currentUser) {
		return newFindEventByDateRangeAndByUserQuery(currentUser, EventType.CREATE_FOLDER, startDate, endDate, username);
	}

	public LogicalSearchQuery newFindModifiedFoldersByDateRangeAndByUserQuery(User currentUser, LocalDateTime startDate,
			LocalDateTime endDate, String username) {
		return newFindEventByDateRangeAndByUserQuery(currentUser, EventType.MODIFY_FOLDER, startDate, endDate, username);
	}

	public LogicalSearchQuery newFindDeletedFoldersByDateRangeAndByUserQuery(User currentUser, LocalDateTime startDate,
			LocalDateTime endDate, String username) {
		return newFindEventByDateRangeAndByUserQuery(currentUser, EventType.DELETE_FOLDER, startDate, endDate, username);
	}



	public LogicalSearchQuery newFindModifiedDocumentsByDateRangeAndByUserQuery(User currentUser, LocalDateTime startDate,
			LocalDateTime endDate, String username) {
		return newFindEventByDateRangeAndByUserQuery(currentUser, EventType.MODIFY_DOCUMENT, startDate, endDate, username);
	}

	public LogicalSearchQuery newFindDeletedDocumentsByDateRangeAndByUserQuery(User currentUser, LocalDateTime startDate,
			LocalDateTime endDate, String username) {
		return newFindEventByDateRangeAndByUserQuery(currentUser, EventType.DELETE_DOCUMENT, startDate, endDate, username);
	}

	public LogicalSearchQuery newFindCreatedUsersByDateRangeAndByUserQuery(User currentUser, LocalDateTime startDate,
			LocalDateTime endDate, String username) {
		return newFindEventByDateRangeAndByUserQuery(currentUser, EventType.CREATE_USER, startDate, endDate, username);
	}

	public LogicalSearchQuery newFindModifiedUsersByDateRangeAndByUserQuery(User currentUser, LocalDateTime startDate,
			LocalDateTime endDate, String username) {
		return newFindEventByDateRangeAndByUserQuery(currentUser, EventType.MODIFY_USER, startDate, endDate, username);
	}

	public LogicalSearchQuery newFindDeletedUsersByDateRangeAndByUserQuery(User currentUser, LocalDateTime startDate,
			LocalDateTime endDate, String username) {
		return newFindEventByDateRangeAndByUserQuery(currentUser, EventType.DELETE_USER, startDate, endDate, username);
	}

	public LogicalSearchQuery newFindGrantedPermissionsByDateRangeAndByUserQuery(User currentUser, LocalDateTime startDate,
			LocalDateTime endDate, String username) {
		return newFindEventByDateRangeAndByUserQuery(currentUser, EventType.GRANT_PERMISSION, startDate, endDate, username);
	}

	public LogicalSearchQuery newFindModifiedPermissionsByDateRangeAndByUserQuery(User currentUser, LocalDateTime startDate,
			LocalDateTime endDate, String username) {
		return newFindEventByDateRangeAndByUserQuery(currentUser, EventType.MODIFY_PERMISSION, startDate, endDate, username);
	}

	public LogicalSearchQuery newFindDeletedPermissionsByDateRangeAndByUserQuery(User currentUser, LocalDateTime startDate,
			LocalDateTime endDate, String username) {
		return newFindEventByDateRangeAndByUserQuery(currentUser, EventType.DELETE_PERMISSION, startDate, endDate, username);
	}

	public LogicalSearchQuery newFindConsultedFoldersByDateRangeAndByUserQuery(User currentUser, LocalDateTime startDate,
			LocalDateTime endDate, String username) {
		return newFindEventByDateRangeAndByUserQuery(currentUser, EventType.VIEW_FOLDER, startDate, endDate, username);
	}

	public LogicalSearchQuery newFindConsultedDocumentsByDateRangeAndByUserQuery(User currentUser, LocalDateTime startDate,
			LocalDateTime endDate, String username) {
		return newFindEventByDateRangeAndByUserQuery(currentUser, EventType.VIEW_DOCUMENT, startDate, endDate, username);
	}

	public LogicalSearchQuery newFindOpenedSessionsByDateRangeAndByUserQuery(User currentUser, LocalDateTime startDate,
			LocalDateTime endDate, String username) {
		return newFindEventByDateRangeAndByUserQuery(currentUser, EventType.OPEN_SESSION, startDate, endDate, username);
	}

	//By Folder and by date range
	public LogicalSearchQuery newFindCreatedFoldersByDateRangeAndByFolderQuery(User currentUser, LocalDateTime startDate,
			LocalDateTime endDate, Folder folder) {
		return newFindEventByDateRangeAndByFolderQuery(currentUser, EventType.CREATE_FOLDER, startDate, endDate, folder);
	}

	public LogicalSearchQuery newFindModifiedFoldersByDateRangeAndByFolderQuery(User currentUser, LocalDateTime startDate,
			LocalDateTime endDate, Folder folder) {
		return newFindEventByDateRangeAndByFolderQuery(currentUser, EventType.MODIFY_FOLDER, startDate, endDate, folder);
	}

	public LogicalSearchQuery newFindDeletedFoldersByDateRangeAndByFolderQuery(User currentUser, LocalDateTime startDate,
			LocalDateTime endDate, Folder folder) {
		return newFindEventByDateRangeAndByFolderQuery(currentUser, EventType.DELETE_FOLDER, startDate, endDate, folder);
	}


	public LogicalSearchQuery newFindModifiedPermissionsByDateRangeAndByFolderQuery(User currentUser, LocalDateTime startDate,
			LocalDateTime endDate, Folder folder) {
		return newFindEventByDateRangeAndByFolderQuery(currentUser, EventType.MODIFY_PERMISSION, startDate, endDate, folder);
	}

	public LogicalSearchQuery newFindDeletedPermissionsByDateRangeAndByFolderQuery(User currentUser, LocalDateTime startDate,
			LocalDateTime endDate, Folder folder) {
		return newFindEventByDateRangeAndByFolderQuery(currentUser, EventType.DELETE_PERMISSION, startDate, endDate, folder);
	}*/

	public LogicalSearchQuery newFindGrantedPermissionsByDateRangeAndByFolderQuery(User currentUser, LocalDateTime startDate,
			LocalDateTime endDate, Folder folder) {
		return newFindEventByDateRangeAndByFolderQuery(currentUser, EventType.GRANT_PERMISSION, startDate, endDate, folder);
	}

	public LogicalSearchQuery newFindCreatedDocumentsByDateRangeAndByUserQuery(User currentUser, LocalDateTime startDate,
			LocalDateTime endDate, String username) {
		return newFindEventByDateRangeAndByUserQuery(currentUser, EventType.CREATE_DOCUMENT, startDate, endDate, username);
	}

	private LogicalSearchQuery newFindEventQuery(User currentUser, String eventType) {
		Metadata eventTypeMetadata = schemas.eventSchema().getMetadata(Event.TYPE);
		LogicalSearchQuery query = new LogicalSearchQuery();
		query.filteredWithUser(currentUser);
		query.setCondition(
				from(schemas.eventSchema()).where(eventTypeMetadata).isEqualTo(eventType));
		return query;
	}

	public LogicalSearchQuery newFindEventByDateRangeQuery(User currentUser, String eventType, LocalDateTime startDate,
			LocalDateTime endDate) {
		Metadata eventTypeMetadata = schemas.eventSchema().getMetadata(Event.TYPE);
		Metadata eventMetaData = Schemas.CREATED_ON;
		LogicalSearchQuery query = new LogicalSearchQuery();
		query.filteredWithUser(currentUser);
		query.setCondition(
				from(schemas.eventSchema()).where(eventTypeMetadata).isEqualTo(eventType)
						.andWhere(eventMetaData).isValueInRange(
						startDate, endDate));
		return query;
	}

	public LogicalSearchQuery newFindEventByDateRangeAndByFolderQuery(User currentUser, String eventType, LocalDateTime startDate,
			LocalDateTime endDate, String folderId) {

		Folder folder = schemas.wrapFolder(getRecord(folderId));
		return newFindEventByDateRangeAndByFolderQuery(currentUser, eventType, startDate, endDate, folder);
	}

	private Record getRecord(String recordId) {
		RecordServices recordServices = modelLayerFactory.newRecordServices();
		return recordServices.getDocumentById(recordId);
	}

	public LogicalSearchQuery newFindEventByDateRangeAndByFolderQuery(User currentUser, String eventType, LocalDateTime startDate,
			LocalDateTime endDate, Folder folder) {
		Metadata eventTypeMetadata = schemas.eventSchema().getMetadata(Event.TYPE);
		Metadata recordIdMetadata = schemas.eventSchema().getMetadata(Event.RECORD_ID);
		Metadata eventMetaData = Schemas.CREATED_ON;
		String folderId = folder.getId();
		LogicalSearchQuery query = new LogicalSearchQuery();
		query.filteredWithUser(currentUser);
		query.setCondition(
				from(schemas.eventSchema()).where(eventTypeMetadata).isEqualTo(eventType)
						.andWhere(eventMetaData).isValueInRange(
						startDate, endDate).andWhere(recordIdMetadata).isEqualTo(folderId));
		return query;
	}

	public LogicalSearchQuery newFindEventByDateRangeAndByUserIdQuery(User currentUser, String eventType, LocalDateTime startDate,
			LocalDateTime endDate, String userId) {
		Record userRecord = getRecord(userId);
		User user = schemas.wrapUser(userRecord);
		String username = user.getUsername();
		return newFindEventByDateRangeAndByUserQuery(currentUser, eventType, startDate, endDate, username);
	}

	public LogicalSearchQuery newFindEventByDateRangeAndByUserQuery(User currentUser, String eventType, LocalDateTime startDate,
			LocalDateTime endDate, String userName) {
		Metadata eventTypeMetadata = schemas.eventSchema().getMetadata(Event.TYPE);
		Metadata eventMetaData = Schemas.CREATED_ON;
		Metadata userMetaData = schemas.eventSchema().getMetadata(Event.USERNAME);
		LogicalSearchQuery query = new LogicalSearchQuery();
		query.filteredWithUser(currentUser);
		query.setCondition(
				from(schemas.eventSchema()).where(eventTypeMetadata).isEqualTo(eventType)
						.andWhere(eventMetaData).isValueInRange(
						startDate, endDate).andWhere(userMetaData).isEqualTo(userName));
		return query;
	}

	/*public LogicalSearchQuery newFindEventByDateRangeAndByAdministrativeUnitIdQuery(User currentUser, String eventType,
			LocalDateTime startDate,
			LocalDateTime endDate, String administrativeUnitId) {
		Record administrativeUnitRecord = getRecord(administrativeUnitId);
		String filingSpacePath = administrativeUnitRecord.get(Schemas.PRINCIPAL_PATH);
		return newFindEventByDateRangeAndByAdministrativeUnitQuery(currentUser, eventType, startDate, endDate, filingSpacePath);
	}*/

	public LogicalSearchQuery newFindEventByDateRangeAndByAdministrativeUnitQuery(User currentUser, String eventType,
			LocalDateTime startDate,
			LocalDateTime endDate, String id) {
		Metadata eventTypeMetadata = schemas.eventSchema().getMetadata(Event.TYPE);
		Metadata eventMetaData = Schemas.CREATED_ON;
		Metadata principalPathMetadata = schemas.eventSchema().getMetadata(Event.EVENT_PRINCIPAL_PATH);
		String filteringSpacePathWithoutFirstAndLastSeparator = StringUtils.removeEnd(id, "/");
		if (filteringSpacePathWithoutFirstAndLastSeparator.startsWith("/")) {
			filteringSpacePathWithoutFirstAndLastSeparator = StringUtils
					.removeStart("/", filteringSpacePathWithoutFirstAndLastSeparator);
		}
		LogicalSearchQuery query = new LogicalSearchQuery();
		query.filteredWithUser(currentUser);

		LogicalSearchCondition containingId = where(principalPathMetadata).isAny(
				endingWithText("/" + filteringSpacePathWithoutFirstAndLastSeparator),
				containingText("/" + filteringSpacePathWithoutFirstAndLastSeparator + "/"));

		query.setCondition(from(schemas.eventSchema())
				.where(containingId)
				.andWhere(eventTypeMetadata).isEqualTo(eventType)
				.andWhere(eventMetaData).isValueInRange(startDate, endDate));

		return query;
	}

	LogicalSearchQuery newFindAllQuery(User currentUser) {
		LogicalSearchQuery query = new LogicalSearchQuery();
		query.filteredWithUser(currentUser);
		Metadata eventTypeMetadata = schemas.eventSchema().getMetadata(Event.TYPE);
		query.setCondition(
				from(schemas.eventSchema()).where(eventTypeMetadata).isNotNull());
		return query;
	}

	private List<Event> findNotCanceledEventsPerUser(User currentUser, String eventType, String eventTypeCancellation) {
		List<Event> returnList = new ArrayList<Event>();
		LogicalSearchQuery searchedEventsQuery = newFindEventQuery(currentUser, eventType);
		SearchServices searchServices = modelLayerFactory.newSearchServices();
		List<Event> searchedEvents = schemas.wrapEvents(searchServices.search(searchedEventsQuery));
		List<Event> mostRecentEventsPerUser = EventUtils.keepOnlyMostRecentEventPerUser(searchedEvents);
		for (Event event : mostRecentEventsPerUser) {
			String username = event.getUsername();
			LocalDateTime eventDate = event.getCreatedOn();
			LogicalSearchQuery eventCancellationAfterEventDateQuery = newFindEventForUserAfterDateQuery(
					currentUser, eventTypeCancellation, username, eventDate);
			long canceledEventsCount = searchServices.getResultsCount(eventCancellationAfterEventDateQuery);
			if (canceledEventsCount == 0) {
				returnList.add(event);
			}
		}
		return returnList;
	}

	private LogicalSearchQuery newFindEventForUserAfterDateQuery(User currentUser, String eventType, String username,
			LocalDateTime date) {
		Metadata eventTypeMetadata = schemas.eventSchema().getMetadata(Event.TYPE);
		Metadata eventMetaData = Schemas.CREATED_ON;
		Metadata userMetaData = schemas.eventSchema().getMetadata(Event.USERNAME);
		LogicalSearchQuery query = new LogicalSearchQuery();
		query.filteredWithUser(currentUser);
		query.setCondition(
				from(schemas.eventSchema()).where(eventTypeMetadata).isEqualTo(eventType)
						.andWhere(userMetaData).isEqualTo(username)
						.andWhere(eventMetaData).isGreaterOrEqualThan(
						date));
		return query;
	}
}
