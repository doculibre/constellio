package com.constellio.model.entities.calculators.evaluators;

import com.constellio.model.entities.calculators.dependencies.LocalDependency;

import java.util.List;

public interface CalculatorEvaluator {

	List<? extends LocalDependency> getDependencies();

	boolean isAutomaticallyFilled(CalculatorEvaluatorParameters parameters);

}
