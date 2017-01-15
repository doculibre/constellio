package com.constellio.model.services.schemas.calculators;

import static com.constellio.model.entities.schemas.MetadataValueType.STRING;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.MetadataValueCalculator;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.LocalDependency;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.schemas.builders.CommonMetadataBuilder;

public class UserTokensCalculator2 implements MetadataValueCalculator<List<String>> {

	LocalDependency<List<String>> allAuthorizationsParam = LocalDependency
			.toAStringList(User.ALL_USER_AUTHORIZATIONS);
	LocalDependency<List<String>> manualTokensParam = LocalDependency.toAStringList(CommonMetadataBuilder.MANUAL_TOKENS);

	@Override
	public List<String> calculate(CalculatorParameters parameters) {
		//		return TokensCalculator2.getTokensForAuthorizationIds(parameters.get(allAuthorizationsParam),
		//				parameters.get(manualTokensParam));
		return new ArrayList<>();
	}

	@Override
	public List<String> getDefaultValue() {
		return Collections.emptyList();
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
		return Arrays.asList(allAuthorizationsParam, manualTokensParam);
	}

}
