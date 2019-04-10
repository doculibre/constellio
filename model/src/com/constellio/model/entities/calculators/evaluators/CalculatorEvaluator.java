package com.constellio.model.entities.calculators.evaluators;

import com.constellio.model.entities.calculators.dependencies.Dependency;

import java.util.List;

public interface CalculatorEvaluator {

	List<? extends Dependency> getDependencies();

	boolean isAutomaticallyFilled(CalculatorEvaluatorParameters parameters);

}
