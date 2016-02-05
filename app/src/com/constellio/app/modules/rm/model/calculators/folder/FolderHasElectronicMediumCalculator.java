package com.constellio.app.modules.rm.model.calculators.folder;

import static java.util.Arrays.asList;

import java.util.List;

import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.type.MediumType;
import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.MetadataValueCalculator;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.ReferenceDependency;
import com.constellio.model.entities.schemas.MetadataValueType;

public class FolderHasElectronicMediumCalculator implements MetadataValueCalculator<Boolean> {

	ReferenceDependency<List<Boolean>> mediumTypesAnalogicalStatusParam = ReferenceDependency
			.toABoolean(Folder.MEDIUM_TYPES, MediumType.ANALOGICAL).whichIsMultivalue();

	@Override
	public Boolean calculate(CalculatorParameters parameters) {
		List<Boolean> mediumTypesAnalogicalStatus = parameters.get(mediumTypesAnalogicalStatusParam);

		for (Boolean mediumTypeAnalogicalStatus : mediumTypesAnalogicalStatus) {
			if (!mediumTypeAnalogicalStatus) {
				return true;
			}
		}

		return false;
	}

	@Override
	public Boolean getDefaultValue() {
		return false;
	}

	@Override
	public MetadataValueType getReturnType() {
		return MetadataValueType.BOOLEAN;
	}

	@Override
	public boolean isMultiValue() {
		return false;
	}

	@Override
	public List<? extends Dependency> getDependencies() {
		return asList(mediumTypesAnalogicalStatusParam);
	}
}
