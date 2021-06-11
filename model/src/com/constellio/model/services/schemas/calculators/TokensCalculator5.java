package com.constellio.model.services.schemas.calculators;

import com.constellio.data.utils.LazyIterator;
import com.constellio.model.entities.calculators.AbstractMetadataValueCalculator;
import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.LocalDependency;
import com.constellio.model.entities.calculators.dependencies.SpecialDependencies;
import com.constellio.model.entities.calculators.dependencies.SpecialDependency;
import com.constellio.model.entities.records.structures.NestedRecordAuthorizations;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.security.Role;
import com.constellio.model.entities.security.SecurityModel;
import com.constellio.model.entities.security.SecurityModelAuthorization;
import com.constellio.model.services.schemas.builders.CommonMetadataBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.constellio.model.entities.security.TransactionSecurityModel.hasActiveOverridingAuth;
import static com.constellio.model.services.schemas.builders.CommonMetadataBuilder.ALL_REMOVED_AUTHS;
import static com.constellio.model.services.schemas.builders.CommonMetadataBuilder.LOGICALLY_DELETED;
import static com.constellio.model.services.schemas.builders.CommonMetadataBuilder.MANUAL_TOKENS;
import static com.constellio.model.services.schemas.builders.CommonMetadataBuilder.NESTED_AUTHORIZATIONS;
import static com.constellio.model.services.schemas.builders.CommonMetadataBuilder.VISIBLE_IN_TREES;
import static java.lang.Boolean.TRUE;

public class TokensCalculator5 extends AbstractMetadataValueCalculator<List<String>> {

	LocalDependency<List<String>> manualTokensParam = LocalDependency.toAStringList(MANUAL_TOKENS);

	LocalDependency<List<String>> allRemovedAuthsParam = LocalDependency.toAStringList(ALL_REMOVED_AUTHS);

	LocalDependency<Boolean> logicallyDeletedParam = LocalDependency.toABoolean(LOGICALLY_DELETED);

	LocalDependency<Boolean> visibleInTreesParam = LocalDependency.toABoolean(VISIBLE_IN_TREES);

	SpecialDependency<SecurityModel> securityModelSpecialDependency = SpecialDependencies.SECURITY_MODEL;

	LocalDependency<Boolean> isDetachedParams = LocalDependency.toABoolean(Schemas.IS_DETACHED_AUTHORIZATIONS.getLocalCode());

	MetadatasProvidingSecurityDynamicDependency metadatasProvidingSecurityParams = new MetadatasProvidingSecurityDynamicDependency();

	LocalDependency<List<String>> attachedAncestorsParam = LocalDependency.toAStringList(CommonMetadataBuilder.ATTACHED_ANCESTORS);

	LocalDependency<NestedRecordAuthorizations> nestedRecordAuthorizationsParam = LocalDependency.toAStructure(NESTED_AUTHORIZATIONS);

	@Override
	public List<String> calculate(CalculatorParameters parameters) {
		SecurityModel securityModel = parameters.get(securityModelSpecialDependency);
		List<SecurityModelAuthorization> authorizations = new ArrayList<>();
		List<SecurityModelAuthorization> removedOrDetachedAuthorizations = new ArrayList<>();
		calculateAppliedAuthorizations(parameters, securityModel, authorizations, removedOrDetachedAuthorizations);

		List<String> manualTokens = parameters.get(manualTokensParam);


		List<SecurityModelAuthorization> nestedRecordAuthorizationsList;
		NestedRecordAuthorizations nestedRecordAuthorizations = parameters.get(nestedRecordAuthorizationsParam);
		if (nestedRecordAuthorizations == null) {
			nestedRecordAuthorizationsList = Collections.emptyList();
		} else {
			nestedRecordAuthorizationsList = securityModel.wrapNestedAuthorizationsOnTarget(nestedRecordAuthorizations);
		}


		final String typeSmallCode = getSchemaTypeSmallCode(parameters);
		final Set<String> negativeTokens = buildNegativeTokens(securityModel, authorizations, typeSmallCode);
		final Set<String> removedNegativeTokens = buildRemovedNegativeTokens(securityModel, removedOrDetachedAuthorizations, typeSmallCode);
		final Set<String> positiveTokens = buildPositiveTokens(securityModel, authorizations, typeSmallCode, negativeTokens);

		final Set<String> nonCascadingPositiveTokens = buildPositiveTokens(securityModel, nestedRecordAuthorizationsList, typeSmallCode, negativeTokens);
		positiveTokens.addAll(nonCascadingPositiveTokens);

		return mergeTokens(manualTokens, removedNegativeTokens, negativeTokens, positiveTokens);
	}

