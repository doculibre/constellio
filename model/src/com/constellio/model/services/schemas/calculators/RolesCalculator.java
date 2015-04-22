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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.MetadataValueCalculator;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.LocalDependency;
import com.constellio.model.entities.calculators.dependencies.ReferenceDependency;
import com.constellio.model.entities.schemas.MetadataValueType;

public class RolesCalculator implements MetadataValueCalculator<List<String>> {
	LocalDependency<List<String>> userRolesParam = LocalDependency.toARequiredStringList("userroles");
	ReferenceDependency<List<String>> groupsParam = ReferenceDependency.toAString("groups", "roles").whichIsMultivalue()
			.whichIsRequired();

	@Override
	public List<String> calculate(CalculatorParameters parameters) {
		List<String> allRoles = new ArrayList<>();

		List<String> userRoles = parameters.get(userRolesParam);
		List<String> groups = parameters.get(groupsParam);

		if (userRoles != null) {
			allRoles.addAll(userRoles);
		}

		if (groups != null) {
			allRoles.addAll(groups);
		}

		return allRoles;
	}

	@Override
	public List<String> getDefaultValue() {
		return new ArrayList<>();
	}

	@Override
	public MetadataValueType getReturnType() {
		return MetadataValueType.STRING;
	}

	@Override
	public boolean isMultiValue() {
		return true;
	}

	@Override
	public List<? extends Dependency> getDependencies() {
		return Arrays.asList(userRolesParam, groupsParam);
	}
}
