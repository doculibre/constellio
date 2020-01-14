package com.constellio.model.entities.calculators;

import com.constellio.data.utils.LangUtils;
import com.constellio.data.utils.LazyIterator;
import com.constellio.data.utils.Pair;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.services.records.RecordServicesRuntimeException.RecordServicesRuntimeException_CalculatorIsUsingAnForbiddenMetadata;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.stream.Collectors.toList;

public class DynamicDependencyValues {

	private MetadataValueCalculator calculator;
	private List<Metadata> availableMetadatas;
	private List<Object> availableMetadataValues;
	private List<String> availableMetadatasLocalCodes;
	private List<Metadata> availableMetadatasWithValue;

	public DynamicDependencyValues(MetadataValueCalculator calculator,
								   List<Metadata> availableMetadatas,
								   List<Object> availableMetadataValues,
								   List<Metadata> availableMetadatasWithValue) {
		this.calculator = calculator;
		this.availableMetadatas = availableMetadatas;
		this.availableMetadatasLocalCodes = availableMetadatas.stream().map((m) -> m.getLocalCode()).collect(toList());
		this.availableMetadatasWithValue = availableMetadatasWithValue;
		this.availableMetadataValues = availableMetadataValues;
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

		int index = availableMetadatasLocalCodes.indexOf(localCode);
		return index == -1 ? null : (T) availableMetadataValues.get(index);
	}

	public List<Metadata> getAvailableMetadatas() {
		return availableMetadatas;
	}

	public List<Metadata> getAvailableMetadatasWithAValue() {
		return availableMetadatasWithValue;
	}

	public Iterator<Pair<Metadata, Object>> iterateWithValues() {
		AtomicInteger index = new AtomicInteger();
		return new LazyIterator<Pair<Metadata, Object>>() {
			@Override
			protected Pair<Metadata, Object> getNextOrNull() {

				while (index.get() < availableMetadatas.size()) {
					Metadata metadata = availableMetadatas.get(index.get());
					Object value = availableMetadataValues.get(index.get());

					index.incrementAndGet();
					if (LangUtils.isNotEmptyValue(value)) {
						return new Pair<>(metadata, value);
					}
				}

				return null;
			}
		};
	}

}
