package com.constellio.app.modules.rm.model.calculators.decommissioningList;

import java.util.Arrays;
import java.util.List;

import com.constellio.app.modules.rm.model.enums.FolderMediaType;
import com.constellio.app.modules.rm.wrappers.DecommissioningList;
import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.MetadataValueCalculator;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.LocalDependency;
import com.constellio.model.entities.schemas.MetadataValueType;

public class DecomListHasAnalogicalMediumTypesCalculator implements MetadataValueCalculator<Boolean> {

	LocalDependency<List<FolderMediaType>> folderMediaTypesParam = LocalDependency
			.toAnEnum(DecommissioningList.FOLDERS_MEDIA_TYPES)
			.whichIsMultivalue();

	@Override
	public Boolean calculate(CalculatorParameters parameters) {
		List<FolderMediaType> folderMediaTypes = parameters.get(folderMediaTypesParam);

		for (FolderMediaType type : folderMediaTypes) {
			if (type.potentiallyHasAnalogMedium()) {
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
		return Arrays.asList(folderMediaTypesParam);
	}
}