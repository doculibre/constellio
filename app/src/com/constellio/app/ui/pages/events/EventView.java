package com.constellio.app.ui.pages.events;

import java.util.Map;

import com.constellio.app.ui.pages.base.BaseView;
import com.constellio.app.ui.pages.viewGroups.LogsViewGroup;

public interface EventView extends BaseView, LogsViewGroup {
	
	Map<String, String> getParameters();

}
