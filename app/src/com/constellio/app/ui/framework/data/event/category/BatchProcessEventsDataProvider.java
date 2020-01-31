package com.constellio.app.ui.framework.data.event.category;

import com.constellio.app.modules.rm.services.events.RMEventsSearchServices;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.framework.data.AbstractDataProvider;
import com.constellio.app.ui.framework.data.event.EventStatistics;
import com.constellio.app.ui.pages.events.EventsCategoryDataProvider;
import com.constellio.model.entities.records.wrappers.EventType;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import org.joda.time.LocalDateTime;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;

public class BatchProcessEventsDataProvider extends AbstractDataProvider implements EventsCategoryDataProvider {

	transient List<EventStatistics> events;

	private LocalDateTime startDate;

	private LocalDateTime endDate;

	private String collection;

	private String currentUserName;

	public BatchProcessEventsDataProvider(ModelLayerFactory modelLayerFactory, String collection,
										  String currentUserName, LocalDateTime startDate,
										  LocalDateTime endDate) {
		this.collection = collection;
		this.currentUserName = currentUserName;
		this.startDate = startDate;
		this.endDate = endDate;
		init(modelLayerFactory);
	}

	private void readObject(java.io.ObjectInputStream stream)
			throws IOException, ClassNotFoundException {
		ConstellioFactories constellioFactories = ConstellioFactories.getInstance();
		init(constellioFactories.getModelLayerFactory());
	}

	void init(ModelLayerFactory modelLayerFactory) {
		SearchServices searchServices = modelLayerFactory.newSearchServices();
		RMEventsSearchServices rmSchemasRecordsServices = new RMEventsSearchServices(modelLayerFactory, collection);
		events = new ArrayList<>();
		EventStatistics createdBatchProcess = new EventStatistics();
		createdBatchProcess.setLabel($("ListEventsView.batchProcessEvents.created"));
		User currentUser = modelLayerFactory.newUserServices().getUserInCollection(currentUserName, collection);
		LogicalSearchQuery query = rmSchemasRecordsServices
				.newFindCreatedBatchProcessesByDateRangeQuery(currentUser, startDate, endDate);
		createdBatchProcess.setValue((float) searchServices.getResultsCount(query));
		events.add(createdBatchProcess);
	}

	@Override
	public String getEventType(Integer index) {
		return EventType.BATCH_PROCESS_CREATED;
	}

	public int size() {
		return 1;
	}

	@Override
	public String getDataTitle() {
		return $("ListEventsView.batchProcessEvents");
	}

	@Override
	public String getDataReportTitle() {
		return "";
	}

	@Override
	public List<EventStatistics> getEvents() {
		if (events == null) {
			ConstellioFactories constellioFactories = ConstellioFactories.getInstance();
			init(constellioFactories.getModelLayerFactory());
		}
		return events;
	}

	@Override
	public EventStatistics getEventStatistics(Integer index) {
		return getEvents().get(index);
	}
}
