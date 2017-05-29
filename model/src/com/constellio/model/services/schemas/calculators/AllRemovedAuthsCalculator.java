package com.constellio.model.services.schemas.calculators;

import static com.constellio.model.entities.schemas.MetadataValueType.STRING;
import static com.constellio.model.services.schemas.builders.CommonMetadataBuilder.DETACHED_AUTHORIZATIONS;
import static com.constellio.model.services.schemas.builders.CommonMetadataBuilder.REMOVED_AUTHORIZATIONS;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.MetadataValueCalculator;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.HierarchyDependencyValue;
import com.constellio.model.entities.calculators.dependencies.LocalDependency;
import com.constellio.model.entities.calculators.dependencies.SpecialDependencies;
import com.constellio.model.entities.calculators.dependencies.SpecialDependency;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.schemas.builders.CommonMetadataBuilder;

public class AllRemovedAuthsCalculator implements MetadataValueCalculator<List<String>> {

	SpecialDependency<HierarchyDependencyValue> inheritedRemovedAuthorizationsParam = SpecialDependencies.HIERARCHY;
	LocalDependency<List<String>> removedAuthorizationsParam = LocalDependency.toAStringList(REMOVED_AUTHORIZATIONS);
	LocalDependency<Boolean> isDetachedParams = LocalDependency.toABoolean(DETACHED_AUTHORIZATIONS);

	@Override
	public List<String> calculate(CalculatorParameters parameters) {

		HierarchyDependencyValue hierarchyDependencyValue = parameters.get(inheritedRemovedAuthorizationsParam);
		List<String> removedLocally = parameters.get(removedAuthorizationsParam);
		boolean isDetached = Boolean.TRUE.equals(parameters.get(isDetachedParams));

		Set<String> calculatedAuthorizations = new HashSet<>();
		calculatedAuthorizations.addAll(removedLocally);
		if (!isDetached) {
			calculatedAuthorizations.addAll(hierarchyDependencyValue.getRemovedAuthorizationIds());
		}

		return new ArrayList<>(calculatedAuthorizations);
	}

	@Override
	public List<String> getDefaultValue() {
		return Collections.emptyList();
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
		return Arrays.asList(inheritedRemovedAuthorizationsParam, removedAuthorizationsParam, isDetachedParams);
	}
}
