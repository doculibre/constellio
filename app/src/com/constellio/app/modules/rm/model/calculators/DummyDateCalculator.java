package com.constellio.app.modules.rm.model.calculators;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.joda.time.LocalDate;

import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.MetadataValueCalculator;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.LocalDependency;
import com.constellio.model.entities.schemas.MetadataValueType;

public class DummyDateCalculator implements MetadataValueCalculator<LocalDate> {
	LocalDependency<String> titleParam = LocalDependency.toAString(Folder.TITLE);
	@Override
	public LocalDate calculate(CalculatorParameters parameters) {
		return new LocalDate();
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
		return Arrays.asList(titleParam);
	}
}
