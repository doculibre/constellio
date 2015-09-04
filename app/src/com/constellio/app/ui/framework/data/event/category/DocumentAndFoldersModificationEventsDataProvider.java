/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
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

public class DocumentAndFoldersModificationEventsDataProvider implements EventsCategoryDataProvider {

	transient List<EventStatistics> events;

	private LocalDateTime startDate;

	private LocalDateTime endDate;

	private String collection;

	private String currentUserName;

	public DocumentAndFoldersModificationEventsDataProvider(ModelLayerFactory modelLayerFactory, String collection,
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
		EventStatistics foldersModification = new EventStatistics();
		foldersModification.setLabel($("ListEventsView.foldersModification"));
		User currentUser = modelLayerFactory.newUserServices().getUserInCollection(currentUserName, collection);
		LogicalSearchQuery query = rmSchemasRecordsServices
				.newFindModifiedFoldersByDateRangeQuery(currentUser, startDate, endDate);
		foldersModification.setValue((float) searchServices.getResultsCount(query));
		events.add(foldersModification);
		EventStatistics documentsModification = new EventStatistics();
		documentsModification.setLabel($("ListEventsView.documentsModification"));
		query = rmSchemasRecordsServices
				.newFindModifiedDocumentsByDateRangeQuery(currentUser, startDate, endDate);
		documentsModification.setValue((float) searchServices.getResultsCount(query));
		events.add(documentsModification);

		EventStatistics tasksModification = new EventStatistics();
		tasksModification.setLabel($("ListEventsView.modifyTask"));
		query = rmSchemasRecordsServices
				.newFindEventByDateRangeQuery(currentUser, EventType.MODIFY_TASK, startDate, endDate);
		tasksModification.setValue((float) searchServices.getResultsCount(query));
		events.add(tasksModification);
	}

	public int size() {
		return 3;
	}

	@Override
	public String getDataTitle() {
		return $("ListEventsView.foldersAndDocumentsModification");
	}

	@Override
	public String getDataReportTitle() {
		return $("ListEventsView.foldersAndDocumentsModification.reportTitle");
	}

	@Override
	public String getEventType(Integer index) {
		if (index == 0) {
			return EventType.MODIFY_FOLDER;
		} else if (index == 1) {
			return EventType.MODIFY_DOCUMENT;
		} else {
			return EventType.MODIFY_TASK;
		}
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
