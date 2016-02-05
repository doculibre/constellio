package com.constellio.model.services.schemas.calculators;

import static com.constellio.model.entities.schemas.MetadataValueType.STRING;

import java.util.Arrays;
import java.util.List;

import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.MetadataValueCalculator;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.LocalDependency;
import com.constellio.model.entities.calculators.dependencies.SpecialDependencies;
import com.constellio.model.entities.calculators.dependencies.SpecialDependency;
import com.constellio.model.entities.schemas.MetadataValueType;

public class PrincipalPathCalculator implements MetadataValueCalculator<String> {

	LocalDependency<List<String>> pathDependency = LocalDependency.toAStringList("path").whichIsRequired();
	SpecialDependency<String> taxoPrincipaleDependency = SpecialDependencies.PRINCIPAL_TAXONOMY_CODE;

	@Override
	public String calculate(CalculatorParameters parameters) {
		String taxo = parameters.get(taxoPrincipaleDependency);
		List<String> pathsValue = parameters.get(pathDependency);
		if (pathsValue != null && !pathsValue.isEmpty() && taxo != null) {
			for (String path : pathsValue) {
				if (path != null && path.contains(taxo)) {
					return path;
				}
			}
		}
		return getDefaultValue();
	}

	@Override
	public String getDefaultValue() {
		return null;
	}

	@Override
	public MetadataValueType getReturnType() {
		return STRING;
	}

	@Override
	public boolean isMultiValue() {
		return false;
	}

	@Override
	public List<? extends Dependency> getDependencies() {
		return Arrays.asList(pathDependency, taxoPrincipaleDependency);
	}
}
