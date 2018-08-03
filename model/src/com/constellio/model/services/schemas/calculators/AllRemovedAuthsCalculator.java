package com.constellio.model.services.schemas.calculators;

import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.MetadataValueCalculator;
import com.constellio.model.entities.calculators.dependencies.*;
import com.constellio.model.entities.schemas.MetadataValueType;

import java.util.*;

import static com.constellio.model.entities.schemas.MetadataValueType.STRING;
import static com.constellio.model.services.schemas.builders.CommonMetadataBuilder.DETACHED_AUTHORIZATIONS;
import static com.constellio.model.services.schemas.builders.CommonMetadataBuilder.REMOVED_AUTHORIZATIONS;

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
