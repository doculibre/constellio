package com.constellio.app.ui.pages.events;

import com.constellio.app.ui.pages.base.BaseView;
import com.constellio.app.ui.pages.viewGroups.LogsViewGroup;

import java.util.Map;

public interface EventView extends BaseView, LogsViewGroup {

	Map<String, String> getParameters();

}
