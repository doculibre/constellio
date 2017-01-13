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

	LocalDependency<List<String>> manualTokensParam = LocalDependency.toAStringList(MANUAL_TOKENS);

	@Override
	public List<String> calculate(CalculatorParameters parameters) {
		List<String> manualTokens = parameters.get(manualTokensParam);

		List<String> tokens = new ArrayList<>();
		tokens.addAll(manualTokens);
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
		return Arrays.asList(manualTokensParam);
	}
}
