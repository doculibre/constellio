package com.constellio.app.modules.rm.model.calculators;

import static java.util.Arrays.asList;

import java.util.List;

import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.MetadataValueCalculator;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.LocalDependency;
import com.constellio.model.entities.schemas.MetadataValueType;

public class ContainerTitleCalculator implements MetadataValueCalculator<String> {

	LocalDependency<String> identifierParam = LocalDependency.toAString(ContainerRecord.IDENTIFIER);
	LocalDependency<String> temporaryIdentifierParam = LocalDependency.toAString(ContainerRecord.TEMPORARY_IDENTIFIER);

	@Override
	public String calculate(CalculatorParameters parameters) {

		String identifier = parameters.get(identifierParam);
		String temporaryIdentifier = parameters.get(temporaryIdentifierParam);

		return identifier != null ? identifier : temporaryIdentifier;

	}

	@Override
	public String getDefaultValue() {
		return null;
	}

	@Override
	public MetadataValueType getReturnType() {
		return MetadataValueType.STRING;
	}

	@Override
	public boolean isMultiValue() {
		return false;
	}

	@Override
	public List<? extends Dependency> getDependencies() {
		return asList(identifierParam, temporaryIdentifierParam);
	}
}
