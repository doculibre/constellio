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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.constellio.data.utils.LangUtils;
import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.MetadataValueCalculator;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.HierarchyDependencyValue;
import com.constellio.model.entities.calculators.dependencies.SpecialDependencies;
import com.constellio.model.entities.calculators.dependencies.SpecialDependency;
import com.constellio.model.entities.schemas.MetadataValueType;

public class ParentPathCalculator implements MetadataValueCalculator<List<String>> {

	SpecialDependency<HierarchyDependencyValue> taxonomiesParam = SpecialDependencies.HIERARCHY;

	@Override
	public List<String> calculate(CalculatorParameters parameters) {
		List<String> calculatedValue = new ArrayList<>();
		HierarchyDependencyValue paramValue = parameters.get(taxonomiesParam);

		List<String> paramValuePaths = paramValue.getPaths();
		if (paramValuePaths != null && !paramValuePaths.isEmpty()) {
			calculatedValue = paramValuePaths;
		} else if (paramValue.getTaxonomy() != null) {
			calculatedValue = Arrays.asList("/" + paramValue.getTaxonomy().getCode());
		}

		Collections.sort(calculatedValue);

		for (int i = 0; i < calculatedValue.size(); i++) {
			String calculatedValueAtI = calculatedValue.get(i);
			if (calculatedValueAtI != null) {
				for (int j = 0; j < calculatedValue.size(); j++) {
					String calculatedValueAtJ = calculatedValue.get(j);
					if (i != j && calculatedValueAtJ != null && calculatedValueAtI.startsWith(calculatedValueAtJ)) {
						calculatedValue.set(j, null);
					}
				}
			}
		}

		return LangUtils.withoutNulls(calculatedValue);
	}

	@Override
	public List<String> getDefaultValue() {
		return null;
	}

	@Override
	public MetadataValueType getReturnType() {
		return STRING;
	}

	@Override
	public boolean isMultiValue() {
		return true;
	}

	@Override
	public List<? extends Dependency> getDependencies() {
		return Arrays.asList(taxonomiesParam);
	}
}
