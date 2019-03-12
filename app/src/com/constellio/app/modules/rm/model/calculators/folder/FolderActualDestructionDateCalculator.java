package com.constellio.app.modules.rm.model.calculators.folder;

import com.constellio.app.modules.rm.model.evaluators.FolderHasParentCalculatorEvaluator;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.model.entities.calculators.AbstractMetadataValueCalculator;
import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.LocalDependency;
import com.constellio.model.entities.calculators.dependencies.ReferenceDependency;
import com.constellio.model.entities.calculators.evaluators.CalculatorEvaluatorParameters;
import com.constellio.model.entities.schemas.MetadataValueType;
import org.joda.time.LocalDate;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FolderActualDestructionDateCalculator extends AbstractMetadataValueCalculator<LocalDate> {

	LocalDependency<String> parentFolderParam = LocalDependency.toAReference(Folder.PARENT_FOLDER);
	ReferenceDependency<LocalDate> parentActualDestructionDateParam =
			ReferenceDependency.toADate(Folder.PARENT_FOLDER, Folder.ACTUAL_DESTRUCTION_DATE);
	LocalDependency<LocalDate> actualDestructionDateParam = LocalDependency.toADate(Folder.ACTUAL_DESTRUCTION_DATE);

	public FolderActualDestructionDateCalculator() {
		calculatorEvaluator = new FolderHasParentCalculatorEvaluator();
	}

	@Override
	public LocalDate calculate(CalculatorParameters parameters) {
		Map<Dependency, Object> values = new HashMap<>();
		values.put(parentFolderParam, parameters.get(parentFolderParam));
		if (calculatorEvaluator.isAutomaticallyFilled(new CalculatorEvaluatorParameters(values))) {
			return parameters.get(parentActualDestructionDateParam);
		}
		return parameters.get(actualDestructionDateParam);
	}

	@Override
	public LocalDate getDefaultValue() {
		return null;
	}

	@Override
	public MetadataValueType getReturnType() {
		return MetadataValueType.DATE;
	}

	@Override
	public boolean isMultiValue() {
		return false;
	}

	@Override
	public List<? extends Dependency> getDependencies() {
		return Arrays.asList(parentFolderParam, parentActualDestructionDateParam, actualDestructionDateParam);
	}
}
