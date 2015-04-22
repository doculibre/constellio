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

public class ByUserEventsDataProvider extends DefaultEventsDataProvider implements EventsCategoryDataProvider {

	public ByUserEventsDataProvider(ModelLayerFactory modelLayerFactory, String collection, String currentUserName,
			LocalDateTime startDate, LocalDateTime endDate, String id) {
		super(modelLayerFactory, collection, currentUserName, startDate, endDate, id);
	}

	@Override
	protected LogicalSearchQuery createSpecificQuery(ModelLayerFactory modelLayerFactory, User currentUser, String eventType,
			LocalDateTime startDate,
			LocalDateTime endDate, String id) {
		RMEventsSearchServices rmSchemasRecordsServices = new RMEventsSearchServices(modelLayerFactory, collection);
		return rmSchemasRecordsServices.newFindEventByDateRangeAndByUserIdQuery(currentUser, eventType, startDate, endDate, id);
	}

	@Override
	public String getEventType(Integer index) {
		if (index == 0){
			return EventType.OPEN_SESSION;
		}else if (index == 1){
			return EventType.VIEW_FOLDER;
		}else if (index == 2){
			return EventType.CREATE_FOLDER;
		}else if (index == 3){
			return EventType.MODIFY_FOLDER;
		}else if (index == 4){
			return EventType.DELETE_FOLDER;
		}else if (index == 5){
			return EventType.VIEW_DOCUMENT;
		}else if (index == 6){
			return EventType.BORROW_DOCUMENT;
		}else if (index == 7){
			return EventType.CREATE_DOCUMENT;
		}else if (index == 8){
			return EventType.MODIFY_DOCUMENT;
		}else if (index == 9){
			return EventType.DELETE_DOCUMENT;
		}else if (index == 10){
			return EventType.CREATE_USER;
		}else if (index == 11){
			return EventType.DELETE_USER;
		}else if (index == 12){
			return EventType.CREATE_GROUP;
		}else if (index == 13){
			return EventType.DELETE_GROUP;
		}else if (index == 14){
			return EventType.GRANT_PERMISSION_FOLDER;
		}else if (index == 15){
			return EventType.MODIFY_PERMISSION_FOLDER;
		}else if (index == 16){
			return EventType.DELETE_PERMISSION_FOLDER;
		}else if (index == 17){
			return EventType.GRANT_PERMISSION_DOCUMENT;
		}else if (index == 18){
			return EventType.MODIFY_PERMISSION_DOCUMENT;
		}else{
			return EventType.DELETE_PERMISSION_DOCUMENT;
		}

	}

	@Override
	public int specificSize() {
		return 20;
	}

	@Override
	public String getDataTitle() {
		return $("ListEventsView.eventsByUser");
	}

	@Override
	public String getDataReportTitle() {
		return $("ListEventsView.reportTitle.allActivities");
	}

}
