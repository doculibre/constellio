package com.constellio.model.entities.calculators.evaluators;

import com.constellio.model.entities.calculators.dependencies.Dependency;
import lombok.AllArgsConstructor;

import java.util.Map;

@AllArgsConstructor
public class CalculatorEvaluatorParameters {
	final Map<Dependency, Object> values;

	@SuppressWarnings("unchecked")
	public <T> T get(Dependency dependency) {
		return (T) values.get(dependency);
	}
}


