package com.constellio.model.services.event;

import org.joda.time.LocalDateTime;

public class DayProcessedEvent {
	private LocalDateTime localDateTime;

	public DayProcessedEvent(LocalDateTime localDateTime) {
		this.localDateTime = localDateTime;
	}

	public LocalDateTime getLocalDateTime() {
		return localDateTime;
	}
}
