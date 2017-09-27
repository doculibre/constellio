package com.constellio.model.entities.calculators;

import java.util.List;

import com.constellio.model.entities.schemas.MetadataValueType;

public abstract class ReferenceListMetadataValueCalculator implements MetadataValueCalculator<List<String>> {

	@Override
	public List<String> getDefaultValue() {
		return null;
	}

	@Override
	public MetadataValueType getReturnType() {
		return MetadataValueType.REFERENCE;
	}

	@Override
	public boolean isMultiValue() {
		return true;
	}

}
