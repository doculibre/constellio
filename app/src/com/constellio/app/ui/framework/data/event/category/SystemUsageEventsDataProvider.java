package com.constellio.app.ui.framework.data.event.category;

import com.constellio.app.modules.rm.services.events.RMEventsSearchServices;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.framework.data.AbstractDataProvider;
import com.constellio.app.ui.framework.data.event.EventStatistics;
import com.constellio.app.ui.pages.events.EventsCategoryDataProvider;
import com.constellio.model.entities.records.wrappers.Collection;
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


public class SystemUsageEventsDataProvider extends AbstractDataProvider implements EventsCategoryDataProvider {

	transient List<EventStatistics> events;

	private LocalDateTime startDate;

	private LocalDateTime endDate;

	private String collection;

	private String currentUserName;

	public SystemUsageEventsDataProvider(ModelLayerFactory modelLayerFactory, String collection,
										 String currentUserName, LocalDateTime startDate, LocalDateTime endDate) {
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
		RMEventsSearchServices rmEvents = new RMEventsSearchServices(modelLayerFactory, collection);
		RMEventsSearchServices rmSystemEvents = new RMEventsSearchServices(modelLayerFactory, Collection.SYSTEM_COLLECTION);
		events = new ArrayList<>();

		EventStatistics openedSessions = new EventStatistics();
		openedSessions.setLabel($("ListEventsView.openedSessions"));
		openedSessions.setType(EventType.OPEN_SESSION);

		EventStatistics failedLogins = new EventStatistics();
		failedLogins.setLabel($("ListEventsView.failedLogins"));
		failedLogins.setType(EventType.ATTEMPTED_OPEN_SESSION);

		User currentUser = modelLayerFactory.newUserServices().getUserInCollection(currentUserName, collection);

		LogicalSearchQuery openedSessionQuery = rmEvents
				.newFindOpenedSessionsByDateRangeQuery(currentUser, startDate, endDate);
		openedSessions.setValue((float) searchServices.getResultsCount(openedSessionQuery));
		events.add(openedSessions);

		LogicalSearchQuery failedLoginsQuery = rmSystemEvents
				.newFindFailedLoginsByDateRangeQuery(currentUser, startDate, endDate);
		failedLogins.setValue((float) searchServices.getResultsCount(failedLoginsQuery));
		events.add(failedLogins);
	}

	public int size() {
		return events.size();
	}

	@Override
	public String getDataTitle() {
		return $("ListEventsView.systemUsage");
	}

	@Override
	public String getDataReportTitle() {
		return $("ListEventsView.systemUsage.reportTitle");
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
	public String getEventType(Integer index) {
		return events.get(index).getType();
	}

	@Override
	public EventStatistics getEventStatistics(Integer index) {
		return getEvents().get(index);
	}
}
