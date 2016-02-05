package com.constellio.app.modules.rm.model.calculators.decommissioningList;

import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.List;

import com.constellio.app.modules.rm.wrappers.DecommissioningList;
import com.constellio.app.modules.rm.wrappers.structures.DecomListFolderDetail;
import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.MetadataValueCalculator;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.LocalDependency;
import com.constellio.model.entities.schemas.MetadataValueType;

public class DecomListFoldersCalculator implements MetadataValueCalculator<List<String>> {

	LocalDependency<List<DecomListFolderDetail>> folderDetailsParam = LocalDependency
			.toAStructure(DecommissioningList.FOLDER_DETAILS).whichIsMultivalue();

	@Override
	public List<String> calculate(CalculatorParameters parameters) {
		List<String> ids = new ArrayList<>();
		List<DecomListFolderDetail> details = parameters.get(folderDetailsParam);

		for (DecomListFolderDetail detail : details) {
			ids.add(detail.getFolderId());
		}

		return ids;
	}

	@Override
	public List<String> getDefaultValue() {
		return new ArrayList<>();
	}

	@Override
	public MetadataValueType getReturnType() {
		return MetadataValueType.REFERENCE;
	}

	@Override
	public boolean isMultiValue() {
		return true;
	}

	@Override
	public List<? extends Dependency> getDependencies() {
		return asList(folderDetailsParam);
	}
}
