package com.constellio.model.entities.schemas.entries;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.constellio.model.entities.calculators.MetadataValueCalculator;

public class CalculatedDataEntry implements DataEntry {

	final MetadataValueCalculator<?> calculator;

	public CalculatedDataEntry(MetadataValueCalculator<?> calculator) {
		super();
		this.calculator = calculator;
	}

	public MetadataValueCalculator<?> getCalculator() {
		return calculator;
	}

	@Override
	public DataEntryType getType() {
		return DataEntryType.CALCULATED;
	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this);
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof CalculatedDataEntry && calculator.getClass().getSimpleName()
				.equals(((CalculatedDataEntry) obj).getCalculator().getClass().getSimpleName());
	}

	@Override
	public String toString() {
		return "CalculatedDataEntry [calculator=" + calculator + "]";
	}

}
