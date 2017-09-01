package com.constellio.model.entities.batchprocess;

import org.joda.time.LocalDateTime;

public interface BatchProcess {

	String getId();

	LocalDateTime getRequestDateTime();

	LocalDateTime getStartDateTime();

	BatchProcessStatus getStatus();

	String getCollection();

	String getUsername();

	String getTitle();

	int getErrors();
}
