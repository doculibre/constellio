package com.constellio.app.modules.es.model.connectors.structures;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.constellio.model.entities.schemas.ModifiableStructure;

public class TraversalSchedule implements ModifiableStructure {
	int weekDay;
	String startTime; // startTime and endTime would be in format "HH:mm", should be easy enough to use later.
	String endTime;
	boolean dirty;

	public TraversalSchedule() {
	}

	public TraversalSchedule(int weekDay, String startTime, String endTime) {
		this.weekDay = weekDay;
		this.startTime = startTime;
		this.endTime = endTime;
	}

	public int getWeekDay() {
		return weekDay;
	}

	public void setWeekDay(int weekDay) {
		dirty = true;
		this.weekDay = weekDay;
	}

	public String getStartTime() {
		return startTime;
	}

	public void setStartTime(String startTime) {
		dirty = true;
		this.startTime = startTime;
	}

	public String getEndTime() {
		return endTime;
	}

	public void setEndTime(String endTime) {
		dirty = true;
		this.endTime = endTime;
	}

	public boolean isValidated() {
		return endTime != null;
	}

	public boolean isValid() {
		return endTime != null && startTime != null;
	}

	@Override
	public boolean isDirty() {
		return dirty;
	}

	@Override
	public String toString() {
		return "TraversalSchedule {" +
				"weekDay='" + weekDay + '\'' +
				", startTime=" + startTime +
				", endTime=" + endTime +
				", dirty=" + dirty +
				'}';
	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this, "dirty");
	}

	@Override
	public boolean equals(Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj, "dirty");
	}

	public boolean hasValuesInAllFields() {
		return weekDay != 0 && StringUtils.isNotBlank(startTime) && StringUtils.isNotBlank(endTime);
	}

	public boolean isEmpty() {
		return weekDay == 0 && StringUtils.isBlank(startTime) && StringUtils.isBlank(endTime);
	}
}
