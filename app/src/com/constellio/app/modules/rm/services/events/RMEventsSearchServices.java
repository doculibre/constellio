package com.constellio.app.modules.rm.services.events;

import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.services.borrowingServices.BorrowingType;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.data.utils.TimeProvider;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.Event;
import com.constellio.model.entities.records.wrappers.EventType;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.LogicalSearchValueCondition;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.model.services.security.AuthorizationsServices;
import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDateTime;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.constellio.model.entities.records.wrappers.Event.EVENT_PRINCIPAL_PATH;
import static com.constellio.model.services.contents.ContentFactory.checkedOut;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.containingText;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.endingWithText;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.startingWithText;

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

	public LogicalSearchQuery newFindFailedLoginsByDateRangeQuery(User currentUser, LocalDateTime startDate,
																  LocalDateTime endDate) {
		return newFindEventByDateRangeQuery(currentUser, EventType.ATTEMPTED_OPEN_SESSION, startDate, endDate);
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
		return new LogicalSearchQuery(from(schemas.documentSchemaType()).where(schemas.documentContent()).is(checkedOut()))
				.filteredWithUser(currentUser).sortAsc(Schemas.TITLE);
	}

	public LogicalSearchQuery newFindCurrentlyBorrowedFoldersQuery(User currentUser) {
		return new LogicalSearchQuery(from(schemas.folder.schemaType())
				.where(schemas.folder.borrowed()).isTrue()
				.andWhere(schemas.folder.borrowingType()).is(BorrowingType.BORROW))
				.filteredWithUser(currentUser).sortAsc(Schemas.TITLE);
	}

	public LogicalSearchQuery newFindCurrentlyBorrowedFoldersByUser(User currentUser, String userId) {
		LogicalSearchCondition condition = from(schemas.folder.schemaType())
				.where(schemas.folder.borrowed()).isTrue()
				.andWhere(schemas.folder.borrowingType()).is(BorrowingType.BORROW)
				.andWhere(schemas.folder.borrowUserEntered()).is(userId);
		return new LogicalSearchQuery(condition).filteredWithUser(currentUser).sortAsc(Schemas.TITLE);
	}

	public LogicalSearchQuery newFindLateBorrowedFoldersByUserAndDateRangeQuery(User currentUser, String userId) {
		LogicalSearchCondition condition = from(schemas.folder.schemaType())
				.where(schemas.folder.borrowed()).isTrue().andWhere(schemas.folder.borrowUserEntered())
				.is(userId).andWhere(schemas.folder.borrowPreviewReturnDate())
				.isLessThan(TimeProvider.getLocalDate());
		return new LogicalSearchQuery(condition).filteredWithUser(currentUser).sortAsc(Schemas.TITLE);
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
		return newFindEventsByDateRangeQuery(currentUser, startDate, endDate, EventType.BORROW_FOLDER, EventType.CONSULTATION_FOLDER);
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
	public LogicalSearchQuery newFindCreatedFoldersByDateRangeAndByFilingSpaceQuery(User currentUser,
																					LocalDateTime startDate,
																					LocalDateTime endDate,
																					String principalPath) {
		return newFindEventByDateRangeAndByAdministrativeUnitQuery(currentUser, EventType.CREATE_FOLDER, startDate, endDate,
				principalPath);
	}

	public LogicalSearchQuery newFindGrantedPermissionsByDateRangeAndByFolderQuery(User currentUser,
																				   LocalDateTime startDate,
																				   LocalDateTime endDate,
																				   Folder folder) {
		return newFindEventByDateRangeAndByFolderQuery(currentUser, EventType.GRANT_PERMISSION, startDate, endDate, folder);
	}

	public LogicalSearchQuery newFindCreatedDocumentsByDateRangeAndByUserQuery(User currentUser,
																			   LocalDateTime startDate,
																			   LocalDateTime endDate, String username) {
		return newFindEventByDateRangeAndByUserQuery(currentUser, EventType.CREATE_DOCUMENT, startDate, endDate, username);
	}

	private LogicalSearchQuery newFindEventQuery(User currentUser, String eventType) {
		Metadata type = schemas.eventSchema().getMetadata(Event.TYPE);
		Metadata timestamp = Schemas.CREATED_ON;

		return new LogicalSearchQuery(fromEventsAccessibleBy(currentUser).andWhere(type).isEqualTo(eventType))
				.sortDesc(timestamp);
	}

	public LogicalSearchQuery newFindEventByDateRangeQuery(User currentUser, String eventType, LocalDateTime startDate,
														   LocalDateTime endDate) {
		Metadata type = schemas.eventSchema().getMetadata(Event.TYPE);
		Metadata timestamp = Schemas.CREATED_ON;

		LogicalSearchCondition condition = fromEventsAccessibleBy(currentUser)
				.andWhere(type).isEqualTo(eventType).andWhere(timestamp).isValueInRange(startDate, endDate);
		return new LogicalSearchQuery(condition).sortDesc(timestamp);
	}

	public LogicalSearchQuery newFindEventsByDateRangeQuery(User currentUser, LocalDateTime startDate,
															LocalDateTime endDate, String... eventType) {
		Metadata type = schemas.eventSchema().getMetadata(Event.TYPE);
		Metadata timestamp = Schemas.CREATED_ON;

		LogicalSearchCondition condition = fromEventsAccessibleBy(currentUser)
				.andWhere(type).isIn(Arrays.asList(eventType)).andWhere(timestamp).isValueInRange(startDate, endDate);
		return new LogicalSearchQuery(condition).sortDesc(timestamp);
	}

	public LogicalSearchQuery newFindEventByDateRangeAndByFolderQuery(User currentUser, String eventType,
																	  LocalDateTime startDate,
																	  LocalDateTime endDate, Folder folder) {
		Metadata type = schemas.eventSchema().getMetadata(Event.TYPE);
		Metadata recordId = schemas.eventSchema().getMetadata(Event.RECORD_ID);
		Metadata timestamp = Schemas.CREATED_ON;

		LogicalSearchCondition condition = fromEventsAccessibleBy(currentUser)
				.andWhere(type).isEqualTo(eventType)
				.andWhere(timestamp).isValueInRange(startDate, endDate)
				.andWhere(recordId).isEqualTo(folder);
		return new LogicalSearchQuery(condition).sortDesc(timestamp);
	}

	public LogicalSearchQuery newFindEventByDateRangeAndByContainerQuery(User currentUser, String eventType,
																		 LocalDateTime startDate,
																		 LocalDateTime endDate, String id) {
		Metadata type = schemas.eventSchema().getMetadata(Event.TYPE);
		Metadata recordId = schemas.eventSchema().getMetadata(Event.RECORD_ID);
		Metadata timestamp = Schemas.CREATED_ON;

		LogicalSearchCondition condition = fromEventsAccessibleBy(currentUser)
				.andWhere(type).isEqualTo(eventType)
				.andWhere(timestamp).isValueInRange(startDate, endDate)
				.andWhere(recordId).isEqualTo(id);
		return new LogicalSearchQuery(condition).sortDesc(timestamp);
	}

	public LogicalSearchQuery newFindEventByDateRangeAndByUserIdQuery(User currentUser, String eventType,
																	  LocalDateTime startDate,
																	  LocalDateTime endDate, String userId) {
		User user = schemas.getUser(userId);
		String username = user.getUsername();
		return newFindEventByDateRangeAndByUserQuery(currentUser, eventType, startDate, endDate, username);
	}

	public LogicalSearchQuery newFindEventByDateRangeAndByUserQuery(User currentUser, String eventType,
																	LocalDateTime startDate,
																	LocalDateTime endDate, String userName) {
		Metadata type = schemas.eventSchema().getMetadata(Event.TYPE);
		Metadata timestamp = Schemas.CREATED_ON;
		Metadata user = schemas.eventSchema().getMetadata(Event.USERNAME);

		LogicalSearchCondition condition = fromEventsAccessibleBy(currentUser)
				.andWhere(type).isEqualTo(eventType)
				.andWhere(timestamp).isValueInRange(startDate, endDate)
				.andWhere(user).isEqualTo(userName);
		return new LogicalSearchQuery(condition).sortDesc(timestamp);
	}

	public LogicalSearchQuery newFindEventByDateRangeAndByAdministrativeUnitQuery(User currentUser, String eventType,
																				  LocalDateTime startDate,
																				  LocalDateTime endDate, String id) {
		Metadata type = schemas.eventSchema().getMetadata(Event.TYPE);
		Metadata timestamp = Schemas.CREATED_ON;
		Metadata principalPath = schemas.eventSchema().getMetadata(EVENT_PRINCIPAL_PATH);

		String filteringSpacePathWithoutFirstAndLastSeparator = StringUtils.removeEnd(id, "/");
		if (filteringSpacePathWithoutFirstAndLastSeparator.startsWith("/")) {
			filteringSpacePathWithoutFirstAndLastSeparator = StringUtils
					.removeStart("/", filteringSpacePathWithoutFirstAndLastSeparator);
		}

		LogicalSearchCondition condition = fromEventsAccessibleBy(currentUser)
				.andWhere(principalPath).isAny(
						endingWithText("/" + filteringSpacePathWithoutFirstAndLastSeparator),
						containingText("/" + filteringSpacePathWithoutFirstAndLastSeparator + "/"))
				.andWhere(type).isEqualTo(eventType)
				.andWhere(timestamp).isValueInRange(startDate, endDate);
		return new LogicalSearchQuery(condition).sortDesc(timestamp);
	}

	private List<Event> findNotCanceledEventsPerUser(User currentUser, String eventType, String eventTypeCancellation) {
		List<Event> returnList = new ArrayList<>();
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
		Metadata type = schemas.eventSchema().getMetadata(Event.TYPE);
		Metadata timestamp = Schemas.CREATED_ON;
		Metadata user = schemas.eventSchema().getMetadata(Event.USERNAME);

		LogicalSearchCondition condition = fromEventsAccessibleBy(currentUser)
				.andWhere(type).isEqualTo(eventType)
				.andWhere(user).isEqualTo(username)
				.andWhere(timestamp).isGreaterOrEqualThan(date);
		return new LogicalSearchQuery(condition).sortDesc(timestamp);
	}

	private LogicalSearchCondition fromEventsAccessibleBy(User user) {
		Metadata eventPrincipalPath = schemas.eventSchema().getMetadata(EVENT_PRINCIPAL_PATH);
		AuthorizationsServices authenticationService = schemas.getModelLayerFactory().newAuthorizationsServices();

		if (user.has(CorePermissions.VIEW_EVENTS).globally()) {
			return from(schemas.eventSchemaType()).returnAll();
		} else {
			SearchServices searchServices = schemas.getModelLayerFactory().newSearchServices();
			List<String> ids;

			ids = authenticationService.getConceptsForWhichUserHasPermission(CorePermissions.VIEW_EVENTS, user);

			List<LogicalSearchValueCondition> ofTheseAdministrativeUnits = new ArrayList<>();
			for (String id : ids) {
				Record concept = modelLayerFactory.newRecordServices().getDocumentById(id);

				List<String> paths = concept.get(Schemas.PATH);

				if (!paths.isEmpty()) {
					ofTheseAdministrativeUnits.add(startingWithText(paths.get(0)));
				}
			}

			return ofTheseAdministrativeUnits.isEmpty() ?
				   null :
				   from(schemas.eventSchemaType()).where(eventPrincipalPath).isAny(ofTheseAdministrativeUnits);
		}
	}

	public LogicalSearchQuery newFindEventByRecordIDQuery(User currentUser, String recordID) {
		Metadata metadataRecordID = schemas.eventSchema().getMetadata(Event.RECORD_ID);
		Metadata timestamp = Schemas.CREATED_ON;

		LogicalSearchCondition condition = fromEventsAccessibleBy(currentUser);
		return condition == null ? LogicalSearchQuery.returningNoResults() : new LogicalSearchQuery(condition.andWhere(metadataRecordID).isEqualTo(recordID))
				.sortDesc(timestamp);
	}

	public LogicalSearchQuery exceptEventTypes(LogicalSearchQuery query, List<String> eventTypesToExclude) {
		Metadata metadataEventType = schemas.eventSchema().getMetadata(Event.TYPE);
		query.setCondition(query.getCondition().andWhere(metadataEventType).isNotIn(eventTypesToExclude));
		return query;
	}

}
