package com.constellio.model.services.schemas.calculators;

import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.MetadataValueCalculator;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.HierarchyDependencyValue;
import com.constellio.model.entities.calculators.dependencies.LocalDependency;
import com.constellio.model.entities.calculators.dependencies.SpecialDependencies;
import com.constellio.model.entities.calculators.dependencies.SpecialDependency;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.security.SecurityModel;
import com.constellio.model.entities.security.SecurityModelAuthorization;

import java.util.ArrayList;
import java.util.List;

import static com.constellio.model.entities.schemas.MetadataValueType.STRING;
import static com.constellio.model.services.schemas.builders.CommonMetadataBuilder.DETACHED_AUTHORIZATIONS;
import static com.constellio.model.services.schemas.calculators.NonTaxonomyAuthorizationsCalculator.hasActiveOverridingAuth;
import static java.util.Arrays.asList;

public class AttachedAncestorsCalculator implements MetadataValueCalculator<List<String>> {

	SpecialDependency<HierarchyDependencyValue> taxonomiesParam = SpecialDependencies.HIERARCHY;
	LocalDependency<Boolean> isDetachedAuthsParams = LocalDependency.toABoolean(DETACHED_AUTHORIZATIONS);
	SpecialDependency<SecurityModel> securityModelDependency = SpecialDependencies.SECURITY_MODEL;
	MetadatasProvidingSecurityDynamicDependency metadatasProvidingSecurityParams = new MetadatasProvidingSecurityDynamicDependency();

	@Override

	public List<String> calculate(CalculatorParameters parameters) {
		SecurityModel securityModel = parameters.get(securityModelDependency);
		HierarchyDependencyValue hierarchyDependencyValue = parameters.get(taxonomiesParam);
		boolean isDetachedAuths = Boolean.TRUE == parameters.get(isDetachedAuthsParams);
		boolean hasSecurity = parameters.getSchemaType().hasSecurity();

		List<String> ancestors = new ArrayList<>();
		if (hasSecurity) {

			List<SecurityModelAuthorization> authsOnMetadatas = securityModel.getAuthorizationDetailsOnMetadatasProvidingSecurity(
					parameters.get(metadatasProvidingSecurityParams));

			if (hierarchyDependencyValue != null && !isDetachedAuths && !hasActiveOverridingAuth(authsOnMetadatas)) {
				ancestors.addAll(hierarchyDependencyValue.getAttachedAncestors());
			}

			if (!isDetachedAuths) {
				for (SecurityModelAuthorization authOnMetadata : authsOnMetadatas) {
					if (!ancestors.contains(authOnMetadata.getDetails().getTarget())) {
						ancestors.add(authOnMetadata.getDetails().getTarget());
					}
				}
			}

			ancestors.add(parameters.getId());
		}
		return ancestors;
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
		return asList(taxonomiesParam, isDetachedAuthsParams, securityModelDependency, metadatasProvidingSecurityParams);
	}
}
