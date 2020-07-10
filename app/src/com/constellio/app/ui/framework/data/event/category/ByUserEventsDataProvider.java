package com.constellio.app.ui.framework.data.event.category;

import com.constellio.app.modules.rm.services.events.RMEventsSearchServices;
import com.constellio.app.ui.pages.events.EventsCategoryDataProvider;
import com.constellio.model.entities.records.wrappers.EventType;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import org.joda.time.LocalDateTime;

import static com.constellio.app.ui.i18n.i18n.$;

public class ByUserEventsDataProvider extends DefaultEventsDataProvider implements EventsCategoryDataProvider {

	public ByUserEventsDataProvider(ModelLayerFactory modelLayerFactory, String collection, String currentUserName,
									LocalDateTime startDate, LocalDateTime endDate, String id) {
		super(modelLayerFactory, collection, currentUserName, startDate, endDate, id);
	}

	@Override
	protected LogicalSearchQuery createSpecificQuery(ModelLayerFactory modelLayerFactory, User currentUser,
													 String eventType,
													 LocalDateTime startDate,
													 LocalDateTime endDate, String id) {
		RMEventsSearchServices rmSchemasRecordsServices = new RMEventsSearchServices(modelLayerFactory, collection);
		if (eventType.equals(EventType.CURRENTLY_BORROWED_FOLDERS)) {
			return rmSchemasRecordsServices.newFindCurrentlyBorrowedFoldersByUser(currentUser, id);
		} else if (eventType.equals(EventType.LATE_BORROWED_FOLDERS)) {
			return rmSchemasRecordsServices
					.newFindLateBorrowedFoldersByUserAndDateRangeQuery(currentUser, id);
		} else {
			return rmSchemasRecordsServices
					.newFindEventByDateRangeAndByUserIdQuery(currentUser, eventType, startDate, endDate, id);
		}
	}

	@Override
	public String getEventType(Integer index) {
		if (index == 0) {
			return EventType.OPEN_SESSION;
		} else if (index == 1) {
			return EventType.VIEW_FOLDER;
		} else if (index == 2) {
			return EventType.CREATE_FOLDER;
		} else if (index == 3) {
			return EventType.MODIFY_FOLDER;
		} else if (index == 4) {
			return EventType.DELETE_FOLDER;
		} else if (index == 5) {
			return EventType.BORROW_FOLDER;
		} else if (index == 6) {
			return EventType.RETURN_FOLDER;
		} else if (index == 7) {
			return EventType.CONSULTATION_FOLDER;
		} else if (index == 8) {
			return EventType.CURRENTLY_BORROWED_FOLDERS;
		} else if (index == 9) {
			return EventType.LATE_BORROWED_FOLDERS;
		} else if (index == 10) {
			return EventType.VIEW_DOCUMENT;
		} else if (index == 11) {
			return EventType.BORROW_DOCUMENT;
		} else if (index == 12) {
			return EventType.CREATE_DOCUMENT;
		} else if (index == 13) {
			return EventType.MODIFY_DOCUMENT;
		} else if (index == 14) {
			return EventType.DELETE_DOCUMENT;
		} else if (index == 15) {
			return EventType.CREATE_USER;
		} else if (index == 16) {
			return EventType.DELETE_USER;
		} else if (index == 17) {
			return EventType.CREATE_GROUP;
		} else if (index == 18) {
			return EventType.DELETE_GROUP;
		} else if (index == 19) {
			return EventType.GRANT_PERMISSION_FOLDER;
		} else if (index == 20) {
			return EventType.MODIFY_PERMISSION_FOLDER;
		} else if (index == 21) {
			return EventType.DELETE_PERMISSION_FOLDER;
		} else if (index == 22) {
			return EventType.GRANT_PERMISSION_DOCUMENT;
		} else if (index == 23) {
			return EventType.MODIFY_PERMISSION_DOCUMENT;
		} else if (index == 24) {
			return EventType.DELETE_PERMISSION_DOCUMENT;
		} else if (index == 25) {
			return EventType.CREATE_TASK;
		} else if (index == 26) {
			return EventType.MODIFY_TASK;
		} else if (index == 27) {
			return EventType.DELETE_TASK;
		} else if (index == 28) {
			return EventType.CREATE_SHARE_FOLDER;
		} else if (index == 29) {
			return EventType.MODIFY_SHARE_FOLDER;
		} else if (index == 30) {
			return EventType.DELETE_SHARE_FOLDER;
		} else if (index == 31) {
			return EventType.CREATE_SHARE_DOCUMENT;
		} else if (index == 32) {
			return EventType.MODIFY_SHARE_DOCUMENT;
		} else if (index == 33) {
			return EventType.DELETE_SHARE_DOCUMENT;
		} else if (index == 34) {
			return EventType.BATCH_PROCESS_CREATED;
		} else {
			return EventType.SIGN_DOCUMENT;
		}
	}

	@Override
	public int specificSize() {
		return 36;
	}

	@Override
	public String getDataTitle() {
		return $("ListEventsView.eventsByUser");
	}

	@Override
	public String getDataReportTitle() {
		return $("ListEventsView.reportTitle.allActivities");
	}

}
