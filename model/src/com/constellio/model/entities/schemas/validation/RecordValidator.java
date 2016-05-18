package com.constellio.model.entities.schemas.validation;

import com.constellio.model.services.records.RecordValidatorParams;

public interface RecordValidator {

	public void validate(RecordValidatorParams params);
}