package com.constellio.model.services.schemas.calculators;

import static com.constellio.model.services.schemas.builders.CommonMetadataBuilder.ALL_REMOVED_AUTHS;
import static com.constellio.model.services.schemas.builders.CommonMetadataBuilder.ATTACHED_ANCESTORS;
import static com.constellio.model.services.schemas.builders.CommonMetadataBuilder.MANUAL_TOKENS;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.MetadataValueCalculator;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.LocalDependency;
import com.constellio.model.entities.schemas.MetadataValueType;

public class TokensCalculator3 implements MetadataValueCalculator<List<String>> {

	LocalDependency<List<String>> ancestorsParam = LocalDependency.toAStringList(ATTACHED_ANCESTORS);
	LocalDependency<List<String>> allRemovedParams = LocalDependency.toAStringList(ALL_REMOVED_AUTHS);
	LocalDependency<List<String>> manualTokensParam = LocalDependency.toAStringList(MANUAL_TOKENS);

	public static List<String> getTokensForAuthorizationIds(List<String> authorizationIds, List<String> manualTokens) {
		List<String> calculatedTokens = new ArrayList<>();
		for (String auth : authorizationIds) {
			calculatedTokens.addAll(getTokensForAuthId(auth));
		}
		calculatedTokens.addAll(manualTokens);

		return calculatedTokens;
	}

	public static List<String> getTokensForAuthId(String auth) {
		List<String> calculatedTokens = new ArrayList<>();
		if (auth != null && !auth.startsWith("-")) {
			String[] authSplitted = auth.split("_");
			String accessCode = authSplitted[0];
			String roles = authSplitted[1];
			String authId = authSplitted[2];
			if (accessCode.length() <= 1) {
				calculatedTokens.add(accessCode + "_" + roles + "_" + authId);

			} else {
				for (int i = 0; i < accessCode.length(); i++) {
					calculatedTokens.add(accessCode.charAt(i) + "_" + roles + "_" + authId);
				}

			}
		}
		return calculatedTokens;
	}

	@Override
	public List<String> calculate(CalculatorParameters parameters) {
		List<String> ancestors = parameters.get(ancestorsParam);
		List<String> allRemoved = parameters.get(allRemovedParams);
		List<String> manualTokens = parameters.get(manualTokensParam);

		return new ArrayList<>();
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
		return Arrays.asList(ancestorsParam, allRemovedParams, manualTokensParam);
	}
}
