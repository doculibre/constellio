package com.constellio.app.ui.framework.containers.event;

import com.constellio.app.ui.framework.containers.RecordVOLazyContainer;
import com.constellio.app.ui.framework.data.RecordVODataProvider;

public class EventContainer extends RecordVOLazyContainer {
	private String eventType;

	public EventContainer(RecordVODataProvider dataProvider, String eventType) {
		super(dataProvider);
		this.eventType = eventType;
	}
}
