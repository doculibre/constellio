package com.constellio.model.entities.calculators;

import org.joda.time.LocalDate;

import com.constellio.model.entities.schemas.MetadataValueType;

public abstract class DateMetadataValueCalculator implements MetadataValueCalculator<LocalDate> {

	@Override
	public LocalDate getDefaultValue() {
		return null;
	}

	@Override
	public MetadataValueType getReturnType() {
		return MetadataValueType.DATE;
	}

	@Override
	public boolean isMultiValue() {
		return false;
	}

}
