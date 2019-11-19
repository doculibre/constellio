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

public class FolderAllowedDocumentTypeCalculator extends AbstractMetadataValueCalculator<List<String>> {

	ReferenceDependency<List<String>> parentAllowedDocumentTypeParam =
			ReferenceDependency.toAReference(Folder.PARENT_FOLDER, Folder.ALLOWED_DOCUMENT_TYPES).whichIsMultivalue();
	LocalDependency<List<String>> allowedDocumentTypeParam =
			LocalDependency.toAReference(Folder.ALLOWED_DOCUMENT_TYPES).whichIsMultivalue();

	public FolderAllowedDocumentTypeCalculator() {
		calculatorEvaluator = new FolderHasParentCalculatorEvaluator();
	}

	@Override
	public List<String> calculate(CalculatorParameters parameters) {
		if (calculatorEvaluator.isAutomaticallyFilled(buildCalculatorEvaluatorParameters(parameters))) {
			return parameters.get(parentAllowedDocumentTypeParam);
		}
		return parameters.get(allowedDocumentTypeParam);
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
		dependencies.addAll(Arrays.asList(parentAllowedDocumentTypeParam, allowedDocumentTypeParam));
		return dependencies;
	}
}
