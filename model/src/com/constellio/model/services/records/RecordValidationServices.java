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
package com.constellio.model.services.records;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.schemas.ConfigProvider;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.entries.DataEntryType;
import com.constellio.model.entities.schemas.validation.RecordMetadataValidator;
import com.constellio.model.entities.schemas.validation.RecordValidator;
import com.constellio.model.frameworks.validation.ValidationErrors;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.schemas.SchemaUtils;
import com.constellio.model.services.schemas.validators.AllowedReferencesValidator;
import com.constellio.model.services.schemas.validators.CyclicHierarchyValidator;
import com.constellio.model.services.schemas.validators.MetadataChildOfValidator;
import com.constellio.model.services.schemas.validators.MetadataUniqueValidator;
import com.constellio.model.services.schemas.validators.MetadataUnmodifiableValidator;
import com.constellio.model.services.schemas.validators.MetadataValueTypeValidator;
import com.constellio.model.services.schemas.validators.RecordPermissionValidator;
import com.constellio.model.services.schemas.validators.ValueRequirementValidator;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.security.AuthorizationsServices;

public class RecordValidationServices {

	private final MetadataSchemasManager schemasManager;
	private final AuthorizationsServices authorizationServices;
	private final ConfigProvider configProvider;
	private SearchServices searchService;

	public RecordValidationServices(ConfigProvider configProvider, MetadataSchemasManager schemasManager, SearchServices searchService) {
		this(configProvider, schemasManager, searchService, null);
	}

	public RecordValidationServices(ConfigProvider configProvider, MetadataSchemasManager schemasManager, SearchServices searchService,
			AuthorizationsServices authorizationsServices) {
		this.configProvider = configProvider;
		this.schemasManager = schemasManager;
		this.searchService = searchService;
		this.authorizationServices = authorizationsServices;
	}

	public void validateManualMetadatas(Record record, RecordProvider recordProvider, Transaction transaction)
			throws RecordServicesException.ValidationException {
		ValidationErrors validationErrors = validateManualMetadatasReturningErrors(record, recordProvider, transaction);
		if (!validationErrors.getValidationErrors().isEmpty()) {
			throw new RecordServicesException.ValidationException(record, validationErrors);
		}
	}

	public void validateCyclicReferences(Record record, RecordProvider recordProvider, Transaction transaction)
			throws RecordServicesException.ValidationException {

		MetadataSchemaTypes schemaTypes = schemasManager.getSchemaTypes(record.getCollection());
		MetadataSchema schema = schemaTypes.getSchema(record.getSchemaCode());
		List<Metadata> metadatas = getManualMetadatas(schema);
		ValidationErrors validationErrors = new ValidationErrors();
		new CyclicHierarchyValidator(schemaTypes, metadatas, recordProvider).validate(record, validationErrors);

		if (!validationErrors.getValidationErrors().isEmpty()) {
			throw new RecordServicesException.ValidationException(record, validationErrors);
		}
	}

	public void validateAutomaticMetadatas(Record record, RecordProvider recordProvider, Transaction transaction)
			throws RecordServicesException.ValidationException {
		ValidationErrors validationErrors = validateAutomaticMetadatasReturningErrors(record, recordProvider, transaction);
		if (!validationErrors.getValidationErrors().isEmpty()) {
			throw new RecordServicesException.ValidationException(record, validationErrors);
		}
	}

	public void validateSchemaUsingCustomSchemaValidator(Record record, RecordProvider recordProvider, Transaction transaction)
			throws RecordServicesException.ValidationException {
		this.validateUsingCustomSchemaValidators(record, recordProvider);

		if (hasSecurityOnSchema(record)) {
			ValidationErrors validationErrors = validateUsingSecurityValidatorsReturningErrors(record, transaction);
			if (!validationErrors.getValidationErrors().isEmpty()) {
				throw new RecordServicesException.ValidationException(record, validationErrors);
			}
		}

	}

	public void validateUsingCustomSchemaValidators(Record record, RecordProvider recordProvider)
			throws RecordServicesException.ValidationException {

		ValidationErrors validationErrors = validateUsingCustomSchemaValidatorsReturningErrors(record);
		if (!validationErrors.getValidationErrors().isEmpty()) {
			throw new RecordServicesException.ValidationException(record, validationErrors);
		}
	}

	boolean hasSecurityOnSchema(Record record) {
		String schemaTypeCode = new SchemaUtils().getSchemaTypeCode(record.getSchemaCode());
		MetadataSchemaType schemaType = schemasManager.getSchemaTypes(record.getCollection()).getSchemaType(schemaTypeCode);
		return schemaType.hasSecurity();
	}

