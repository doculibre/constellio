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

import org.joda.time.LocalDateTime;

import com.constellio.app.modules.rm.services.events.RMEventsSearchServices;
import com.constellio.app.ui.pages.events.EventsCategoryDataProvider;
import com.constellio.model.entities.records.wrappers.EventType;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;

public class ByFolderEventsDataProvider extends DefaultEventsDataProvider implements EventsCategoryDataProvider {

	public ByFolderEventsDataProvider(ModelLayerFactory modelLayerFactory, String collection, String currentUserName,
			LocalDateTime startDate, LocalDateTime endDate, String id) {
		super(modelLayerFactory, collection, currentUserName, startDate, endDate, id);
	}

	@Override
	protected LogicalSearchQuery createSpecificQuery(ModelLayerFactory modelLayerFactory, User currentUser, String eventType,
			LocalDateTime startDate,
			LocalDateTime endDate, String id) {
		RMEventsSearchServices rmSchemasRecordsServices = new RMEventsSearchServices(modelLayerFactory, collection);
		return rmSchemasRecordsServices.newFindEventByDateRangeAndByFolderQuery(currentUser, eventType, startDate, endDate, id);
	}

	@Override
	public String getEventType(Integer index) {
		if(index == 0){
			return EventType.CREATE_FOLDER;
		}else if (index == 1){
			return EventType.MODIFY_FOLDER;
		}else if (index == 2){
			return EventType.VIEW_FOLDER;
		}else if (index == 3){
			return EventType.GRANT_PERMISSION_FOLDER;
		}else if (index == 4){
			return EventType.MODIFY_PERMISSION_FOLDER;
		}else{
			return EventType.DELETE_PERMISSION_FOLDER;
		}
	}

	@Override
	public int specificSize() {
		return 6;
	}

	@Override
	public String getDataTitle() {
		return $("ListEventsView.eventsByFolder");
	}

	@Override
	public String getDataReportTitle() {
		return $("ListEventsView.reportTitle.allActivities");
	}

}
