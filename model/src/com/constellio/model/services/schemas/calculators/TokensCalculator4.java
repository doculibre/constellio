package com.constellio.model.services.schemas.calculators;

import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.MetadataValueCalculator;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.HierarchyDependencyValue;
import com.constellio.model.entities.calculators.dependencies.LocalDependency;
import com.constellio.model.entities.calculators.dependencies.SpecialDependencies;
import com.constellio.model.entities.calculators.dependencies.SpecialDependency;
import com.constellio.model.entities.records.wrappers.Group;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.security.Role;
import com.constellio.model.entities.security.SecurityModel;
import com.constellio.model.entities.security.SecurityModelAuthorization;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.constellio.model.services.schemas.builders.CommonMetadataBuilder.ALL_REMOVED_AUTHS;
import static com.constellio.model.services.schemas.builders.CommonMetadataBuilder.LOGICALLY_DELETED;
import static com.constellio.model.services.schemas.builders.CommonMetadataBuilder.MANUAL_TOKENS;
import static com.constellio.model.services.schemas.builders.CommonMetadataBuilder.VISIBLE_IN_TREES;

public class TokensCalculator4 implements MetadataValueCalculator<List<String>> {

	LocalDependency<List<String>> manualTokensParam = LocalDependency.toAStringList(MANUAL_TOKENS);

	LocalDependency<List<String>> allRemovedAuthsParam = LocalDependency.toAStringList(ALL_REMOVED_AUTHS);

	LocalDependency<Boolean> logicallyDeletedParam = LocalDependency.toABoolean(LOGICALLY_DELETED);

	LocalDependency<Boolean> visibleInTreesParam = LocalDependency.toABoolean(VISIBLE_IN_TREES);

	SpecialDependency<SecurityModel> securityModelSpecialDependency = SpecialDependencies.SECURITY_MODEL;

	SpecialDependency<HierarchyDependencyValue> hierarchyDependencyValuesParam = SpecialDependencies.HIERARCHY;

	LocalDependency<Boolean> isDetachedParams = LocalDependency.toABoolean(Schemas.IS_DETACHED_AUTHORIZATIONS.getLocalCode());

	@Override
	public List<String> calculate(CalculatorParameters parameters) {
		Set<String> tokens = new HashSet<>();
		SecurityModel securityModel = parameters.get(securityModelSpecialDependency);
		List<String> manualTokens = parameters.get(manualTokensParam);
		HierarchyDependencyValue hierarchyDependencyValue = parameters.get(hierarchyDependencyValuesParam);
		List<SecurityModelAuthorization> authorizations = new ArrayList<>(securityModel.getAuthorizationsOnTarget(parameters.getId()));
		boolean detached = Boolean.TRUE.equals(parameters.get(isDetachedParams));

		List<String> allRemovedAuths = parameters.get(allRemovedAuthsParam);

		if (!detached) {
			for (String inheritedNonTaxonomyAuthId : hierarchyDependencyValue.getInheritedNonTaxonomyAuthorizations()) {
				//hierarchyDependencyValue.getRemovedAuthorizationIds().contains(inheritedNonTaxonomyAuthId)
				if (!allRemovedAuths.contains(inheritedNonTaxonomyAuthId)) {
					authorizations.add(securityModel.getAuthorizationWithId(inheritedNonTaxonomyAuthId));
				}
			}
		}

		String typeSmallCode = parameters.getSchemaType().getSmallCode();
		if (typeSmallCode == null) {
			typeSmallCode = parameters.getSchemaType().getCode();
		}
		for (SecurityModelAuthorization authorization : authorizations) {
			if (authorization.getDetails().isActiveAuthorization() && !authorization.isConceptAuth()) {
				for (String access : authorization.getDetails().getRoles()) {
					for (User user : authorization.getUsers()) {
						addPrincipalTokens(tokens, typeSmallCode, access, user.getId());
					}

					for (Group group : authorization.getGroups()) {
						if (securityModel.isGroupActive(group)) {
							addPrincipalTokens(tokens, typeSmallCode, access, group.getId());

							for (Group aGroup : securityModel.getGroupsInheritingAuthorizationsFrom(group)) {
								addPrincipalTokens(tokens, typeSmallCode, access, aGroup.getId());
							}
						}
					}
				}
			}
		}

		tokens.addAll(manualTokens);

		List<String> tokensList = new ArrayList<>(tokens);
		Collections.sort(tokensList);
		return tokensList;
	}

	protected void addPrincipalTokens(Set<String> tokens, String typeSmallCode, String access, String principalId) {
		if (Role.READ.equals(access)) {
			tokens.add("r_" + principalId);
			tokens.add("r" + typeSmallCode + "_" + principalId);

		} else if (Role.WRITE.equals(access)) {
			tokens.add("r_" + principalId);
			tokens.add("w_" + principalId);//TODO Check to remove this token
			tokens.add("r" + typeSmallCode + "_" + principalId);//TODO Check to remove this token
			tokens.add("w" + typeSmallCode + "_" + principalId);

		} else if (Role.DELETE.equals(access)) {
			tokens.add("r_" + principalId);
			tokens.add("r" + typeSmallCode + "_" + principalId);

		} else {
			tokens.add(access + "_" + principalId);
		}
	}

	@Override
	public List<String> getDefaultValue() {
		return Collections.emptyList();
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
		return Arrays.asList(securityModelSpecialDependency, manualTokensParam, logicallyDeletedParam,
				visibleInTreesParam, hierarchyDependencyValuesParam, allRemovedAuthsParam, isDetachedParams);
	}
}
