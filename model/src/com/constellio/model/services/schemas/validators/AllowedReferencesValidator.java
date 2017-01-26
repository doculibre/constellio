package com.constellio.model.services.schemas.validators;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.constellio.model.entities.Language;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.frameworks.validation.ValidationErrors;
import com.constellio.model.frameworks.validation.Validator;
import com.constellio.model.services.records.RecordProvider;

public class AllowedReferencesValidator implements Validator<Record> {

	public static final String CANNOT_REFERENCE_ITSELF = "cannotReferenceItself";
	public static final String UNALLOWED_REFERENCE_FOR_METADATA = "unallowedReferenceForMetadata";
	public static final String UNALLOWED_CODE = "unallowedCode";

	private final List<Metadata> metadatas;
	private final MetadataSchemaTypes schemaTypes;
	private final RecordProvider recordProvider;

	public AllowedReferencesValidator(MetadataSchemaTypes schemaTypes, List<Metadata> metadatas,
			RecordProvider recordProvider) {
		this.schemaTypes = schemaTypes;
		this.metadatas = metadatas;
		this.recordProvider = recordProvider;

	}

	@SuppressWarnings("rawtypes")
	@Override
	public void validate(Record record, ValidationErrors validationErrors) {
		for (Metadata metadata : metadatas) {
			if (metadata.getType() == MetadataValueType.REFERENCE && record.isModified(metadata)
					&& record.get(metadata) != null) {
				if (metadata.isMultivalue()) {
					List referencedValues = (List) record.get(metadata);
					for (Object referenceValueStr : referencedValues) {
						if (referenceValueStr != null) {
							Record referencedRecord = recordProvider.getRecord((String) referenceValueStr);
							MetadataSchema schema = getSchema(referencedRecord);
							if (!(metadata.getAllowedReferences().isAllowed(schema))) {
								addValidationErrors(validationErrors, UNALLOWED_REFERENCE_FOR_METADATA, metadata,
										schema.getCode());
							}

							if (record.getId().equals(referenceValueStr)) {
								addValidationErrors(validationErrors, CANNOT_REFERENCE_ITSELF, metadata,
										schema.getCode());
							}
						}
					}
				} else {
					String referenceValue = record.get(metadata);
					Record referencedRecord = recordProvider.getRecord(referenceValue);
					MetadataSchema schema = getSchema(referencedRecord);
					if (!(metadata.getAllowedReferences().isAllowed(schema))) {
						addValidationErrors(validationErrors, UNALLOWED_REFERENCE_FOR_METADATA, metadata,
								schema.getCode());
					}
					if (record.getId().equals(referenceValue)) {
						addValidationErrors(validationErrors, CANNOT_REFERENCE_ITSELF, metadata,
								schema.getCode());
					}
				}

			}
		}
	}

	private MetadataSchema getSchema(Record referencedRecord) {

		String referencedSchemaCode = referencedRecord.getSchemaCode();
		return schemaTypes.getSchema(referencedSchemaCode);
	}

	public void addValidationErrors(ValidationErrors validationErrors, String code, Metadata metadata, String unallowedSchema) {
		Map<String, Object> parameters = new HashMap<>();
		parameters.put(METADATA_CODE, metadata.getCode());
		parameters.put(METADATA_LABEL, metadata.getLabelsByLanguageCodes());
		parameters.put(UNALLOWED_CODE, unallowedSchema);
		validationErrors.add(getClass(), code, parameters);
	}
}
