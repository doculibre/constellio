package com.constellio.app.modules.rm.model.evaluators;

import com.constellio.app.modules.rm.RMConfigs;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.model.entities.calculators.dependencies.ConfigDependency;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.LocalDependency;
import com.constellio.model.entities.calculators.evaluators.CalculatorEvaluator;
import com.constellio.model.entities.calculators.evaluators.CalculatorEvaluatorParameters;

import java.util.List;

import static java.util.Arrays.asList;

public class FolderCanInheritFromParentCalculatorEvaluator implements CalculatorEvaluator {

	ConfigDependency<Boolean> configSubFoldersDecommissionParam = RMConfigs.SUB_FOLDER_DECOMMISSIONING.dependency();
	LocalDependency<String> parentFolderParam = LocalDependency.toAReference(Folder.PARENT_FOLDER);

	@Override
	public List<? extends Dependency> getDependencies() {
		return asList(parentFolderParam, configSubFoldersDecommissionParam);
	}

	@Override
	public boolean isAutomaticallyFilled(CalculatorEvaluatorParameters parameters) {
		if ((boolean) parameters.get(configSubFoldersDecommissionParam)) {
			return false;
		}
		String parentFolderId = parameters.get(parentFolderParam);
		return parentFolderId != null;
	}

}
