/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package com.constellio.model.entities.batchprocess;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.joda.time.LocalDateTime;

public class BatchProcess {

	private final String id;

	private final BatchProcessStatus status;

	private final LocalDateTime requestDateTime;

	private final LocalDateTime startDateTime;

	private final int handledRecordsCount;

	private final int totalRecordsCount;

	private final int errors;

	private final BatchProcessAction action;

	private final String collection;

	public BatchProcess(String id, BatchProcessStatus status, LocalDateTime requestDateTime, LocalDateTime startDateTime,
			int handleRecordsCount, int totalRecordsCount, int errors, BatchProcessAction action, String collection) {
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

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this, "action");
	}

	@Override
	public boolean equals(Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj, "action");
	}

}
