package com.constellio.model.entities.calculators.dependencies;

import com.constellio.model.entities.schemas.MetadataValueType;

public interface Dependency {

	MetadataValueType getReturnType();

	boolean isMultivalue();

	boolean isRequired();

	String getLocalMetadataCode();
}
