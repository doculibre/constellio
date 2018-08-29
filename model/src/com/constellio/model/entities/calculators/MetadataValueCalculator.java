package com.constellio.model.entities.calculators;

import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.schemas.MetadataValueType;

import java.io.Serializable;
import java.util.List;

public interface MetadataValueCalculator<T> extends Serializable {

	T calculate(CalculatorParameters parameters);

	T getDefaultValue();

	MetadataValueType getReturnType();

	boolean isMultiValue();

	List<? extends Dependency> getDependencies();

}
