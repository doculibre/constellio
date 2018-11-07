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
import static com.constellio.model.services.schemas.calculators.NonTaxonomyAuthorizationsCalculator.hasActiveOverridingAuth;
import static java.lang.Boolean.TRUE;

public class TokensCalculator4 implements MetadataValueCalculator<List<String>> {

	LocalDependency<List<String>> manualTokensParam = LocalDependency.toAStringList(MANUAL_TOKENS);

	LocalDependency<List<String>> allRemovedAuthsParam = LocalDependency.toAStringList(ALL_REMOVED_AUTHS);

	LocalDependency<Boolean> logicallyDeletedParam = LocalDependency.toABoolean(LOGICALLY_DELETED);

	LocalDependency<Boolean> visibleInTreesParam = LocalDependency.toABoolean(VISIBLE_IN_TREES);

	SpecialDependency<SecurityModel> securityModelSpecialDependency = SpecialDependencies.SECURITY_MODEL;

	SpecialDependency<HierarchyDependencyValue> hierarchyDependencyValuesParam = SpecialDependencies.HIERARCHY;

	LocalDependency<Boolean> isDetachedParams = LocalDependency.toABoolean(Schemas.IS_DETACHED_AUTHORIZATIONS.getLocalCode());

	MetadatasProvidingSecurityDynamicDependency metadatasProvidingSecurityParams = new MetadatasProvidingSecurityDynamicDependency();

	@Override
	public List<String> calculate(CalculatorParameters parameters) {
		SecurityModel securityModel = parameters.get(securityModelSpecialDependency);
		List<SecurityModelAuthorization> authorizations = new ArrayList<>();
		List<SecurityModelAuthorization> removedOrDetachedAuthorizations = new ArrayList<>();
		calculateAppliedAuthorizations(parameters, securityModel, authorizations, removedOrDetachedAuthorizations);
		return buildTokens(parameters, securityModel, authorizations, removedOrDetachedAuthorizations);
	}

	private void calculateAppliedAuthorizations(CalculatorParameters parameters,
												SecurityModel securityModel,
												List<SecurityModelAuthorization> authorizations,
												List<SecurityModelAuthorization> removedOrDetachedAuthorizations) {
		HierarchyDependencyValue hierarchyDependencyValue = parameters.get(hierarchyDependencyValuesParam);
		authorizations.addAll(securityModel.getAuthorizationsOnTarget(parameters.getId()));
		boolean detached = TRUE.equals(parameters.get(isDetachedParams));

		List<String> allRemovedAuths = parameters.get(allRemovedAuthsParam);

		List<SecurityModelAuthorization> authsFromMetadatas = securityModel.getAuthorizationDetailsOnMetadatasProvidingSecurity(
				parameters.get(metadatasProvidingSecurityParams));

		authorizations.addAll(authsFromMetadatas);

		if (!hasActiveOverridingAuth(authsFromMetadatas)) {
			if (!detached) {
				for (String inheritedNonTaxonomyAuthId : hierarchyDependencyValue.getInheritedNonTaxonomyAuthorizations()) {
					SecurityModelAuthorization auth = securityModel.getAuthorizationWithId(inheritedNonTaxonomyAuthId);
					if (!allRemovedAuths.contains(inheritedNonTaxonomyAuthId)) {
						if (auth != null) {
							authorizations.add(auth);
						}
					} else {
						if (auth != null) {
							removedOrDetachedAuthorizations.add(auth);
						}
					}
				}
			} else {
				for (String inheritedNonTaxonomyAuthId : hierarchyDependencyValue.getInheritedNonTaxonomyAuthorizations()) {
					SecurityModelAuthorization auth = securityModel.getAuthorizationWithId(inheritedNonTaxonomyAuthId);
					if (auth != null) {
						removedOrDetachedAuthorizations.add(auth);
					}
				}
			}
		}
	}

