package com.constellio.model.services.schemas.validators;

import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.entries.CalculatedDataEntry;
import com.constellio.model.frameworks.validation.ValidationErrors;
import com.constellio.model.frameworks.validation.Validator;
import com.constellio.model.services.records.RecordAutomaticMetadataServices;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.constellio.model.entities.schemas.entries.DataEntryType.CALCULATED;

public class ValueRequirementValidator implements Validator<Record> {

	public static final String BASED_ON_METADATAS = "basedOnMetadatas";

	public static final String REQUIRED_VALUE_FOR_METADATA = "requiredValueForMetadata";

	private final List<Metadata> metadatas;

	private boolean skipUSRMetadatas;

	private RecordAutomaticMetadataServices recordAutomaticMetadataServices;

	public ValueRequirementValidator(List<Metadata> metadatas, boolean skipUSRMetadatas,
									 RecordAutomaticMetadataServices recordAutomaticMetadataServices) {
		this.metadatas = metadatas;
		this.skipUSRMetadatas = skipUSRMetadatas;
		this.recordAutomaticMetadataServices = recordAutomaticMetadataServices;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void validate(Record record, ValidationErrors validationErrors) {
		for (Metadata metadata : metadatas) {
			Object value = record.get(metadata);
			if (metadata.isDefaultRequirement()
				&& (!metadata.getLocalCode().startsWith("USR") || !skipUSRMetadatas)
				&& (metadata.getDataEntry().getType() != CALCULATED ||
					!recordAutomaticMetadataServices.isValueAutomaticallyFilled(metadata, record))
				&& (value == null || (metadata.isMultivalue() && ((List) value).size() == 0))
				&& metadata.isEnabled()) {
				addValidationErrors(record.getId(), validationErrors, REQUIRED_VALUE_FOR_METADATA, metadata);
			}
		}
	}

	private void addValidationErrors(String recordId, ValidationErrors validationErrors, String errorCode,
									 Metadata metadata) {
		Map<String, Object> parameters = new HashMap<>();
		parameters.put(RECORD, recordId);
		parameters.put(METADATA_CODE, metadata.getCode());
		parameters.put(METADATA_LABEL, metadata.getLabelsByLanguageCodes());
		if (metadata.getDataEntry().getType() == CALCULATED) {
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
