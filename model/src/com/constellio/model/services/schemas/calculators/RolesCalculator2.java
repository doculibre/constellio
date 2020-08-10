package com.constellio.model.services.schemas.calculators;

import com.constellio.model.entities.calculators.AbstractMetadataValueCalculator;
import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.LocalDependency;
import com.constellio.model.entities.calculators.dependencies.ReferenceDependency;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.security.global.GlobalGroupStatus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

public class RolesCalculator2 extends AbstractMetadataValueCalculator<List<String>> {
	LocalDependency<List<String>> userRolesParam = LocalDependency.toARequiredStringList("userroles");
	ReferenceDependency<SortedMap<String, List<String>>> groupsParam = ReferenceDependency.toAString("groups", "roles")
			.whichIsMultivalue()
			.whichAreReferencedSingleValueGroupedByReference()
			.whichIsRequired();
	ReferenceDependency<SortedMap<String, GlobalGroupStatus>> statusParam = ReferenceDependency.toAnEnum("groups", "status")
			.whichAreReferencedSingleValueGroupedByReference();

	@Override
	public List<String> calculate(CalculatorParameters parameters) {
		List<String> allRoles = new ArrayList<>();

		List<String> userRoles = parameters.get(userRolesParam);
		SortedMap<String, List<String>> groups = parameters.get(groupsParam);
		SortedMap<String, GlobalGroupStatus> statuses = parameters.get(statusParam);

		if (userRoles != null) {
			allRoles.addAll(userRoles);
		}

		if (groups != null && statuses != null) {
			for (Map.Entry<String, List<String>> entry : groups.entrySet()) {
				GlobalGroupStatus status = statuses.get(entry.getKey());
				if (status != GlobalGroupStatus.INACTIVE) {
					for (String role : entry.getValue()) {
						if (!allRoles.contains(role)) {
							allRoles.add(role);
						}
					}
				}
			}
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
		return Arrays.asList(userRolesParam, groupsParam, statusParam);
	}
}
