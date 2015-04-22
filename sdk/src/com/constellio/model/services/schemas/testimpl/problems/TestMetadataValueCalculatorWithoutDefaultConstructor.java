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
package com.constellio.model.services.schemas.testimpl.problems;

import java.util.ArrayList;
import java.util.List;

import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.MetadataValueCalculator;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.LocalDependency;
import com.constellio.model.entities.schemas.MetadataValueType;

public class TestMetadataValueCalculatorWithoutDefaultConstructor implements MetadataValueCalculator<String> {

	public TestMetadataValueCalculatorWithoutDefaultConstructor(String s) {
		super();
	}

	@Override
	public String calculate(CalculatorParameters parameters) {
		return null;
	}

	@Override
	public String getDefaultValue() {
		return null;
	}

	@Override
	public MetadataValueType getReturnType() {
		return MetadataValueType.STRING;
	}

	@Override
	public List<Dependency> getDependencies() {
		List<Dependency> dependencies = new ArrayList<Dependency>();
		dependencies.add(LocalDependency.toAString("codeSchema_default_dependence").whichIsRequired());
		return dependencies;
	}

	@Override
	public boolean isMultiValue() {
		return false;
	}
}
