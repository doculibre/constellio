package com.constellio.app.modules.tasks.ui.entities;

import java.io.Serializable;

import org.joda.time.LocalDate;

public class TaskReminderVO implements Serializable {
	LocalDate fixedDate;
	int numberOfDaysToRelativeDate;
	Boolean beforeRelativeDate;
	String relativeDateMetadataCode;

	public TaskReminderVO(LocalDate fixedDate, int numberOfDaysToRelativeDate,
			String relativeDateMetadataCode, Boolean beforeFlexibleDate) {
		this.fixedDate = fixedDate;
		this.numberOfDaysToRelativeDate = numberOfDaysToRelativeDate;
		this.relativeDateMetadataCode = relativeDateMetadataCode;
		this.beforeRelativeDate = beforeFlexibleDate;
	}

	public TaskReminderVO() {

	}

	public LocalDate getFixedDate() {
		return fixedDate;
	}

	public void setFixedDate(LocalDate fixedDate) {
		this.fixedDate = fixedDate;
	}

	public int getNumberOfDaysToRelativeDate() {
		return numberOfDaysToRelativeDate;
	}

	public void setNumberOfDaysToRelativeDate(int numberOfDaysToRelativeDate) {
		this.numberOfDaysToRelativeDate = numberOfDaysToRelativeDate;
	}

	public Boolean getBeforeRelativeDate() {
		return beforeRelativeDate;
	}

	public void setBeforeRelativeDate(Boolean beforeRelativeDate) {
		this.beforeRelativeDate = beforeRelativeDate;
	}

	public String getRelativeDateMetadataCode() {
		return relativeDateMetadataCode;
	}

	public void setRelativeDateMetadataCode(String relativeDateMetadataCode) {
		this.relativeDateMetadataCode = relativeDateMetadataCode;
	}
}
