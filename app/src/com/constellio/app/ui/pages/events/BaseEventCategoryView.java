package com.constellio.app.ui.pages.events;

import com.constellio.app.ui.pages.base.BaseView;
import com.constellio.app.ui.pages.viewGroups.LogsViewGroup;

import java.util.Date;

public interface BaseEventCategoryView extends BaseView, LogsViewGroup {
	Date getEventStartDate();

	Date getEventEndDate();

	String getEventId();
}