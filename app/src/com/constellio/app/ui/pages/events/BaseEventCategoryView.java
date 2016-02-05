package com.constellio.app.ui.pages.events;

import java.util.Date;

import com.constellio.app.ui.pages.base.BaseView;
import com.constellio.app.ui.pages.viewGroups.LogsViewGroup;

public interface BaseEventCategoryView extends BaseView, LogsViewGroup {
	Date getEventStartDate();

	Date getEventEndDate();

	String getEventId();
}
