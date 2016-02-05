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

public class DecomListUniformCategoryCalculator2 implements MetadataValueCalculator<String> {

	ReferenceDependency<List<String>> foldersCategoriesParam = ReferenceDependency.toAReference(DecommissioningList.FOLDERS,
			Folder.CATEGORY).whichIsMultivalue();

	ReferenceDependency<List<String>> documentsCategoriesParam = ReferenceDependency.toAReference(DecommissioningList.DOCUMENTS,
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
