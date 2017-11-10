package com.constellio.model.services.schemas.calculators;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.StringListMetadataValueCalculator;
import com.constellio.model.entities.calculators.dependencies.AllPrincipalsAuthsDependencyValue;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.ReferenceDependency;
import com.constellio.model.entities.calculators.dependencies.SpecialDependencies;
import com.constellio.model.entities.calculators.dependencies.SpecialDependency;
import com.constellio.model.entities.records.wrappers.SolrAuthorizationDetails;
import com.constellio.model.services.schemas.builders.CommonMetadataBuilder;

public class PrincipalsWithSpecificAuthCalculator extends StringListMetadataValueCalculator {

	ReferenceDependency<SortedMap<String, List<String>>> authorizationsRolesParam = ReferenceDependency.toAString(
			CommonMetadataBuilder.NON_TAXONOMY_AUTHORIZATIONS, SolrAuthorizationDetails.ROLES).whichIsMultivalue()
			.whichAreReferencedMultiValueGroupedByReference();

	SpecialDependency<AllPrincipalsAuthsDependencyValue> allPrincipalsAuthsParam = SpecialDependencies.ALL_PRINCIPALS;

	@Override
	public List<String> calculate(CalculatorParameters parameters) {
		AllPrincipalsAuthsDependencyValue principalsAuthorizations = parameters.get(allPrincipalsAuthsParam);

		SortedMap<String, List<String>> authorizationsRoles = parameters.get(authorizationsRolesParam);
		Map<String, List<String>> nonTaxonomyAuthorizationsAccesses = new HashMap<>();

		List<String> principals = new ArrayList<>(
				principalsAuthorizations.getPrincipalIdsWithAnyAuthorization(authorizationsRoles).getNestedMap().keySet());

		Collections.sort(principals);
		return principals;
	}

	@Override
	public List<? extends Dependency> getDependencies() {
		return Arrays.asList(allPrincipalsAuthsParam, authorizationsRolesParam);
	}
}
