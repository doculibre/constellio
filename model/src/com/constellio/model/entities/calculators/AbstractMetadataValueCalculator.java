package com.constellio.model.entities.calculators;

import com.constellio.model.entities.calculators.dependencies.LocalDependency;
import com.constellio.model.entities.calculators.evaluators.CalculatorEvaluator;
import com.constellio.model.entities.calculators.evaluators.CalculatorEvaluatorParameters;

import java.util.Collections;
import java.util.List;

public abstract class AbstractMetadataValueCalculator<T> implements MetadataValueCalculator<T> {

	protected CalculatorEvaluator calculatorEvaluator = null;

	@Override
	public T getDefaultValue() {
		return null;
	}

	@Override
	public boolean isMultiValue() {
		return false;
	}

	@Override
	public CalculatorEvaluator getCalculatorEvaluator() {
		return calculatorEvaluator;
	}

	@Override
	public List<? extends LocalDependency> getEvaluatorDependencies() {
		if (hasEvaluator()) {
			return getCalculatorEvaluator().getDependencies();
		}
		return Collections.emptyList();
	}

	@Override
	public boolean isAutomaticallyFilled(CalculatorEvaluatorParameters parameters) {
		if (hasEvaluator()) {
			return getCalculatorEvaluator().isAutomaticallyFilled(parameters);
		}
		return true;
	}

	@Override
	public boolean hasEvaluator() {
		return calculatorEvaluator != null;
	}
}
