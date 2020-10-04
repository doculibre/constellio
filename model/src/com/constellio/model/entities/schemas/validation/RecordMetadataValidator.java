package com.constellio.model.entities.schemas.validation;

import com.constellio.model.services.records.RecordMetadataValidatorParams;

import java.io.Serializable;

public interface RecordMetadataValidator<T> extends Serializable {

	final String METADATA_CODE = "metadataCode";

	final String METADATA_LABEL = "metadataLabel";

	final String METADATA_VALUE = "metadataValue";

	public void validate(RecordMetadataValidatorParams recordMetadataValidatorParams);
}