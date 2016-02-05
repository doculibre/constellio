package com.constellio.app.ui.framework.data.event.category;

import static com.constellio.app.ui.i18n.i18n.$;

import java.io.IOException;
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

public class CurrentlyBorrowedFoldersEventDataProvider implements EventsCategoryDataProvider {

	transient List<EventStatistics> events;

	private String collection;

	private String currentUserName;
	private ArrayList<String> eventsIds;

	public CurrentlyBorrowedFoldersEventDataProvider(ModelLayerFactory modelLayerFactory, String collection,
			String currentUserName) {
		this.collection = collection;
		this.currentUserName = currentUserName;
		init(modelLayerFactory);
	}

	private void readObject(java.io.ObjectInputStream stream)
			throws IOException, ClassNotFoundException {
		ConstellioFactories constellioFactories = ConstellioFactories.getInstance();
		init(constellioFactories.getModelLayerFactory());
	}

	void init(ModelLayerFactory modelLayerFactory) {
		RMEventsSearchServices rmSchemasRecordsServices = new RMEventsSearchServices(modelLayerFactory, collection);
		events = new ArrayList<>();
		EventStatistics currentlyBorrowedFolders = new EventStatistics();
		currentlyBorrowedFolders.setLabel($("ListEventsView.currentlyBorrowedFolders"));
		User currentUser = modelLayerFactory.newUserServices().getUserInCollection(currentUserName, collection);

		//		LogicalSearchQuery query = rmSchemasRecordsServices
		//				.newFindCurrentlyBorrowedFoldersQuery(currentUser);
		//
		//		long borrowedFoldersCount = modelLayerFactory.newSearchServices().getResultsCount(query);
		//		currentlyBorrowedFolders.setValue((float) borrowedFoldersCount);
		//		events.add(currentlyBorrowedFolders);

		List<Event> borrowedFoldersEvents = rmSchemasRecordsServices
				.findCurrentlyBorrowedFolders(currentUser);

		eventsIds = new ArrayList<>();
		for (Event currentEvent : borrowedFoldersEvents) {
			eventsIds.add(currentEvent.getWrappedRecord().getId());
		}
		currentlyBorrowedFolders.setValue((float) borrowedFoldersEvents.size());
		events.add(currentlyBorrowedFolders);
	}

	public int size() {
		return 1;
	}

	@Override
	public String getDataTitle() {
		return $("ListEventsView.currentlyBorrowedFolders");
	}

	@Override
	public String getDataReportTitle() {
		return $("ListEventsView.currentlyBorrowedFolders.reportTitle");
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
	public String getEventType(Integer index) {
		return EventType.BORROW_FOLDER;
	}
}
