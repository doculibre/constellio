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

import java.util.ArrayList;
import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;
import static java.util.Arrays.asList;

public class ReindexAndRestartEventDataProvider extends AbstractDataProvider implements EventsCategoryDataProvider {
	private List<EventStatistics> events;

	private String currentUserName;

	private LocalDateTime startDate;

	private LocalDateTime endDate;

	private String collection;

	public ReindexAndRestartEventDataProvider(ModelLayerFactory modelLayerFactory, String collection,
											  String currentUserName, LocalDateTime startDate, LocalDateTime endDate) {
		this.currentUserName = currentUserName;
		this.startDate = startDate;
		this.endDate = endDate;
		this.collection = collection;
		init(modelLayerFactory);
	}

	@Override
	public String getDataTitle() {
		return $("ListEventsView.reIndexAndRestart");
	}

	@Override
	public String getDataReportTitle() {
		return null;
	}

	@Override
	public String getEventType(Integer index) {
		switch (index) {
			case 0:
				return EventType.RESTARTING;
			case 1:
				return EventType.REINDEXING;
			default:
				throw new RuntimeException("Unsupported");
		}
	}

	public void init(ModelLayerFactory modelLayerFactory) {
		SearchServices searchServices = modelLayerFactory.newSearchServices();
		RMEventsSearchServices rmSchemasRecordsServices = new RMEventsSearchServices(modelLayerFactory, collection);
		User currentUser = modelLayerFactory.newUserServices().getUserInCollection(this.currentUserName, this.collection);
		events = new ArrayList<>();

		EventStatistics restart = new EventStatistics();
		restart.setLabel($("ListEventsView.restarting"));
		LogicalSearchQuery query = rmSchemasRecordsServices.newFindEventByDateRangeQuery(currentUser, EventType.RESTARTING, this.startDate, this.endDate);
		restart.setValue((float) searchServices.getResultsCount(query));

		EventStatistics reindex = new EventStatistics();
		reindex.setLabel($("ListEventsView.reindexing"));
		query = rmSchemasRecordsServices.newFindEventByDateRangeQuery(currentUser, EventType.REINDEXING, this.startDate, this.endDate);
		reindex.setValue((float) searchServices.getResultsCount(query));
		events.addAll(asList(restart, reindex));
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

	@Override
	public int size() {
		return 2;
	}

	@Override
	public void addDataRefreshListener(DataRefreshListener dataRefreshListener) {

	}

	@Override
	public List<DataRefreshListener> getDataRefreshListeners() {
		return null;
	}

	@Override
	public void removeDataRefreshListener(DataRefreshListener dataRefreshListener) {

	}

	@Override
	public void fireDataRefreshEvent() {

	}
}
