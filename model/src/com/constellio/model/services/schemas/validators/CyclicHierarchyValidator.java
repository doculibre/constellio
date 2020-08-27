package com.constellio.model.services.schemas.validators;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.frameworks.validation.ValidationErrors;
import com.constellio.model.frameworks.validation.Validator;
import com.constellio.model.services.records.RecordProvider;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CyclicHierarchyValidator implements Validator<Record> {

	public static final String CANNOT_REFERENCE_A_DESCENDANT_IN_A_CHILD_OF_REFERENCE = "cannotReferenceADescendantInAChildOfReference";
	public static final String UNALLOWED_CODE = "unallowedCode";

	private final List<Metadata> metadatas;
	private final MetadataSchemaTypes schemaTypes;
	private final RecordProvider recordProvider;

	public CyclicHierarchyValidator(MetadataSchemaTypes schemaTypes, List<Metadata> metadatas,
									RecordProvider recordProvider) {
		this.schemaTypes = schemaTypes;
		this.metadatas = metadatas;
		this.recordProvider = recordProvider;

	}

	@Override
	public void validate(Record record, ValidationErrors validationErrors) {
		for (Metadata metadata : metadatas) {

			if (metadata.getType() == MetadataValueType.REFERENCE && record.isModified(metadata)
				&& record.get(metadata) != null) {
				if (!metadata.isMultivalue()) {
					String referenceValue = record.get(metadata);
					Record referencedRecord = recordProvider.getRecordSummary(referenceValue);
					MetadataSchema schema = getSchema(referencedRecord);

					List<String> ids = new ArrayList<>();

					String referencedId = referenceValue;
					while (referencedId != null) {
						if (ids.contains(referencedId)) {
							referencedId = null;
						} else {
							ids.add(referencedId);
							Record referencedRecordSummary = recordProvider.getRecordSummary(referencedId);
							MetadataSchema referencedRecordSummarySchema = getSchema(record);
							referencedId = referencedRecordSummary.getParentId(referencedRecordSummarySchema);
						}
					}

					if (metadata.isChildOfRelationship()) {
						if (ids.contains(record.getId())) {
							addValidationErrors(validationErrors, CANNOT_REFERENCE_A_DESCENDANT_IN_A_CHILD_OF_REFERENCE,
									metadata, schema.getCode());
						}

					}
				}

			}

		}
	}

	private boolean isInPrincipalPath(String recordId, String principalPath) {
		boolean inPrincipalPath = false;
		if (principalPath != null) {
			String[] principalPathSplitted = StringUtils.split(principalPath, "/");
			for (String principalPathPart : principalPathSplitted) {
				if (principalPathPart.equals(recordId)) {
					inPrincipalPath = true;
					break;
				}
			}
		}
		return inPrincipalPath;
	}

	private MetadataSchema getSchema(Record referencedRecord) {

		String referencedSchemaCode = referencedRecord.getSchemaCode();
		return schemaTypes.getSchema(referencedSchemaCode);
	}

	public void addValidationErrors(ValidationErrors validationErrors, String code, Metadata metadata,
									String unallowedSchema) {
		Map<String, Object> parameters = new HashMap<>();
		parameters.put(METADATA_CODE, metadata.getCode());
		parameters.put(METADATA_LABEL, metadata.getLabelsByLanguageCodes());
		parameters.put(UNALLOWED_CODE, unallowedSchema);
		validationErrors.add(getClass(), code, parameters);
	}
}
