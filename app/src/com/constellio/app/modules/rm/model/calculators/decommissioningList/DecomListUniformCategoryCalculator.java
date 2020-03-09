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

public class DecomListUniformCategoryCalculator extends AbstractMetadataValueCalculator<String> {

	ReferenceDependency<List<String>> foldersCategoriesParam = ReferenceDependency.toAReference("folders",
			Folder.CATEGORY).whichIsMultivalue();

	@Override
	public String calculate(CalculatorParameters parameters) {

		List<String> foldersCategories = parameters.get(foldersCategoriesParam);

		Set<String> foldersCategoriesWithoutDuplicates = new HashSet<>(foldersCategories);
		return foldersCategoriesWithoutDuplicates.size() == 1 ? foldersCategories.get(0) : null;
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
		return asList(foldersCategoriesParam);
	}
}
