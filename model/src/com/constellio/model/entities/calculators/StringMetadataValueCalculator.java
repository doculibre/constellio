package com.constellio.model.entities.calculators;

import com.constellio.model.entities.schemas.MetadataValueType;

public abstract class StringMetadataValueCalculator implements MetadataValueCalculator<String> {

	@Override
	public String getDefaultValue() {
		return null;
	}

	@Override
	public MetadataValueType getReturnType() {
		return MetadataValueType.STRING;
	}

	@Override
	public boolean isMultiValue() {
		return false;
	}

}