	private List<String> buildTokens(CalculatorParameters parameters, SecurityModel securityModel,
									 List<SecurityModelAuthorization> authorizations,
									 List<SecurityModelAuthorization> removedOrDetachedAuthorizations) {

		List<String> manualTokens = parameters.get(manualTokensParam);


		String typeSmallCode = parameters.getSchemaType().getSmallCode();
		if (typeSmallCode == null) {
			typeSmallCode = parameters.getSchemaType().getCode();
		}

		Set<String> removedNegativeTokens = new HashSet<>();
		Set<String> negativeTokens = new HashSet<>();

		for (SecurityModelAuthorization authorization : authorizations) {
			if (authorization.getDetails().isActiveAuthorization() && !authorization.isConceptOrValueList()
				&& authorization.getDetails().isNegative()) {
				for (String access : authorization.getDetails().getRoles()) {
					for (User user : authorization.getUsers()) {
						addPrincipalNegativeTokens(negativeTokens, typeSmallCode, access, user.getId());
					}

					for (Group group : authorization.getGroups()) {
						if (securityModel.isGroupActive(group)) {
							addPrincipalNegativeTokens(negativeTokens, typeSmallCode, access, group.getId());

							for (Group aGroup : securityModel.getGroupsInheritingAuthorizationsFrom(group)) {
								addPrincipalNegativeTokens(negativeTokens, typeSmallCode, access, aGroup.getId());
							}
						}
					}
				}
			}
		}


		for (SecurityModelAuthorization authorization : removedOrDetachedAuthorizations) {
			if (authorization.getDetails().isActiveAuthorization() && !authorization.isConceptOrValueList()
				&& authorization.getDetails().isNegative()) {
				for (String access : authorization.getDetails().getRoles()) {
					for (User user : authorization.getUsers()) {
						addPrincipalNegativeTokens(removedNegativeTokens, typeSmallCode, access, user.getId());
					}

					for (Group group : authorization.getGroups()) {
						if (securityModel.isGroupActive(group)) {
							addPrincipalNegativeTokens(removedNegativeTokens, typeSmallCode, access, group.getId());

							for (Group aGroup : securityModel.getGroupsInheritingAuthorizationsFrom(group)) {
								addPrincipalNegativeTokens(removedNegativeTokens, typeSmallCode, access, aGroup.getId());
							}
						}
					}
				}
			}
		}

		Set<String> positiveTokens = new HashSet<>();
		for (SecurityModelAuthorization authorization : authorizations) {
			if (authorization.getDetails().isActiveAuthorization() && !authorization.isConceptOrValueList()
				&& !authorization.getDetails().isNegative()) {
				for (String access : authorization.getDetails().getRoles()) {
					for (User user : authorization.getUsers()) {
						addPrincipalPositiveTokens(positiveTokens, negativeTokens, typeSmallCode, access, user.getId());
					}

					for (Group group : authorization.getGroups()) {
						if (securityModel.isGroupActive(group)) {
							addPrincipalPositiveTokens(positiveTokens, negativeTokens, typeSmallCode, access, group.getId());

							for (Group aGroup : securityModel.getGroupsInheritingAuthorizationsFrom(group)) {
								addPrincipalPositiveTokens(positiveTokens, negativeTokens, typeSmallCode, access, aGroup.getId());
							}
						}
					}
				}
			}
		}

		Set<String> tokens = new HashSet<>();
		tokens.addAll(positiveTokens);
		for (String negativeToken : negativeTokens) {
			tokens.add("n" + negativeToken);
		}

		for (String removedNegativeToken : removedNegativeTokens) {
			if (!negativeTokens.contains(removedNegativeToken)) {
				tokens.add("-n" + removedNegativeToken);
			}
		}

		tokens.addAll(manualTokens);

		List<String> tokensList = new ArrayList<>(tokens);
		Collections.sort(tokensList);
		return tokensList;
	}

	private void addPrincipalPositiveTokens(Set<String> positiveTokens, Set<String> negativeTokens,
											String typeSmallCode, String access, String principalId) {

		if (Role.READ.equals(access)) {
			String readOnRecordsOfAnyTypeToken = "r_" + principalId;
			String readOnRecordsOfRecordTypeToken = "r" + typeSmallCode + "_" + principalId;

			if (!negativeTokens.contains(readOnRecordsOfAnyTypeToken)) {
				positiveTokens.add(readOnRecordsOfAnyTypeToken);
				positiveTokens.add(readOnRecordsOfRecordTypeToken);
			}

		} else if (Role.WRITE.equals(access)) {
			String readOnRecordsOfAnyTypeToken = "r_" + principalId;
			String readOnRecordsOfRecordTypeToken = "r" + typeSmallCode + "_" + principalId; //TODO Check to remove this token
			if (!negativeTokens.contains(readOnRecordsOfAnyTypeToken)) {
				positiveTokens.add(readOnRecordsOfAnyTypeToken);
				positiveTokens.add(readOnRecordsOfRecordTypeToken);
			}

			String writeOnRecordsOfAnyTypeToken = "w_" + principalId; //TODO Check to remove this token
			String writeOnRecordsOfRecordTypeToken = "w" + typeSmallCode + "_" + principalId;
			if (!negativeTokens.contains(writeOnRecordsOfAnyTypeToken)) {
				positiveTokens.add(writeOnRecordsOfAnyTypeToken);
				positiveTokens.add(writeOnRecordsOfRecordTypeToken);
			}

		} else if (Role.DELETE.equals(access)) {
			String readOnRecordsOfAnyTypeToken = "r_" + principalId;
			String readOnRecordsOfRecordTypeToken = "r" + typeSmallCode + "_" + principalId;

			if (!negativeTokens.contains(readOnRecordsOfAnyTypeToken)) {
				positiveTokens.add(readOnRecordsOfAnyTypeToken);
				positiveTokens.add(readOnRecordsOfRecordTypeToken);
			}

		} else {
			positiveTokens.add(access + "_" + principalId);
		}
	}

	private void addPrincipalNegativeTokens(Set<String> negativeTokens, String typeSmallCode, String access,
											String principalId) {

		if (Role.READ.equals(access)) {
			negativeTokens.add("r_" + principalId);
			negativeTokens.add("w_" + principalId);//TODO Check to remove this token
			negativeTokens.add("d_" + principalId);
		} else if (Role.WRITE.equals(access)) {
			negativeTokens.add("w_" + principalId);//TODO Check to remove this token


		} else if (Role.DELETE.equals(access)) {
			negativeTokens.add("d_" + principalId);//TODO Check to remove this token

		} else {
			negativeTokens.add(access + "_" + principalId);
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
				visibleInTreesParam, hierarchyDependencyValuesParam, allRemovedAuthsParam, isDetachedParams,
				metadatasProvidingSecurityParams);
	}
}
