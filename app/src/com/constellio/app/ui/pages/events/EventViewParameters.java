package com.constellio.app.ui.pages.events;

import com.constellio.app.ui.application.NavigatorConfigurationService;
import com.constellio.app.ui.params.ParamUtils;
import org.joda.time.LocalDateTime;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

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
			this.startDate = LocalDateTime.parse(dateString).withTime(0, 0, 0, 0).toDate();
		}else{
			this.startDate = new LocalDateTime().minusWeeks(1).withTime(0, 0, 0, 0).toDate();
		}
		String endString = parameters.get(EventViewParameters.EVENT_END_DATE);
		if (endString != null){
			this.endDate = LocalDateTime.parse(endString).withTime(23, 59, 59, 999).toDate();
		}else{
			this.endDate = new LocalDateTime().withTime(23, 59, 59, 999).toDate();
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
