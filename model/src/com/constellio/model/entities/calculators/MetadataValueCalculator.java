package com.constellio.model.entities.calculators;

import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.evaluators.CalculatorEvaluator;
import com.constellio.model.entities.calculators.evaluators.CalculatorEvaluatorParameters;
import com.constellio.model.entities.schemas.MetadataValueType;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

public interface MetadataValueCalculator<T> extends Serializable {

	T calculate(CalculatorParameters parameters);

	T getDefaultValue();

	List<? extends Dependency> getDependencies();

	default MetadataValueType getReturnType() {
		return null;
	}

	default boolean isMultiValue() {
		return false;
	}

	default List<? extends Dependency> getEvaluatorDependencies() {
		return Collections.emptyList();
	}

	default boolean isAutomaticallyFilled(CalculatorEvaluatorParameters parameters) {
		return true;
	}

	default CalculatorEvaluator getCalculatorEvaluator() {
		return null;
	}

	default boolean hasEvaluator() {
		return false;
	}

}
