package com.constellio.app.modules.rm.model.calculators.decommissioningList;

import static java.util.Arrays.asList;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.constellio.app.modules.rm.wrappers.DecommissioningList;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.MetadataValueCalculator;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.ReferenceDependency;
import com.constellio.model.entities.schemas.MetadataValueType;

public class DecomListUniformCategoryCalculator implements MetadataValueCalculator<String> {

	ReferenceDependency<List<String>> foldersCategoriesParam = ReferenceDependency.toAReference(DecommissioningList.FOLDERS,
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
