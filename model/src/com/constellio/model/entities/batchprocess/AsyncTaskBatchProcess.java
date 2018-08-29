package com.constellio.model.entities.batchprocess;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.joda.time.LocalDateTime;

public class AsyncTaskBatchProcess implements BatchProcess {

	private final String id;

	private final BatchProcessStatus status;

	private final LocalDateTime requestDateTime;

	private final LocalDateTime startDateTime;

	private final int errors;

	private final AsyncTask task;

	private final String username;

	private final String title;

	private final String collection;

	public AsyncTaskBatchProcess(String id, BatchProcessStatus status, LocalDateTime requestDateTime,
								 LocalDateTime startDateTime, int errors, AsyncTask task, String username, String title,
								 String collection) {
		this.id = id;
		this.status = status;
		this.requestDateTime = requestDateTime;
		this.startDateTime = startDateTime;
		this.errors = errors;
		this.task = task;
		this.username = username;
		this.title = title;
		this.collection = collection;
	}

	public String getId() {
		return id;
	}

	public BatchProcessStatus getStatus() {
		return status;
	}

	public LocalDateTime getRequestDateTime() {
		return requestDateTime;
	}

	public LocalDateTime getStartDateTime() {
		return startDateTime;
	}

	public int getErrors() {
		return errors;
	}

	public AsyncTask getTask() {
		return task;
	}

	public String getUsername() {
		return username;
	}

	public String getTitle() {
		return title;
	}

	public String getCollection() {
		return collection;
	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this, "action");
	}

	@Override
	public boolean equals(Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj, "action");
	}

}
