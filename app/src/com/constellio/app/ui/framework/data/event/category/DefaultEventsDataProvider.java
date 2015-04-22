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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDateTime;

import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.framework.data.event.EventStatistics;
import com.constellio.app.ui.framework.data.event.EventTypeUtils;
import com.constellio.app.ui.pages.events.EventsCategoryDataProvider;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;

public abstract class DefaultEventsDataProvider implements EventsCategoryDataProvider {

	transient List<EventStatistics> events;

	private LocalDateTime startDate;

	private LocalDateTime endDate;

	protected String collection;

	protected String currentUserName;

	private String id;

	public DefaultEventsDataProvider(ModelLayerFactory modelLayerFactory, String collection,
			String currentUserName, LocalDateTime startDate, LocalDateTime endDate, String id) {
		this.collection = collection;
		this.currentUserName = currentUserName;
		this.startDate = startDate;
		this.endDate = endDate;
		this.id = id;
		init(modelLayerFactory);
	}

	private void readObject(ObjectInputStream stream)
			throws IOException, ClassNotFoundException {
		ConstellioFactories constellioFactories = ConstellioFactories.getInstance();
		init(constellioFactories.getModelLayerFactory());
	}

	void init(ModelLayerFactory modelLayerFactory) {
		SearchServices searchServices = modelLayerFactory.newSearchServices();
		events = new ArrayList<>();

		User currentUser = modelLayerFactory.newUserServices().getUserInCollection(currentUserName, collection);

		for (int i = 0; i < size(); i++){
			EventStatistics currentEventStatistics = new EventStatistics();
			String caption = getEventCaption(i);

			currentEventStatistics.setLabel(caption);
			LogicalSearchQuery query = getQuery(i, modelLayerFactory, currentUser);
			currentEventStatistics.setValue((float) searchServices.getResultsCount(query));
			events.add(currentEventStatistics);
		}
	}

	protected LogicalSearchQuery getQuery(int i, ModelLayerFactory modelLayerFactory, User currentUser) {
		String eventType = getEventType(i);
		return createSpecificQuery(modelLayerFactory, currentUser, eventType, startDate, endDate, id);
	}

	protected String getEventCaption(int i) {
		String eventType = getEventType(i);
		return EventTypeUtils.getEventTypeCaption(eventType);
	}

	@Override
	public int size(){
		if(id == null || StringUtils.isBlank(id)){
			return 0;
		}else{
			return specificSize();
		}
	}

	protected abstract int specificSize();

	protected abstract LogicalSearchQuery createSpecificQuery(ModelLayerFactory modelLayerFactory, User currentUser,
			String eventType, LocalDateTime startDate,
			LocalDateTime endDate, String id);

	public List<EventStatistics> getEvents() {
		if (events == null){
			ConstellioFactories constellioFactories = ConstellioFactories.getInstance();
			init(constellioFactories.getModelLayerFactory());
		}
		return events;
	}

	public EventStatistics getEventStatistics(Integer index) {
		return getEvents().get(index);
	}

}
