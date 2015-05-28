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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.MetadataValueCalculator;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.LocalDependency;
import com.constellio.model.entities.schemas.MetadataValueType;

public class PathPartsCalculator implements MetadataValueCalculator<List<String>> {

	LocalDependency<List<String>> pathDependency = LocalDependency.toAStringList("path");

	@Override
	public List<String> calculate(CalculatorParameters parameters) {
		return getPathsParts(parameters.get(pathDependency));
	}

	@Override
	public List<String> getDefaultValue() {
		return new ArrayList<>();
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
		return Arrays.asList(pathDependency);
	}

	private List<String> getPathsParts(List<String> paths) {
		Set<String> pathsParts = new HashSet<>();

		for (String path : paths) {
			String[] splittedPath = path.split("/");
			if (splittedPath.length >= 3) {
				String taxonomyCode = splittedPath[1];
				for (int i = 2; i < splittedPath.length; i++) {
					int level = i - 2;
					pathsParts.add(taxonomyCode + "_" + level + "_" + splittedPath[i]);
				}
			}

		}
		return new ArrayList<>(pathsParts);
	}

}
