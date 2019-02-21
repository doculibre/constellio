package com.constellio.model.services.event;

import org.joda.time.LocalDateTime;

import java.io.File;

public class DayProcessedEvent {
	private LocalDateTime localDateTime;
	private File file;

	public DayProcessedEvent(LocalDateTime localDateTime, File file) {
		this.localDateTime = localDateTime;
		this.file = file;
	}

	public File getFile() {
		return file;
	}

	public LocalDateTime getLocalDateTime() {
		return localDateTime;
	}
}
