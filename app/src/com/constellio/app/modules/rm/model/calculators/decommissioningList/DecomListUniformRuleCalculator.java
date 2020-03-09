package com.constellio.app.modules.rm.model.calculators.decommissioningList;

import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.model.entities.calculators.AbstractMetadataValueCalculator;
import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.ReferenceDependency;
import com.constellio.model.entities.schemas.MetadataValueType;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.util.Arrays.asList;

public class DecomListUniformRuleCalculator extends AbstractMetadataValueCalculator<String> {

	ReferenceDependency<List<String>> foldersRulesParam = ReferenceDependency.toAReference("folders",
			Folder.RETENTION_RULE).whichIsMultivalue();

	@Override
	public String calculate(CalculatorParameters parameters) {

		List<String> foldersRulesParams = parameters.get(foldersRulesParam);

		Set<String> foldersRulesParamsWithoutDuplicates = new HashSet<>(foldersRulesParams);
		return foldersRulesParamsWithoutDuplicates.size() == 1 ? foldersRulesParams.get(0) : null;
	}

	@Override
	public String getDefaultValue() {
		return null;
	}

	@Override
	public MetadataValueType getReturnType() {
		return MetadataValueType.REFERENCE;
	}

	@Override
	public boolean isMultiValue() {
		return false;
	}

	@Override
	public List<? extends Dependency> getDependencies() {
		return asList(foldersRulesParam);
	}
}
