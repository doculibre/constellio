package com.constellio.app.modules.rm.model.calculators.folder;

import com.constellio.app.modules.rm.model.evaluators.FolderHasParentCalculatorEvaluator;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.model.entities.calculators.AbstractMetadataValueCalculator;
import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.LocalDependency;
import com.constellio.model.entities.calculators.dependencies.ReferenceDependency;
import com.constellio.model.entities.schemas.MetadataValueType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FolderAllowedFolderTypeCalculator extends AbstractMetadataValueCalculator<List<String>> {

	ReferenceDependency<List<String>> parentAllowedFolderTypeParam =
			ReferenceDependency.toAReference(Folder.PARENT_FOLDER, Folder.ALLOWED_FOLDER_TYPES).whichIsMultivalue();
	LocalDependency<List<String>> allowedFolderTypeParam =
			LocalDependency.toAReference(Folder.ALLOWED_FOLDER_TYPES).whichIsMultivalue();

	public FolderAllowedFolderTypeCalculator() {
		calculatorEvaluator = new FolderHasParentCalculatorEvaluator();
	}

	@Override
	public List<String> calculate(CalculatorParameters parameters) {
		if (calculatorEvaluator.isAutomaticallyFilled(buildCalculatorEvaluatorParameters(parameters))) {
			return parameters.get(parentAllowedFolderTypeParam);
		}
		return parameters.get(allowedFolderTypeParam);
	}

	@Override
	public List<String> getDefaultValue() {
		return null;
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
		List<Dependency> dependencies = new ArrayList<>();
		dependencies.addAll(calculatorEvaluator.getDependencies());
		dependencies.addAll(Arrays.asList(parentAllowedFolderTypeParam, allowedFolderTypeParam));
		return dependencies;
	}
}
