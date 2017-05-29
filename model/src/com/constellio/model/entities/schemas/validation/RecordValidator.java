package com.constellio.model.entities.schemas.validation;

import java.io.Serializable;

import com.constellio.model.services.records.RecordValidatorParams;

public interface RecordValidator extends Serializable {

	public void validate(RecordValidatorParams params);
}