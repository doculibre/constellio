package com.constellio.model.services.schemas.calculators;

import com.constellio.model.entities.calculators.AbstractMetadataValueCalculator;
import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.LocalDependency;
import com.constellio.model.entities.schemas.MetadataValueType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.constellio.model.entities.schemas.MetadataValueType.STRING;

@Deprecated
public class AllUserAuthorizationsCalculator extends AbstractMetadataValueCalculator<List<String>> {

	LocalDependency<List<String>> authorizationsParam = LocalDependency.toAStringList("authorizations").whichIsRequired();

	@Override
	public List<String> calculate(CalculatorParameters parameters) {
		Set<String> calculatedAuthorizations = new HashSet<>();

		//calculatedAuthorizations.addAll(parameters.get(authorizationsParam));
		//calculatedAuthorizations.addAll(parameters.get(groupsAuthorizationsParam));

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
		return Arrays.asList(authorizationsParam);
	}
}
