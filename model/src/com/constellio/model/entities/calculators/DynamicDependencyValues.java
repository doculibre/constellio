package com.constellio.model.entities.calculators;

import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.services.records.RecordServicesRuntimeException.RecordServicesRuntimeException_CalculatorIsUsingAnForbiddenMetadata;

import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

public class DynamicDependencyValues {

	Map<String, Object> values;
	private MetadataValueCalculator calculator;
	private List<Metadata> availableMetadatas;
	private List<String> availableMetadatasLocalCodes;
	private List<Metadata> availableMetadatasWithValue;

	public DynamicDependencyValues(MetadataValueCalculator calculator, Map<String, Object> values,
								   List<Metadata> availableMetadatas,
								   List<Metadata> availableMetadatasWithValue) {
		this.calculator = calculator;
		this.values = values;
		this.availableMetadatas = availableMetadatas;
		this.availableMetadatasLocalCodes = availableMetadatas.stream().map((m) -> m.getLocalCode()).collect(toList());
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

	public List<Metadata> getAvailableMetadatas() {
		return availableMetadatas;
	}

	public List<Metadata> getAvailableMetadatasWithAValue() {
		return availableMetadatasWithValue;
	}

}
