package com.constellio.model.services.records;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.ConfigProvider;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.validation.RecordValidator;
import com.constellio.model.frameworks.validation.ValidationErrors;

public class RecordValidatorParams {

	final Record record;
	final MetadataSchemaTypes types;
	final MetadataSchema schema;
	final RecordValidator validator;
	final ValidationErrors validationErrors;
	final ConfigProvider configProvider;
	final RecordProvider recordProvider;

	public RecordValidatorParams(Record record, MetadataSchemaTypes types,
			MetadataSchema schema, RecordValidator validator,
			ValidationErrors validationErrors, ConfigProvider configProvider,
			RecordProvider recordProvider) {
		this.record = record;
		this.types = types;
		this.schema = schema;
		this.validator = validator;
		this.validationErrors = validationErrors;
		this.configProvider = configProvider;
		this.recordProvider = recordProvider;
	}

	public Record getValidatedRecord() {
		return record;
	}

	public Record getRecord(String id) {
		return recordProvider.getRecord(id);
	}

	public MetadataSchemaTypes getTypes() {
		return types;
	}

	public MetadataSchema getSchema() {
		return schema;
	}

	public RecordValidator getValidator() {
		return validator;
	}

	public ValidationErrors getValidationErrors() {
		return validationErrors;
	}

	public ConfigProvider getConfigProvider() {
		return configProvider;
	}

	public RecordProvider getRecordProvider() {
		return recordProvider;
	}
}
