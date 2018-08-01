package com.constellio.model.entities.calculators;

import com.constellio.model.entities.schemas.MetadataValueType;
import org.joda.time.LocalDate;

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
