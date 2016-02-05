package com.constellio.app.ui.framework.data.event.category;

import static com.constellio.app.ui.i18n.i18n.$;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.joda.time.LocalDateTime;

import com.constellio.app.modules.rm.services.events.RMEventsSearchServices;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.framework.data.event.EventStatistics;
import com.constellio.app.ui.pages.events.EventsCategoryDataProvider;
import com.constellio.model.entities.records.wrappers.EventType;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;

/**
 * Created by Nouha on 2015-01-30.
 */
public class BorrowedOrReturnedDocumentsEventsDataProvider implements EventsCategoryDataProvider {

	transient List<EventStatistics> events;

	private LocalDateTime startDate;

	private LocalDateTime endDate;

	private String collection;

	private String currentUserName;

	public BorrowedOrReturnedDocumentsEventsDataProvider(ModelLayerFactory modelLayerFactory, String collection,
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
		EventStatistics borrowedDocuments = new EventStatistics();
		borrowedDocuments.setLabel($("ListEventsView.borrowedDocuments"));
		User currentUser = modelLayerFactory.newUserServices().getUserInCollection(currentUserName, collection);
		LogicalSearchQuery query = rmSchemasRecordsServices
				.newFindBorrowedDocumentsByDateRangeQuery(currentUser, startDate, endDate);
		borrowedDocuments.setValue((float) searchServices.getResultsCount(query));
		events.add(borrowedDocuments);
		EventStatistics returnedDocuments = new EventStatistics();
		returnedDocuments.setLabel($("ListEventsView.returnedDocuments"));
		query = rmSchemasRecordsServices
				.newFindReturnedDocumentsByDateRangeQuery(currentUser, startDate, endDate);
		returnedDocuments.setValue((float) searchServices.getResultsCount(query));
		events.add(returnedDocuments);
	}

	@Override
	public String getEventType(Integer index) {
		if(index == 0){
			return EventType.BORROW_DOCUMENT;
		}else{
			return EventType.RETURN_DOCUMENT;
		}
	}

	public int size() {
		return 2;
	}

	@Override
	public String getDataTitle() {
		return $("ListEventsView.documentsBorrowOrReturn");
	}

	@Override
	public String getDataReportTitle() {
		return "";
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
	public EventStatistics getEventStatistics(Integer index) {
		return getEvents().get(index);
	}
}
