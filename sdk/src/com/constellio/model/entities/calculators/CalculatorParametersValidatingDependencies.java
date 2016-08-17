package com.constellio.model.entities.calculators;

import com.constellio.model.entities.calculators.dependencies.ConfigDependency;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.DynamicLocalDependency;
import com.constellio.model.entities.calculators.dependencies.LocalDependency;
import com.constellio.model.entities.calculators.dependencies.ReferenceDependency;
import com.constellio.model.entities.calculators.dependencies.SpecialDependency;

public class CalculatorParametersValidatingDependencies extends CalculatorParameters {

	CalculatorParameters parameters;

	MetadataValueCalculator calculator;

	public CalculatorParametersValidatingDependencies(CalculatorParameters parameters,
			MetadataValueCalculator calculator) {
		super(parameters.values, parameters.getId(), parameters.getLegacyId(), parameters.getCollection());
		this.parameters = parameters;
		this.calculator = calculator;
	}

	private void ensureDependencyAvailable(Dependency dependency) {
		if (!calculator.getDependencies().contains(dependency)) {
			throw new RuntimeException(
					dependency + " is not returned by getDependencies() in calculator " + calculator.getClass().getSimpleName());
		}
	}

	@Override
	public <T> T get(LocalDependency<T> dependency) {
		ensureDependencyAvailable(dependency);
		return parameters.get(dependency);
	}

	@Override
	public <T> T get(ReferenceDependency<T> dependency) {
		ensureDependencyAvailable(dependency);
		return parameters.get(dependency);
	}

	@Override
	public <T> T get(SpecialDependency<T> dependency) {
		ensureDependencyAvailable(dependency);
		return parameters.get(dependency);
	}

	@Override
	public <T> T get(ConfigDependency<T> dependency) {
		ensureDependencyAvailable(dependency);
		return parameters.get(dependency);
	}

	@Override
	public DynamicDependencyValues get(DynamicLocalDependency dependency) {
		ensureDependencyAvailable(dependency);
		return parameters.get(dependency);
	}
}
