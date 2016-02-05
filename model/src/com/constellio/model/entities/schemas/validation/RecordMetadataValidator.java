package com.constellio.model.entities.schemas.validation;

import com.constellio.model.entities.schemas.ConfigProvider;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.frameworks.validation.ValidationErrors;

public interface RecordMetadataValidator<T> {

	final String METADATA_CODE = "metadataCode";

	final String METADATA_LABEL = "metadataLabel";

	final String METADATA_VALUE = "metadataValue";

	public void validate(Metadata metadata, T value, ConfigProvider configProvider, ValidationErrors validationErrors);
}