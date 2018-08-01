package com.constellio.model.services.schemas.calculators;

import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.MetadataValueCalculator;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.LocalDependency;
import com.constellio.model.entities.schemas.MetadataValueType;

import java.util.*;

import static com.constellio.model.entities.schemas.MetadataValueType.STRING;

public class AllAuthorizationsCalculator implements MetadataValueCalculator<List<String>> {

	LocalDependency<List<String>> authorizationsParam = LocalDependency.toAStringList("authorizations");
	LocalDependency<List<String>> inheritedAuthorizationsParam = LocalDependency.toAStringList("inheritedauthorizations");

	@Override
	public List<String> calculate(CalculatorParameters parameters) {
		Set<String> calculatedAuthorizations = new HashSet<>();
		List<String> authorizations = parameters.get(authorizationsParam);
		List<String> inheritedAuthorizations = parameters.get(inheritedAuthorizationsParam);

		calculatedAuthorizations.addAll(authorizations);
		calculatedAuthorizations.addAll(inheritedAuthorizations);
		return new ArrayList<>(calculatedAuthorizations);
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
		return Arrays.asList(authorizationsParam, inheritedAuthorizationsParam);
	}
}
