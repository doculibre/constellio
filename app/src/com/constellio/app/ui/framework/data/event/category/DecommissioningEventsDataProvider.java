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

public class DecommissioningEventsDataProvider extends AbstractDataProvider implements EventsCategoryDataProvider {

	transient List<EventStatistics> events;

	private LocalDateTime startDate;

	private LocalDateTime endDate;

	private String collection;

	private String currentUserName;

	public DecommissioningEventsDataProvider(ModelLayerFactory modelLayerFactory, String collection,
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

		EventStatistics folderRelocation = new EventStatistics();
		folderRelocation.setLabel($("ListEventsView.folderRelocation"));
		User currentUser = modelLayerFactory.newUserServices().getUserInCollection(currentUserName, collection);
		LogicalSearchQuery query = rmSchemasRecordsServices
				.newFindRelocatedFoldersByDateRangeQuery(currentUser, startDate, endDate);
		folderRelocation.setValue((float) searchServices.getResultsCount(query));
		events.add(folderRelocation);

		EventStatistics folderDeposit = new EventStatistics();
		folderDeposit.setLabel($("ListEventsView.folderDeposit"));
		query = rmSchemasRecordsServices
				.newFindFolderDepositByDateRangeQuery(currentUser, startDate, endDate);
		folderDeposit.setValue((float) searchServices.getResultsCount(query));
		events.add(folderDeposit);

		EventStatistics folderDestruction = new EventStatistics();
		folderDestruction.setLabel($("ListEventsView.folderDestruction"));
		query = rmSchemasRecordsServices
				.newFindDestructedFoldersByDateRangeQuery(currentUser, startDate, endDate);
		folderDestruction.setValue((float) searchServices.getResultsCount(query));
		events.add(folderDestruction);

		EventStatistics pdfAGeneration = new EventStatistics();
		pdfAGeneration.setLabel($("ListEventsView.pdfAGeneration"));
		query = rmSchemasRecordsServices
				.newFindPdfAGenerationEventsByDateRangeQuery(currentUser, startDate, endDate);
		pdfAGeneration.setValue((float) searchServices.getResultsCount(query));
		events.add(pdfAGeneration);

		EventStatistics receiveFolder = new EventStatistics();
		receiveFolder.setLabel($("ListEventsView.receiveFolder"));
		query = rmSchemasRecordsServices
				.newFindReceivedFoldersByDateRangeQuery(currentUser, startDate, endDate);
		receiveFolder.setValue((float) searchServices.getResultsCount(query));
		events.add(receiveFolder);

		EventStatistics receiveContainer = new EventStatistics();
		receiveContainer.setLabel($("ListEventsView.receiveContainer"));
		query = rmSchemasRecordsServices
				.newFindReceivedContainersByDateRangeQuery(currentUser, startDate, endDate);
		receiveContainer.setValue((float) searchServices.getResultsCount(query));
		events.add(receiveContainer);
	}

	@Override
	public String getEventType(Integer index) {
		if(index == 0){
			return EventType.FOLDER_RELOCATION;
		}else if (index == 1){
			return EventType.FOLDER_DEPOSIT;
		}else if (index == 2){
			return EventType.FOLDER_DESTRUCTION;
		} else {//if (index == 3){
			return EventType.PDF_A_GENERATION;
		}/*else if (index == 4){
			return EventType.RECEIVE_FOLDER;
		}else{
			return EventType.RECEIVE_CONTAINER;
		}*/
	}

	public int size() {
		return 4;//6
	}

	@Override
	public String getDataTitle() {
		return $("ListEventsView.decommissioningEvents");
	}

	@Override
	public String getDataReportTitle() {
		return $("");
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
