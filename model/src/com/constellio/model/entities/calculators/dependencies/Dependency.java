package com.constellio.model.entities.calculators.dependencies;

import java.io.Serializable;

import com.constellio.model.entities.schemas.MetadataValueType;

public interface Dependency extends Serializable {

	MetadataValueType getReturnType();

	boolean isMultivalue();

	boolean isRequired();

	String getLocalMetadataCode();
}
