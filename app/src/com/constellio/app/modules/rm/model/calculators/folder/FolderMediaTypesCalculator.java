package com.constellio.app.modules.rm.model.calculators.folder;

import static java.util.Arrays.asList;

import java.util.List;

import com.constellio.app.modules.rm.model.enums.FolderMediaType;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.type.MediumType;
import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.MetadataValueCalculator;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.ReferenceDependency;
import com.constellio.model.entities.schemas.MetadataValueType;

public class FolderMediaTypesCalculator implements MetadataValueCalculator<FolderMediaType> {

	ReferenceDependency<List<Boolean>> mediumTypesAnalogStatusParam = ReferenceDependency
			.toABoolean(Folder.MEDIUM_TYPES, MediumType.ANALOGICAL).whichIsMultivalue();

	@Override
	public FolderMediaType calculate(CalculatorParameters parameters) {
		List<Boolean> mediumTypesAnalogStatus = parameters.get(mediumTypesAnalogStatusParam);

		boolean hasAnalog = false;
		boolean hasElectronic = false;

		for (Boolean mediumTypeAnalogStatus : mediumTypesAnalogStatus) {
			if (mediumTypeAnalogStatus) {
				hasAnalog = true;
			} else {
				hasElectronic = true;
			}
		}

		if (hasAnalog && hasElectronic) {
			return FolderMediaType.HYBRID;

		} else if (hasAnalog) {
			return FolderMediaType.ANALOG;

		} else if (hasElectronic) {
			return FolderMediaType.ELECTRONIC;

		} else {
			return FolderMediaType.UNKNOWN;
		}

	}

	@Override
	public FolderMediaType getDefaultValue() {
		return FolderMediaType.UNKNOWN;
	}

	@Override
	public MetadataValueType getReturnType() {
		return MetadataValueType.ENUM;
	}

	@Override
	public boolean isMultiValue() {
		return false;
	}

	@Override
	public List<? extends Dependency> getDependencies() {
		return asList(mediumTypesAnalogStatusParam);
	}
}
