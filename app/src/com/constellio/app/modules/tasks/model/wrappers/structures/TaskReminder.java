package com.constellio.app.modules.tasks.model.wrappers.structures;

import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.model.entities.schemas.ModifiableStructure;
import com.constellio.model.entities.schemas.Schemas;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;

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

		Object object = task.get(relativeDateMetadataCode);

		if (object == null) {
			return null;
		}
		LocalDate relativeDate;

		if (object instanceof LocalDate) {
			relativeDate = (LocalDate) object;
		} else {
			relativeDate = ((LocalDateTime) task.get(relativeDateMetadataCode)).toLocalDate();
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

	public boolean isRelativeToCreationDate() {
		String relativeDateMetadataLocaleCode = getLocaleCode(relativeDateMetadataCode);
		return relativeDateMetadataLocaleCode.equals(Schemas.CREATED_ON.getLocalCode());
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

	public LocalDate computeDate(LocalDateTime creationDate, LocalDate startDate, LocalDate endDate) {
		if (getFixedDate() != null) {
			return getFixedDate();
		}
		LocalDate baseComputationDate;
		if (isRelativeToCreationDate()) {
			if (creationDate == null) {
				return null;
			}
			baseComputationDate = creationDate.toLocalDate();
		} else if (isRelativeToStartDate()) {
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
