package com.constellio.model.services.schemas.validators;

import com.constellio.data.utils.LangUtils;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.frameworks.validation.ValidationErrors;
import com.constellio.model.frameworks.validation.Validator;
import com.constellio.model.services.records.RecordProvider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.constellio.data.utils.LangUtils.isEqual;
import static com.constellio.model.entities.schemas.Schemas.LOGICALLY_DELETED_STATUS;
import static java.lang.Boolean.TRUE;

public class AllowedReferencesValidator implements Validator<Record> {

	public static final String CANNOT_REFERENCE_LOGICALLY_DELETED_RECORD = "cannotReferenceLogicallyDeletedRecord";
	public static final String CANNOT_REFERENCE_ITSELF = "cannotReferenceItself";
	public static final String UNALLOWED_REFERENCE_FOR_METADATA = "unallowedReferenceForMetadata";
	public static final String UNALLOWED_CODE = "unallowedCode";
	public static final String REFERENCED_ID = "referencedId";

	private final List<Metadata> metadatas;
	private final MetadataSchemaTypes schemaTypes;
	private final RecordProvider recordProvider;
	private boolean skippingReferenceToLogicallyDeletedValidation;

	public AllowedReferencesValidator(MetadataSchemaTypes schemaTypes, List<Metadata> metadatas,
									  RecordProvider recordProvider,
									  boolean skippingReferenceToLogicallyDeletedValidation) {
		this.schemaTypes = schemaTypes;
		this.metadatas = metadatas;
		this.recordProvider = recordProvider;
		this.skippingReferenceToLogicallyDeletedValidation = skippingReferenceToLogicallyDeletedValidation;

	}

	@SuppressWarnings("rawtypes")
	@Override
	public void validate(Record record, ValidationErrors validationErrors) {
		Record wasRecord = null;
		if (record.isSaved()) {
			wasRecord = record.getCopyOfOriginalRecord();
		}
		for (Metadata metadata : metadatas) {
			if (metadata.getType() == MetadataValueType.REFERENCE && record.isModified(metadata)
				&& record.get(metadata) != null) {
				if (metadata.isMultivalue()) {
					List<String> wasValue = wasRecord == null ? new ArrayList<String>() : wasRecord.<String>getList(metadata);
					List<String> referencedValues = (List) record.get(metadata);
					List<String> newItems = LangUtils.compare(wasValue, referencedValues).getNewItems();
					for (Object referenceValueStr : referencedValues) {
						if (referenceValueStr != null) {
							Record referencedRecord = recordProvider.getRecord((String) referenceValueStr);
							MetadataSchema schema = getSchema(referencedRecord);
							if (!(metadata.getAllowedReferences().isAllowed(schema))) {
								addValidationErrors(validationErrors, UNALLOWED_REFERENCE_FOR_METADATA, metadata,
										schema.getCode(), (String) referenceValueStr);
							}

							if (record.getId().equals(referenceValueStr)) {
								addValidationErrors(validationErrors, CANNOT_REFERENCE_ITSELF, metadata,
										schema.getCode(), (String) referenceValueStr);
							}

							if (newItems.contains(referenceValueStr) && !skippingReferenceToLogicallyDeletedValidation
								&& TRUE.equals(referencedRecord.get(LOGICALLY_DELETED_STATUS))) {

								addValidationErrors(validationErrors, CANNOT_REFERENCE_LOGICALLY_DELETED_RECORD, metadata,
										schema.getCode(), (String) referenceValueStr);
							}
						}
					}

				} else {
					String referenceValue = record.get(metadata);
					String wasValue = wasRecord == null ? null : wasRecord.<String>get(metadata);
					Record referencedRecord = recordProvider.getRecordSummary(referenceValue);
					MetadataSchema schema = getSchema(referencedRecord);
					if (!(metadata.getAllowedReferences().isAllowed(schema))) {
						addValidationErrors(validationErrors, UNALLOWED_REFERENCE_FOR_METADATA, metadata,
								schema.getCode(), referenceValue);
					}
					if (record.getId().equals(referenceValue)) {
						addValidationErrors(validationErrors, CANNOT_REFERENCE_ITSELF, metadata,
								schema.getCode(), referenceValue);
					}

					if (!isEqual(wasValue, referenceValue) && TRUE.equals(referencedRecord.get(LOGICALLY_DELETED_STATUS))) {

						addValidationErrors(validationErrors, CANNOT_REFERENCE_LOGICALLY_DELETED_RECORD, metadata,
								schema.getCode(), referenceValue);
					}
				}

			}
		}
	}

	private MetadataSchema getSchema(Record referencedRecord) {

		String referencedSchemaCode = referencedRecord.getSchemaCode();
		return schemaTypes.getSchema(referencedSchemaCode);
	}

	public void addValidationErrors(ValidationErrors validationErrors, String code, Metadata metadata,
									String unallowedSchema,
									String referencedId) {
		Map<String, Object> parameters = new HashMap<>();
		parameters.put(METADATA_CODE, metadata.getCode());
		parameters.put(METADATA_LABEL, metadata.getLabelsByLanguageCodes());
		parameters.put(UNALLOWED_CODE, unallowedSchema);
		parameters.put(REFERENCED_ID, referencedId);
		validationErrors.add(getClass(), code, parameters);
	}
}
