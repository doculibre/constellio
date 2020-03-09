package com.constellio.app.modules.rm.model.calculators.decommissioningList;

import com.constellio.app.modules.rm.wrappers.Document;
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

public class DecomListUniformCategoryCalculator2 extends AbstractMetadataValueCalculator<String> {

	ReferenceDependency<List<String>> foldersCategoriesParam = ReferenceDependency.toAReference("folders",
			Folder.CATEGORY).whichIsMultivalue();

	ReferenceDependency<List<String>> documentsCategoriesParam = ReferenceDependency.toAReference("documents",
			Document.FOLDER_CATEGORY).whichIsMultivalue();

	@Override
	public String calculate(CalculatorParameters parameters) {

		List<String> foldersCategories = parameters.get(foldersCategoriesParam);
		List<String> documentsCategories = parameters.get(documentsCategoriesParam);

		Set<String> foldersCategoriesWithoutDuplicates = new HashSet<>(foldersCategories);
		foldersCategoriesWithoutDuplicates.addAll(documentsCategories);
		return foldersCategoriesWithoutDuplicates.size() == 1 ? foldersCategoriesWithoutDuplicates.iterator().next() : null;
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
		return asList(foldersCategoriesParam, documentsCategoriesParam);
	}
}
