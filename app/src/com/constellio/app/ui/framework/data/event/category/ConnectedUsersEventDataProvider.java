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

import com.constellio.app.modules.rm.services.events.RMEventsSearchServices;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.framework.data.event.EventStatistics;
import com.constellio.app.ui.pages.events.EventsCategoryDataProvider;
import com.constellio.model.entities.records.wrappers.Event;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.factories.ModelLayerFactory;

public class ConnectedUsersEventDataProvider implements EventsCategoryDataProvider {

	transient List<EventStatistics> events;

	private List<String> eventsIds;

	private String collection;

	private String currentUserName;

	public ConnectedUsersEventDataProvider(ModelLayerFactory modelLayerFactory, String collection,
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
		EventStatistics connectedUsersStatistic = new EventStatistics();
		connectedUsersStatistic.setLabel($("ListEventsView.connectedUsersEvent"));
		User currentUser = modelLayerFactory.newUserServices().getUserInCollection(currentUserName, collection);
		List<Event> connectedUsersEvents = rmSchemasRecordsServices
				.findLoggedUsers(currentUser);
		eventsIds = new ArrayList<>();
		for(Event currentEvent : connectedUsersEvents){
			eventsIds.add(currentEvent.getWrappedRecord().getId());
		}
		connectedUsersStatistic.setValue((float) connectedUsersEvents.size());
		events.add(connectedUsersStatistic);
	}

	@Override
	public String getEventType(Integer index) {
		return "";
	}

	public int size() {
		return 1;
	}

	@Override
	public String getDataTitle() {
		return $("ListEventsView.connectedUsersEvent");
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
