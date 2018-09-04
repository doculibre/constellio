package com.constellio.model.entities.batchprocess;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.joda.time.LocalDateTime;

import java.util.List;

public class RecordBatchProcess implements BatchProcess {

	private final String id;

	private final BatchProcessStatus status;

	private final LocalDateTime requestDateTime;

	private final LocalDateTime startDateTime;

	private final int handledRecordsCount;

	private final int totalRecordsCount;

	private final int errors;

	private final BatchProcessAction action;

	private final String username;

	private final String title;

	private final String collection;

	private final String query;

	private final List<String> records;

	public RecordBatchProcess(String id, BatchProcessStatus status, LocalDateTime requestDateTime,
							  LocalDateTime startDateTime,
							  int handleRecordsCount, int totalRecordsCount, int errors, BatchProcessAction action,
							  String collection,
							  String query, List<String> records, String username, String title) {
		super();
		this.id = id;
		this.status = status;
		this.requestDateTime = requestDateTime;
		this.startDateTime = startDateTime;
		this.handledRecordsCount = handleRecordsCount;
		this.totalRecordsCount = totalRecordsCount;
		this.errors = errors;
		this.action = action;
		this.collection = collection;
		this.query = query;
		this.records = records;
		this.username = username;
		this.title = title;
	}

	public String getQuery() {
		return query;
	}

	public String getId() {
		return id;
	}

	public LocalDateTime getRequestDateTime() {
		return requestDateTime;
	}

	public LocalDateTime getStartDateTime() {
		return startDateTime;
	}

	public int getHandledRecordsCount() {
		return handledRecordsCount;
	}

	public int getTotalRecordsCount() {
		return totalRecordsCount;
	}

	public BatchProcessStatus getStatus() {
		return status;
	}

	public int getErrors() {
		return errors;
	}

	public BatchProcessAction getAction() {
		return action;
	}

	public String getCollection() {
		return collection;
	}

	public String getUsername() {
		return username;
	}

	public String getTitle() {
		return title;
	}

	public List<String> getRecords() {
		return records;
	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this, "action");
	}

	@Override
	public boolean equals(Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj, "action");
	}

	public RecordBatchProcess withQuery(String query) {
		return new RecordBatchProcess(id, status, requestDateTime, startDateTime, handledRecordsCount, totalRecordsCount, errors,
				action, collection, query, records, username, title);
	}

	public RecordBatchProcess withRecords(List<String> records) {
		return new RecordBatchProcess(id, status, requestDateTime, startDateTime, handledRecordsCount, totalRecordsCount, errors,
				action, collection, query, records, username, title);
	}
}
