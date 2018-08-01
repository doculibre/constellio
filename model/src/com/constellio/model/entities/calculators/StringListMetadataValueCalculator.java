package com.constellio.model.entities.calculators;

import com.constellio.model.entities.schemas.MetadataValueType;

import java.util.List;

public abstract class StringListMetadataValueCalculator implements MetadataValueCalculator<List<String>> {

	@Override
	public List<String> getDefaultValue() {
		return null;
	}

	@Override
	public MetadataValueType getReturnType() {
		return MetadataValueType.STRING;
	}

	@Override
	public boolean isMultiValue() {
		return true;
	}

}
