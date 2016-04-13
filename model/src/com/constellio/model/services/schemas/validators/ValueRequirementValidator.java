package com.constellio.model.services.schemas.validators;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.entries.CalculatedDataEntry;
import com.constellio.model.entities.schemas.entries.DataEntryType;
import com.constellio.model.frameworks.validation.ValidationErrors;
import com.constellio.model.frameworks.validation.Validator;

public class ValueRequirementValidator implements Validator<Record> {

	public static final String BASED_ON_METADATAS = "basedOnMetadatas";

	public static final String REQUIRED_VALUE_FOR_METADATA = "requiredValueForMetadata";

	private final List<Metadata> metadatas;

	public ValueRequirementValidator(List<Metadata> metadatas) {
		this.metadatas = metadatas;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void validate(Record record, ValidationErrors validationErrors) {
		for (Metadata metadata : metadatas) {
			Object value = record.get(metadata);
			if (metadata.isDefaultRequirement() && (value == null || (metadata.isMultivalue() && ((List) value).size() == 0))
					&& metadata.isEnabled()) {
				addValidationErrors(validationErrors, REQUIRED_VALUE_FOR_METADATA, metadata);
			}
		}
	}

	private void addValidationErrors(ValidationErrors validationErrors, String errorCode, Metadata metadata) {
		Map<String, String> parameters = new HashMap<>();
		parameters.put(METADATA_CODE, metadata.getCode());
		//TODO Thiago
		//		parameters.put(METADATA_LABEL, metadata.getLabel());
		if (metadata.getDataEntry().getType() == DataEntryType.CALCULATED) {
			List<String> basedOnMetadatas = new ArrayList<>();
			CalculatedDataEntry calculatedDataEntry = (CalculatedDataEntry) metadata.getDataEntry();
			for (Dependency dependency : calculatedDataEntry.getCalculator().getDependencies()) {
				basedOnMetadatas.add(dependency.getLocalMetadataCode());
			}
			parameters.put(BASED_ON_METADATAS, basedOnMetadatas.toString());
		}
		validationErrors.add(getClass(), errorCode, parameters);
	}
}
