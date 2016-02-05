package com.constellio.app.modules.rm.model.calculators.decommissioningList;

import static java.util.Arrays.asList;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.constellio.app.modules.rm.model.enums.CopyType;
import com.constellio.app.modules.rm.wrappers.DecommissioningList;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.MetadataValueCalculator;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.ReferenceDependency;
import com.constellio.model.entities.schemas.MetadataValueType;

public class DecomListUniformCopyTypeCalculator2 implements MetadataValueCalculator<CopyType> {

	ReferenceDependency<List<CopyType>> foldersCopyTypesParam = ReferenceDependency.toAnEnum(DecommissioningList.FOLDERS,
			Folder.COPY_STATUS).whichIsMultivalue();

	ReferenceDependency<List<CopyType>> documentsCopyTypesParam = ReferenceDependency.toAnEnum(DecommissioningList.DOCUMENTS,
			Document.COPY_STATUS).whichIsMultivalue();

	@Override
	public CopyType calculate(CalculatorParameters parameters) {

		List<CopyType> foldersCopyTypes = parameters.get(foldersCopyTypesParam);
		List<CopyType> documentsCopyTypes = parameters.get(documentsCopyTypesParam);
		Set<CopyType> foldersCopyTypesWithoutDuplicates = new HashSet<>(foldersCopyTypes);
		foldersCopyTypesWithoutDuplicates.addAll(documentsCopyTypes);
		return foldersCopyTypesWithoutDuplicates.size() == 1 ? foldersCopyTypesWithoutDuplicates.iterator().next() : null;

	}

	@Override
	public CopyType getDefaultValue() {
		return null;
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
		return asList(foldersCopyTypesParam, documentsCopyTypesParam);
	}
}
