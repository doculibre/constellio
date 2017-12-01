package com.constellio.model.services.schemas.calculators;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.ReferenceListMetadataValueCalculator;
import com.constellio.model.entities.calculators.dependencies.AllAuthorizationsTargettingRecordDependencyValue;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.HierarchyDependencyValue;
import com.constellio.model.entities.calculators.dependencies.LocalDependency;
import com.constellio.model.entities.calculators.dependencies.SpecialDependencies;
import com.constellio.model.entities.calculators.dependencies.SpecialDependency;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.security.global.AuthorizationDetails;
import com.constellio.model.services.schemas.builders.CommonMetadataBuilder;

public class NonTaxonomyAuthorizationsCalculator extends ReferenceListMetadataValueCalculator {

	LocalDependency<List<String>> allRemovedAuthsParam = LocalDependency
			.toAStringList(CommonMetadataBuilder.ALL_REMOVED_AUTHS);

	SpecialDependency<HierarchyDependencyValue> hierarchyDependencyValuesParam = SpecialDependencies.HIERARCHY;

	SpecialDependency<AllAuthorizationsTargettingRecordDependencyValue> authorizationsParam = SpecialDependencies.AURHORIZATIONS_TARGETTING_RECORD;

	LocalDependency<Boolean> isDetachedParams = LocalDependency.toABoolean(Schemas.IS_DETACHED_AUTHORIZATIONS.getLocalCode());

	@Override
	public List<String> calculate(CalculatorParameters parameters) {
		AllAuthorizationsTargettingRecordDependencyValue authorizations = parameters.get(authorizationsParam);
		List<String> allRemovedAuths = parameters.get(allRemovedAuthsParam);
		HierarchyDependencyValue hierarchyDependencyValues = parameters.get(hierarchyDependencyValuesParam);
		boolean detached = Boolean.TRUE.equals(parameters.get(isDetachedParams));

		List<String> returnedIds = new ArrayList<>();

		if (!parameters.isPrincipalTaxonomyConcept()) {

			for (AuthorizationDetails auth : authorizations.getAuthorizationDetails()) {
				if (auth.isActiveAuthorization()) {
					returnedIds.add(auth.getId());
				}
			}

			if (!detached) {
				for (String auth : hierarchyDependencyValues.getInheritedNonTaxonomyAuthorizations()) {
					if (!allRemovedAuths.contains(auth)) {
						returnedIds.add(auth);
					}
				}
			}

		}

		return returnedIds;
	}

	@Override
	public List<? extends Dependency> getDependencies() {
		return Arrays.asList(authorizationsParam, allRemovedAuthsParam, hierarchyDependencyValuesParam, isDetachedParams);
	}
}
