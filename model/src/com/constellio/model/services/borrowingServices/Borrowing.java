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
package com.constellio.model.services.borrowingServices;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.joda.time.LocalDateTime;

import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.ModifiableStructure;

public class Borrowing implements ModifiableStructure {

	BorrowingType borrowingType;
	String borrowerId;
	String borrowerUsername;
	String returnerId;
	String returnerUsername;
	LocalDateTime borrowDateTime;
	LocalDateTime returnDateTime;
	LocalDateTime previewReturnDateTime;
	boolean dirty;

	public Borrowing(BorrowingType borrowingType, User borrower, LocalDateTime borrowDateTime,
			LocalDateTime previewReturnDateTime,
			LocalDateTime returnDateTime, User returner) {
		this.borrowingType = borrowingType;
		this.borrowerId = borrower.getId();
		this.borrowerUsername = borrower.getUsername();
		this.borrowDateTime = borrowDateTime;
		this.previewReturnDateTime = previewReturnDateTime;
		this.returnDateTime = returnDateTime;
		this.returnerId = returner.getId();
		this.returnerUsername = returner.getUsername();
	}

	public Borrowing() {
	}

	public BorrowingType getBorrowingType() {
		return borrowingType;
	}

	public void setBorrowingType(BorrowingType borrowingType) {
		dirty = true;
		this.borrowingType = borrowingType;
	}

	public String getReturnerId() {
		return returnerId;
	}

	public String getReturnerUsername() {
		return returnerUsername;
	}

	public String getBorrowerId() {
		return borrowerId;
	}

	public String getBorrowerUsername() {
		return borrowerUsername;
	}

	public void setBorrower(User user) {
		dirty = true;
		if (user != null) {
			borrowerId = user.getId();
			borrowerUsername = user.getUsername();
		}
	}

	public LocalDateTime getBorrowDateTime() {
		return borrowDateTime;
	}

	public void setBorrowDateTime(LocalDateTime borrowDateTime) {
		dirty = true;
		this.borrowDateTime = borrowDateTime;
	}

	public LocalDateTime getReturnDateTime() {
		return returnDateTime;
	}

	public void setReturnDateTime(LocalDateTime returnDateTime) {
		dirty = true;
		this.returnDateTime = returnDateTime;
	}

	public LocalDateTime getPreviewReturnDateTime() {
		return previewReturnDateTime;
	}

	public void setPreviewReturnDateTime(LocalDateTime previewReturnDateTime) {
		dirty = true;
		this.previewReturnDateTime = previewReturnDateTime;
	}

	@Override
	public boolean isDirty() {
		return dirty;
	}

	@Override
	public String toString() {
		return "Borrowing{" +
				"borrowingType='" + borrowingType + '\'' +
				", borrowUserId='" + borrowerId + '\'' +
				", borrowerUsername='" + borrowerUsername + '\'' +
				", borrowDateTime=" + borrowDateTime +
				", returnDateTime=" + returnDateTime +
				", previewReturnDateTime=" + previewReturnDateTime +
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
}
