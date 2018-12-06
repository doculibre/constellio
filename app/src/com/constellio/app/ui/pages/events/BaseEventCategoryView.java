package com.constellio.app.ui.pages.events;

import com.constellio.app.ui.pages.base.BaseView;
import com.constellio.app.ui.pages.viewGroups.LogsViewGroup;

import java.io.InputStream;
import java.util.Date;

public interface BaseEventCategoryView extends BaseView, LogsViewGroup {
	Date getEventStartDate();

	Date getEventEndDate();

	String getEventId();

	String getTitle();

	void startDownload(String filename, final InputStream inputStream, String mimeType);

	EventViewParameters getEventViewParameters();
}
