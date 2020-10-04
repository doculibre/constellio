package com.constellio.model.services.records;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.ConfigProvider;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.validation.RecordValidator;
import com.constellio.model.frameworks.validation.ValidationErrors;
import com.constellio.model.services.search.SearchServices;

public class RecordValidatorParams {

	final Record record;
	final MetadataSchemaTypes types;
	final MetadataSchema schema;
	final RecordValidator validator;
	final ValidationErrors validationErrors;
	final ConfigProvider configProvider;
	final RecordProvider recordProvider;
	final SearchServices searchServices;
	final boolean skipNonEssential;

	public RecordValidatorParams(Record record, MetadataSchemaTypes types,
								 MetadataSchema schema, RecordValidator validator,
								 ValidationErrors validationErrors, ConfigProvider configProvider,
								 RecordProvider recordProvider, SearchServices searchServices,
								 boolean skipNonEssential) {
		this.record = record;
		this.types = types;
		this.schema = schema;
		this.validator = validator;
		this.validationErrors = validationErrors;
		this.configProvider = configProvider;
		this.recordProvider = recordProvider;
		this.searchServices = searchServices;
		this.skipNonEssential = skipNonEssential;
	}

	public Record getValidatedRecord() {
		return record;
	}

	public Record getRecord(String id) {
		return recordProvider.getRecord(id);
	}

	public Record getRecordSummary(String id) {
		return recordProvider.getRecordSummary(id);
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

	public Record getRecord() {
		return record;
	}

	public SearchServices getSearchServices() {
		return searchServices;
	}

}
