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

public class UsersAndGroupsAddOrRemoveEventsDataProvider extends DefaultEventsDataProvider implements EventsCategoryDataProvider {

	public UsersAndGroupsAddOrRemoveEventsDataProvider(ModelLayerFactory modelLayerFactory, String collection, String currentUserName,
			LocalDateTime startDate, LocalDateTime endDate) {
		super(modelLayerFactory, collection, currentUserName, startDate, endDate, null);
	}

	@Override
	protected LogicalSearchQuery createSpecificQuery(ModelLayerFactory modelLayerFactory, User currentUser, String eventType,
			LocalDateTime startDate,
			LocalDateTime endDate, String id) {
		RMEventsSearchServices rmSchemasRecordsServices = new RMEventsSearchServices(modelLayerFactory, collection);
		return rmSchemasRecordsServices.newFindEventByDateRangeQuery(currentUser, eventType, startDate, endDate);
	}

	@Override
	public String getEventType(Integer index) {
		if(index == 0){
			return EventType.CREATE_USER;
		}else if (index == 1){
			return EventType.DELETE_USER;
		}else if (index == 2){
			return EventType.CREATE_GROUP;
		}else {
			return EventType.DELETE_GROUP;
		}
	}

	@Override
	public int size() {
		return 4;
	}

	@Override
	protected int specificSize() {
		return size();
	}

	@Override
	public String getDataTitle() {
		return $("ListEventsView.usersAndGroupsAddOrRemoveEvents");
	}

	@Override
	public String getDataReportTitle() {
		return $("ListEventsView.usersAndGroupsAddOrRemoveEvents.allActivities");
	}

}
