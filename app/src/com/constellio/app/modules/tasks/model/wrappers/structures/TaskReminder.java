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
package com.constellio.app.modules.tasks.model.wrappers.structures;

import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.model.entities.schemas.ModifiableStructure;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.joda.time.LocalDate;

public class TaskReminder implements ModifiableStructure {
	private boolean dirty;
	LocalDate fixedDate;
	int numberOfDaysToRelativeDate;
	Boolean beforeRelativeDate;
	String relativeDateMetadataCode;
	boolean processed = false;

	public TaskReminder setDirty(boolean dirty) {
		this.dirty = dirty;
		return this;
	}

	public LocalDate getFixedDate() {
		return fixedDate;
	}

	public TaskReminder setFixedDate(LocalDate fixedDate) {
		dirty = true;
		this.fixedDate = fixedDate;
		return this;
	}

	public int getNumberOfDaysToRelativeDate() {
		return numberOfDaysToRelativeDate;
	}

	public TaskReminder setNumberOfDaysToRelativeDate(int numberOfDaysToRelativeDate) {
		dirty = true;
		this.numberOfDaysToRelativeDate = numberOfDaysToRelativeDate;
		return this;
	}

	public Boolean isBeforeRelativeDate() {
		return beforeRelativeDate;
	}

	public TaskReminder setBeforeRelativeDate(Boolean beforeRelativeDate) {
		dirty = true;
		this.beforeRelativeDate = beforeRelativeDate;
		return this;
	}

	public String getRelativeDateMetadataCode() {
		return relativeDateMetadataCode;
	}

	public TaskReminder setRelativeDateMetadataCode(String relativeDateMetadataCode) {
		dirty = true;
		this.relativeDateMetadataCode = relativeDateMetadataCode;
		return this;
	}

	public boolean isProcessed() {
		return processed;
	}

	public TaskReminder setProcessed(boolean processed) {
		dirty = true;
		this.processed = processed;
		return this;
	}

	public LocalDate computeDate(Task task) {
		if (fixedDate != null) {
			return fixedDate;
		}
		LocalDate relativeDate = task.get(relativeDateMetadataCode);
		if (relativeDate == null) {
			return null;
		}
		if (beforeRelativeDate) {
			return relativeDate.minusDays(numberOfDaysToRelativeDate);
		} else {
			return relativeDate.plusDays(numberOfDaysToRelativeDate);
		}
	}

	@Override
	public boolean isDirty() {
		return dirty;
	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this, "dirty");
	}

	@Override
	public boolean equals(Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj, "dirty");
	}

	public boolean isRelativeToStartDate() {
		String relativeDateMetadataLocaleCode = getLocaleCode(relativeDateMetadataCode);
		return relativeDateMetadataLocaleCode.equals(Task.START_DATE);
	}

	private String getLocaleCode(String relativeDateMetadataCode) {
		if (StringUtils.isBlank(relativeDateMetadataCode)) {
			return "";
		}
		if (relativeDateMetadataCode.contains("_")) {
			return StringUtils.substringAfterLast(relativeDateMetadataCode, "_");
		} else {
			return relativeDateMetadataCode;
		}
	}

	public boolean isRelativeToDueDate() {
		String relativeDateMetadataLocaleCode = getLocaleCode(relativeDateMetadataCode);
		return relativeDateMetadataLocaleCode.equals(Task.DUE_DATE);
	}

	public LocalDate computeDate(LocalDate startDate, LocalDate endDate) {
		if (getFixedDate() != null) {
			return getFixedDate();
		}
		LocalDate baseComputationDate;
		if (isRelativeToStartDate()) {
			if (startDate == null) {
				return null;
			}
			baseComputationDate = startDate;
		} else if (isRelativeToDueDate()) {
			if (endDate == null) {
				return null;
			}
			baseComputationDate = endDate;
		} else {
			throw new InvalidRelativeDateRuntimeException(relativeDateMetadataCode);
		}
		if (beforeRelativeDate != null && beforeRelativeDate) {
			return baseComputationDate.minusDays(numberOfDaysToRelativeDate);
		} else {
			return baseComputationDate.plusDays(numberOfDaysToRelativeDate);
		}
	}

	public static class InvalidRelativeDateRuntimeException extends RuntimeException {
		public InvalidRelativeDateRuntimeException(String flexibleDateMetadataCode) {
			super(flexibleDateMetadataCode);
		}
	}
}
