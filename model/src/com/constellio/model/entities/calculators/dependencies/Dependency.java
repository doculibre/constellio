package com.constellio.model.entities.calculators.dependencies;

import com.constellio.model.entities.schemas.MetadataValueType;

import java.io.Serializable;

public interface Dependency extends Serializable {

	MetadataValueType getReturnType();

	boolean isMultivalue();

	boolean isRequired();

	String getLocalMetadataCode();
}
