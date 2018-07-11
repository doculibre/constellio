package com.constellio.model.services.schemas.calculators;

import static com.constellio.model.services.schemas.builders.CommonMetadataBuilder.LOGICALLY_DELETED;
import static com.constellio.model.services.schemas.builders.CommonMetadataBuilder.MANUAL_TOKENS;
import static com.constellio.model.services.schemas.builders.CommonMetadataBuilder.VISIBLE_IN_TREES;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.MetadataValueCalculator;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.LocalDependency;
import com.constellio.model.entities.calculators.dependencies.SpecialDependencies;
import com.constellio.model.entities.calculators.dependencies.SpecialDependency;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.security.Role;
import com.constellio.model.entities.security.SecurityModel;
import com.constellio.model.entities.security.SecurityModelAuthorization;

public class TokensCalculator5 implements MetadataValueCalculator<List<String>> {

	LocalDependency<List<String>> manualTokensParam = LocalDependency.toAStringList(MANUAL_TOKENS);

	LocalDependency<Boolean> logicallyDeletedParam = LocalDependency.toABoolean(LOGICALLY_DELETED);

	LocalDependency<Boolean> visibleInTreesParam = LocalDependency.toABoolean(VISIBLE_IN_TREES);

	SpecialDependency<SecurityModel> securityModelSpecialDependency = SpecialDependencies.SECURITY_MODEL;

	@Override
	public List<String> calculate(CalculatorParameters parameters) {
		Set<String> tokens = new HashSet<>();
		SecurityModel securityModel = parameters.get(securityModelSpecialDependency);
		List<String> manualTokens = parameters.get(manualTokensParam);
		List<SecurityModelAuthorization> authorizations = securityModel.getAuthorizationsOnTarget(parameters.getId());

		String typeSmallCode = parameters.getSchemaType().getSmallCode();
		if (typeSmallCode == null) {
			typeSmallCode = parameters.getSchemaType().getCode();
		}
		for (SecurityModelAuthorization authorization : authorizations) {
			for (String access : authorization.getDetails().getRoles()) {
				for (String principalId : authorization.getPrincipalIds()) {
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
		return Arrays.asList(securityModelSpecialDependency, manualTokensParam, logicallyDeletedParam, visibleInTreesParam);
	}
}
