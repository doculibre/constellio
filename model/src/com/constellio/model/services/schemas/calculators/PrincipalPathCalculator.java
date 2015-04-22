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
package com.constellio.model.services.schemas.calculators;

import static com.constellio.model.entities.schemas.MetadataValueType.STRING;

import java.util.Arrays;
import java.util.List;

import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.MetadataValueCalculator;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.LocalDependency;
import com.constellio.model.entities.calculators.dependencies.SpecialDependencies;
import com.constellio.model.entities.calculators.dependencies.SpecialDependency;
import com.constellio.model.entities.schemas.MetadataValueType;

public class PrincipalPathCalculator implements MetadataValueCalculator<String> {

	LocalDependency<List<String>> pathDependency = LocalDependency.toAStringList("path").whichIsRequired();
	SpecialDependency<String> taxoPrincipaleDependency = SpecialDependencies.PRINCIPAL_TAXONOMY_CODE;

	@Override
	public String calculate(CalculatorParameters parameters) {
		String taxo = parameters.get(taxoPrincipaleDependency);
		List<String> pathsValue = parameters.get(pathDependency);
		if (pathsValue != null && !pathsValue.isEmpty() && taxo != null) {
			for (String path : pathsValue) {
				if (path != null && path.contains(taxo)) {
					return path;
				}
			}
		}
		return getDefaultValue();
	}

	@Override
	public String getDefaultValue() {
		return null;
	}

	@Override
	public MetadataValueType getReturnType() {
		return STRING;
	}

	@Override
	public boolean isMultiValue() {
		return false;
	}

	@Override
	public List<? extends Dependency> getDependencies() {
		return Arrays.asList(pathDependency, taxoPrincipaleDependency);
	}
}
