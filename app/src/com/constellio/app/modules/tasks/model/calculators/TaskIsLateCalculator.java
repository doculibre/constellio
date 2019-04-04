package com.constellio.app.modules.tasks.model.calculators;

import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.MetadataValueCalculator;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.LocalDependency;
import com.constellio.model.entities.schemas.MetadataValueType;
import org.joda.time.LocalDate;

import java.util.List;

import static java.util.Arrays.asList;

public class TaskIsLateCalculator implements MetadataValueCalculator<Boolean> {

	private LocalDependency<LocalDate> dueDateParam = LocalDependency.toADate(Task.DUE_DATE);
	private LocalDependency<LocalDate> endDateParam = LocalDependency.toADate(Task.END_DATE);

	@Override
	public Boolean calculate(CalculatorParameters parameters) {
		LocalDate dueDate = parameters.get(dueDateParam);
		LocalDate endDate = parameters.get(endDateParam);
		if (dueDate != null) {
			if ((endDate != null && endDate.isAfter(dueDate)) ||
				(endDate == null && dueDate.isBefore(LocalDate.now()))) {
				return true;
			}
		}
		return null;
	}

	@Override
	public Boolean getDefaultValue() {
		return null;
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
		return asList(dueDateParam, endDateParam);
	}

}
