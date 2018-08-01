package com.constellio.model.entities.schemas.validation;

import com.constellio.model.services.records.RecordValidatorParams;

import java.io.Serializable;

public interface RecordValidator extends Serializable {

	public void validate(RecordValidatorParams params);
}