package com.constellio.app.modules.rm.model.calculators.decommissioningList;

import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.model.entities.calculators.AbstractMetadataValueCalculator;
import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.ReferenceDependency;
import com.constellio.model.entities.schemas.MetadataValueType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FoldersMediumTypesCalculator extends AbstractMetadataValueCalculator<List<String>> {

	ReferenceDependency<List<String>> mediumTypesParam = ReferenceDependency.toAReference("folders",
			Folder.MEDIUM_TYPES).whichIsMultivalue();

	@Override
	public List<String> calculate(CalculatorParameters parameters) {

		List<String> newMediumTypes = removeRepeatedValues(parameters);

		return newMediumTypes;
	}

	@Override
	public List<String> getDefaultValue() {
		return new ArrayList<>();
	}

	@Override
	public MetadataValueType getReturnType() {
		return MetadataValueType.REFERENCE;
	}

	@Override
	public boolean isMultiValue() {
		return true;
	}

	@Override
	public List<? extends Dependency> getDependencies() {
		return Arrays.asList(mediumTypesParam);
	}

	//
	private List<String> removeRepeatedValues(CalculatorParameters parameters) {
		List<String> mediumTypes = parameters.get(mediumTypesParam);
		List<String> newMediumTypes = new ArrayList<>();
		Set<String> mediumTypesSet = new HashSet<>();
		mediumTypesSet.addAll(mediumTypes);
		newMediumTypes.addAll(mediumTypesSet);
		return newMediumTypes;
	}
}
