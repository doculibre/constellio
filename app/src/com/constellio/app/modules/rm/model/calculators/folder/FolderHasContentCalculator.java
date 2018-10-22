package com.constellio.app.modules.rm.model.calculators.folder;

import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.MetadataValueCalculator;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.LocalDependency;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.Schemas;

import java.util.List;

import static java.util.Arrays.asList;

public class FolderHasContentCalculator implements MetadataValueCalculator<Boolean> {

	private LocalDependency<Boolean> subFoldersWithContentParam =
			LocalDependency.toABoolean(Folder.SUB_FOLDERS_WITH_CONTENT);
	private LocalDependency<Boolean> documentsWithContentParam =
			LocalDependency.toABoolean(Folder.DOCUMENTS_WITH_CONTENT);
	private LocalDependency<Boolean> logicallyDeletedParam =
			LocalDependency.toABoolean(Schemas.LOGICALLY_DELETED_STATUS.getLocalCode());

	@Override
	public Boolean calculate(CalculatorParameters parameters) {
		Boolean subFoldersWithContent = parameters.get(subFoldersWithContentParam);
		Boolean documentsWithContent = parameters.get(documentsWithContentParam);
		Boolean logicallyDeleted = parameters.get(logicallyDeletedParam);
		return !Boolean.TRUE.equals(logicallyDeleted) &&
			   (Boolean.TRUE.equals(subFoldersWithContent) || Boolean.TRUE.equals(documentsWithContent));
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
		return asList(subFoldersWithContentParam, documentsWithContentParam, logicallyDeletedParam);
	}

}
