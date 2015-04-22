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
package com.constellio.app.ui.pages.events;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

import org.joda.time.LocalDateTime;

import com.constellio.app.ui.application.NavigatorConfigurationService;
import com.constellio.app.ui.params.ParamUtils;

public class EventViewParameters implements Serializable{
	transient public static String EVENT_TYPE = "eventType";
	transient public static  String EVENT_CATEGORY = "eventCategory";
	transient public static String BY_ID_EVENT_PARAMETER = "id";
	transient public static String EVENT_START_DATE = "startDate";
	transient public static String EVENT_END_DATE = "endDate";

	private String parametersString;
	transient private EventCategory eventCategory;
	transient private Date startDate;
	transient private Date endDate;
	transient private String id;
	transient private String eventType;

	public EventViewParameters(String parametersString) {
		this.parametersString = parametersString;
	}

	public EventCategory getEventCategory() {
		if (eventCategory == null){
			buildParameters();
		}
		return eventCategory;
	}

	public Date getEventStartDate() {
		if (startDate == null){
			buildParameters();
		}
		return startDate;
	}

	public Date getEventEndDate() {
		if (endDate == null){
			buildParameters();
		}
		return endDate;
	}

	public String getEventId() {
		if (eventCategory == null){
			buildParameters();
		}
		return id;
	}

	public String getEventType() {
		if (eventType == null){
			buildParameters();
		}
		return eventType;
	}

	private void buildParameters() {
		String viewNameAndParameters = NavigatorConfigurationService.EVENT_CATEGORY + "/" + this.parametersString;
		Map<String, String> parameters = ParamUtils.getParamsMap(viewNameAndParameters);
		String dateString = parameters.get(EventViewParameters.EVENT_START_DATE);
		if (dateString != null){
			this.startDate = LocalDateTime.parse(dateString).toDate();
		}else{
			this.startDate = new LocalDateTime().minusWeeks(1).toDate();
		}
		String endString = parameters.get(EventViewParameters.EVENT_END_DATE);
		if (endString != null){
			this.endDate = LocalDateTime.parse(endString).toDate();
		}else{
			this.endDate = new LocalDateTime().toDate();
		}
		this.eventType = parameters.get(EventViewParameters.EVENT_TYPE);
		String eventCategoryName = parameters.get(EventViewParameters.EVENT_CATEGORY);
		if(eventCategoryName == null){
			eventCategoryName = this.parametersString;
		}
		this.eventCategory = EventCategory.valueOf(eventCategoryName);
		this.id = parameters.get(EventViewParameters.BY_ID_EVENT_PARAMETER);
		if (id == null){
			id = "";
		}
	}

}
