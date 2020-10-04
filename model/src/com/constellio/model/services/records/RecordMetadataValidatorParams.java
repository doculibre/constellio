package com.constellio.model.services.records;

import com.constellio.model.entities.schemas.ConfigProvider;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.frameworks.validation.ValidationErrors;

public class RecordMetadataValidatorParams<T> {
	Metadata metadata;
	T value;
	ConfigProvider configProvider;
	ValidationErrors validationErrors;
	boolean skipNonEssential;

	public RecordMetadataValidatorParams(Metadata metadata, T value,
										 ConfigProvider configProvider,
										 ValidationErrors validationErrors, boolean skipNonEssential) {
		this.metadata = metadata;
		this.value = value;
		this.configProvider = configProvider;
		this.validationErrors = validationErrors;
		this.skipNonEssential = skipNonEssential;
	}

	public Metadata getMetadata() {
		return metadata;
	}

	public T getValue() {
		return value;
	}

	public ConfigProvider getConfigProvider() {
		return configProvider;
	}

	public ValidationErrors getValidationErrors() {
		return validationErrors;
	}

	public boolean isSkipNonEssential() {
		return skipNonEssential;
	}
}
