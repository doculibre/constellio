package com.constellio.app.modules.rm.model.calculators.decommissioningList;

import static java.util.Arrays.asList;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.constellio.app.modules.rm.wrappers.DecommissioningList;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.MetadataValueCalculator;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.ReferenceDependency;
import com.constellio.model.entities.schemas.MetadataValueType;

public class DecomListUniformRuleCalculator2 implements MetadataValueCalculator<String> {

	ReferenceDependency<List<String>> foldersRulesParam = ReferenceDependency.toAReference(DecommissioningList.FOLDERS,
			Folder.RETENTION_RULE).whichIsMultivalue();

	ReferenceDependency<List<String>> documentsRulesParam = ReferenceDependency.toAReference(DecommissioningList.DOCUMENTS,
			Document.FOLDER_RETENTION_RULE).whichIsMultivalue();

	@Override
	public String calculate(CalculatorParameters parameters) {

		List<String> foldersRulesParams = parameters.get(foldersRulesParam);
		List<String> documentsRules = parameters.get(documentsRulesParam);

		Set<String> foldersRulesParamsWithoutDuplicates = new HashSet<>(foldersRulesParams);
		foldersRulesParamsWithoutDuplicates.addAll(documentsRules);
		return foldersRulesParamsWithoutDuplicates.size() == 1 ? foldersRulesParamsWithoutDuplicates.iterator().next() : null;
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
		return asList(foldersRulesParam, documentsRulesParam);
	}
}
