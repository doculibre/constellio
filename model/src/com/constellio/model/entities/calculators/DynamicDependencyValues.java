package com.constellio.model.entities.calculators;

import java.util.List;
import java.util.Map;

import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.services.records.RecordServicesRuntimeException.RecordServicesRuntimeException_CalculatorIsUsingAnForbiddenMetadata;
import com.constellio.model.services.schemas.MetadataList;

public class DynamicDependencyValues {

	Map<String, Object> values;
	private MetadataValueCalculator calculator;
	private MetadataList availableMetadatas;
	private List<String> availableMetadatasLocalCodes;
	private MetadataList availableMetadatasWithValue;

	public DynamicDependencyValues(MetadataValueCalculator calculator, Map<String, Object> values,
			MetadataList availableMetadatas,
			MetadataList availableMetadatasWithValue) {
		this.calculator = calculator;
		this.values = values;
		this.availableMetadatas = availableMetadatas;
		this.availableMetadatasLocalCodes = availableMetadatas.toLocalCodesList();
		this.availableMetadatasWithValue = availableMetadatasWithValue;
	}

	public <T> T getValue(Metadata metadata) {
		return getValue(metadata.getLocalCode());
	}

	public <T> T getValue(String localCode) {
		if (localCode.contains("_")) {
			String[] parts = localCode.split("_");
			localCode = parts[parts.length - 1];
		}

		if (!availableMetadatasLocalCodes.contains(localCode)) {
			throw new RecordServicesRuntimeException_CalculatorIsUsingAnForbiddenMetadata(
					calculator.getClass().getName(), localCode);
		}

		return (T) values.get(localCode);
	}

	public MetadataList getAvailableMetadatas() {
		return availableMetadatas;
	}

	public MetadataList getAvailableMetadatasWithAValue() {
		return availableMetadatasWithValue;
	}
}
