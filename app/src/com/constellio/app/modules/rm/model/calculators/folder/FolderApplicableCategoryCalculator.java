package com.constellio.app.modules.rm.model.calculators.folder;

import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.model.entities.calculators.AbstractMetadataValueCalculator;
import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.LocalDependency;
import com.constellio.model.entities.calculators.dependencies.ReferenceDependency;
import com.constellio.model.entities.schemas.MetadataValueType;

import java.util.List;

import static java.util.Arrays.asList;

public class FolderApplicableCategoryCalculator extends AbstractMetadataValueCalculator<String> {

	ReferenceDependency<String> parentCategoryParam = ReferenceDependency
			.toAReference(Folder.PARENT_FOLDER, Folder.CATEGORY);
	LocalDependency<String> enteredCategoryParam = LocalDependency.toAReference(Folder.CATEGORY_ENTERED);

	@Override
	public String calculate(CalculatorParameters parameters) {
		String parentCategory = parameters.get(parentCategoryParam);
		String enteredCategory = parameters.get(enteredCategoryParam);

		if (parentCategory != null) {
			return parentCategory;
		} else {
			return enteredCategory;
		}

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
		return asList(parentCategoryParam, enteredCategoryParam);
	}
}
