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
