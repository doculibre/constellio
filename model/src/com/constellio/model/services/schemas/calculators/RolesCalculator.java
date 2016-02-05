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
