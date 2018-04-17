package com.constellio.model.services.schemas.calculators;

import static com.constellio.model.services.schemas.builders.CommonMetadataBuilder.LOGICALLY_DELETED;
import static com.constellio.model.services.schemas.builders.CommonMetadataBuilder.MANUAL_TOKENS;
import static com.constellio.model.services.schemas.builders.CommonMetadataBuilder.VISIBLE_IN_TREES;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;

import com.constellio.data.utils.KeyListMap;
import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.MetadataValueCalculator;
import com.constellio.model.entities.calculators.dependencies.AllPrincipalsAuthsDependencyValue;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.LocalDependency;
import com.constellio.model.entities.calculators.dependencies.ReferenceDependency;
import com.constellio.model.entities.calculators.dependencies.SpecialDependencies;
import com.constellio.model.entities.calculators.dependencies.SpecialDependency;
import com.constellio.model.entities.records.wrappers.SolrAuthorizationDetails;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.security.Role;
import com.constellio.model.services.schemas.builders.CommonMetadataBuilder;

public class TokensCalculator4 implements MetadataValueCalculator<List<String>> {

	LocalDependency<List<String>> manualTokensParam = LocalDependency.toAStringList(MANUAL_TOKENS);

	LocalDependency<Boolean> logicallyDeletedParam = LocalDependency.toABoolean(LOGICALLY_DELETED);

	LocalDependency<Boolean> visibleInTreesParam = LocalDependency.toABoolean(VISIBLE_IN_TREES);

	ReferenceDependency<SortedMap<String, List<String>>> authorizationsRolesParam = ReferenceDependency.toAString(
			CommonMetadataBuilder.NON_TAXONOMY_AUTHORIZATIONS, SolrAuthorizationDetails.ROLES).whichIsMultivalue()
			.whichAreReferencedMultiValueGroupedByReference();

	SpecialDependency<AllPrincipalsAuthsDependencyValue> allPrincipalsAuthsParam = SpecialDependencies.ALL_PRINCIPALS;

	@Override
	public List<String> calculate(CalculatorParameters parameters) {
		Set<String> tokens = new HashSet<>();

		List<String> manualTokens = parameters.get(manualTokensParam);
		AllPrincipalsAuthsDependencyValue principalsAuthorizations = parameters.get(allPrincipalsAuthsParam);

		SortedMap<String, List<String>> authorizationsRoles = parameters.get(authorizationsRolesParam);

		KeyListMap<String, String> principalsTokens = principalsAuthorizations
				.getPrincipalIdsWithAnyAuthorization(authorizationsRoles);

		String typeSmallCode = parameters.getSchemaType().getSmallCode();
		if (typeSmallCode == null) {
			typeSmallCode = parameters.getSchemaType().getCode();
		}
		for (Entry<String, List<String>> entry : principalsTokens.getMapEntries()) {
			for (String access : entry.getValue()) {
				if (Role.READ.equals(access)) {
					tokens.add("r_" + entry.getKey());
					tokens.add("r" + typeSmallCode + "_" + entry.getKey());

				} else if (Role.WRITE.equals(access)) {
					tokens.add("r_" + entry.getKey());
					tokens.add("w_" + entry.getKey());//TODO Check to remove this token
					tokens.add("r" + typeSmallCode + "_" + entry.getKey());//TODO Check to remove this token
					tokens.add("w" + typeSmallCode + "_" + entry.getKey());

				} else if (Role.DELETE.equals(access)) {
					tokens.add("r_" + entry.getKey());
					tokens.add("r" + typeSmallCode + "_" + entry.getKey());

				} else {
					tokens.add(access + "_" + entry.getKey());
				}
			}
		}

		tokens.addAll(manualTokens);

		List<String> tokensList = new ArrayList<>(tokens);
		Collections.sort(tokensList);
		return tokensList;
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
		return Arrays.asList(allPrincipalsAuthsParam, authorizationsRolesParam, manualTokensParam, logicallyDeletedParam,
				visibleInTreesParam);
	}
}
