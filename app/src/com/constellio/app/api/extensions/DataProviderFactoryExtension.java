package com.constellio.app.api.extensions;

import com.constellio.app.ui.pages.events.EventsCategoryDataProvider;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.joda.time.LocalDateTime;

public abstract class DataProviderFactoryExtension {

	public EventsCategoryDataProvider getEventsListDataProviderFactory(
			EventsListDataProviderFactoryExtensionParams params) {
		return null;
	}

	@AllArgsConstructor
	@Getter
	public static class EventsListDataProviderFactoryExtensionParams {
		private final String eventCategory;
		private final LocalDateTime startDate;
		private final LocalDateTime endDate;
		private final String currentUserName;
		private final String id;
	}

}
