package com.constellio.app.ui.pages.events;

import java.util.List;

import com.constellio.app.ui.framework.data.DataProvider;
import com.constellio.app.ui.framework.data.event.EventStatistics;

public interface EventsCategoryDataProvider extends DataProvider {
	List<EventStatistics> getEvents();
	EventStatistics getEventStatistics(Integer index);
	int size();
	String getDataTitle();
	String getDataReportTitle();
	String getEventType(Integer index);
}