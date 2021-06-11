package com.constellio.app.ui.framework.data.event.category;

import com.constellio.app.modules.rm.services.events.RMEventsSearchServices;
import com.constellio.app.ui.framework.data.AbstractDataProvider;
import com.constellio.app.ui.framework.data.event.EventStatistics;
import com.constellio.app.ui.pages.events.EventsCategoryDataProvider;
import com.constellio.model.entities.records.wrappers.EventType;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import org.joda.time.LocalDateTime;

import java.util.ArrayList;
import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;

public class ByScannedDocumentEventsDataProvider extends AbstractDataProvider implements EventsCategoryDataProvider {

	transient List<EventStatistics> events;

	private LocalDateTime startDate;

	private LocalDateTime endDate;

	private String collection;

	private String currentUserName;

	private ModelLayerFactory model;

	public ByScannedDocumentEventsDataProvider(ModelLayerFactory modelLayerFactory, String collection,
											  String currentUserName,
											  LocalDateTime startDate, LocalDateTime endDate) {
		this.model = modelLayerFactory;
		this.collection = collection;
		this.currentUserName = currentUserName;
		this.startDate = startDate;
		this.endDate = endDate;
	}

	@Override
	public List<EventStatistics> getEvents() {
		if (events == null) {
			init(this.model);
		}
		return events;
	}


	void init(ModelLayerFactory modelLayerFactory) {
		SearchServices searchServices = modelLayerFactory.newSearchServices();
		RMEventsSearchServices rmSchemasRecordsServices = new RMEventsSearchServices(modelLayerFactory, collection);
		User currentUser = modelLayerFactory.newUserServices().getUserInCollection(this.currentUserName, this.collection);
		events = new ArrayList<>();

		EventStatistics acceptedRequest = new EventStatistics();
		acceptedRequest.setLabel($("ListEventsView.scannedDocument"));
		LogicalSearchQuery query = rmSchemasRecordsServices.newFindEventByDateRangeQuery(currentUser, EventType.SCANNED_DOCUMENT, this.startDate, this.endDate);
		acceptedRequest.setValue((float) searchServices.getResultsCount(query));
		events.add(acceptedRequest);
	}

	@Override
	public EventStatistics getEventStatistics(Integer index) {
		return getEvents().get(index);
	}

	@Override
	public int size() {
		return 1;
	}

	@Override
	public String getEventType(Integer index) {
		if (index == 0) {
			return EventType.SCANNED_DOCUMENT;
		} else {
			throw new RuntimeException("Unsupported");
		}
	}

	@Override
	public String getDataTitle() {
		return $("ListEventsView.scannedDocument");
	}

	@Override
	public String getDataReportTitle() {
		return $("ListEventsView.reportTitle.allActivities");
	}

}
