package com.constellio.model.services.schemas.calculators;

import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.ReferenceListMetadataValueCalculator;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.HierarchyDependencyValue;
import com.constellio.model.entities.calculators.dependencies.LocalDependency;
import com.constellio.model.entities.calculators.dependencies.SpecialDependencies;
import com.constellio.model.entities.calculators.dependencies.SpecialDependency;
import com.constellio.model.entities.records.wrappers.Authorization;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.security.SecurityModel;
import com.constellio.model.entities.security.SecurityModelAuthorization;
import com.constellio.model.services.schemas.builders.CommonMetadataBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;

/**
 * This calculator is used to feed a list of authorizations that are placed directly on records in a taxonomy.
 * Since a modification to the auth requires the indexing of the records, the metadata is used to calculate impacts
 */
public class NonTaxonomyAuthorizationsCalculator extends ReferenceListMetadataValueCalculator {


	LocalDependency<List<String>> allRemovedAuthsParam = LocalDependency.toAStringList(CommonMetadataBuilder.ALL_REMOVED_AUTHS);

	SpecialDependency<HierarchyDependencyValue> hierarchyDependencyValuesParam = SpecialDependencies.HIERARCHY;

	SpecialDependency<SecurityModel> securityModelSpecialDependency = SpecialDependencies.SECURITY_MODEL;

	LocalDependency<Boolean> isDetachedParams = LocalDependency.toABoolean(Schemas.IS_DETACHED_AUTHORIZATIONS.getLocalCode());

	MetadatasProvidingSecurityDynamicDependency metadatasProvidingSecurityParams = new MetadatasProvidingSecurityDynamicDependency();

	@Override
	public List<String> calculate(CalculatorParameters parameters) {

		if (parameters.isPrincipalTaxonomyConcept()) {
			return Collections.emptyList();
		}

		SecurityModel securityModel = parameters.get(securityModelSpecialDependency);
		List<String> allRemovedAuths = parameters.get(allRemovedAuthsParam);
		HierarchyDependencyValue hierarchyDependencyValues = parameters.get(hierarchyDependencyValuesParam);
		boolean detached = Boolean.TRUE.equals(parameters.get(isDetachedParams));

		List<String> returnedIds = new ArrayList<>();

		addActiveAuthorizations(returnedIds, securityModel.getAuthorizationsOnTarget(parameters.getId()));

		List<SecurityModelAuthorization> metadataAuths = securityModel.getAuthorizationDetailsOnMetadatasProvidingSecurity(
				parameters.get(metadatasProvidingSecurityParams));

		addActiveAuthorizations(returnedIds, metadataAuths);

		if (!detached && hierarchyDependencyValues != null && !hasActiveOverridingAuth(metadataAuths)) {
			for (String inheritedNonTaxonomyAuthId : hierarchyDependencyValues.getInheritedNonTaxonomyAuthorizations()) {
				if (!allRemovedAuths.contains(inheritedNonTaxonomyAuthId)) {
					returnedIds.add(inheritedNonTaxonomyAuthId);
				}
			}
		}
		return returnedIds;
	}

	protected void addActiveAuthorizations(List<String> returnedIds,
										   List<SecurityModelAuthorization> metadataAuths) {
		for (SecurityModelAuthorization auth : metadataAuths) {
			Authorization authorizationDetails = (Authorization) auth.getDetails();
			if (authorizationDetails.isActiveAuthorization()) {
				returnedIds.add(authorizationDetails.getId());
			}
		}
	}


	public static boolean hasActiveOverridingAuth(List<SecurityModelAuthorization> authorizations) {
		for (SecurityModelAuthorization auth : authorizations) {
			if (auth.getDetails().isActiveAuthorization() && auth.getDetails().isOverrideInherited()) {
				return true;
			}
		}
		return false;
	}

	@Override
	public List<? extends Dependency> getDependencies() {
		return asList(securityModelSpecialDependency, allRemovedAuthsParam, hierarchyDependencyValuesParam,
				isDetachedParams, metadatasProvidingSecurityParams);
	}
}