	private List<SecurityModelAuthorization> getInheritedAuthorizationsTargettingSecurisedRecords(
			SecurityModel securityModel,
			List<String> attachedAncestors,
			boolean detached) {

		List<SecurityModelAuthorization> returnedAuths = new ArrayList<>();

		if (!detached) {
			for (String attachedAncestor : attachedAncestors) {
				if (!attachedAncestor.startsWith("-")) {
					for (SecurityModelAuthorization inheritedNonTaxonomyAuth : securityModel.getAuthorizationsOnTarget(attachedAncestor)) {
						if (inheritedNonTaxonomyAuth.isSecurableRecord()) {
							returnedAuths.add(inheritedNonTaxonomyAuth);
						}
					}
				}
			}
		}
		return returnedAuths;
	}

	private List<SecurityModelAuthorization> getInheritedAuthorizationsTargettingAnyRecordsNoMatterIfDetached(
			SecurityModel securityModel,
			List<String> attachedAncestors) {

		List<SecurityModelAuthorization> returnedAuths = new ArrayList<>();

		for (String attachedAncestor : attachedAncestors) {

			String ancestor = attachedAncestor;
			if (attachedAncestor.startsWith("-")) {
				ancestor = attachedAncestor.substring(1);
			}

			returnedAuths.addAll(securityModel.getAuthorizationsOnTarget(ancestor));
		}
		return returnedAuths;
	}

	private void calculateAppliedAuthorizations(CalculatorParameters parameters,
												SecurityModel securityModel,
												List<SecurityModelAuthorization> authorizations,
												List<SecurityModelAuthorization> removedOrDetachedAuthorizations) {

		authorizations.addAll(securityModel.getAuthorizationsOnTarget(parameters.getId()));
		boolean detached = TRUE.equals(parameters.get(isDetachedParams));

		List<String> allRemovedAuths = parameters.get(allRemovedAuthsParam);
		List<String> attachedAncestors = parameters.get(attachedAncestorsParam);

		List<SecurityModelAuthorization> authsFromMetadatas = securityModel.getAuthorizationDetailsOnMetadatasProvidingSecurity(
				parameters.get(metadatasProvidingSecurityParams));

		authorizations.addAll(authsFromMetadatas);

		if (!hasActiveOverridingAuth(authsFromMetadatas)) {


			if (!detached) {
				List<SecurityModelAuthorization> inheritedAuthorizationsTargettingSecurisedRecords =
						getInheritedAuthorizationsTargettingSecurisedRecords(securityModel, attachedAncestors, detached);
				for (SecurityModelAuthorization auth : inheritedAuthorizationsTargettingSecurisedRecords) {
					if (!allRemovedAuths.contains(auth.getDetails().getId())) {
						authorizations.add(auth);
					} else {
						removedOrDetachedAuthorizations.add(auth);
					}
				}
			} else {
				List<SecurityModelAuthorization> inheritedAuthorizationsTargettingAnyRecords =
						getInheritedAuthorizationsTargettingAnyRecordsNoMatterIfDetached(securityModel, attachedAncestors);
				removedOrDetachedAuthorizations.addAll(inheritedAuthorizationsTargettingAnyRecords);
			}
		}
	}


	private String getSchemaTypeSmallCode(CalculatorParameters parameters) {
		final String typeSmallCode;
		if (parameters.getSchemaType().getSmallCode() != null) {
			typeSmallCode = parameters.getSchemaType().getSmallCode();

		} else {
			typeSmallCode = parameters.getSchemaType().getCode();

		}
		return typeSmallCode;
	}


	@NotNull
	private Set<String> buildPositiveTokens(SecurityModel securityModel,
											List<SecurityModelAuthorization> authorizations, final String typeSmallCode,
											final Set<String> negativeTokens) {
		final Set<String> positiveTokens = new HashSet<>();
		for (SecurityModelAuthorization authorization : authorizations) {
			if (authorization.getDetails().isActiveAuthorization() && authorization.isSecurableRecord()
				&& !authorization.getDetails().isNegative()) {

				forEachAccessAndPrincipalInheriting(securityModel, authorization, new TokensCalculator5.Caller() {
					@Override
					public void call(String access, String principalId) {
						addPrincipalPositiveTokens(positiveTokens, negativeTokens, typeSmallCode, access, principalId);
					}
				});
			}
		}
		return positiveTokens;
	}

	//	@NotNull
	//	private Set<String> buildNonCascadingPositiveTokens(SecurityModel securityModel,
	//														NestedRecordAuthorizations nestedRecordAuthorizations,
	//														final String typeSmallCode,
	//														final Set<String> negativeTokens) {
	//		final Set<String> positiveTokens = new HashSet<>();
	//		for (NestedRecordAuthorization authorization : nestedRecordAuthorizations.getAuthorizations()) {
	//			if (!authorization.isNegative()) {
	//				forEachAccessAndPrincipalInheriting(securityModel, authorization, new Caller() {
	//					@Override
	//					public void call(String access, String principalId) {
	//						addPrincipalPositiveTokens(positiveTokens, negativeTokens, typeSmallCode, access, principalId);
	//					}
	//				});
	//			}
	//		}
	//		return positiveTokens;
	//	}

