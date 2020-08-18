package com.constellio.model.services.schemas.calculators;

import com.constellio.data.dao.dto.records.RecordId;
import com.constellio.data.utils.LangUtils;
import com.constellio.model.entities.calculators.AbstractMetadataValueCalculator;
import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.DynamicDependencyValues;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.HierarchyDependencyValue;
import com.constellio.model.entities.calculators.dependencies.LocalDependency;
import com.constellio.model.entities.calculators.dependencies.SpecialDependencies;
import com.constellio.model.entities.calculators.dependencies.SpecialDependency;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.security.SecurityModel;
import com.constellio.model.entities.security.SecurityModelAuthorization;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.constellio.model.entities.schemas.MetadataValueType.STRING;
import static com.constellio.model.entities.security.TransactionSecurityModel.hasActiveOverridingAuth;
import static com.constellio.model.services.schemas.builders.CommonMetadataBuilder.DETACHED_AUTHORIZATIONS;
import static java.util.Arrays.asList;

public class AttachedAncestorsCalculator extends AbstractMetadataValueCalculator<List<String>> {

	SpecialDependency<HierarchyDependencyValue> taxonomiesParam = SpecialDependencies.HIERARCHY;
	LocalDependency<Boolean> isDetachedAuthsParams = LocalDependency.toABoolean(DETACHED_AUTHORIZATIONS);
	SpecialDependency<SecurityModel> securityModelDependency = SpecialDependencies.SECURITY_MODEL;
	MetadatasProvidingSecurityDynamicDependency metadatasProvidingSecurityParams = new MetadatasProvidingSecurityDynamicDependency();

	@Override
	public List<String> calculate(CalculatorParameters parameters) {
		SecurityModel securityModel = parameters.get(securityModelDependency);
		HierarchyDependencyValue hierarchyDependencyValue = parameters.get(taxonomiesParam);

		//		Set<String> ids = new HashSet<>();
		//		ids.addAll(hierarchyDependencyValue.getAttachedPrincipalConceptsIntIdsFromParent());

		//return ids.stream().map((intId) -> RecordId.toId(intId).stringValue()).collect(Collectors.toList());

		boolean isDetachedAuths = Boolean.TRUE.equals(parameters.get(isDetachedAuthsParams));
		boolean hasSecurity = parameters.getSchemaType().hasSecurity();

		Set<String> ancestors = new HashSet<>();
		List<String> possiblyDetachedAncestors = new ArrayList<>();
		if (hasSecurity) {
			DynamicDependencyValues values = parameters.get(metadatasProvidingSecurityParams);
			List<SecurityModelAuthorization> authsOnMetadatas = securityModel.getAuthorizationDetailsOnMetadatasProvidingSecurity(values);

			if (hierarchyDependencyValue != null) {
				if (!isDetachedAuths && !hasActiveOverridingAuth(authsOnMetadatas)) {
					ancestors.addAll(hierarchyDependencyValue.getPrincipalAncestorsIntIds()
							.stream().map((id) -> RecordId.toId(id).stringValue()).collect(Collectors.toList()));
					hierarchyDependencyValue.getAttachedPrincipalConceptsIntIdsFromParent()
							.forEach((intId) -> ancestors.add(RecordId.toId(intId).stringValue()));
					//	ancestors.addAll(hierarchyDependencyValue.getAttachedAncestors());
				} else {
					//possiblyDetachedAncestors.addAll(hierarchyDependencyValue.getAttachedAncestors());
					hierarchyDependencyValue.getAttachedPrincipalConceptsIntIdsFromParent()
							.forEach((intId) -> possiblyDetachedAncestors.add(RecordId.toId(intId).stringValue()));
				}
			}

			if (!isDetachedAuths) {
				for (Metadata metadata : values.getAvailableMetadatasWithAValue()) {
					if (metadata.isMultivalue()) {
						ancestors.addAll(values.<List<String>>getValue(metadata));
					} else {
						ancestors.add(values.<String>getValue(metadata));
					}
				}
			}

			ancestors.add(parameters.getId());
		}

		for (String possiblyDetachedAncestor : possiblyDetachedAncestors) {
			if (!ancestors.contains(possiblyDetachedAncestor)) {
				ancestors.add("-" + possiblyDetachedAncestor);
			}
		}

		return LangUtils.toSortedList(ancestors);
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
