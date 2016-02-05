package com.constellio.model.entities.calculators.dependencies;

import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataValueType;

public abstract class DynamicLocalDependency implements Dependency {

	@Override
	public MetadataValueType getReturnType() {
		return null;
	}

	@Override
	public boolean isMultivalue() {
		return false;
	}

	@Override
	public boolean isRequired() {
		return false;
	}

	@Override
	public String getLocalMetadataCode() {
		return null;
	}

	public abstract boolean isDependentOf(Metadata metadata);

}