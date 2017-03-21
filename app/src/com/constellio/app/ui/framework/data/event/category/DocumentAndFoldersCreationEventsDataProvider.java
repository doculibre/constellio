package com.constellio.app.ui.framework.data.event.category;

import static com.constellio.app.ui.i18n.i18n.$;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.joda.time.LocalDateTime;

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

public class DocumentAndFoldersCreationEventsDataProvider extends AbstractDataProvider implements EventsCategoryDataProvider {

	transient List<EventStatistics> events;

	private LocalDateTime startDate;

	private LocalDateTime endDate;

	private String collection;

	private String currentUserName;

	public DocumentAndFoldersCreationEventsDataProvider(ModelLayerFactory modelLayerFactory, String collection,
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
		RMEventsSearchServices rmSchemasRecordsServices = new RMEventsSearchServices(modelLayerFactory, collection);
		events = new ArrayList<>();
		EventStatistics foldersCreation = new EventStatistics();
		foldersCreation.setLabel($("ListEventsView.foldersCreation"));
		User currentUser = modelLayerFactory.newUserServices().getUserInCollection(currentUserName, collection);
		LogicalSearchQuery query = rmSchemasRecordsServices
				.newFindCreatedFoldersByDateRangeQuery(currentUser, startDate, endDate);
		foldersCreation.setValue((float) searchServices.getResultsCount(query));
		events.add(foldersCreation);
		EventStatistics documentsCreation = new EventStatistics();
		documentsCreation.setLabel($("ListEventsView.documentsCreation"));
		query = rmSchemasRecordsServices
				.newFindCreatedDocumentsByDateRangeQuery(currentUser, startDate, endDate);
		documentsCreation.setValue((float) searchServices.getResultsCount(query));
		events.add(documentsCreation);

		EventStatistics tasksCreation = new EventStatistics();
		tasksCreation.setLabel($("ListEventsView.createTask"));
		query = rmSchemasRecordsServices
				.newFindEventByDateRangeQuery(currentUser, EventType.CREATE_TASK, startDate, endDate);
		tasksCreation.setValue((float) searchServices.getResultsCount(query));
		events.add(tasksCreation);
	}

	@Override
	public String getEventType(Integer index) {
		if (index == 0) {
			return EventType.CREATE_FOLDER;
		} else if (index == 1) {
			return EventType.CREATE_DOCUMENT;
		} else {
			return EventType.CREATE_TASK;
		}
	}

	public int size() {
		return 3;
	}

	@Override
	public String getDataTitle() {
		return $("ListEventsView.foldersAndDocumentsCreation");
	}

	@Override
	public String getDataReportTitle() {
		return $("ListEventsView.foldersAndDocumentsCreation.reportTitle");
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
