package com.constellio.app.modules.rm.model.calculators.container;

import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.StorageSpace;
import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.MetadataValueCalculator;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.ReferenceDependency;
import com.constellio.model.entities.schemas.MetadataValueType;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;

public class ContainerRecordMultipleStorageSpacesLocalizationCalculator implements MetadataValueCalculator<String> {

	ReferenceDependency<List<String>> storageSpaceTitleParam =
			ReferenceDependency.toAString(ContainerRecord.STORAGE_SPACE, StorageSpace.TITLE).whichIsRequired();

	@Override
	public String calculate(CalculatorParameters parameters) {
		List<String> storageSpaceTitle = new ArrayList<>(parameters.get(this.storageSpaceTitleParam));
		String localization = "";
		if(storageSpaceTitle != null) {
			boolean isFirst = true;
			for(String singleStorageSpaceTitle: storageSpaceTitle) {
				if(isFirst) {
					localization += singleStorageSpaceTitle;
					isFirst = false;
				} else {
					localization += ", " + singleStorageSpaceTitle;
				}
			}
		}

		return localization;
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
		return asList(storageSpaceTitleParam);
	}
}
