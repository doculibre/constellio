package com.constellio.app.modules.rm.model.calculators.folder;

import com.constellio.app.modules.rm.model.evaluators.FolderCanInheritFromParentCalculatorEvaluator;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.model.entities.calculators.AbstractMetadataValueCalculator;
import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.LocalDependency;
import com.constellio.model.entities.calculators.dependencies.ReferenceDependency;
import com.constellio.model.entities.schemas.MetadataValueType;
import org.joda.time.LocalDate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FolderActualDestructionDateCalculator extends AbstractMetadataValueCalculator<LocalDate> {

	ReferenceDependency<LocalDate> parentActualDestructionDateParam =
			ReferenceDependency.toADate(Folder.PARENT_FOLDER, Folder.ACTUAL_DESTRUCTION_DATE);
	LocalDependency<LocalDate> actualDestructionDateParam = LocalDependency.toADate(Folder.ACTUAL_DESTRUCTION_DATE);

	public FolderActualDestructionDateCalculator() {
		calculatorEvaluator = new FolderCanInheritFromParentCalculatorEvaluator();
	}

	@Override
	public LocalDate calculate(CalculatorParameters parameters) {
		if (calculatorEvaluator.isAutomaticallyFilled(buildCalculatorEvaluatorParameters(parameters))) {
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
		List<Dependency> dependencies = new ArrayList<>();
		dependencies.addAll(calculatorEvaluator.getDependencies());
		dependencies.addAll(Arrays.asList(parentActualDestructionDateParam, actualDestructionDateParam));
		return dependencies;
	}
}
