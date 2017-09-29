package com.constellio.model.entities.calculators;

import java.util.List;

import com.constellio.model.entities.schemas.MetadataValueType;

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
