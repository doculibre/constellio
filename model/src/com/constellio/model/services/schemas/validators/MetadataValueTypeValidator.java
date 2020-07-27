package com.constellio.model.services.schemas.validators;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.frameworks.validation.ValidationErrors;
import com.constellio.model.frameworks.validation.Validator;
import org.joda.time.LocalDateTime;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MetadataValueTypeValidator implements Validator<Record> {

	public static final long MAX_SIZE_FOR_STRING_VALUE = 10000;

	public static final String REQUIRED_VALUE_FOR_METADATA = "requiredValueForMetadata";
	public static final String INVALID_VALUE_FOR_METADATA = "invalidValueForMetadata";
	public static final String EXPECTED_TYPE_MESSAGE_PARAM = "expectedType";
	public static final String METADATA_CODE_MESSAGE_PARAM = "metadataCode";
	public static final String METADATA_LABEL_MESSAGE_PARAM = "metadataLabel";
	public static final String WAS_VALUE_CLASS_MESSAGE_PARAM = "wasValueOfClass";

	private final List<Metadata> metadatas;
	private boolean skipIfNotEssential;

	public MetadataValueTypeValidator(List<Metadata> metadatas, boolean skipIfNotEssential) {
		this.metadatas = metadatas;
		this.skipIfNotEssential = skipIfNotEssential;
	}

	@Override
	public boolean isEssential() {
		return true;
	}

	@Override
	public void validate(Record record, ValidationErrors validationErrors) {
		if (!skipValidation()) {
			for (Metadata metadata : metadatas) {
				Object value = record.get(metadata);
				if (value != null) {
					if (metadata.getType() == MetadataValueType.STRING) {
						verifyTextValue(metadata, value, validationErrors);
					} else if (metadata.getType() == MetadataValueType.BOOLEAN) {
						verifyBooleanValue(metadata, value, validationErrors);
					} else if (metadata.getType() == MetadataValueType.NUMBER) {
						verifyNumberValue(metadata, value, validationErrors);
					} else if (metadata.getType() == MetadataValueType.DATE_TIME) {
						verifyDateValue(metadata, value, validationErrors);
					} else if (metadata.getType() == MetadataValueType.REFERENCE) {
						verifyReferenceValue(metadata, value, validationErrors);
					}
				}
			}
		}
	}

	private boolean skipValidation() {
		return !isEssential() && skipIfNotEssential;
	}

	@SuppressWarnings("rawtypes")
	private void verifyTextValue(Metadata metadata, Object value, ValidationErrors validationErrors) {
		if (metadata.isMultivalue()) {
			List values = (List) value;
			for (Object object : values) {
				if (object != null && !(object instanceof String)) {
					addValidationErrors(metadata, object, validationErrors, "STRING");
				}
			}
		} else if (!(value instanceof String)) {
			addValidationErrors(metadata, value, validationErrors, "STRING");
		}
	}

	@SuppressWarnings("rawtypes")
	private void verifyBooleanValue(Metadata metadata, Object value, ValidationErrors validationErrors) {
		if (metadata.isMultivalue()) {
			List values = (List) value;
			for (Object object : values) {
				if (object != null && !(object instanceof Boolean)) {
					addValidationErrors(metadata, object, validationErrors, "BOOLEAN");
				}
			}
		} else if (!(value instanceof Boolean)) {
			addValidationErrors(metadata, value, validationErrors, "BOOLEAN");
		}
	}

	@SuppressWarnings("rawtypes")
	private void verifyNumberValue(Metadata metadata, Object value, ValidationErrors validationErrors) {
		if (metadata.isMultivalue()) {
			List values = (List) value;
			for (Object object : values) {
				if (object != null && !(object instanceof Number)) {
					addValidationErrors(metadata, object, validationErrors, "NUMBER");
				}
			}
		} else if (!(value instanceof Number)) {
			addValidationErrors(metadata, value, validationErrors, "NUMBER");
		}
	}

	@SuppressWarnings("rawtypes")
	private void verifyDateValue(Metadata metadata, Object value, ValidationErrors validationErrors) {
		if (metadata.isMultivalue()) {
			List values = (List) value;
			for (Object object : values) {
				if (object != null && !(object instanceof LocalDateTime)) {
					addValidationErrors(metadata, object, validationErrors, "DATE_TIME");
				}
			}
		} else if (!(value instanceof LocalDateTime)) {
			addValidationErrors(metadata, value, validationErrors, "DATE_TIME");
		}
	}

	@SuppressWarnings("rawtypes")
	private void verifyReferenceValue(Metadata metadata, Object value, ValidationErrors validationErrors) {
		if (metadata.isMultivalue()) {
			List values = (List) value;
			for (Object object : values) {
				if (object != null && !(object instanceof String)) {
					addValidationErrors(metadata, object, validationErrors, "REFERENCE");
				}
			}
		} else if (!(value instanceof String)) {
			addValidationErrors(metadata, value, validationErrors, "REFERENCE");
		}
	}

	//@formatter:off
	private void addValidationErrors(Metadata metadata, Object value, ValidationErrors validationErrors,
									 String metadataValueType) {
		Map<String, Object> parameters = createParameters(metadata, value, metadataValueType);
		validationErrors.add(getClass(), INVALID_VALUE_FOR_METADATA, parameters);
	}
	//@formatter:on

	private Map<String, Object> createParameters(Metadata metadata, Object value, String expected) {
		Map<String, Object> parameters = new HashMap<>();
		parameters.put(METADATA_CODE_MESSAGE_PARAM, metadata.getCode());
		parameters.put(METADATA_LABEL_MESSAGE_PARAM, metadata.getLabelsByLanguageCodes());
		parameters.put(EXPECTED_TYPE_MESSAGE_PARAM, expected);
		parameters.put(WAS_VALUE_CLASS_MESSAGE_PARAM, value == null ? "null" : value.getClass().getName());
		return parameters;
	}
}
