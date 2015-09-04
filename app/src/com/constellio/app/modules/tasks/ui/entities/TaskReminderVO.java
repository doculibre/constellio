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
