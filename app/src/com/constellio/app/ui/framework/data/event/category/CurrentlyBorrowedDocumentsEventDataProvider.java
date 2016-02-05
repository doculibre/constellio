package com.constellio.app.ui.framework.data.event.category;

import static com.constellio.app.ui.i18n.i18n.$;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;

import com.constellio.app.modules.rm.services.events.RMEventsSearchServices;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.framework.data.event.EventStatistics;
import com.constellio.app.ui.pages.events.EventsCategoryDataProvider;
import com.constellio.model.entities.records.wrappers.Event;
import com.constellio.model.entities.records.wrappers.EventType;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;

public class CurrentlyBorrowedDocumentsEventDataProvider implements EventsCategoryDataProvider {

	transient List<EventStatistics> events;

	private String collection;

	private String currentUserName;
	private ArrayList<String> eventsIds;

	public CurrentlyBorrowedDocumentsEventDataProvider(ModelLayerFactory modelLayerFactory, String collection,
			String currentUserName) {
		this.collection = collection;
		this.currentUserName = currentUserName;
		init(modelLayerFactory);
	}

	private void readObject(ObjectInputStream stream)
			throws IOException, ClassNotFoundException {
		ConstellioFactories constellioFactories = ConstellioFactories.getInstance();
		init(constellioFactories.getModelLayerFactory());
	}

	void init(ModelLayerFactory modelLayerFactory) {
		RMEventsSearchServices rmSchemasRecordsServices = new RMEventsSearchServices(modelLayerFactory, collection);

		events = new ArrayList<>();
		EventStatistics currentlyBorrowedDocuments = new EventStatistics();
		currentlyBorrowedDocuments.setLabel($("ListEventsView.currentlyBorrowedDocuments"));
		User currentUser = modelLayerFactory.newUserServices().getUserInCollection(currentUserName, collection);
		LogicalSearchQuery query = rmSchemasRecordsServices
				.newFindCurrentlyBorrowedDocumentsQuery(currentUser);
		long borrowedDocumentsCount = modelLayerFactory.newSearchServices().getResultsCount(query);
		currentlyBorrowedDocuments.setValue((float) borrowedDocumentsCount);
		events.add(currentlyBorrowedDocuments);
	}

	public int size() {
		return 1;
	}

	@Override
	public String getDataTitle() {
		return $("ListEventsView.currentlyBorrowedDocuments");
	}

	@Override
	public String getDataReportTitle() {
		return $("ListEventsView.currentlyBorrowedDocuments.reportTitle");
	}

	@Override
	public List<EventStatistics> getEvents() {
		if (events == null){
			ConstellioFactories constellioFactories = ConstellioFactories.getInstance();
			init(constellioFactories.getModelLayerFactory());
		}
		return events;
	}

	@Override
	public String getEventType(Integer index) {
		return EventType.CURRENT_BORROW_DOCUMENT;
	}

	@Override
	public EventStatistics getEventStatistics(Integer index) {
		return getEvents().get(index);
	}
}
