package com.constellio.model.services.schemas.validators;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.frameworks.validation.ValidationErrors;
import com.constellio.model.frameworks.validation.Validator;
import com.constellio.model.utils.MaskUtils;
import com.constellio.model.utils.MaskUtilsException;

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
	public void validate(Record record, ValidationErrors validationErrors) {
		for (Metadata metadata : metadatas) {
			if (metadata.getInputMask() != null) {
				Object value = record.get(metadata);
				if (value != null && value instanceof String) {
					String strValue = (String) value;
					if (!MaskUtils.isValid(metadata.getInputMask(), strValue)) {

						try {
							String formattedValue = MaskUtils.format(metadata.getInputMask(), strValue);
							record.set(metadata, formattedValue);
						} catch (MaskUtilsException e) {
							LOGGER.info("Failed to format value '" + strValue + "' using mask '" + metadata.getInputMask() + "'");
							addValidationErrors(validationErrors, (String) value, metadata);
						}

					}
				}
			}
		}
	}

	private void addValidationErrors(ValidationErrors validationErrors, String value, Metadata metadata) {
		Map<String, String> parameters = new HashMap<>();
		parameters.put(METADATA_CODE, metadata.getCode());
		parameters.put(METADATA_LABEL, metadata.getLabel());
		parameters.put(MASK, metadata.getInputMask());
		parameters.put(VALUE, value);
		validationErrors.add(getClass(), VALUE_INCOMPATIBLE_WITH_SPECIFIED_MASK, parameters);
	}
}
