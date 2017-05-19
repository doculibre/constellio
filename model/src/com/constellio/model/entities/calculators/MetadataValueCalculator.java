package com.constellio.model.entities.calculators;

import java.io.Serializable;
import java.util.List;

import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.schemas.MetadataValueType;

public interface MetadataValueCalculator<T> extends Serializable {

	T calculate(CalculatorParameters parameters);

	T getDefaultValue();

	MetadataValueType getReturnType();

	boolean isMultiValue();

	List<? extends Dependency> getDependencies();

}
