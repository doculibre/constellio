package com.constellio.app.modules.rm.model.calculators;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.LocalDate;

import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.MetadataValueCalculator;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.schemas.MetadataValueType;

public class DummyDateCalculator implements MetadataValueCalculator<LocalDate> {
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
		return new ArrayList<>();
	}
}
