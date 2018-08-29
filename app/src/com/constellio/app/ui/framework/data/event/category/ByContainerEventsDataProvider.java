package com.constellio.app.ui.framework.data.event.category;

import com.constellio.app.modules.rm.services.events.RMEventsSearchServices;
import com.constellio.app.ui.pages.events.EventsCategoryDataProvider;
import com.constellio.model.entities.records.wrappers.EventType;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import org.joda.time.LocalDateTime;

import static com.constellio.app.ui.i18n.i18n.$;

public class ByContainerEventsDataProvider extends DefaultEventsDataProvider implements EventsCategoryDataProvider {

	public ByContainerEventsDataProvider(ModelLayerFactory modelLayerFactory, String collection, String currentUserName,
										 LocalDateTime startDate, LocalDateTime endDate, String id) {
		super(modelLayerFactory, collection, currentUserName, startDate, endDate, id);
	}

	@Override
	protected LogicalSearchQuery createSpecificQuery(ModelLayerFactory modelLayerFactory, User currentUser,
													 String eventType,
													 LocalDateTime startDate,
													 LocalDateTime endDate, String id) {
		RMEventsSearchServices rmSchemasRecordsServices = new RMEventsSearchServices(modelLayerFactory, collection);
		return rmSchemasRecordsServices.newFindEventByDateRangeAndByContainerQuery(currentUser, eventType, startDate, endDate, id);//newFindEventByDateRangeAndByFolderQuery(currentUser, eventType, startDate, endDate, id);
	}

	@Override
	public String getEventType(Integer index) {
		if (index == 0) {
			return EventType.BORROW_CONTAINER;
		} else if (index == 1) {
			return EventType.RETURN_CONTAINER;
		} else if (index == 2) {
			return EventType.BORROW_REQUEST_CONTAINER;
		} else if (index == 3) {
			return EventType.RETURN_REQUEST_CONTAINER;
		} else if (index == 4) {
			return EventType.BORROW_EXTENSION_REQUEST_CONTAINER;
		} else {
			return EventType.REACTIVATION_REQUEST_CONTAINER;
		}
	}

	@Override
	public int specificSize() {
		return 6;
	}

	@Override
	public String getDataTitle() {
		return $("ListEventsView.eventsByContainer");
	}

	@Override
	public String getDataReportTitle() {
		return $("ListEventsView.reportTitle.allActivities");
	}

}
