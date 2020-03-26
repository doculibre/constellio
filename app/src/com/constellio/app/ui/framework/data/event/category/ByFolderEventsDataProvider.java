package com.constellio.app.ui.framework.data.event.category;

import com.constellio.app.modules.rm.services.events.RMEventsSearchServices;
import com.constellio.app.ui.pages.events.EventsCategoryDataProvider;
import com.constellio.model.entities.records.wrappers.EventType;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import org.joda.time.LocalDateTime;

import static com.constellio.app.ui.i18n.i18n.$;

public class ByFolderEventsDataProvider extends DefaultEventsDataProvider implements EventsCategoryDataProvider {

	public ByFolderEventsDataProvider(ModelLayerFactory modelLayerFactory, String collection, String currentUserName,
									  LocalDateTime startDate, LocalDateTime endDate, String id) {
		super(modelLayerFactory, collection, currentUserName, startDate, endDate, id);
	}

	@Override
	protected LogicalSearchQuery createSpecificQuery(ModelLayerFactory modelLayerFactory, User currentUser,
													 String eventType,
													 LocalDateTime startDate,
													 LocalDateTime endDate, String id) {
		RMEventsSearchServices rmSchemasRecordsServices = new RMEventsSearchServices(modelLayerFactory, collection);
		return rmSchemasRecordsServices.newFindEventByDateRangeAndByAdministrativeUnitQuery(currentUser, eventType, startDate, endDate, id);//newFindEventByDateRangeAndByFolderQuery(currentUser, eventType, startDate, endDate, id);
	}

	@Override
	public String getEventType(Integer index) {
		if (index == 0) {
			return EventType.CREATE_FOLDER;
		} else if (index == 1) {
			return EventType.MODIFY_FOLDER;
		} else if (index == 2) {
			return EventType.VIEW_FOLDER;
		} else if (index == 3) {
			return EventType.GRANT_PERMISSION_FOLDER;
		} else if (index == 4) {
			return EventType.MODIFY_PERMISSION_FOLDER;
		} else if (index == 5) {
			return EventType.DELETE_PERMISSION_FOLDER;
		} else if (index == 6) {
			return EventType.CREATE_SHARE_FOLDER;
		} else if (index == 7) {
			return EventType.MODIFY_SHARE_FOLDER;
		} else {
			return EventType.DELETE_SHARE_FOLDER;
		}
	}

	@Override
	public int specificSize() {
		return 9;
	}

	@Override
	public String getDataTitle() {
		return $("ListEventsView.eventsByFolder");
	}

	@Override
	public String getDataReportTitle() {
		return $("ListEventsView.reportTitle.allActivities");
	}

}