	@NotNull
	private Set<String> buildNegativeTokens(SecurityModel securityModel,
											List<SecurityModelAuthorization> authorizations,
											final String typeSmallCode) {
		final Set<String> negativeTokens = new HashSet<>();

		for (SecurityModelAuthorization authorization : authorizations) {
			if (authorization.getDetails().isActiveAuthorization()
				&& authorization.isSecurableRecord()
				&& authorization.getDetails().isNegative()) {

				forEachAccessAndPrincipalInheriting(securityModel, authorization, new Caller() {
					@Override
					public void call(String access, String principalId) {
						addPrincipalNegativeTokens(negativeTokens, typeSmallCode, access, principalId);
					}
				});
			}
		}
		return negativeTokens;
	}

	@NotNull
	private Set<String> buildRemovedNegativeTokens(SecurityModel securityModel,
												   List<SecurityModelAuthorization> removedOrDetachedAuthorizations,
												   final String typeSmallCode) {
		final Set<String> removedNegativeTokens = new HashSet<>();
		for (SecurityModelAuthorization authorization : removedOrDetachedAuthorizations) {
			if (authorization.getDetails().isActiveAuthorization() && authorization.isSecurableRecord()
				&& authorization.getDetails().isNegative()) {

				forEachAccessAndPrincipalInheriting(securityModel, authorization, new Caller() {
					@Override
					public void call(String access, String principalId) {
						addPrincipalNegativeTokens(removedNegativeTokens, typeSmallCode, access, principalId);
					}
				});
			}
		}
		return removedNegativeTokens;
	}

	@NotNull
	private List<String> mergeTokens(List<String> manualTokens, Set<String> removedNegativeTokens,
									 Set<String> negativeTokens, Set<String> positiveTokens) {
		Set<String> tokens = new HashSet<>(positiveTokens);
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

	private void forEachAccessAndPrincipalInheriting(SecurityModel securityModel,
													 SecurityModelAuthorization authorization, Caller caller) {
		for (String access : authorization.getDetails().getRoles()) {
			for (String userId : authorization.getUserIds()) {
				caller.call(access, userId);
			}

			for (String groupId : authorization.getGroupIds()) {
				if (securityModel.isGroupActive(groupId)) {
					for (String aGroup : securityModel.getGroupsInheritingAuthorizationsFrom(groupId)) {
						caller.call(access, aGroup);
					}
				}
			}
		}
	}

	private interface Caller {
		void call(String access, String principalIdInheritingIt);
	}

	private class PrincipalsInheritingAuthorizationIterator extends LazyIterator<String> {

		@Override
		protected String getNextOrNull() {
			return null;
		}
	}

	private void addPrincipalPositiveTokens(Set<String> positiveTokens, Set<String> negativeTokens,
											String typeSmallCode, String access, String principalId) {

		if (Role.READ.equals(access)) {
			addPrincipalPositiveReadTokens(positiveTokens, negativeTokens, typeSmallCode, principalId);

		} else if (Role.WRITE.equals(access)) {
			addPrincipalPositiveWriteTokens(positiveTokens, negativeTokens, typeSmallCode, principalId);

		} else if (Role.DELETE.equals(access)) {
			addPrincipalPositiveDeleteTokens(positiveTokens, negativeTokens, typeSmallCode, principalId);

		} else {
			positiveTokens.add(access + "_" + principalId);
		}
	}

	private void addPrincipalPositiveWriteTokens(Set<String> positiveTokens, Set<String> negativeTokens,
												 String typeSmallCode, String principalId) {
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
	}

	private void addPrincipalPositiveDeleteTokens(Set<String> positiveTokens, Set<String> negativeTokens,
												  String typeSmallCode, String principalId) {
		String readOnRecordsOfAnyTypeToken = "r_" + principalId;
		String readOnRecordsOfRecordTypeToken = "r" + typeSmallCode + "_" + principalId;

		if (!negativeTokens.contains(readOnRecordsOfAnyTypeToken)) {
			positiveTokens.add(readOnRecordsOfAnyTypeToken);
			positiveTokens.add(readOnRecordsOfRecordTypeToken);
		}
	}

	private void addPrincipalPositiveReadTokens(Set<String> positiveTokens, Set<String> negativeTokens,
												String typeSmallCode, String principalId) {
		String readOnRecordsOfAnyTypeToken = "r_" + principalId;
		String readOnRecordsOfRecordTypeToken = "r" + typeSmallCode + "_" + principalId;

		if (!negativeTokens.contains(readOnRecordsOfAnyTypeToken)) {
			positiveTokens.add(readOnRecordsOfAnyTypeToken);
			positiveTokens.add(readOnRecordsOfRecordTypeToken);
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
				visibleInTreesParam, attachedAncestorsParam, allRemovedAuthsParam, isDetachedParams,
				metadatasProvidingSecurityParams, nestedRecordAuthorizationsParam);
	}

}
