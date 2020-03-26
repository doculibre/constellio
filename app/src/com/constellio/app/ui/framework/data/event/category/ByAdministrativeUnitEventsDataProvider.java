package com.constellio.app.ui.framework.data.event.category;

import com.constellio.app.modules.rm.services.events.RMEventsSearchServices;
import com.constellio.app.ui.pages.events.EventsCategoryDataProvider;
import com.constellio.model.entities.records.wrappers.EventType;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import org.joda.time.LocalDateTime;

import static com.constellio.app.ui.i18n.i18n.$;

public class ByAdministrativeUnitEventsDataProvider extends DefaultEventsDataProvider implements EventsCategoryDataProvider {

	public ByAdministrativeUnitEventsDataProvider(ModelLayerFactory modelLayerFactory, String collection,
												  String currentUserName,
												  LocalDateTime startDate, LocalDateTime endDate, String id) {
		super(modelLayerFactory, collection, currentUserName, startDate, endDate, id);
	}

	@Override
	protected LogicalSearchQuery createSpecificQuery(ModelLayerFactory modelLayerFactory, User currentUser,
													 String eventType,
													 LocalDateTime startDate,
													 LocalDateTime endDate, String id) {
		RMEventsSearchServices rmSchemasRecordsServices = new RMEventsSearchServices(modelLayerFactory, collection);
		return rmSchemasRecordsServices
				.newFindEventByDateRangeAndByAdministrativeUnitQuery(currentUser, eventType, startDate, endDate, id);
	}

	@Override
	public String getEventType(Integer index) {
		if (index == 0) {
			return EventType.CREATE_FOLDER;
		} else if (index == 1) {
			return EventType.MODIFY_FOLDER;
		} else if (index == 2) {
			return EventType.DELETE_FOLDER;
		} else if (index == 3) {
			return EventType.CREATE_DOCUMENT;
		} else if (index == 4) {
			return EventType.MODIFY_DOCUMENT;
		} else if (index == 5) {
			return EventType.DELETE_DOCUMENT;
		} else if (index == 6) {
			return EventType.CREATE_USER;
		} else if (index == 7) {
			return EventType.MODIFY_USER;
		} else if (index == 8) {
			return EventType.DELETE_USER;
		} else if (index == 9) {
			return EventType.GRANT_PERMISSION_FOLDER;
		} else if (index == 10) {
			return EventType.MODIFY_PERMISSION_FOLDER;
		} else if (index == 11) {
			return EventType.DELETE_PERMISSION_FOLDER;
		} else if (index == 12) {
			return EventType.GRANT_PERMISSION_DOCUMENT;
		} else if (index == 13) {
			return EventType.MODIFY_PERMISSION_DOCUMENT;
		} else if (index == 14) {
			return EventType.DELETE_PERMISSION_DOCUMENT;
		} else if (index == 15) {
			return EventType.CREATE_SHARE_FOLDER;
		} else if (index == 16) {
			return EventType.MODIFY_SHARE_FOLDER;
		} else if (index == 17) {
			return EventType.DELETE_SHARE_FOLDER;
		} else if (index == 18) {
			return EventType.CREATE_SHARE_DOCUMENT;
		} else if (index == 19) {
			return EventType.MODIFY_SHARE_DOCUMENT;
		} else if (index == 20) {
			return EventType.DELETE_SHARE_DOCUMENT;
		} else if (index == 21) {
			return EventType.CREATE_TASK;
		} else if (index == 22) {
			return EventType.MODIFY_TASK;
		} else {
			return EventType.DELETE_TASK;
		}
	}

	@Override
	public int specificSize() {
		return 24;
	}

	@Override
	public String getDataTitle() {
		return $("ListEventsView.eventsByAdministrativeUnit");
	}

	@Override
	public String getDataReportTitle() {
		return $("ListEventsView.reportTitle.allActivities");
	}

}
