package com.constellio.app.modules.rm.model.evaluators;

import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.model.entities.calculators.dependencies.LocalDependency;
import com.constellio.model.entities.calculators.evaluators.CalculatorEvaluator;
import com.constellio.model.entities.calculators.evaluators.CalculatorEvaluatorParameters;

import java.util.Collections;
import java.util.List;

public class FolderHasParentCalculatorEvaluator implements CalculatorEvaluator {

	LocalDependency<String> parentFolderParam = LocalDependency.toAReference(Folder.PARENT_FOLDER);

	@Override
	public List<? extends LocalDependency> getDependencies() {
		return Collections.singletonList(parentFolderParam);
	}

	@Override
	public boolean isAutomaticallyFilled(CalculatorEvaluatorParameters parameters) {
		String parentFolderId = parameters.get(parentFolderParam);
		return parentFolderId != null;
	}

}
