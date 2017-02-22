package com.constellio.app.modules.rm.model.calculators.container;

import static java.util.Arrays.asList;

import java.util.List;

import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.StorageSpace;
import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.MetadataValueCalculator;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.LocalDependency;
import com.constellio.model.entities.calculators.dependencies.ReferenceDependency;
import com.constellio.model.entities.schemas.MetadataValueType;

public class ContainerRecordLocalizationCalculator implements MetadataValueCalculator<String> {

	ReferenceDependency<String> storageSpaceTitleParam =
			ReferenceDependency.toAString(ContainerRecord.STORAGE_SPACE, StorageSpace.TITLE).whichIsRequired();

	LocalDependency<String> titleParam = LocalDependency.toAString(ContainerRecord.TITLE);

	@Override
	public String calculate(CalculatorParameters parameters) {
		String storageSpaceTitle = parameters.get(this.storageSpaceTitleParam);
		String title = parameters.get(this.titleParam);

		return storageSpaceTitle + "-" + title;
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
		return asList(storageSpaceTitleParam, titleParam);
	}
}
