package com.constellio.model.services.event;

import org.joda.time.LocalDateTime;

import java.io.File;

public class DayProcessedEvent {
	private LocalDateTime localDateTime;
	private File file;
	private String pathToFile;

	public DayProcessedEvent(LocalDateTime localDateTime, File file, String pathToFile) {
		this.localDateTime = localDateTime;
		this.file = file;
		this.pathToFile = pathToFile;
	}

	public File getFile() {
		return file;
	}

	public LocalDateTime getLocalDateTime() {
		return localDateTime;
	}

	public String getPathToFile() {
		return pathToFile;
	}
}
