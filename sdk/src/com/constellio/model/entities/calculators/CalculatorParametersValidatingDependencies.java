/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package com.constellio.model.entities.calculators;

import com.constellio.model.entities.calculators.dependencies.ConfigDependency;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.LocalDependency;
import com.constellio.model.entities.calculators.dependencies.ReferenceDependency;
import com.constellio.model.entities.calculators.dependencies.SpecialDependency;

public class CalculatorParametersValidatingDependencies extends CalculatorParameters {

	CalculatorParameters parameters;

	MetadataValueCalculator calculator;

	public CalculatorParametersValidatingDependencies(CalculatorParameters parameters,
			MetadataValueCalculator calculator) {
		super(parameters.values, parameters.getCollection());
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
}
