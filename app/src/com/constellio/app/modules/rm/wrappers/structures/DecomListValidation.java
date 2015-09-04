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
package com.constellio.app.modules.rm.wrappers.structures;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.joda.time.LocalDate;

import com.constellio.model.entities.schemas.ModifiableStructure;

public class DecomListValidation implements ModifiableStructure {
	String userId;
	LocalDate requestDate;
	LocalDate validationDate;
	boolean dirty;

	public DecomListValidation() {
	}

	public DecomListValidation(String userId, LocalDate requestDate) {
		this.userId = userId;
		this.requestDate = requestDate;
	}

	public String getUserId() {
		return userId;
	}

	public DecomListValidation setUserId(String userId) {
		dirty = true;
		this.userId = userId;
		return this;
	}

	public LocalDate getRequestDate() {
		return requestDate;
	}

	public DecomListValidation setRequestDate(LocalDate requestDate) {
		dirty = true;
		this.requestDate = requestDate;
		return this;
	}

	public LocalDate getValidationDate() {
		return validationDate;
	}

	public DecomListValidation setValidationDate(LocalDate validationDate) {
		dirty = true;
		this.validationDate = validationDate;
		return this;
	}

	public boolean isValidated() {
		return validationDate != null;
	}

	@Override
	public boolean isDirty() {
		return dirty;
	}

	@Override
	public String toString() {
		return "DecomValidations {" +
				"userId='" + userId + '\'' +
				", requestDate=" + requestDate +
				", validationDate=" + validationDate +
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
