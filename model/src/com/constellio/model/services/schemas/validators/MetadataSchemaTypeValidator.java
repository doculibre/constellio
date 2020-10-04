package com.constellio.model.services.schemas.validators;

import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.frameworks.validation.ValidationErrors;
import com.constellio.model.frameworks.validation.Validator;

import java.util.HashMap;
import java.util.Map;

public class MetadataSchemaTypeValidator implements Validator<MetadataSchemaType> {

	private static final String NO_TYPE_IN_METADATA_WITHOUT_INHERITANCE = "NoTypeInNonInheritedMetadata";
	private static final String NO_LABEL_IN_METADATA_WITHOUT_INHERITANCE = "NoLabelInNonInheritedMetadata";
	private static final String ALLOWED_REFERENCES_IN_NON_REFERENCE_METADATA = "AllowedReferencesInNonReferenceMetadata";
	private static final String ALLOWED_REFERENCES_IN_REFERENCE_METADATA_NOT_SPECIFIED = "NoAllowedReferencesInReferenceMetadata";
	//TODO Valider que 2 métadonnées de profil custom sans héritage ne peuvent avoir le même nom

	@Override
	public void validate(MetadataSchemaType schemaType, ValidationErrors validationErrors, boolean skipNonEssential) {
		validateDefaultSchema(schemaType.getDefaultSchema(), validationErrors);
		for (MetadataSchema customSchema : schemaType.getCustomSchemas()) {
			validateCustomSchema(customSchema, validationErrors);
		}
	}

	void validateDefaultSchema(MetadataSchema defaultSchema, ValidationErrors validationErrors) {
		for (Metadata metadata : defaultSchema.getMetadatas()) {
			validateMetadata(metadata, validationErrors);
		}
	}

	void validateMetadata(Metadata metadata, ValidationErrors validationErrors) {
		if (metadata.getInheritance() == null) {
			validateMetadataBasicInfo(metadata, validationErrors);
		}
		validateReferenceMetadata(metadata, validationErrors);
	}

	void validateMetadataBasicInfo(Metadata metadata, ValidationErrors validationErrors) {
		validateMetadataLabelNotNull(metadata, validationErrors);
		validateMetadataTypeNotNull(metadata, validationErrors);
	}

	void validateMetadataTypeNotNull(Metadata metadata, ValidationErrors validationErrors) {
		if (metadata.getType() == null) {
			validationErrors.add(getClass(), NO_TYPE_IN_METADATA_WITHOUT_INHERITANCE, createMapWithCode(metadata));
		}
	}

	void validateMetadataLabelNotNull(Metadata metadata, ValidationErrors validationErrors) {
		if (metadata.getLabels() == null || metadata.getLabels().isEmpty()) {
			validationErrors.add(getClass(), NO_LABEL_IN_METADATA_WITHOUT_INHERITANCE, createMapWithCode(metadata));
		}
	}

	Map<String, Object> createMapWithCode(Metadata metadata) {
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("localCode", metadata.getLocalCode());
		parameters.put("label", metadata.getLabelsByLanguageCodes());
		return parameters;
	}

	void validateCustomSchema(MetadataSchema customSchema, ValidationErrors validationErrors) {
		for (Metadata customMetadata : customSchema.getMetadatas()) {
			validateMetadata(customMetadata, validationErrors);
		}
	}

	void validateReferenceMetadata(Metadata metadata, ValidationErrors validationErrors) {
		if (isReferenceType(metadata) && !hasAllowedReferences(metadata)) {
			addNoAllowedReferencesInReferenceMetadataError(metadata, validationErrors);
		} else if (!isReferenceType(metadata) && hasAllowedReferences(metadata)) {
			addAllowedReferencesInNonReferenceMetadataError(metadata, validationErrors);
		}
	}

	private boolean hasAllowedReferences(Metadata metadata) {
		return !metadata.getAllowedReferences().getAllowedSchemas().isEmpty();
	}

	private boolean isReferenceType(Metadata metadata) {
		return metadata.getType() == MetadataValueType.REFERENCE;
	}

	private void addAllowedReferencesInNonReferenceMetadataError(Metadata metadata, ValidationErrors validationErrors) {
		Map<String, Object> parameters = createMapWithCodeLabelAndType(metadata);
		validationErrors.add(getClass(), ALLOWED_REFERENCES_IN_NON_REFERENCE_METADATA, parameters);
	}

	private void addNoAllowedReferencesInReferenceMetadataError(Metadata metadata, ValidationErrors validationErrors) {
		Map<String, Object> parameters = createMapWithCode(metadata);
		validationErrors.add(getClass(), ALLOWED_REFERENCES_IN_REFERENCE_METADATA_NOT_SPECIFIED, parameters);
	}

	Map<String, Object> createMapWithCodeLabelAndType(Metadata metadata) {
		Map<String, Object> parameters = createMapWithCode(metadata);
		parameters.put("type", metadata.getType().toString());
		return parameters;
	}
}