	List<Metadata> getManualMetadatas(MetadataSchema schema) {
		List<Metadata> manualMetadatas = new ArrayList<>();
		for (Metadata metadata : schema.getMetadatas()) {
			if (metadata.getDataEntry().getType() == DataEntryType.MANUAL && !metadata.isSystemReserved()) {
				manualMetadatas.add(metadata);
			}
		}
		return manualMetadatas;
	}

	ValidationErrors validateManualMetadatasReturningErrors(Record record, RecordProvider recordProvider,
			Transaction transaction) {
		MetadataSchemaTypes schemaTypes = schemasManager.getSchemaTypes(record.getCollection());
		MetadataSchema schema = schemaTypes.getSchema(record.getSchemaCode());
		List<Metadata> metadatas = getManualMetadatas(schema);
		return validateMetadatasReturningErrors(record, recordProvider, schemaTypes, metadatas, transaction);
	}

	ValidationErrors validateAutomaticMetadatasReturningErrors(Record record, RecordProvider recordProvider,
			Transaction transaction) {
		MetadataSchemaTypes schemaTypes = schemasManager.getSchemaTypes(record.getCollection());
		MetadataSchema schema = schemaTypes.getSchema(record.getSchemaCode());
		List<Metadata> metadatas = schema.getAutomaticMetadatas();
		return validateMetadatasReturningErrors(record, recordProvider, schemaTypes, metadatas, transaction);
	}

	ValidationErrors validateMetadatasReturningErrors(Record record, RecordProvider recordProvider,
			MetadataSchemaTypes schemaTypes, List<Metadata> metadatas, Transaction transaction) {
		ValidationErrors validationErrors = new ValidationErrors();
		new AllowedReferencesValidator(schemaTypes, metadatas, recordProvider).validate(record, validationErrors);
		new MetadataValueTypeValidator(metadatas).validate(record, validationErrors);
		if (!transaction.isSkippingRequiredValuesValidation()) {
			new ValueRequirementValidator(metadatas).validate(record, validationErrors);
		}
		new MetadataUnmodifiableValidator(metadatas).validate(record, validationErrors);
		new MetadataUniqueValidator(metadatas, schemaTypes, searchService).validate(record, validationErrors);
		new MetadataChildOfValidator(metadatas, schemaTypes).validate(record, validationErrors);
		return validationErrors;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	ValidationErrors validateUsingCustomSchemaValidatorsReturningErrors(Record record) {
		final ValidationErrors validationErrors = new ValidationErrors();
		String schemaCode = record.getSchemaCode();
		MetadataSchemaTypes types = schemasManager.getSchemaTypes(record.getCollection());
		MetadataSchema schema = types.getSchema(schemaCode);

		for (RecordValidator validator : schema.getValidators()) {
			callSchemaValidator(record, types, schema, validator, validationErrors);
		}
		for (final Metadata metadata : schema.getMetadatas()) {
			Set<RecordMetadataValidator<Object>> validators = (Set) metadata.getValidators();
			for (RecordMetadataValidator<Object> validator : validators) {
				callMetadataValidator(record, metadata, validator, validationErrors);
			}
		}
		return validationErrors;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	ValidationErrors validateUsingSecurityValidatorsReturningErrors(Record record, Transaction transaction) {
		final ValidationErrors validationErrors = new ValidationErrors();

		new RecordPermissionValidator(transaction, authorizationServices).validate(record, validationErrors);

		return validationErrors;
	}

	private void callMetadataValidator(Record record, final Metadata metadata,
			RecordMetadataValidator<Object> validator, final ValidationErrors validationErrors) {
		final Object value = record.get(metadata);
		ValidationErrors validationErrorsWithFailedMetadataParameters = new ValidationErrors() {
			@Override
			public void add(Class<?> validatorClass, String code, Map<String, String> parameters) {
				parameters.put("metadataCode", metadata.getCode());
				parameters.put("metadataLabel", metadata.getLabel());
				parameters.put("metadataValue", value.toString());
				validationErrors.add(validatorClass, code, parameters);
			}
		};
		validator.validate(metadata, value,configProvider, validationErrorsWithFailedMetadataParameters);
	}

	private void callSchemaValidator(Record record, MetadataSchemaTypes types, final MetadataSchema schema,
			RecordValidator validator, final ValidationErrors validationErrors) {

		validator.validate(record, types, schema, configProvider, new ValidationErrors() {
			@Override
			public void add(Class<?> validatorClass, String code, Map<String, String> parameters) {
				parameters.put("schemaCode", schema.getCode());
				parameters.put("schemaLabel", schema.getLabel());
				validationErrors.add(validatorClass, code, parameters);
			}
		});
	}
}
