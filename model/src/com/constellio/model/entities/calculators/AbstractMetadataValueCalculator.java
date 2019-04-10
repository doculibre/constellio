package com.constellio.model.entities.calculators;

import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.evaluators.CalculatorEvaluator;
import com.constellio.model.entities.calculators.evaluators.CalculatorEvaluatorParameters;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
	public List<? extends Dependency> getEvaluatorDependencies() {
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

	protected CalculatorEvaluatorParameters buildCalculatorEvaluatorParameters(CalculatorParameters parameters) {
		Map<Dependency, Object> values = new HashMap<>();
		for (Dependency dependency : getEvaluatorDependencies()) {
			values.put(dependency, parameters.get(dependency));
		}
		return new CalculatorEvaluatorParameters(values);
	}
}
