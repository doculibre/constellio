package com.constellio.model.services.schemas.validators;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.frameworks.validation.ValidationErrors;
import com.constellio.model.frameworks.validation.Validator;
import com.constellio.model.utils.MaskUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MaskedMetadataValidator implements Validator<Record> {
	private static final Logger LOGGER = LoggerFactory.getLogger(MaskedMetadataValidator.class);
	public static final String VALUE_INCOMPATIBLE_WITH_SPECIFIED_MASK = "valueIncompatibleWithSpecifiedMask";
	public static final String METADATA_LABEL = "metadataLabel";
	public static final String VALUE = "value";
	public static final String MASK = "mask";

	private final List<Metadata> metadatas;

	public MaskedMetadataValidator(List<Metadata> metadatas) {
		this.metadatas = metadatas;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void validate(Record record, ValidationErrors validationErrors, boolean skipNonEssential) {
		for (Metadata metadata : metadatas) {
			if (StringUtils.isNotBlank(metadata.getInputMask())) {
				Object value = record.get(metadata);
				validateMetadata(validationErrors, metadata, value);
			}
		}
	}

	public void validateMetadata(ValidationErrors validationErrors, Metadata metadata, Object value) {
		validate(validationErrors, metadata, value);
	}

	public static void validate(ValidationErrors validationErrors, Metadata metadata, Object value) {
		if (value != null && value instanceof String) {
			String strValue = (String) value;
			if (!MaskUtils.isValid(metadata.getInputMask(), strValue)) {
				LOGGER.info("Failed to format value '" + strValue + "' using mask '" + metadata.getInputMask() + "'");
				addValidationErrors(validationErrors, (String) value, metadata);
			}
		} else if (value != null && value instanceof List) {
			for (Object valueInList : (List) value) {
				if (valueInList != null && valueInList instanceof String) {
					String strValue = (String) valueInList;
					if (!MaskUtils.isValid(metadata.getInputMask(), strValue)) {
						LOGGER.info("Failed to format value '" + strValue + "' using mask '" + metadata.getInputMask() + "'");
						addValidationErrors(validationErrors, (String) valueInList, metadata);
					}
				}
			}
		}
	}

	private static void addValidationErrors(ValidationErrors validationErrors, String value, Metadata metadata) {
		Map<String, Object> parameters = new HashMap<>();
		parameters.put(METADATA_CODE, metadata.getCode());
		parameters.put(METADATA_LABEL, metadata.getLabelsByLanguageCodes());
		parameters.put(MASK, metadata.getInputMask());
		parameters.put(VALUE, value);
		validationErrors.add(MaskedMetadataValidator.class, VALUE_INCOMPATIBLE_WITH_SPECIFIED_MASK, parameters);
	}
}
