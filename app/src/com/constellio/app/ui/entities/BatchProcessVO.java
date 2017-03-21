package com.constellio.app.ui.entities;

import java.io.Serializable;
import java.util.List;

import org.joda.time.LocalDateTime;

import com.constellio.model.entities.batchprocess.BatchProcessStatus;

public class BatchProcessVO implements Serializable {

	private final String id;

	private final BatchProcessStatus status;

	private final LocalDateTime requestDateTime;

	private final LocalDateTime startDateTime;

	private final int handledRecordsCount;

	private final int totalRecordsCount;

	private final int errors;

	private final String username;

	private final String title;

	private final String collection;

	private final String query;

	private final List<String> records;

	public BatchProcessVO(String id, BatchProcessStatus status, LocalDateTime requestDateTime, LocalDateTime startDateTime,
						int handledRecordsCount, int totalRecordsCount, int errors, String collection,
						String query, List<String> records, String username, String title) {
		super();
		this.id = id;
		this.status = status;
		this.requestDateTime = requestDateTime;
		this.startDateTime = startDateTime;
		this.handledRecordsCount = handledRecordsCount;
		this.totalRecordsCount = totalRecordsCount;
		this.errors = errors;
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
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BatchProcessVO other = (BatchProcessVO) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
	
}
